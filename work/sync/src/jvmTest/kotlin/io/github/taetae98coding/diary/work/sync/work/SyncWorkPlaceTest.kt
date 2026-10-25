package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memoplace.entity.MemoPlaceRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagRemoteEntity
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity
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

class SyncWorkPlaceTest :
    FunSpec({
        listOf(
            Triple("메모", "태그", listOf("tag", "tag", "memo")),
            Triple("메모와 태그의 연결", "메모", listOf("memo", "memo", "memoTag")),
            Triple("메모와 장소의 연결", "장소", listOf("place", "place", "memoPlace")),
        ).forEach { (label, dependency, expectedOrder) ->
            test("TC-674eebce-610c-4b46-8dce-79aef5252327 $label 은 $dependency 의 마지막 요청이 끝난 뒤에 시작한다") {
                val context =
                    context(
                        tagList = if (dependency == "태그") tags(size = 101) else emptyList(),
                        placeList = if (dependency == "장소") places(size = 101) else emptyList(),
                        memoList =
                            when {
                                dependency == "메모" -> memos(size = 101)
                                label == "메모" -> memos(size = 1)
                                else -> emptyList()
                            },
                        memoTagList = if (label == "메모와 태그의 연결") memoTags(size = 1) else emptyList(),
                        memoPlaceList = if (label == "메모와 장소의 연결") memoPlaces(size = 1) else emptyList(),
                    )
                val requestOrder = mutableListOf<String>()
                coEvery { context.tagRemoteDataSource.push(any()) } coAnswers { requestOrder += "tag" }
                coEvery { context.placeRemoteDataSource.push(any()) } coAnswers { requestOrder += "place" }
                coEvery { context.memoRemoteDataSource.push(any()) } coAnswers { requestOrder += "memo" }
                coEvery { context.memoTagRemoteDataSource.push(any()) } coAnswers { requestOrder += "memoTag" }
                coEvery { context.memoPlaceRemoteDataSource.push(any()) } coAnswers { requestOrder += "memoPlace" }

                context.subject.doWork()

                requestOrder shouldContainExactly expectedOrder
            }
        }

        test("TC-674eebce-610c-4b46-8dce-79aef5252327 장소와 메모 요청이 모두 성공한 뒤 메모·장소 연결 전용 요청을 시작한다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    placeList = places(size = 101),
                    memoList = memos(size = 101),
                    memoPlaceList = memoPlaces(size = 101),
                )
            val requestOrder = mutableListOf<String>()
            coEvery { context.placeRemoteDataSource.push(any()) } coAnswers { requestOrder += "place" }
            coEvery { context.memoRemoteDataSource.push(any()) } coAnswers { requestOrder += "memo" }
            coEvery { context.memoPlaceRemoteDataSource.push(any()) } coAnswers { requestOrder += "memoPlace" }

            context.subject.doWork()

            requestOrder.takeLast(2) shouldContainExactly listOf("memoPlace", "memoPlace")
            requestOrder.dropLast(2) shouldContainExactlyInAnyOrder listOf("place", "place", "memo", "memo")
        }

        listOf(
            "태그" to "장소",
            "장소" to "태그",
        ).forEach { (delayed, other) ->
            test("TC-d02e1fe4-3710-497f-b721-5bd09e834ad1 $delayed 업로드가 지연되어도 $other 업로드를 먼저 시작한다") {
                runTest {
                    val context = context(tagList = tags(size = 1), placeList = places(size = 1))
                    val callOrder = mutableListOf<String>()
                    coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                        if (delayed == "태그") delay(PUSH_DELAY)
                        callOrder += "tagPushEnd"
                    }
                    coEvery { context.placeRemoteDataSource.push(any()) } coAnswers {
                        if (delayed == "장소") delay(PUSH_DELAY)
                        callOrder += "placePushEnd"
                    }

                    context.subject.doWork()

                    val delayedEnd = if (delayed == "태그") "tagPushEnd" else "placePushEnd"
                    callOrder.last() shouldBe delayedEnd
                }
            }
        }

        test("장소만 대기하면 장소 요청만 발생한다") {
            val context = context(placeList = places(size = 1))

            context.subject.doWork()

            coVerify(exactly = 1) { context.placeRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.webRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoTagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoPlaceRemoteDataSource.push(any()) }
        }

        test("메모·장소 연결만 대기하면 그 연결 요청만 발생한다") {
            val context = context(memoPlaceList = memoPlaces(size = 1))

            context.subject.doWork()

            coVerify(exactly = 1) { context.memoPlaceRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.placeRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoTagRemoteDataSource.push(any()) }
        }

        test("대기 항목이 없으면 일곱 종류 요청이 모두 없다") {
            val context = context()

            context.subject.doWork()

            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.placeRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.webRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoTagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoPlaceRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagLinkRemoteDataSource.push(any()) }
        }

        test("장소 묶음이 실패하면 메모와 장소의 연결을 시도하지 않는다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    placeList = places(size = 201),
                    memoList = memos(size = 1),
                    memoTagList = memoTags(size = 1),
                    memoPlaceList = memoPlaces(size = 1),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            val placeRequests = mutableListOf<List<PlaceRemoteEntity>>()
            coEvery { context.placeRemoteDataSource.push(any()) } coAnswers {
                placeRequests += firstArg<List<PlaceRemoteEntity>>()
                if (placeRequests.size == 2) throw failure
            }

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

            actual.message shouldBe failure.message
            placeRequests.map { request -> request.size } shouldContainExactly listOf(100, 100)
            coVerify(exactly = 0) { context.memoPlaceSyncLocalDataSource.readPendingList(any()) }
            coVerify(exactly = 0) { context.memoPlaceRemoteDataSource.push(any()) }
            coVerify(exactly = 1) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 1) { context.memoRemoteDataSource.push(any()) }
            coVerify(exactly = 1) { context.memoTagRemoteDataSource.push(any()) }
        }

        test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 메모·태그 연결이 실패해도 메모·장소 연결은 끝까지 업로드한다") {
            val context =
                context(
                    memoTagList = memoTags(size = 201),
                    memoPlaceList = memoPlaces(size = 201),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            val memoTagRequests = mutableListOf<List<MemoTagRemoteEntity>>()
            val memoPlaceRequests = mutableListOf<List<MemoPlaceRemoteEntity>>()
            coEvery { context.memoTagRemoteDataSource.push(any()) } coAnswers {
                memoTagRequests += firstArg<List<MemoTagRemoteEntity>>()
                if (memoTagRequests.size == 2) throw failure
            }
            coEvery { context.memoPlaceRemoteDataSource.push(any()) } coAnswers {
                memoPlaceRequests += firstArg<List<MemoPlaceRemoteEntity>>()
            }

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

            actual.message shouldBe failure.message
            memoTagRequests.map { request -> request.size } shouldContainExactly listOf(100, 100)
            memoPlaceRequests.map { request -> request.size } shouldContainExactly listOf(100, 100, 1)
        }

        test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 태그가 실패해도 장소는 끝까지 업로드한다") {
            val context = context(tagList = tags(size = 201), placeList = places(size = 201))
            val failure = TestException(fixtureMonkey.giveMeOne())
            val tagRequests = mutableListOf<Int>()
            val placeRequests = mutableListOf<List<PlaceRemoteEntity>>()
            coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                tagRequests += 1
                if (tagRequests.size == 2) throw failure
            }
            coEvery { context.placeRemoteDataSource.push(any()) } coAnswers {
                placeRequests += firstArg<List<PlaceRemoteEntity>>()
            }

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

            actual.message shouldBe failure.message
            tagRequests.size shouldBe 2
            placeRequests.map { request -> request.size } shouldContainExactly listOf(100, 100, 1)
        }

        test("TC-bb1f2746-1f2d-465a-8da5-35cff756be8c 태그가 실패하면 태그를 기다리는 종류를 시도하지 않는다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    placeList = places(size = 1),
                    memoList = memos(size = 1),
                    memoTagList = memoTags(size = 1),
                    memoPlaceList = memoPlaces(size = 1),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoTagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoPlaceRemoteDataSource.push(any()) }
            coVerify(exactly = 1) { context.placeRemoteDataSource.push(any()) }
        }

        test("TC-de4069c2-e7bb-4e76-b57a-4a67d557b007 메모가 실패하면 두 종류의 연결을 시도하지 않는다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    placeList = places(size = 1),
                    memoList = memos(size = 1),
                    memoTagList = memoTags(size = 1),
                    memoPlaceList = memoPlaces(size = 1),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.memoRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 0) { context.memoTagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoPlaceRemoteDataSource.push(any()) }
            coVerify(exactly = 1) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 1) { context.placeRemoteDataSource.push(any()) }
        }

        test("TC-d6ad6c31-e1e9-435a-ab90-64a6c654d457 장소 내려받기가 지연되어도 나머지 종류의 내려받기를 먼저 시작한다") {
            runTest {
                val context = context()
                val callOrder = mutableListOf<String>()
                coEvery { context.placeRemoteDataSource.pull(usn = 0L) } coAnswers {
                    delay(PULL_DELAY)
                    callOrder += "placePullEnd"
                    emptyList()
                }
                coEvery { context.tagRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "tagPullEnd"
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
                coEvery { context.webRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "webPullEnd"
                    emptyList()
                }
                coEvery { context.tagLinkRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "tagLinkPullEnd"
                    emptyList()
                }

                context.subject.doWork()

                callOrder.last() shouldBe "placePullEnd"
                callOrder.dropLast(1) shouldContainExactlyInAnyOrder
                    listOf("tagPullEnd", "webPullEnd", "memoPullEnd", "memoTagPullEnd", "memoPlacePullEnd", "tagLinkPullEnd")
            }
        }

        test("TC-319f896c-9872-4455-b4d1-a91dc09e1e9b 장소 내려받기가 실패해도 메모·장소 연결은 빈 응답까지 내려받는다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val firstPullList = memoPlacePulls(usnList = listOf(2L))
            val secondPullList = memoPlacePulls(usnList = listOf(5L))
            coEvery { context.placeRemoteDataSource.pull(any()) } throws failure
            coEvery { context.memoPlaceRemoteDataSource.pull(usn = 0L) } returns firstPullList
            coEvery { context.memoPlaceRemoteDataSource.pull(usn = 2L) } returns secondPullList
            coEvery { context.memoPlaceRemoteDataSource.pull(usn = 5L) } returns emptyList()

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 1) {
                context.accountMemoPlaceSyncTransaction.upsert(
                    accountId = context.accountId,
                    memoPlaceList = firstPullList.map { pull -> pull.memoPlace.toLocal() },
                    cursor = 2L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoPlaceSyncTransaction.upsert(
                    accountId = context.accountId,
                    memoPlaceList = secondPullList.map { pull -> pull.memoPlace.toLocal() },
                    cursor = 5L,
                )
            }
            coVerify(exactly = 1) { context.memoPlaceRemoteDataSource.pull(usn = 5L) }
        }

        listOf(
            "장소" to true,
            "메모·장소 연결" to false,
        ).forEach { (label, isPlaceFailed) ->
            test("TC-51b016e3-825d-4ef5-9968-ecbf31b4c8a4 $label 내려받기가 실패하면 동기화가 실패한다") {
                val context = context()
                val failure = TestException(fixtureMonkey.giveMeOne())
                if (isPlaceFailed) {
                    coEvery { context.placeRemoteDataSource.pull(any()) } throws failure
                } else {
                    coEvery { context.memoPlaceRemoteDataSource.pull(any()) } throws failure
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

        test("TC-cce8783a-21bb-4316-bdb7-74d4d2fe8853 장소와 메모·장소 연결은 각각 기록된 순번으로 내려받기를 시작한다") {
            val context = context()
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.PLACE) } returns 9L
            coEvery {
                context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.MEMO_PLACE)
            } returns 14L

            context.subject.doWork()

            coVerify(exactly = 1) { context.placeRemoteDataSource.pull(usn = 9L) }
            coVerify(exactly = 1) { context.memoPlaceRemoteDataSource.pull(usn = 14L) }
        }

        test("TC-5dd13cc9-5011-41cf-bfae-1314fe0084b6 장소와 메모·장소 연결의 내려받기 위치는 서로 덮어쓰지 않는다") {
            val context = context()
            val placePullList = placePulls(usnList = listOf(7L))
            val memoPlacePullList = memoPlacePulls(usnList = listOf(11L))
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.PLACE) } returns 3L
            coEvery {
                context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.MEMO_PLACE)
            } returns 4L
            coEvery { context.placeRemoteDataSource.pull(usn = 3L) } returns placePullList
            coEvery { context.placeRemoteDataSource.pull(usn = 7L) } returns emptyList()
            coEvery { context.memoPlaceRemoteDataSource.pull(usn = 4L) } returns memoPlacePullList
            coEvery { context.memoPlaceRemoteDataSource.pull(usn = 11L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountPlaceSyncTransaction.upsert(
                    accountId = context.accountId,
                    placeList = placePullList.map { pull -> pull.place.toLocal() },
                    cursor = 7L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMemoPlaceSyncTransaction.upsert(
                    accountId = context.accountId,
                    memoPlaceList = memoPlacePullList.map { pull -> pull.memoPlace.toLocal() },
                    cursor = 11L,
                )
            }
            coVerify(exactly = 0) { context.placeRemoteDataSource.pull(usn = 11L) }
            coVerify(exactly = 0) { context.memoPlaceRemoteDataSource.pull(usn = 7L) }
        }

        listOf(
            1 to listOf(1),
            100 to listOf(100),
            101 to listOf(100, 1),
            201 to listOf(100, 100, 1),
        ).forEach { (itemCount, expectedRequestSizes) ->
            test("TC-400fe05d-3f17-47cf-b0bb-ca12fe6625c0 장소 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val placeList = places(size = itemCount)
                val context = context(placeList = placeList)
                val requests = mutableListOf<List<PlaceRemoteEntity>>()
                coEvery { context.placeRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<PlaceRemoteEntity>>()
                }

                context.subject.doWork()

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly placeList.map { place -> place.toRemote() }
            }
        }

        test("TC-2445b736-3b95-4d18-8570-a101b35b6c55 장소는 삭제 상태를 담아 업로드된다") {
            val placeList = places(size = 1).map { place -> place.copy(isDeleted = true) }
            val context = context(placeList = placeList)
            val requests = mutableListOf<List<PlaceRemoteEntity>>()
            coEvery { context.placeRemoteDataSource.push(any()) } coAnswers {
                requests += firstArg<List<PlaceRemoteEntity>>()
            }

            context.subject.doWork()

            requests.flatten().map { request -> request.isDeleted } shouldContainExactly listOf(true)
        }

        test("TC-1298845b-6317-4c0d-847d-ae2dc27a13da 연결은 메모·장소 업로드가 아니라 연결 전용 업로드 요청에 담긴다") {
            val memoList = memos(size = 1)
            val placeList = places(size = 1)
            val memoPlaceList = memoPlaces(size = 1)
            val context =
                context(
                    memoList = memoList,
                    placeList = placeList,
                    memoPlaceList = memoPlaceList,
                )
            val memoPlaceRequests = mutableListOf<List<MemoPlaceRemoteEntity>>()
            val placeRequests = mutableListOf<List<PlaceRemoteEntity>>()
            val memoRequests = mutableListOf<List<MemoRemoteEntity>>()
            coEvery { context.memoPlaceRemoteDataSource.push(any()) } coAnswers {
                memoPlaceRequests += firstArg<List<MemoPlaceRemoteEntity>>()
            }
            coEvery { context.placeRemoteDataSource.push(any()) } coAnswers {
                placeRequests += firstArg<List<PlaceRemoteEntity>>()
            }
            coEvery { context.memoRemoteDataSource.push(any()) } coAnswers {
                memoRequests += firstArg<List<MemoRemoteEntity>>()
            }

            context.subject.doWork()

            memoPlaceRequests.flatten() shouldContainExactly memoPlaceList.map { memoPlace -> memoPlace.toRemote() }
            placeRequests.flatten() shouldContainExactly placeList.map { place -> place.toRemote() }
            memoRequests.flatten() shouldContainExactly memoList.map { memo -> memo.toRemote() }
        }

        test("TC-e4330573-edde-4307-8791-186f1c7533c1 해제된 메모·장소 연결도 해제 상태로 업로드된다") {
            val memoPlaceList = memoPlaces(size = 1).map { memoPlace -> memoPlace.copy(isDeleted = true) }
            val context = context(memoPlaceList = memoPlaceList)
            val requests = mutableListOf<List<MemoPlaceRemoteEntity>>()
            coEvery { context.memoPlaceRemoteDataSource.push(any()) } coAnswers {
                requests += firstArg<List<MemoPlaceRemoteEntity>>()
            }

            context.subject.doWork()

            requests.flatten() shouldContainExactly memoPlaceList.map { memoPlace -> memoPlace.toRemote() }
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 장소 업로드가 성공하면 보낸 항목의 대기 해제를 요청한다") {
            val placeList = places(size = 101)
            val context = context(placeList = placeList)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountPlaceSyncTransaction.clearPending(
                    accountId = context.accountId,
                    placeList = placeList.take(100),
                )
            }
            coVerify(exactly = 1) {
                context.accountPlaceSyncTransaction.clearPending(
                    accountId = context.accountId,
                    placeList = placeList.drop(100),
                )
            }
        }
    })
