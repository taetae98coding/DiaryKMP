package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactRemoteEntity
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class SyncWorkContactTest :
    FunSpec({
        test("연락처만 대기하면 연락처 요청만 발생한다") {
            val context = context(contactList = contacts(size = 1))

            context.subject.doWork()

            coVerify(exactly = 1) { context.contactRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.placeRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.webRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
        }

        listOf(
            "연락처" to "태그",
            "태그" to "연락처",
        ).forEach { (delayed, other) ->
            test("TC-DATA-SYNC-DOMAIN-055 $delayed 업로드가 지연되어도 $other 업로드를 먼저 시작한다") {
                runTest {
                    val context = context(tagList = tags(size = 1), contactList = contacts(size = 1))
                    val callOrder = mutableListOf<String>()
                    coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                        if (delayed == "태그") delay(PUSH_DELAY)
                        callOrder += "tagPushEnd"
                    }
                    coEvery { context.contactRemoteDataSource.push(any()) } coAnswers {
                        if (delayed == "연락처") delay(PUSH_DELAY)
                        callOrder += "contactPushEnd"
                    }

                    context.subject.doWork()

                    val delayedEnd = if (delayed == "태그") "tagPushEnd" else "contactPushEnd"
                    callOrder.last() shouldBe delayedEnd
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-054 태그가 실패해도 연락처는 끝까지 업로드한다") {
            val context = context(tagList = tags(size = 1), contactList = contacts(size = 201))
            val failure = TestException(fixtureMonkey.giveMeOne())
            val contactRequests = mutableListOf<Int>()
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure
            coEvery { context.contactRemoteDataSource.push(any()) } answers {
                contactRequests += firstArg<List<ContactRemoteEntity>>().size
            }

            shouldThrowExactly<TestException> { context.subject.doWork() }

            contactRequests shouldContainExactly listOf(100, 100, 1)
        }

        test("TC-DATA-SYNC-DOMAIN-054 연락처가 실패해도 태그와 장소와 웹 항목은 끝까지 업로드한다") {
            val context =
                context(
                    tagList = tags(size = 201),
                    placeList = places(size = 201),
                    webList = webs(size = 201),
                    contactList = contacts(size = 1),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            val tagRequests = mutableListOf<Int>()
            val placeRequests = mutableListOf<Int>()
            val webRequests = mutableListOf<Int>()
            coEvery { context.contactRemoteDataSource.push(any()) } throws failure
            coEvery { context.tagRemoteDataSource.push(any()) } answers { tagRequests += firstArg<List<Any>>().size }
            coEvery { context.placeRemoteDataSource.push(any()) } answers { placeRequests += firstArg<List<Any>>().size }
            coEvery { context.webRemoteDataSource.push(any()) } answers { webRequests += firstArg<List<Any>>().size }

            shouldThrowExactly<TestException> { context.subject.doWork() }

            tagRequests shouldContainExactly listOf(100, 100, 1)
            placeRequests shouldContainExactly listOf(100, 100, 1)
            webRequests shouldContainExactly listOf(100, 100, 1)
        }

        test("TC-DATA-SYNC-DATA-005 연락처 업로드는 최대 100개씩 나누어 요청한다") {
            val context = context(contactList = contacts(size = 250))
            val requestSizeList = mutableListOf<Int>()
            coEvery { context.contactRemoteDataSource.push(any()) } answers {
                requestSizeList += firstArg<List<ContactRemoteEntity>>().size
            }

            context.subject.doWork()

            requestSizeList shouldContainExactly listOf(100, 100, 50)
        }

        test("TC-DATA-SYNC-DATA-032 연락처 업로드에는 전화번호 목록 전체가 순서대로 담긴다") {
            val phoneNumberList =
                listOf(
                    ContactPhoneNumberLocalEntity(number = "010-1234-5678"),
                    ContactPhoneNumberLocalEntity(number = "02-123-4567"),
                    ContactPhoneNumberLocalEntity(number = "010-1234-5678"),
                )
            val stored = contact().let { value -> value.copy(detail = value.detail.copy(phoneNumberList = phoneNumberList)) }
            val context = context(contactList = listOf(stored))
            val requestList = mutableListOf<List<ContactRemoteEntity>>()
            coEvery { context.contactRemoteDataSource.push(any()) } answers { requestList += firstArg<List<ContactRemoteEntity>>() }

            context.subject.doWork()

            requestList.single().single() shouldBe stored.toRemote()
            requestList
                .single()
                .single()
                .detail.phoneNumberList
                .map { phoneNumber -> phoneNumber.number } shouldContainExactly
                phoneNumberList.map { phoneNumber -> phoneNumber.number }
        }

        listOf(
            "양력 생일" to ContactBirthdayCalendarLocalEntity.SOLAR,
            "음력 생일" to ContactBirthdayCalendarLocalEntity.LUNAR,
            "생일 없음" to null,
        ).forEach { (label, birthdayCalendar) ->
            test("TC-DATA-SYNC-DATA-034 $label 연락처의 업로드에는 생일의 날짜와 달력 구분이 함께 담긴다") {
                val birthday = birthdayCalendar?.let { fixtureMonkey.giveMeOne<LocalDate>() }
                val stored =
                    contact().let { value ->
                        value.copy(detail = value.detail.copy(birthday = birthday, birthdayCalendar = birthdayCalendar))
                    }
                val context = context(contactList = listOf(stored))
                val requestList = mutableListOf<List<ContactRemoteEntity>>()
                coEvery { context.contactRemoteDataSource.push(any()) } answers { requestList += firstArg<List<ContactRemoteEntity>>() }

                context.subject.doWork()

                val detail = requestList.single().single().detail
                detail.birthday shouldBe birthday
                detail.birthdayCalendar shouldBe birthdayCalendar?.toRemote()
            }
        }

        test("TC-DATA-SYNC-DATA-033 내려받은 연락처의 전화번호 목록이 기기의 목록을 대체한다") {
            val context = context()
            val pullList = contactPulls(usnList = listOf(1L))
            coEvery { context.contactRemoteDataSource.pull(usn = 0L) } returns pullList
            coEvery { context.contactRemoteDataSource.pull(usn = 1L) } returns emptyList()
            val savedList = mutableListOf<List<Any>>()
            coEvery { context.accountContactSyncTransaction.save(any(), any(), any()) } answers {
                savedList += secondArg<List<Any>>()
            }

            context.subject.doWork()

            savedList.single() shouldContainExactly pullList.map { pull -> pull.contact.toLocal() }
        }

        test("TC-DATA-SYNC-DATA-016 연락처 내려받기는 기록된 커서로 요청하고 저장한 가장 큰 순번으로 다음 묶음을 요청한다") {
            val context = context()
            val cursor = 42L
            val pullList = contactPulls(usnList = listOf(50L, 47L))
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.CONTACT) } returns cursor
            coEvery { context.contactRemoteDataSource.pull(usn = cursor) } returns pullList
            coEvery { context.contactRemoteDataSource.pull(usn = 50L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) { context.contactRemoteDataSource.pull(usn = cursor) }
            coVerify(exactly = 1) {
                context.accountContactSyncTransaction.save(
                    context.accountId,
                    pullList.map { pull -> pull.contact.toLocal() },
                    50L,
                )
            }
            coVerify(exactly = 1) { context.contactRemoteDataSource.pull(usn = 50L) }
        }

        test("TC-DATA-SYNC-DATA-020 연락처 내려받기는 빈 응답을 받을 때까지 반복한다") {
            val context = context()
            coEvery { context.contactRemoteDataSource.pull(usn = 0L) } returns contactPulls(usnList = listOf(1L, 2L))
            coEvery { context.contactRemoteDataSource.pull(usn = 2L) } returns contactPulls(usnList = listOf(3L))
            coEvery { context.contactRemoteDataSource.pull(usn = 3L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) { context.contactRemoteDataSource.pull(usn = 0L) }
            coVerify(exactly = 1) { context.contactRemoteDataSource.pull(usn = 2L) }
            coVerify(exactly = 1) { context.contactRemoteDataSource.pull(usn = 3L) }
            coVerify(exactly = 1) { context.accountContactSyncTransaction.save(context.accountId, any(), 2L) }
            coVerify(exactly = 1) { context.accountContactSyncTransaction.save(context.accountId, any(), 3L) }
        }

        test("TC-DATA-SYNC-DOMAIN-040 연락처 내려받기가 실패하면 동기화가 실패한다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.contactRemoteDataSource.pull(any()) } throws failure

            shouldThrowExactly<TestException> { context.subject.doWork() }

            coVerify(exactly = 1) { context.webRemoteDataSource.pull(any()) }
            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(any()) }
        }

        test("업로드 묶음이 성공하면 그 묶음의 대기 상태를 해제한다") {
            val contactList = contacts(size = 150)
            val context = context(contactList = contactList)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountContactSyncTransaction.clearPending(context.accountId, contactList.take(100))
            }
            coVerify(exactly = 1) {
                context.accountContactSyncTransaction.clearPending(context.accountId, contactList.drop(100))
            }
        }
    })
