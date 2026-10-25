package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
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
            test("TC-d02e1fe4-3710-497f-b721-5bd09e834ad1 $delayed 업로드가 지연되어도 $other 업로드를 먼저 시작한다") {
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

        test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 태그가 실패해도 연락처는 끝까지 업로드한다") {
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

        test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 연락처가 실패해도 태그와 장소와 웹 항목은 끝까지 업로드한다") {
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

        test("TC-400fe05d-3f17-47cf-b0bb-ca12fe6625c0 연락처 업로드는 최대 100개씩 나누어 요청한다") {
            val context = context(contactList = contacts(size = 250))
            val requestSizeList = mutableListOf<Int>()
            coEvery { context.contactRemoteDataSource.push(any()) } answers {
                requestSizeList += firstArg<List<ContactRemoteEntity>>().size
            }

            context.subject.doWork()

            requestSizeList shouldContainExactly listOf(100, 100, 50)
        }

        test("TC-576f1564-4dff-442c-a107-8beb008e36c9 연락처 업로드에는 전화번호 목록 전체가 순서대로 담긴다") {
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
            Triple("양력 생일", ContactBirthdayCalendarLocalEntity.SOLAR, false),
            Triple("음력 생일", ContactBirthdayCalendarLocalEntity.LUNAR, false),
            Triple("생일 없이 태어난 연도만 있는", null, true),
            Triple("생일과 태어난 연도가 모두 없는", null, false),
        ).forEach { (label, birthdayCalendar, hasBirthYearOnly) ->
            test("TC-2ab0df88-4ebe-4b95-a0cb-5403c8f569d8 $label 연락처의 업로드에는 생일의 날짜와 달력 구분, 태어난 연도가 함께 담긴다") {
                val birthday = birthdayCalendar?.let { fixtureMonkey.giveMeOne<LocalDate>() }
                val birthYear = birthday?.year ?: if (hasBirthYearOnly) fixtureMonkey.giveMeOne<LocalDate>().year else null
                val stored =
                    contact().let { value ->
                        value.copy(detail = value.detail.copy(birthday = birthday, birthdayCalendar = birthdayCalendar, birthYear = birthYear))
                    }
                val context = context(contactList = listOf(stored))
                val requestList = mutableListOf<List<ContactRemoteEntity>>()
                coEvery { context.contactRemoteDataSource.push(any()) } answers { requestList += firstArg<List<ContactRemoteEntity>>() }

                context.subject.doWork()

                val detail = requestList.single().single().detail
                detail.birthday shouldBe birthday
                detail.birthdayCalendar shouldBe birthdayCalendar?.toRemote()
                detail.birthYear shouldBe birthYear
            }
        }

        test("TC-f5c3db7c-930a-44dc-bc0f-de71333148ed 내려받은 연락처의 전화번호 목록이 기기의 목록을 대체한다") {
            val context = context()
            val pullList = contactPulls(usnList = listOf(1L))
            coEvery { context.contactRemoteDataSource.pull(usn = 0L) } returns pullList
            coEvery { context.contactRemoteDataSource.pull(usn = 1L) } returns emptyList()
            val savedList = mutableListOf<List<Any>>()
            coEvery { context.accountContactSyncTransaction.upsert(any(), any(), any()) } answers {
                savedList += secondArg<List<Any>>()
            }

            context.subject.doWork()

            savedList.single() shouldContainExactly pullList.map { pull -> pull.contact.toLocal() }
        }

        test("TC-85437a34-b1a2-4fa0-ae7a-ec766c5b8952 연락처 내려받기는 기록된 커서로 요청하고 저장한 가장 큰 순번으로 다음 묶음을 요청한다") {
            val context = context()
            val cursor = 42L
            val pullList = contactPulls(usnList = listOf(50L, 47L))
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.CONTACT) } returns cursor
            coEvery { context.contactRemoteDataSource.pull(usn = cursor) } returns pullList
            coEvery { context.contactRemoteDataSource.pull(usn = 50L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) { context.contactRemoteDataSource.pull(usn = cursor) }
            coVerify(exactly = 1) {
                context.accountContactSyncTransaction.upsert(
                    context.accountId,
                    pullList.map { pull -> pull.contact.toLocal() },
                    50L,
                )
            }
            coVerify(exactly = 1) { context.contactRemoteDataSource.pull(usn = 50L) }
        }

        test("TC-21ff0303-9f3c-4cb3-9f35-4251ec918ee8 연락처 내려받기는 빈 응답을 받을 때까지 반복한다") {
            val context = context()
            coEvery { context.contactRemoteDataSource.pull(usn = 0L) } returns contactPulls(usnList = listOf(1L, 2L))
            coEvery { context.contactRemoteDataSource.pull(usn = 2L) } returns contactPulls(usnList = listOf(3L))
            coEvery { context.contactRemoteDataSource.pull(usn = 3L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) { context.contactRemoteDataSource.pull(usn = 0L) }
            coVerify(exactly = 1) { context.contactRemoteDataSource.pull(usn = 2L) }
            coVerify(exactly = 1) { context.contactRemoteDataSource.pull(usn = 3L) }
            coVerify(exactly = 1) { context.accountContactSyncTransaction.upsert(context.accountId, any(), 2L) }
            coVerify(exactly = 1) { context.accountContactSyncTransaction.upsert(context.accountId, any(), 3L) }
        }

        test("TC-51b016e3-825d-4ef5-9968-ecbf31b4c8a4 연락처 내려받기가 실패하면 동기화가 실패한다") {
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
