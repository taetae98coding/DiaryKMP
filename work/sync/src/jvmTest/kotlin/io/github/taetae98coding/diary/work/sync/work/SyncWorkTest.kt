package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.network.api.memo.datasource.MemoRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memotag.datasource.MemoTagRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagRemoteEntity
import io.github.taetae98coding.diary.core.network.api.tag.datasource.TagRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import java.net.UnknownHostException
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SyncWorkTest :
    FunSpec({
        test("TC-DATA-SYNC-DOMAIN-019 모든 태그 요청이 성공한 뒤 메모 요청을 시작한다") {
            val tagList = tags(size = 201)
            val memoList = memos(size = 101)
            val context = context(tagList = tagList, memoList = memoList)
            val requestOrder = mutableListOf<String>()
            coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                requestOrder += "tag"
            }
            coEvery { context.memoRemoteDataSource.push(any()) } coAnswers {
                requestOrder += "memo"
            }

            context.subject.doWork(accountId = context.accountId)

            requestOrder shouldContainExactly listOf("tag", "tag", "tag", "memo", "memo")
        }

        listOf(
            Triple(0, 1, Pair(0, 1)),
            Triple(1, 0, Pair(1, 0)),
            Triple(0, 0, Pair(0, 0)),
        ).forEach { (tagCount, memoCount, expectedRequestCounts) ->
            test("TC-DATA-SYNC-DOMAIN-020 태그 $tagCount 개, 메모 $memoCount 개면 빈 종류 요청을 생략한다") {
                val context = context(tagList = tags(tagCount), memoList = memos(memoCount))

                context.subject.doWork(accountId = context.accountId)

                coVerify(exactly = expectedRequestCounts.first) { context.tagRemoteDataSource.push(any()) }
                coVerify(exactly = expectedRequestCounts.second) { context.memoRemoteDataSource.push(any()) }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-021 태그 묶음 실패 시 이후 태그와 모든 메모를 시도하지 않는다") {
            val tagList = tags(size = 201)
            val memoList = memos(size = 1)
            val context = context(tagList = tagList, memoList = memoList)
            val failure = TestException(fixtureMonkey.giveMeOne())
            val tagRequests = mutableListOf<List<TagRemoteEntity>>()
            coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                tagRequests += firstArg<List<TagRemoteEntity>>()
                if (tagRequests.size == 2) throw failure
            }

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork(accountId = context.accountId)
                }

            actual.message shouldBe failure.message
            tagRequests.map { request -> request.size } shouldContainExactly listOf(100, 100)
            coVerify(exactly = 0) { context.memoSyncLocalDataSource.findPending(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-022 메모 묶음 실패 시 이후 메모를 시도하지 않는다") {
            val tagList = tags(size = 1)
            val memoList = memos(size = 201)
            val context = context(tagList = tagList, memoList = memoList)
            val failure = TestException(fixtureMonkey.giveMeOne())
            val memoRequests = mutableListOf<List<MemoRemoteEntity>>()
            coEvery { context.memoRemoteDataSource.push(any()) } coAnswers {
                memoRequests += firstArg<List<MemoRemoteEntity>>()
                if (memoRequests.size == 2) throw failure
            }

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork(accountId = context.accountId)
                }

            actual.message shouldBe failure.message
            memoRequests.map { request -> request.size } shouldContainExactly listOf(100, 100)
        }

        test("TC-DATA-SYNC-DOMAIN-024 요청에 전달된 계정의 대상만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val context = context(accountId = accountId)
            coEvery {
                context.tagSyncLocalDataSource.findPending(accountId = otherAccountId)
            } returns tags(size = 1)
            coEvery {
                context.memoSyncLocalDataSource.findPending(accountId = otherAccountId)
            } returns memos(size = 1)

            context.subject.doWork(accountId = accountId)

            coVerify(exactly = 1) {
                context.tagSyncLocalDataSource.findPending(accountId = accountId)
            }
            coVerify(exactly = 1) {
                context.memoSyncLocalDataSource.findPending(accountId = accountId)
            }
            coVerify(exactly = 0) {
                context.tagSyncLocalDataSource.findPending(accountId = otherAccountId)
            }
            coVerify(exactly = 0) {
                context.memoSyncLocalDataSource.findPending(accountId = otherAccountId)
            }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-038 태그 내려받기가 지연되어도 메모 내려받기를 먼저 시작한다") {
            runTest {
                val context = context(tagList = tags(size = 1), memoList = memos(size = 1))
                val callOrder = mutableListOf<String>()
                coEvery { context.tagRemoteDataSource.push(any()) } coAnswers { callOrder += "tagPush" }
                coEvery { context.memoRemoteDataSource.push(any()) } coAnswers { callOrder += "memoPush" }
                coEvery { context.tagRemoteDataSource.pull(usn = 0L) } coAnswers {
                    delay(PULL_DELAY)
                    callOrder += "tagPullEnd"
                    emptyList()
                }
                coEvery { context.memoRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "memoPullEnd"
                    emptyList()
                }

                context.subject.doWork(accountId = context.accountId)

                callOrder shouldContainExactly
                    listOf("tagPush", "memoPush", "memoPullEnd", "tagPullEnd")
            }
        }

        test("TC-DATA-SYNC-DOMAIN-038 메모 내려받기가 지연되어도 태그 내려받기를 먼저 시작한다") {
            runTest {
                val context = context(tagList = tags(size = 1), memoList = memos(size = 1))
                val callOrder = mutableListOf<String>()
                coEvery { context.tagRemoteDataSource.push(any()) } coAnswers { callOrder += "tagPush" }
                coEvery { context.memoRemoteDataSource.push(any()) } coAnswers { callOrder += "memoPush" }
                coEvery { context.memoRemoteDataSource.pull(usn = 0L) } coAnswers {
                    delay(PULL_DELAY)
                    callOrder += "memoPullEnd"
                    emptyList()
                }
                coEvery { context.tagRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "tagPullEnd"
                    emptyList()
                }

                context.subject.doWork(accountId = context.accountId)

                callOrder shouldContainExactly
                    listOf("tagPush", "memoPush", "tagPullEnd", "memoPullEnd")
            }
        }

        test("TC-DATA-SYNC-DOMAIN-039 태그 내려받기가 실패해도 메모는 빈 응답까지 내려받는다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val firstMemoPullList = memoPulls(usnList = listOf(2L))
            val secondMemoPullList = memoPulls(usnList = listOf(5L))
            coEvery { context.tagRemoteDataSource.pull(any()) } throws failure
            coEvery { context.memoRemoteDataSource.pull(usn = 0L) } returns firstMemoPullList
            coEvery { context.memoRemoteDataSource.pull(usn = 2L) } returns secondMemoPullList
            coEvery { context.memoRemoteDataSource.pull(usn = 5L) } returns emptyList()

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.save(
                    accountId = context.accountId,
                    memoList = firstMemoPullList.map { pull -> pull.memo.toLocal() },
                    cursor = 2L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.save(
                    accountId = context.accountId,
                    memoList = secondMemoPullList.map { pull -> pull.memo.toLocal() },
                    cursor = 5L,
                )
            }
            coVerify(exactly = 1) { context.memoRemoteDataSource.pull(usn = 5L) }
        }

        test("TC-DATA-SYNC-DOMAIN-039 메모 내려받기가 실패해도 태그는 빈 응답까지 내려받는다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val firstTagPullList = tagPulls(usnList = listOf(3L))
            val secondTagPullList = tagPulls(usnList = listOf(6L))
            coEvery { context.memoRemoteDataSource.pull(any()) } throws failure
            coEvery { context.tagRemoteDataSource.pull(usn = 0L) } returns firstTagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 3L) } returns secondTagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 6L) } returns emptyList()

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.save(
                    accountId = context.accountId,
                    tagList = firstTagPullList.map { pull -> pull.tag.toLocal() },
                    cursor = 3L,
                )
            }
            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.save(
                    accountId = context.accountId,
                    tagList = secondTagPullList.map { pull -> pull.tag.toLocal() },
                    cursor = 6L,
                )
            }
            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 6L) }
        }

        listOf(
            Triple("태그", true, false),
            Triple("메모", false, true),
            Triple("태그와 메모 모두", true, true),
        ).forEach { (label, isTagFailed, isMemoFailed) ->
            test("TC-DATA-SYNC-DOMAIN-040 $label 내려받기가 실패하면 동기화가 실패한다") {
                val context = context()
                val failure = TestException(fixtureMonkey.giveMeOne())
                if (isTagFailed) {
                    coEvery { context.tagRemoteDataSource.pull(any()) } throws failure
                }
                if (isMemoFailed) {
                    coEvery { context.memoRemoteDataSource.pull(any()) } throws failure
                }

                val actual =
                    shouldThrowExactly<TestException> {
                        context.subject.doWork(accountId = context.accountId)
                    }

                actual.message shouldBe failure.message
                coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 0L) }
                coVerify(exactly = 1) { context.memoRemoteDataSource.pull(usn = 0L) }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드 묶음이 성공하면 보낸 항목의 대기 해제를 요청한다") {
            val tagList = tags(size = 101)
            val memoList = memos(size = 1)
            val context = context(tagList = tagList, memoList = memoList)

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.clearPending(
                    accountId = context.accountId,
                    tagList = tagList.take(100),
                )
            }
            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.clearPending(
                    accountId = context.accountId,
                    tagList = tagList.drop(100),
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.clearPending(
                    accountId = context.accountId,
                    memoList = memoList,
                )
            }
        }

        test("TC-DATA-SYNC-DOMAIN-028 업로드 묶음이 실패하면 그 묶음의 대기 해제를 요청하지 않는다") {
            val tagList = tags(size = 201)
            val context = context(tagList = tagList)
            val failure = TestException(fixtureMonkey.giveMeOne())
            val tagRequests = mutableListOf<List<TagRemoteEntity>>()
            coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                tagRequests += firstArg<List<TagRemoteEntity>>()
                if (tagRequests.size == 2) throw failure
            }

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.clearPending(
                    accountId = context.accountId,
                    tagList = tagList.take(100),
                )
            }
            coVerify(exactly = 0) {
                context.accountTagSyncTransaction.clearPending(
                    accountId = context.accountId,
                    tagList = tagList.subList(100, 200),
                )
            }
        }

        test("TC-DATA-SYNC-DOMAIN-029 업로드가 실패하면 내려받기를 진행하지 않는다") {
            val context = context(tagList = tags(size = 1), memoList = memos(size = 1))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.memoRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            coVerify(exactly = 0) { context.tagRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.pull(any()) }
        }

        test("TC-DATA-SYNC-DATA-016 응답을 저장한 뒤 가장 큰 순번으로 다음 묶음을 요청한다") {
            val context = context()
            val tagPullList = tagPulls(usnList = listOf(4L, 9L, 7L))
            coEvery { context.tagRemoteDataSource.pull(usn = 0L) } returns tagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 9L) } returns emptyList()

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.save(
                    accountId = context.accountId,
                    tagList = tagPullList.map { pull -> pull.tag.toLocal() },
                    cursor = 9L,
                )
            }
            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 9L) }
        }

        test("TC-DATA-SYNC-DATA-017 기록된 순번을 첫 내려받기 요청에 사용한다") {
            val context = context()
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.TAG) } returns 0L
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.MEMO) } returns 12L

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 0L) }
            coVerify(exactly = 1) { context.memoRemoteDataSource.pull(usn = 12L) }
        }

        test("TC-DATA-SYNC-DATA-020 빈 응답을 받으면 같은 종류의 다음 묶음을 요청하지 않는다") {
            val context = context()
            val tagPullList = tagPulls(usnList = listOf(5L))
            coEvery { context.tagRemoteDataSource.pull(usn = 0L) } returns tagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 5L) } returns emptyList()

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 0L) }
            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 5L) }
            coVerify(exactly = 2) { context.tagRemoteDataSource.pull(any()) }
            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.save(
                    accountId = context.accountId,
                    tagList = tagPullList.map { pull -> pull.tag.toLocal() },
                    cursor = 5L,
                )
            }
        }

        test("TC-DATA-SYNC-DATA-021 내려받기가 실패하면 저장하지 않고 마지막 순번으로 다시 요청한다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.TAG) } returns 8L
            coEvery { context.tagRemoteDataSource.pull(usn = 8L) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            coVerify(exactly = 0) {
                context.accountTagSyncTransaction.save(any(), any(), any())
            }

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            coVerify(exactly = 2) { context.tagRemoteDataSource.pull(usn = 8L) }
        }

        test("TC-DATA-SYNC-DATA-019 내려받은 항목의 전체 상태를 기기 저장 요청에 담는다") {
            val context = context()
            val memoPullList = memoPulls(usnList = listOf(2L))
            coEvery { context.memoRemoteDataSource.pull(usn = 0L) } returns memoPullList
            coEvery { context.memoRemoteDataSource.pull(usn = 2L) } returns emptyList()

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.save(
                    accountId = context.accountId,
                    memoList = memoPullList.map { pull -> pull.memo.toLocal() },
                    cursor = 2L,
                )
            }
        }

        test("TC-DATA-SYNC-DATA-002 업로드 실패는 로컬 사용자 데이터와 대기 상태를 변경하지 않는다") {
            val tagList = tags(size = 1)
            val originalTagList = tagList.map { tag -> tag.copy(detail = tag.detail.copy()) }
            val context = context(tagList = tagList)
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork(accountId = context.accountId)
                }

            actual.message shouldBe failure.message
            tagList shouldBe originalTagList
            coVerify(exactly = 1) {
                context.tagSyncLocalDataSource.findPending(accountId = context.accountId)
            }
            coVerify(exactly = 1) {
                context.tagRemoteDataSource.push(tagList = tagList.map { tag -> tag.toRemote() })
            }
            coVerify(exactly = 0) { context.accountTagSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.memoSyncLocalDataSource.findPending(any()) }
            confirmVerified(
                context.tagSyncLocalDataSource,
                context.memoSyncLocalDataSource,
                context.tagRemoteDataSource,
                context.memoRemoteDataSource,
            )
        }

        test("태그와 메모의 전체 대상을 각각 한 번 조회해 모두 요청한다") {
            val tagList = tags(size = 137)
            val memoList = memos(size = 123)
            val context = context(tagList = tagList, memoList = memoList)
            val tagRequests = mutableListOf<List<TagRemoteEntity>>()
            val memoRequests = mutableListOf<List<MemoRemoteEntity>>()
            coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                tagRequests += firstArg<List<TagRemoteEntity>>()
            }
            coEvery { context.memoRemoteDataSource.push(any()) } coAnswers {
                memoRequests += firstArg<List<MemoRemoteEntity>>()
            }

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) {
                context.tagSyncLocalDataSource.findPending(accountId = context.accountId)
            }
            coVerify(exactly = 1) {
                context.memoSyncLocalDataSource.findPending(accountId = context.accountId)
            }
            tagRequests.flatten() shouldContainExactly tagList.map { tag -> tag.toRemote() }
            memoRequests.flatten() shouldContainExactly memoList.map { memo -> memo.toRemote() }
        }

        listOf(
            1 to listOf(1),
            100 to listOf(100),
            101 to listOf(100, 1),
            200 to listOf(100, 100),
            201 to listOf(100, 100, 1),
        ).forEach { (itemCount, expectedRequestSizes) ->
            test("TC-DATA-SYNC-DATA-005 태그 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val tagList = tags(size = itemCount)
                val context = context(tagList = tagList)
                val requests = mutableListOf<List<TagRemoteEntity>>()
                coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<TagRemoteEntity>>()
                }

                context.subject.doWork(accountId = context.accountId)

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly tagList.map { tag -> tag.toRemote() }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-009 조회된 기존 대기 항목을 새 항목과 함께 요청한다") {
            val pendingTagList = tags(size = 3)
            val context = context(tagList = pendingTagList)
            val requests = mutableListOf<List<TagRemoteEntity>>()
            coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                requests += firstArg<List<TagRemoteEntity>>()
            }

            context.subject.doWork(accountId = context.accountId)

            requests.flatten() shouldContainExactly pendingTagList.map { tag -> tag.toRemote() }
        }

        test("TC-DATA-SYNC-DOMAIN-013 실패한 묶음은 같은 실행에서 다시 시도하지 않는다") {
            val context = context(tagList = tags(size = 1))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            coVerify(exactly = 1) { context.tagRemoteDataSource.push(any()) }
        }

        test("코루틴 취소는 그대로 전파한다") {
            val context = context(tagList = tags(size = 1))
            val cancellationException = CancellationException(fixtureMonkey.giveMeOne<String>())
            coEvery { context.tagRemoteDataSource.push(any()) } throws cancellationException

            val actual =
                shouldThrowExactly<CancellationException> {
                    context.subject.doWork(accountId = context.accountId)
                }

            actual.message shouldBe cancellationException.message
        }

        test("TC-DATA-SYNC-DATA-028 동시에 진행된 두 종류의 내려받기 위치는 서로 덮어쓰지 않는다") {
            val context = context()
            val tagPullList = tagPulls(usnList = listOf(7L))
            val memoPullList = memoPulls(usnList = listOf(11L))
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.TAG) } returns 3L
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.MEMO) } returns 4L
            coEvery { context.tagRemoteDataSource.pull(usn = 3L) } returns tagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 7L) } returns emptyList()
            coEvery { context.memoRemoteDataSource.pull(usn = 4L) } returns memoPullList
            coEvery { context.memoRemoteDataSource.pull(usn = 11L) } returns emptyList()

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.save(
                    accountId = context.accountId,
                    tagList = tagPullList.map { pull -> pull.tag.toLocal() },
                    cursor = 7L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.save(
                    accountId = context.accountId,
                    memoList = memoPullList.map { pull -> pull.memo.toLocal() },
                    cursor = 11L,
                )
            }
            coVerify(exactly = 0) { context.tagRemoteDataSource.pull(usn = 11L) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.pull(usn = 7L) }
        }

        test("내려받기 중 코루틴 취소는 그대로 전파한다") {
            val context = context()
            val cancellationException = CancellationException(fixtureMonkey.giveMeOne<String>())
            coEvery { context.tagRemoteDataSource.pull(any()) } throws cancellationException

            val actual =
                shouldThrowExactly<CancellationException> {
                    context.subject.doWork(accountId = context.accountId)
                }

            actual.message shouldBe cancellationException.message
        }

        test("응답의 순번이 커서보다 크지 않으면 같은 묶음을 다시 요청하지 않는다") {
            val context = context()
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.TAG) } returns 5L
            coEvery { context.tagRemoteDataSource.pull(usn = 5L) } returns tagPulls(usnList = listOf(5L))

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(any()) }
        }

        listOf<Pair<String, (TestContext, Throwable) -> Unit>>(
            "업로드: 태그" to { context, failure -> coEvery { context.tagRemoteDataSource.push(any()) } throws failure },
            "업로드: 메모" to { context, failure -> coEvery { context.memoRemoteDataSource.push(any()) } throws failure },
            "업로드: 연결" to { context, failure -> coEvery { context.memoTagRemoteDataSource.push(any()) } throws failure },
            "내려받기: 태그" to { context, failure -> coEvery { context.tagRemoteDataSource.pull(any()) } throws failure },
            "내려받기: 메모" to { context, failure -> coEvery { context.memoRemoteDataSource.pull(any()) } throws failure },
            "내려받기: 연결" to { context, failure -> coEvery { context.memoTagRemoteDataSource.pull(any()) } throws failure },
        ).forEach { (label, prepareFailure) ->
            test("TC-DATA-SYNC-DOMAIN-048 $label 단계가 실패하면 실패 원인을 담은 오류 보고가 한 번 남는다") {
                val context =
                    context(
                        tagList = tags(size = 1),
                        memoList = memos(size = 1),
                        memoTagList = memoTags(size = 1),
                    )
                val failure = TestException(fixtureMonkey.giveMeOne())
                prepareFailure(context, failure)
                val reportList = recordCrashlyticsLog()

                shouldThrowExactly<TestException> {
                    context.subject.doWork(accountId = context.accountId)
                }

                val report = reportList.single()
                report.throwable.shouldBeInstanceOf<TestException>()
                report.throwable.message shouldBe failure.message
                report.message shouldBe SYNC_FAILURE_REPORT_MESSAGE
            }
        }

        test("TC-DATA-SYNC-DOMAIN-049 여러 종류의 내려받기가 실패해도 오류 보고는 한 번만 남는다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.pull(any()) } throws failure
            coEvery { context.memoRemoteDataSource.pull(any()) } throws failure
            coEvery { context.memoTagRemoteDataSource.pull(any()) } throws failure
            val reportList = recordCrashlyticsLog()

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            reportList.shouldHaveSize(1)
        }

        test("TC-DATA-SYNC-DOMAIN-050 동기화가 성공하면 오류 보고가 남지 않는다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    memoList = memos(size = 1),
                    memoTagList = memoTags(size = 1),
                )
            val reportList = recordCrashlyticsLog()

            context.subject.doWork(accountId = context.accountId)

            reportList.shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-051 진행 중인 동기화가 취소되면 오류 보고가 남지 않는다") {
            val context = context(tagList = tags(size = 1))
            val cancellationException = CancellationException(fixtureMonkey.giveMeOne<String>())
            coEvery { context.tagRemoteDataSource.push(any()) } throws cancellationException
            val reportList = recordCrashlyticsLog()

            shouldThrowExactly<CancellationException> {
                context.subject.doWork(accountId = context.accountId)
            }

            reportList.shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-052 오프라인으로 인한 동기화 실패도 오류 보고로 남는다") {
            val context = context(tagList = tags(size = 1))
            val failure = UnknownHostException(fixtureMonkey.giveMeOne<String>())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure
            val reportList = recordCrashlyticsLog()

            shouldThrowExactly<UnknownHostException> {
                context.subject.doWork(accountId = context.accountId)
            }

            val report = reportList.single()
            report.throwable.shouldBeInstanceOf<UnknownHostException>()
            report.throwable.message shouldBe failure.message
        }
    })

private const val SYNC_FAILURE_REPORT_MESSAGE: String = "데이터 동기화 실패"
