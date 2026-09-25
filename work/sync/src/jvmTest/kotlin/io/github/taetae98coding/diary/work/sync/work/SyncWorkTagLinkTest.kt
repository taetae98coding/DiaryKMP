package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkRemoteEntity
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
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SyncWorkTagLinkTest :
    FunSpec({
        test("TC-DATA-SYNC-DOMAIN-019 TC-TAG-LINK-DATA-004 모든 태그 요청이 성공한 뒤 태그 연결 전용 요청을 시작한다") {
            val context =
                context(
                    tagList = tags(size = 101),
                    tagLinkList = tagLinks(size = 101),
                )
            val requestOrder = mutableListOf<String>()
            coEvery { context.tagRemoteDataSource.push(any()) } coAnswers { requestOrder += "tag" }
            coEvery { context.tagLinkRemoteDataSource.push(any()) } coAnswers { requestOrder += "tagLink" }

            context.subject.doWork()

            requestOrder shouldContainExactly listOf("tag", "tag", "tagLink", "tagLink")
        }

        test("태그 연결만 대기하면 태그 연결 요청만 발생한다") {
            val context = context(tagLinkList = tagLinks(size = 1))

            context.subject.doWork()

            coVerify(exactly = 1) { context.tagLinkRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.placeRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.webRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoTagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoPlaceRemoteDataSource.push(any()) }
        }

        listOf(
            1 to listOf(1),
            100 to listOf(100),
            201 to listOf(100, 100, 1),
        ).forEach { (itemCount, expectedRequestSizes) ->
            test("TC-DATA-SYNC-DATA-005 태그 연결 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val tagLinkList = tagLinks(size = itemCount)
                val context = context(tagLinkList = tagLinkList)
                val requests = mutableListOf<List<TagLinkRemoteEntity>>()
                coEvery { context.tagLinkRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<TagLinkRemoteEntity>>()
                }

                context.subject.doWork()

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly tagLinkList.map { tagLink -> tagLink.toRemote() }
            }
        }

        test("TC-TAG-LINK-DATA-005 해제된 연결도 해제 상태로 업로드된다") {
            val tagLinkList = listOf(tagLink(isDeleted = true), tagLink(isDeleted = false))
            val context = context(tagLinkList = tagLinkList)
            val requests = mutableListOf<List<TagLinkRemoteEntity>>()
            coEvery { context.tagLinkRemoteDataSource.push(any()) } coAnswers {
                requests += firstArg<List<TagLinkRemoteEntity>>()
            }

            context.subject.doWork()

            requests.flatten() shouldContainExactly tagLinkList.map { tagLink -> tagLink.toRemote() }
            requests.flatten().map { request -> request.isDeleted } shouldContainExactly listOf(true, false)
        }

        test("TC-DATA-SYNC-DOMAIN-026 태그 연결 업로드 묶음이 성공하면 보낸 항목의 대기 해제를 요청한다") {
            val tagLinkList = tagLinks(size = 101)
            val context = context(tagLinkList = tagLinkList)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountTagLinkSyncTransaction.clearPending(
                    accountId = context.accountId,
                    tagLinkList = tagLinkList.take(100),
                )
            }
            coVerify(exactly = 1) {
                context.accountTagLinkSyncTransaction.clearPending(
                    accountId = context.accountId,
                    tagLinkList = tagLinkList.drop(100),
                )
            }
        }

        test("TC-DATA-SYNC-DOMAIN-029 태그 연결 업로드가 실패하면 내려받기를 진행하지 않는다") {
            val context = context(tagLinkList = tagLinks(size = 1))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagLinkRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 0) { context.accountTagLinkSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.tagLinkRemoteDataSource.pull(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-021 태그 업로드가 실패하면 태그 연결을 시도하지 않는다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    tagLinkList = tagLinks(size = 1),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 0) { context.tagLinkSyncLocalDataSource.findPending(any()) }
            coVerify(exactly = 0) { context.tagLinkRemoteDataSource.push(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-054 메모 업로드가 실패해도 태그 연결은 끝까지 업로드한다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    memoList = memos(size = 201),
                    tagLinkList = tagLinks(size = 201),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            val memoRequestCount = mutableListOf<Int>()
            coEvery { context.memoRemoteDataSource.push(any()) } coAnswers {
                memoRequestCount += 1
                if (memoRequestCount.size == 2) throw failure
            }

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 3) { context.tagLinkRemoteDataSource.push(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-054 태그 연결 업로드가 실패해도 메모와 메모·태그 연결은 끝까지 업로드한다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    memoList = memos(size = 201),
                    memoTagList = memoTags(size = 201),
                    tagLinkList = tagLinks(size = 201),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            val tagLinkRequestCount = mutableListOf<Int>()
            coEvery { context.tagLinkRemoteDataSource.push(any()) } coAnswers {
                tagLinkRequestCount += 1
                if (tagLinkRequestCount.size == 2) throw failure
            }

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            tagLinkRequestCount.size shouldBe 2
            coVerify(exactly = 3) { context.memoRemoteDataSource.push(any()) }
            coVerify(exactly = 3) { context.memoTagRemoteDataSource.push(any()) }
        }

        test("TC-TAG-LINK-DATA-006 태그 연결 내려받기는 기록된 순번부터 빈 응답까지 반복한다") {
            val context = context()
            val firstPullList = tagLinkPulls(usnList = listOf(4L, 6L))
            val secondPullList = tagLinkPulls(usnList = listOf(9L))
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.TAG_LINK) } returns 2L
            coEvery { context.tagLinkRemoteDataSource.pull(usn = 2L) } returns firstPullList
            coEvery { context.tagLinkRemoteDataSource.pull(usn = 6L) } returns secondPullList
            coEvery { context.tagLinkRemoteDataSource.pull(usn = 9L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountTagLinkSyncTransaction.save(
                    accountId = context.accountId,
                    tagLinkList = firstPullList.map { pull -> pull.tagLink.toLocal() },
                    cursor = 6L,
                )
            }
            coVerify(exactly = 1) {
                context.accountTagLinkSyncTransaction.save(
                    accountId = context.accountId,
                    tagLinkList = secondPullList.map { pull -> pull.tagLink.toLocal() },
                    cursor = 9L,
                )
            }
            coVerify(exactly = 3) { context.tagLinkRemoteDataSource.pull(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-038 태그 연결 내려받기가 지연되어도 나머지 종류의 내려받기를 먼저 시작한다") {
            runTest {
                val context = context()
                val callOrder = mutableListOf<String>()
                coEvery { context.tagLinkRemoteDataSource.pull(usn = 0L) } coAnswers {
                    delay(PULL_DELAY)
                    callOrder += "tagLinkPullEnd"
                    emptyList()
                }
                coEvery { context.tagRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "tagPullEnd"
                    emptyList()
                }
                coEvery { context.placeRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "placePullEnd"
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
                coEvery { context.memoTagRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "memoTagPullEnd"
                    emptyList()
                }
                coEvery { context.memoPlaceRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "memoPlacePullEnd"
                    emptyList()
                }

                context.subject.doWork()

                callOrder.last() shouldBe "tagLinkPullEnd"
                callOrder.dropLast(1) shouldContainExactlyInAnyOrder
                    listOf("tagPullEnd", "placePullEnd", "webPullEnd", "memoPullEnd", "memoTagPullEnd", "memoPlacePullEnd")
            }
        }

        test("TC-DATA-SYNC-DOMAIN-039 TC-DATA-SYNC-DOMAIN-040 태그 연결 내려받기가 실패해도 태그와 메모는 빈 응답까지 내려받고 동기화는 실패한다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val tagPullList = tagPulls(usnList = listOf(3L))
            val memoPullList = memoPulls(usnList = listOf(4L))
            coEvery { context.tagLinkRemoteDataSource.pull(any()) } throws failure
            coEvery { context.tagRemoteDataSource.pull(usn = 0L) } returns tagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 3L) } returns emptyList()
            coEvery { context.memoRemoteDataSource.pull(usn = 0L) } returns memoPullList
            coEvery { context.memoRemoteDataSource.pull(usn = 4L) } returns emptyList()

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

            actual.message shouldBe failure.message
            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.save(
                    accountId = context.accountId,
                    tagList = tagPullList.map { pull -> pull.tag.toLocal() },
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
            coVerify(exactly = 0) { context.accountTagLinkSyncTransaction.save(any(), any(), any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-024 요청에 전달된 계정의 태그 연결만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val context = context(accountId = accountId)
            coEvery {
                context.tagLinkSyncLocalDataSource.findPending(accountId = otherAccountId)
            } returns tagLinks(size = 1)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.tagLinkSyncLocalDataSource.findPending(accountId = accountId)
            }
            coVerify(exactly = 0) {
                context.tagLinkSyncLocalDataSource.findPending(accountId = otherAccountId)
            }
            coVerify(exactly = 0) { context.tagLinkRemoteDataSource.push(any()) }
        }
    }) {
    public companion object {
        private fun tagLink(isDeleted: Boolean): TagLinkLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLinkLocalEntity>()
                .setExp(TagLinkLocalEntity::isDeleted, isDeleted)
                .setExp(TagLinkLocalEntity::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(TagLinkLocalEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
