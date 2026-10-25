package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.model.account.Account
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import java.net.UnknownHostException
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SyncWorkTest :
    FunSpec({
        test("TC-674eebce-610c-4b46-8dce-79aef5252327 모든 태그 요청이 성공한 뒤 메모 요청을 시작한다") {
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

            context.subject.doWork()

            requestOrder shouldContainExactly listOf("tag", "tag", "tag", "memo", "memo")
        }

        listOf(
            Triple(0, 1, Pair(0, 1)),
            Triple(1, 0, Pair(1, 0)),
            Triple(0, 0, Pair(0, 0)),
        ).forEach { (tagCount, memoCount, expectedRequestCounts) ->
            test("태그 $tagCount 개, 메모 $memoCount 개면 빈 종류 요청을 생략한다") {
                val context = context(tagList = tags(tagCount), memoList = memos(memoCount))

                context.subject.doWork()

                coVerify(exactly = expectedRequestCounts.first) { context.tagRemoteDataSource.push(any()) }
                coVerify(exactly = expectedRequestCounts.second) { context.memoRemoteDataSource.push(any()) }
            }
        }

        test("TC-bb1f2746-1f2d-465a-8da5-35cff756be8c 태그 묶음 실패 시 이후 태그와 모든 메모를 시도하지 않는다") {
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
                    context.subject.doWork()
                }

            actual.message shouldBe failure.message
            tagRequests.map { request -> request.size } shouldContainExactly listOf(100, 100)
            coVerify(exactly = 0) { context.memoSyncLocalDataSource.readPendingList(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
        }

        test("TC-de4069c2-e7bb-4e76-b57a-4a67d557b007 메모 묶음 실패 시 이후 메모를 시도하지 않는다") {
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
                    context.subject.doWork()
                }

            actual.message shouldBe failure.message
            memoRequests.map { request -> request.size } shouldContainExactly listOf(100, 100)
        }

        test("TC-28dda1bd-78ef-4aef-943d-5bf25ae21551 실행 시점에 확인된 계정의 대상만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val context = context(accountId = accountId)
            coEvery {
                context.tagSyncLocalDataSource.readPendingList(accountId = otherAccountId)
            } returns tags(size = 1)
            coEvery {
                context.memoSyncLocalDataSource.readPendingList(accountId = otherAccountId)
            } returns memos(size = 1)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.tagSyncLocalDataSource.readPendingList(accountId = accountId)
            }
            coVerify(exactly = 1) {
                context.memoSyncLocalDataSource.readPendingList(accountId = accountId)
            }
            coVerify(exactly = 0) {
                context.tagSyncLocalDataSource.readPendingList(accountId = otherAccountId)
            }
            coVerify(exactly = 0) {
                context.memoSyncLocalDataSource.readPendingList(accountId = otherAccountId)
            }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
        }

        test("TC-cdfd501c-c0ba-469f-a52a-bc040216c2e6 요청과 실행 사이에 계정이 바뀌면 바뀐 계정을 동기화한다") {
            val firstAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val secondAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val context =
                context(
                    accountId = secondAccountId,
                    accountFlow =
                        flowOf(
                            Result.success(sessionValidUser(accountId = firstAccountId)),
                            Result.success(sessionValidUser(accountId = secondAccountId)),
                        ).drop(1),
                )
            coEvery {
                context.tagSyncLocalDataSource.readPendingList(accountId = firstAccountId)
            } returns tags(size = 1)

            context.subject.doWork()

            coVerify(exactly = 1) { context.tagSyncLocalDataSource.readPendingList(accountId = secondAccountId) }
            coVerify(exactly = 0) { context.tagSyncLocalDataSource.readPendingList(accountId = firstAccountId) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
        }

        test("TC-1e1afcba-2529-4795-86dd-6b45477383c9 실행 시점에 계정이 게스트면 아무것도 동기화하지 않고 정상적으로 끝낸다") {
            val reportList = recordCrashlyticsLog()
            val context =
                context(
                    accountFlow = flowOf(Result.success(Account.Guest)),
                    tagList = tags(size = 1),
                    memoList = memos(size = 1),
                )

            context.subject.doWork()

            coVerify(exactly = 0) { context.tagSyncLocalDataSource.readPendingList(accountId = any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.pull(any()) }
            reportList.shouldBeEmpty()
        }

        listOf(
            "세션 확인 중" to true,
            "인증되지 않은 것으로 확인됨" to false,
        ).forEach { (label, isSessionPending) ->
            test("TC-184c320d-7763-4932-8c52-55eb8b129cad 세션이 $label 이면 갱신될 때까지 기다린 뒤 동기화한다") {
                runTest {
                    val accountId = fixtureMonkey.giveMeOne<Uuid>()
                    val accountFlow = MutableSharedFlow<Result<Account>>(replay = 1)
                    accountFlow.emit(Result.success(sessionInvalidUser(accountId = accountId).copy(isSessionPending = isSessionPending)))
                    val context = context(accountId = accountId, accountFlow = accountFlow, tagList = tags(size = 1))

                    val job = launch { context.subject.doWork() }
                    runCurrent()

                    job.isCompleted shouldBe false
                    coVerify(exactly = 0) { context.tagSyncLocalDataSource.readPendingList(accountId = any()) }
                    coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }

                    accountFlow.emit(Result.success(sessionValidUser(accountId = accountId)))
                    job.join()

                    coVerify(exactly = 1) { context.tagSyncLocalDataSource.readPendingList(accountId = accountId) }
                    coVerify(exactly = 1) { context.tagRemoteDataSource.push(any()) }
                }
            }
        }

        test("TC-d6ad6c31-e1e9-435a-ab90-64a6c654d457 태그 내려받기가 지연되어도 메모 내려받기를 먼저 시작한다") {
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

                context.subject.doWork()

                callOrder shouldContainExactly
                    listOf("tagPush", "memoPush", "memoPullEnd", "tagPullEnd")
            }
        }

        test("TC-d6ad6c31-e1e9-435a-ab90-64a6c654d457 메모 내려받기가 지연되어도 태그 내려받기를 먼저 시작한다") {
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

                context.subject.doWork()

                callOrder shouldContainExactly
                    listOf("tagPush", "memoPush", "tagPullEnd", "memoPullEnd")
            }
        }

        test("TC-319f896c-9872-4455-b4d1-a91dc09e1e9b 태그 내려받기가 실패해도 메모는 빈 응답까지 내려받는다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val firstMemoPullList = memoPulls(usnList = listOf(2L))
            val secondMemoPullList = memoPulls(usnList = listOf(5L))
            coEvery { context.tagRemoteDataSource.pull(any()) } throws failure
            coEvery { context.memoRemoteDataSource.pull(usn = 0L) } returns firstMemoPullList
            coEvery { context.memoRemoteDataSource.pull(usn = 2L) } returns secondMemoPullList
            coEvery { context.memoRemoteDataSource.pull(usn = 5L) } returns emptyList()

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.upsert(
                    accountId = context.accountId,
                    memoList = firstMemoPullList.map { pull -> pull.memo.toLocal() },
                    cursor = 2L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.upsert(
                    accountId = context.accountId,
                    memoList = secondMemoPullList.map { pull -> pull.memo.toLocal() },
                    cursor = 5L,
                )
            }
            coVerify(exactly = 1) { context.memoRemoteDataSource.pull(usn = 5L) }
        }

        test("TC-319f896c-9872-4455-b4d1-a91dc09e1e9b 메모 내려받기가 실패해도 태그는 빈 응답까지 내려받는다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val firstTagPullList = tagPulls(usnList = listOf(3L))
            val secondTagPullList = tagPulls(usnList = listOf(6L))
            coEvery { context.memoRemoteDataSource.pull(any()) } throws failure
            coEvery { context.tagRemoteDataSource.pull(usn = 0L) } returns firstTagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 3L) } returns secondTagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 6L) } returns emptyList()

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.upsert(
                    accountId = context.accountId,
                    tagList = firstTagPullList.map { pull -> pull.tag.toLocal() },
                    cursor = 3L,
                )
            }
            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.upsert(
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
            test("TC-51b016e3-825d-4ef5-9968-ecbf31b4c8a4 $label 내려받기가 실패하면 동기화가 실패한다") {
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
                        context.subject.doWork()
                    }

                actual.message shouldBe failure.message
                coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 0L) }
                coVerify(exactly = 1) { context.memoRemoteDataSource.pull(usn = 0L) }
            }
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 업로드 묶음이 성공하면 보낸 항목의 대기 해제를 요청한다") {
            val tagList = tags(size = 101)
            val memoList = memos(size = 1)
            val context = context(tagList = tagList, memoList = memoList)

            context.subject.doWork()

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

        test("TC-a31c9e09-00c4-4621-8826-33dfae97e093 업로드 묶음이 실패하면 그 묶음의 대기 해제를 요청하지 않는다") {
            val tagList = tags(size = 201)
            val context = context(tagList = tagList)
            val failure = TestException(fixtureMonkey.giveMeOne())
            val tagRequests = mutableListOf<List<TagRemoteEntity>>()
            coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                tagRequests += firstArg<List<TagRemoteEntity>>()
                if (tagRequests.size == 2) throw failure
            }

            shouldThrowExactly<TestException> {
                context.subject.doWork()
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

        test("TC-929d599a-bd66-4c34-8b8f-fd0df8e6aeee 업로드가 실패하면 내려받기를 진행하지 않는다") {
            val context = context(tagList = tags(size = 1), memoList = memos(size = 1))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.memoRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 0) { context.tagRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.pull(any()) }
        }

        test("TC-85437a34-b1a2-4fa0-ae7a-ec766c5b8952 응답을 저장한 뒤 가장 큰 순번으로 다음 묶음을 요청한다") {
            val context = context()
            val tagPullList = tagPulls(usnList = listOf(4L, 9L, 7L))
            coEvery { context.tagRemoteDataSource.pull(usn = 0L) } returns tagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 9L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.upsert(
                    accountId = context.accountId,
                    tagList = tagPullList.map { pull -> pull.tag.toLocal() },
                    cursor = 9L,
                )
            }
            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 9L) }
        }

        test("TC-cce8783a-21bb-4316-bdb7-74d4d2fe8853 기록된 순번을 첫 내려받기 요청에 사용한다") {
            val context = context()
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.TAG) } returns 0L
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.MEMO) } returns 12L

            context.subject.doWork()

            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 0L) }
            coVerify(exactly = 1) { context.memoRemoteDataSource.pull(usn = 12L) }
        }

        test("TC-21ff0303-9f3c-4cb3-9f35-4251ec918ee8 빈 응답을 받으면 같은 종류의 다음 묶음을 요청하지 않는다") {
            val context = context()
            val tagPullList = tagPulls(usnList = listOf(5L))
            coEvery { context.tagRemoteDataSource.pull(usn = 0L) } returns tagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 5L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 0L) }
            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 5L) }
            coVerify(exactly = 2) { context.tagRemoteDataSource.pull(any()) }
            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.upsert(
                    accountId = context.accountId,
                    tagList = tagPullList.map { pull -> pull.tag.toLocal() },
                    cursor = 5L,
                )
            }
        }

        test("TC-2d656b4a-c48b-4cf5-b9da-04dbd7457b62 내려받기가 실패하면 저장하지 않고 마지막 순번으로 다시 요청한다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.TAG) } returns 8L
            coEvery { context.tagRemoteDataSource.pull(usn = 8L) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 0) {
                context.accountTagSyncTransaction.upsert(any(), any(), any())
            }

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 2) { context.tagRemoteDataSource.pull(usn = 8L) }
        }

        test("TC-c368908b-b191-4ceb-9b5d-e82025cf6da8 내려받은 항목의 전체 상태를 기기 저장 요청에 담는다") {
            val context = context()
            val memoPullList = memoPulls(usnList = listOf(2L))
            coEvery { context.memoRemoteDataSource.pull(usn = 0L) } returns memoPullList
            coEvery { context.memoRemoteDataSource.pull(usn = 2L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.upsert(
                    accountId = context.accountId,
                    memoList = memoPullList.map { pull -> pull.memo.toLocal() },
                    cursor = 2L,
                )
            }
        }

        test("TC-93f39ef0-82c2-433f-9711-df1f20a51b65 업로드 실패는 로컬 사용자 데이터와 대기 상태를 변경하지 않는다") {
            val tagList = tags(size = 1)
            val originalTagList = tagList.map { tag -> tag.copy(detail = tag.detail.copy()) }
            val context = context(tagList = tagList)
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

            actual.message shouldBe failure.message
            tagList shouldBe originalTagList
            coVerify(exactly = 1) {
                context.tagSyncLocalDataSource.readPendingList(accountId = context.accountId)
            }
            coVerify(exactly = 1) {
                context.tagRemoteDataSource.push(tagList = tagList.map { tag -> tag.toRemote() })
            }
            coVerify(exactly = 0) { context.accountTagSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.memoSyncLocalDataSource.readPendingList(any()) }
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

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.tagSyncLocalDataSource.readPendingList(accountId = context.accountId)
            }
            coVerify(exactly = 1) {
                context.memoSyncLocalDataSource.readPendingList(accountId = context.accountId)
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
            test("TC-400fe05d-3f17-47cf-b0bb-ca12fe6625c0 태그 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val tagList = tags(size = itemCount)
                val context = context(tagList = tagList)
                val requests = mutableListOf<List<TagRemoteEntity>>()
                coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<TagRemoteEntity>>()
                }

                context.subject.doWork()

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly tagList.map { tag -> tag.toRemote() }
            }
        }

        test("TC-8acaf2e7-bda0-44da-aa89-2b41b9addcb8 조회된 기존 대기 항목을 새 항목과 함께 요청한다") {
            val pendingTagList = tags(size = 3)
            val context = context(tagList = pendingTagList)
            val requests = mutableListOf<List<TagRemoteEntity>>()
            coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                requests += firstArg<List<TagRemoteEntity>>()
            }

            context.subject.doWork()

            requests.flatten() shouldContainExactly pendingTagList.map { tag -> tag.toRemote() }
        }

        test("TC-b4f2910f-0ea0-4fb8-a19f-9c663c09cd76 실패한 묶음은 같은 실행에서 다시 시도하지 않는다") {
            val context = context(tagList = tags(size = 1))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 1) { context.tagRemoteDataSource.push(any()) }
        }

        test("코루틴 취소는 그대로 전파한다") {
            val context = context(tagList = tags(size = 1))
            val cancellationException = CancellationException(fixtureMonkey.giveMeOne<String>())
            coEvery { context.tagRemoteDataSource.push(any()) } throws cancellationException

            val actual =
                shouldThrowExactly<CancellationException> {
                    context.subject.doWork()
                }

            actual.message shouldBe cancellationException.message
        }

        test("TC-5dd13cc9-5011-41cf-bfae-1314fe0084b6 동시에 진행된 두 종류의 내려받기 위치는 서로 덮어쓰지 않는다") {
            val context = context()
            val tagPullList = tagPulls(usnList = listOf(7L))
            val memoPullList = memoPulls(usnList = listOf(11L))
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.TAG) } returns 3L
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.MEMO) } returns 4L
            coEvery { context.tagRemoteDataSource.pull(usn = 3L) } returns tagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 7L) } returns emptyList()
            coEvery { context.memoRemoteDataSource.pull(usn = 4L) } returns memoPullList
            coEvery { context.memoRemoteDataSource.pull(usn = 11L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.upsert(
                    accountId = context.accountId,
                    tagList = tagPullList.map { pull -> pull.tag.toLocal() },
                    cursor = 7L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.upsert(
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
                    context.subject.doWork()
                }

            actual.message shouldBe cancellationException.message
        }

        test("응답의 순번이 커서보다 크지 않으면 같은 묶음을 다시 요청하지 않는다") {
            val context = context()
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.TAG) } returns 5L
            coEvery { context.tagRemoteDataSource.pull(usn = 5L) } returns tagPulls(usnList = listOf(5L))

            context.subject.doWork()

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
            test("TC-ff799c65-2ad3-472d-893b-a41a7586012c $label 단계가 실패하면 실패 원인을 담은 오류 보고가 한 번 남는다") {
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
                    context.subject.doWork()
                }

                val report = reportList.single()
                report.throwable.shouldBeInstanceOf<TestException>()
                report.throwable.message shouldBe failure.message
                report.message shouldBe SYNC_FAILURE_REPORT_MESSAGE
            }
        }

        test("TC-06eb87fd-45cd-42b6-a11f-176c8073a21d 여러 종류의 내려받기가 실패해도 오류 보고는 한 번만 남는다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.pull(any()) } throws failure
            coEvery { context.memoRemoteDataSource.pull(any()) } throws failure
            coEvery { context.memoTagRemoteDataSource.pull(any()) } throws failure
            val reportList = recordCrashlyticsLog()

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            reportList.shouldHaveSize(1)
        }

        test("TC-367c7b17-98eb-4fb9-a44e-072b4be2c056 동기화가 성공하면 오류 보고가 남지 않는다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    memoList = memos(size = 1),
                    memoTagList = memoTags(size = 1),
                )
            val reportList = recordCrashlyticsLog()

            context.subject.doWork()

            reportList.shouldBeEmpty()
        }

        test("TC-08cbe2b5-73ec-4ef2-b388-f95850b0b1ed 진행 중인 동기화가 취소되면 오류 보고가 남지 않는다") {
            val context = context(tagList = tags(size = 1))
            val cancellationException = CancellationException(fixtureMonkey.giveMeOne<String>())
            coEvery { context.tagRemoteDataSource.push(any()) } throws cancellationException
            val reportList = recordCrashlyticsLog()

            shouldThrowExactly<CancellationException> {
                context.subject.doWork()
            }

            reportList.shouldBeEmpty()
        }

        test("TC-89d9c2ca-e9f9-4c8e-99c3-eba824b3717e 오프라인으로 인한 동기화 실패도 오류 보고로 남는다") {
            val context = context(tagList = tags(size = 1))
            val failure = UnknownHostException(fixtureMonkey.giveMeOne<String>())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure
            val reportList = recordCrashlyticsLog()

            shouldThrowExactly<UnknownHostException> {
                context.subject.doWork()
            }

            val report = reportList.single()
            report.throwable.shouldBeInstanceOf<UnknownHostException>()
            report.throwable.message shouldBe failure.message
        }
    })

private const val SYNC_FAILURE_REPORT_MESSAGE: String = "데이터 동기화 실패"
