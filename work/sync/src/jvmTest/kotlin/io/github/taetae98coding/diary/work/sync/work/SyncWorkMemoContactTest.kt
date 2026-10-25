package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.network.api.memocontact.entity.MemoContactRemoteEntity
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

class SyncWorkMemoContactTest :
    FunSpec({
        test("TC-674eebce-610c-4b46-8dce-79aef5252327 연락처와 메모 요청이 모두 성공한 뒤 메모·연락처 연결 전용 요청을 시작한다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    contactList = contacts(size = 101),
                    memoList = memos(size = 101),
                    memoContactList = memoContacts(size = 101),
                )
            val requestOrder = mutableListOf<String>()
            coEvery { context.contactRemoteDataSource.push(any()) } coAnswers { requestOrder += "contact" }
            coEvery { context.memoRemoteDataSource.push(any()) } coAnswers { requestOrder += "memo" }
            coEvery { context.memoContactRemoteDataSource.push(any()) } coAnswers { requestOrder += "memoContact" }

            context.subject.doWork()

            requestOrder.takeLast(2) shouldContainExactly listOf("memoContact", "memoContact")
            requestOrder.dropLast(2) shouldContainExactlyInAnyOrder listOf("contact", "contact", "memo", "memo")
        }

        test("메모·연락처 연결만 대기하면 메모·연락처 연결 요청만 발생한다") {
            val context = context(memoContactList = memoContacts(size = 1))

            context.subject.doWork()

            coVerify(exactly = 1) { context.memoContactRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.placeRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.contactRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoTagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoPlaceRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.webTagRemoteDataSource.push(any()) }
        }

        listOf(
            1 to listOf(1),
            100 to listOf(100),
            201 to listOf(100, 100, 1),
        ).forEach { (itemCount, expectedRequestSizes) ->
            test("TC-400fe05d-3f17-47cf-b0bb-ca12fe6625c0 TC-04b80b1a-7bba-496f-836c-41b1d6ee52dc 메모·연락처 연결 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val memoContactList = memoContacts(size = itemCount)
                val context = context(memoContactList = memoContactList)
                val requests = mutableListOf<List<MemoContactRemoteEntity>>()
                coEvery { context.memoContactRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<MemoContactRemoteEntity>>()
                }

                context.subject.doWork()

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly memoContactList.map { memoContact -> memoContact.toRemote() }
            }
        }

        test("TC-dc3a84bc-f558-477f-929e-d4297835a79d 해제된 연결도 해제 상태로 업로드된다") {
            val memoContactList = listOf(memoContact(isDeleted = true), memoContact(isDeleted = false))
            val context = context(memoContactList = memoContactList)
            val requests = mutableListOf<List<MemoContactRemoteEntity>>()
            coEvery { context.memoContactRemoteDataSource.push(any()) } coAnswers {
                requests += firstArg<List<MemoContactRemoteEntity>>()
            }

            context.subject.doWork()

            requests.flatten() shouldContainExactly memoContactList.map { memoContact -> memoContact.toRemote() }
            requests.flatten().map { request -> request.isDeleted } shouldContainExactly listOf(true, false)
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 메모·연락처 연결 업로드 묶음이 성공하면 보낸 항목의 대기 해제를 요청한다") {
            val memoContactList = memoContacts(size = 101)
            val context = context(memoContactList = memoContactList)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountMemoContactSyncTransaction.clearPending(
                    accountId = context.accountId,
                    memoContactList = memoContactList.take(100),
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoContactSyncTransaction.clearPending(
                    accountId = context.accountId,
                    memoContactList = memoContactList.drop(100),
                )
            }
        }

        test("TC-929d599a-bd66-4c34-8b8f-fd0df8e6aeee 메모·연락처 연결 업로드가 실패하면 내려받기를 진행하지 않는다") {
            val context = context(memoContactList = memoContacts(size = 1))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.memoContactRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 0) { context.accountMemoContactSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.memoContactRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.contactRemoteDataSource.pull(any()) }
        }

        test("TC-98a227dd-770f-4c9a-9c31-1602b3c77614 연락처 업로드가 실패하면 메모·연락처 연결을 시도하지 않는다") {
            val context =
                context(
                    contactList = contacts(size = 1),
                    memoContactList = memoContacts(size = 1),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.contactRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 0) { context.memoContactSyncLocalDataSource.readPendingList(any()) }
            coVerify(exactly = 0) { context.memoContactRemoteDataSource.push(any()) }
        }

        test("TC-bb1f2746-1f2d-465a-8da5-35cff756be8c 태그 업로드가 실패하면 메모·연락처 연결을 시도하지 않는다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    contactList = contacts(size = 1),
                    memoContactList = memoContacts(size = 1),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 0) { context.memoContactSyncLocalDataSource.readPendingList(any()) }
            coVerify(exactly = 0) { context.memoContactRemoteDataSource.push(any()) }
        }

        test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 메모·연락처 연결 업로드가 실패해도 메모와 태그 연결은 끝까지 업로드한다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    contactList = contacts(size = 1),
                    memoList = memos(size = 201),
                    memoTagList = memoTags(size = 201),
                    memoContactList = memoContacts(size = 201),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            val memoContactRequestCount = mutableListOf<Int>()
            coEvery { context.memoContactRemoteDataSource.push(any()) } coAnswers {
                memoContactRequestCount += 1
                if (memoContactRequestCount.size == 2) throw failure
            }

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            memoContactRequestCount.size shouldBe 2
            coVerify(exactly = 3) { context.memoRemoteDataSource.push(any()) }
            coVerify(exactly = 3) { context.memoTagRemoteDataSource.push(any()) }
        }

        test("TC-02386be4-0935-4eb9-9804-d5ad18fe2e96 메모·연락처 연결 내려받기는 기록된 순번부터 빈 응답까지 반복한다") {
            val context = context()
            val firstPullList = memoContactPulls(usnList = listOf(4L, 6L))
            val secondPullList = memoContactPulls(usnList = listOf(9L))
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.MEMO_CONTACT) } returns 2L
            coEvery { context.memoContactRemoteDataSource.pull(usn = 2L) } returns firstPullList
            coEvery { context.memoContactRemoteDataSource.pull(usn = 6L) } returns secondPullList
            coEvery { context.memoContactRemoteDataSource.pull(usn = 9L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountMemoContactSyncTransaction.upsert(
                    accountId = context.accountId,
                    memoContactList = firstPullList.map { pull -> pull.memoContact.toLocal() },
                    cursor = 6L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoContactSyncTransaction.upsert(
                    accountId = context.accountId,
                    memoContactList = secondPullList.map { pull -> pull.memoContact.toLocal() },
                    cursor = 9L,
                )
            }
            coVerify(exactly = 3) { context.memoContactRemoteDataSource.pull(any()) }
        }

        test("TC-d6ad6c31-e1e9-435a-ab90-64a6c654d457 메모·연락처 연결 내려받기가 지연되어도 나머지 종류의 내려받기를 먼저 시작한다") {
            runTest {
                val context = context()
                val callOrder = mutableListOf<String>()
                coEvery { context.memoContactRemoteDataSource.pull(usn = 0L) } coAnswers {
                    delay(PULL_DELAY)
                    callOrder += "memoContactPullEnd"
                    emptyList()
                }
                coEvery { context.tagRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "tagPullEnd"
                    emptyList()
                }
                coEvery { context.contactRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "contactPullEnd"
                    emptyList()
                }
                coEvery { context.memoRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "memoPullEnd"
                    emptyList()
                }

                context.subject.doWork()

                callOrder.last() shouldBe "memoContactPullEnd"
                callOrder shouldContainExactlyInAnyOrder
                    listOf("tagPullEnd", "contactPullEnd", "memoPullEnd", "memoContactPullEnd")
            }
        }

        test("TC-319f896c-9872-4455-b4d1-a91dc09e1e9b TC-51b016e3-825d-4ef5-9968-ecbf31b4c8a4 메모·연락처 연결 내려받기가 실패해도 연락처와 메모는 빈 응답까지 내려받고 동기화는 실패한다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val contactPullList = contactPulls(usnList = listOf(3L))
            val memoPullList = memoPulls(usnList = listOf(4L))
            coEvery { context.memoContactRemoteDataSource.pull(any()) } throws failure
            coEvery { context.contactRemoteDataSource.pull(usn = 0L) } returns contactPullList
            coEvery { context.contactRemoteDataSource.pull(usn = 3L) } returns emptyList()
            coEvery { context.memoRemoteDataSource.pull(usn = 0L) } returns memoPullList
            coEvery { context.memoRemoteDataSource.pull(usn = 4L) } returns emptyList()

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

            actual.message shouldBe failure.message
            coVerify(exactly = 1) {
                context.accountContactSyncTransaction.upsert(
                    accountId = context.accountId,
                    contactList = contactPullList.map { pull -> pull.contact.toLocal() },
                    cursor = 3L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.upsert(
                    accountId = context.accountId,
                    memoList = memoPullList.map { pull -> pull.memo.toLocal() },
                    cursor = 4L,
                )
            }
            coVerify(exactly = 0) { context.accountMemoContactSyncTransaction.upsert(any(), any(), any()) }
        }

        test("TC-28dda1bd-78ef-4aef-943d-5bf25ae21551 실행 시점에 확인된 계정의 메모·연락처 연결만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val context = context(accountId = accountId)
            coEvery {
                context.memoContactSyncLocalDataSource.readPendingList(accountId = otherAccountId)
            } returns memoContacts(size = 1)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.memoContactSyncLocalDataSource.readPendingList(accountId = accountId)
            }
            coVerify(exactly = 0) {
                context.memoContactSyncLocalDataSource.readPendingList(accountId = otherAccountId)
            }
        }
    })
