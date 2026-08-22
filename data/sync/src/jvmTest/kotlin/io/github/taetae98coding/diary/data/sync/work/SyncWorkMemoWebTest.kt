package io.github.taetae98coding.diary.data.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.mapper.memo.toLocal
import io.github.taetae98coding.diary.core.mapper.memoweb.toLocal
import io.github.taetae98coding.diary.core.mapper.memoweb.toRemote
import io.github.taetae98coding.diary.core.mapper.web.toLocal
import io.github.taetae98coding.diary.core.network.api.memoweb.entity.MemoWebRemoteEntity
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

class SyncWorkMemoWebTest :
    FunSpec({
        test("TC-DATA-SYNC-DOMAIN-019 웹 항목과 메모 요청이 모두 성공한 뒤 메모·웹 연결 전용 요청을 시작한다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    webList = webs(size = 101),
                    memoList = memos(size = 101),
                    memoWebList = memoWebs(size = 101),
                )
            val requestOrder = mutableListOf<String>()
            coEvery { context.webRemoteDataSource.push(any()) } coAnswers { requestOrder += "web" }
            coEvery { context.memoRemoteDataSource.push(any()) } coAnswers { requestOrder += "memo" }
            coEvery { context.memoWebRemoteDataSource.push(any()) } coAnswers { requestOrder += "memoWeb" }

            context.subject.doWork(accountId = context.accountId)

            requestOrder.takeLast(2) shouldContainExactly listOf("memoWeb", "memoWeb")
            requestOrder.dropLast(2) shouldContainExactlyInAnyOrder listOf("web", "web", "memo", "memo")
        }

        test("TC-DATA-SYNC-DOMAIN-020 메모·웹 연결만 대기하면 메모·웹 연결 요청만 발생한다") {
            val context = context(memoWebList = memoWebs(size = 1))

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) { context.memoWebRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.placeRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.webRemoteDataSource.push(any()) }
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
            test("TC-DATA-SYNC-DATA-005 TC-MEMO-WEB-DATA-006 메모·웹 연결 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val memoWebList = memoWebs(size = itemCount)
                val context = context(memoWebList = memoWebList)
                val requests = mutableListOf<List<MemoWebRemoteEntity>>()
                coEvery { context.memoWebRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<MemoWebRemoteEntity>>()
                }

                context.subject.doWork(accountId = context.accountId)

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly memoWebList.map { memoWeb -> memoWeb.toRemote() }
            }
        }

        test("TC-MEMO-WEB-DATA-007 해제된 연결도 해제 상태로 업로드된다") {
            val memoWebList = listOf(memoWeb(isDeleted = true), memoWeb(isDeleted = false))
            val context = context(memoWebList = memoWebList)
            val requests = mutableListOf<List<MemoWebRemoteEntity>>()
            coEvery { context.memoWebRemoteDataSource.push(any()) } coAnswers {
                requests += firstArg<List<MemoWebRemoteEntity>>()
            }

            context.subject.doWork(accountId = context.accountId)

            requests.flatten() shouldContainExactly memoWebList.map { memoWeb -> memoWeb.toRemote() }
            requests.flatten().map { request -> request.isDeleted } shouldContainExactly listOf(true, false)
        }

        test("TC-DATA-SYNC-DOMAIN-026 메모·웹 연결 업로드 묶음이 성공하면 보낸 항목의 대기 해제를 요청한다") {
            val memoWebList = memoWebs(size = 101)
            val context = context(memoWebList = memoWebList)

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) {
                context.accountMemoWebSyncTransaction.clearPending(
                    accountId = context.accountId,
                    memoWebList = memoWebList.take(100),
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoWebSyncTransaction.clearPending(
                    accountId = context.accountId,
                    memoWebList = memoWebList.drop(100),
                )
            }
        }

        test("TC-DATA-SYNC-DOMAIN-029 메모·웹 연결 업로드가 실패하면 내려받기를 진행하지 않는다") {
            val context = context(memoWebList = memoWebs(size = 1))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.memoWebRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            coVerify(exactly = 0) { context.accountMemoWebSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.memoWebRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.webRemoteDataSource.pull(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-021 웹 항목 업로드가 실패하면 메모·웹 연결을 시도하지 않는다") {
            val context =
                context(
                    webList = webs(size = 1),
                    memoWebList = memoWebs(size = 1),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.webRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            coVerify(exactly = 0) { context.memoWebSyncLocalDataSource.findPending(any()) }
            coVerify(exactly = 0) { context.memoWebRemoteDataSource.push(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-021 태그 업로드가 실패하면 메모·웹 연결을 시도하지 않는다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    webList = webs(size = 1),
                    memoWebList = memoWebs(size = 1),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            coVerify(exactly = 0) { context.memoWebSyncLocalDataSource.findPending(any()) }
            coVerify(exactly = 0) { context.memoWebRemoteDataSource.push(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-054 메모·웹 연결 업로드가 실패해도 메모와 태그 연결은 끝까지 업로드한다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    webList = webs(size = 1),
                    memoList = memos(size = 201),
                    memoTagList = memoTags(size = 201),
                    memoWebList = memoWebs(size = 201),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            val memoWebRequestCount = mutableListOf<Int>()
            coEvery { context.memoWebRemoteDataSource.push(any()) } coAnswers {
                memoWebRequestCount += 1
                if (memoWebRequestCount.size == 2) throw failure
            }

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            memoWebRequestCount.size shouldBe 2
            coVerify(exactly = 3) { context.memoRemoteDataSource.push(any()) }
            coVerify(exactly = 3) { context.memoTagRemoteDataSource.push(any()) }
        }

        test("TC-MEMO-WEB-DATA-008 메모·웹 연결 내려받기는 기록된 순번부터 빈 응답까지 반복한다") {
            val context = context()
            val firstPullList = memoWebPulls(usnList = listOf(4L, 6L))
            val secondPullList = memoWebPulls(usnList = listOf(9L))
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.MEMO_WEB) } returns 2L
            coEvery { context.memoWebRemoteDataSource.pull(usn = 2L) } returns firstPullList
            coEvery { context.memoWebRemoteDataSource.pull(usn = 6L) } returns secondPullList
            coEvery { context.memoWebRemoteDataSource.pull(usn = 9L) } returns emptyList()

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) {
                context.accountMemoWebSyncTransaction.save(
                    accountId = context.accountId,
                    memoWebList = firstPullList.map { pull -> pull.memoWeb.toLocal() },
                    cursor = 6L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoWebSyncTransaction.save(
                    accountId = context.accountId,
                    memoWebList = secondPullList.map { pull -> pull.memoWeb.toLocal() },
                    cursor = 9L,
                )
            }
            coVerify(exactly = 3) { context.memoWebRemoteDataSource.pull(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-038 메모·웹 연결 내려받기가 지연되어도 나머지 종류의 내려받기를 먼저 시작한다") {
            runTest {
                val context = context()
                val callOrder = mutableListOf<String>()
                coEvery { context.memoWebRemoteDataSource.pull(usn = 0L) } coAnswers {
                    delay(PULL_DELAY)
                    callOrder += "memoWebPullEnd"
                    emptyList()
                }
                coEvery { context.tagRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "tagPullEnd"
                    emptyList()
                }
                coEvery { context.webRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "webPullEnd"
                    emptyList()
                }
                coEvery { context.memoRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "memoPullEnd"
                    emptyList()
                }

                context.subject.doWork(accountId = context.accountId)

                callOrder.last() shouldBe "memoWebPullEnd"
                callOrder shouldContainExactlyInAnyOrder
                    listOf("tagPullEnd", "webPullEnd", "memoPullEnd", "memoWebPullEnd")
            }
        }

        test("TC-DATA-SYNC-DOMAIN-039 TC-DATA-SYNC-DOMAIN-040 메모·웹 연결 내려받기가 실패해도 웹 항목과 메모는 빈 응답까지 내려받고 동기화는 실패한다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val webPullList = webPulls(usnList = listOf(3L))
            val memoPullList = memoPulls(usnList = listOf(4L))
            coEvery { context.memoWebRemoteDataSource.pull(any()) } throws failure
            coEvery { context.webRemoteDataSource.pull(usn = 0L) } returns webPullList
            coEvery { context.webRemoteDataSource.pull(usn = 3L) } returns emptyList()
            coEvery { context.memoRemoteDataSource.pull(usn = 0L) } returns memoPullList
            coEvery { context.memoRemoteDataSource.pull(usn = 4L) } returns emptyList()

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork(accountId = context.accountId)
                }

            actual.message shouldBe failure.message
            coVerify(exactly = 1) {
                context.accountWebSyncTransaction.save(
                    accountId = context.accountId,
                    webList = webPullList.map { pull -> pull.web.toLocal() },
                    cursor = 3L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.save(
                    accountId = context.accountId,
                    memoList = memoPullList.map { pull -> pull.memo.toLocal() },
                    cursor = 4L,
                )
            }
            coVerify(exactly = 0) { context.accountMemoWebSyncTransaction.save(any(), any(), any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-024 요청에 전달된 계정의 메모·웹 연결만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val context = context(accountId = accountId)
            coEvery {
                context.memoWebSyncLocalDataSource.findPending(accountId = otherAccountId)
            } returns memoWebs(size = 1)

            context.subject.doWork(accountId = accountId)

            coVerify(exactly = 1) {
                context.memoWebSyncLocalDataSource.findPending(accountId = accountId)
            }
            coVerify(exactly = 0) {
                context.memoWebSyncLocalDataSource.findPending(accountId = otherAccountId)
            }
        }
    })
