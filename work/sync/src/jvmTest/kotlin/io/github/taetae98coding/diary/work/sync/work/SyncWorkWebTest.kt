package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebRemoteEntity
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

class SyncWorkWebTest :
    FunSpec({
        listOf(
            "웹 항목" to "태그",
            "태그" to "웹 항목",
        ).forEach { (delayed, other) ->
            test("TC-d02e1fe4-3710-497f-b721-5bd09e834ad1 $delayed 업로드가 지연되어도 $other 업로드를 먼저 시작한다") {
                runTest {
                    val context = context(tagList = tags(size = 1), webList = webs(size = 1))
                    val callOrder = mutableListOf<String>()
                    coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                        if (delayed == "태그") delay(PUSH_DELAY)
                        callOrder += "tagPushEnd"
                    }
                    coEvery { context.webRemoteDataSource.push(any()) } coAnswers {
                        if (delayed == "웹 항목") delay(PUSH_DELAY)
                        callOrder += "webPushEnd"
                    }

                    context.subject.doWork()

                    val delayedEnd = if (delayed == "태그") "tagPushEnd" else "webPushEnd"
                    callOrder.last() shouldBe delayedEnd
                }
            }
        }

        test("웹 항목만 대기하면 웹 항목 요청만 발생한다") {
            val context = context(webList = webs(size = 1))

            context.subject.doWork()

            coVerify(exactly = 1) { context.webRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.placeRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoTagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoPlaceRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagLinkRemoteDataSource.push(any()) }
        }

        test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 웹 항목이 실패해도 태그와 장소는 끝까지 업로드한다") {
            val context =
                context(
                    tagList = tags(size = 201),
                    placeList = places(size = 201),
                    webList = webs(size = 201),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            val webRequests = mutableListOf<List<WebRemoteEntity>>()
            val tagRequests = mutableListOf<Int>()
            val placeRequests = mutableListOf<Int>()
            coEvery { context.webRemoteDataSource.push(any()) } coAnswers {
                webRequests += firstArg<List<WebRemoteEntity>>()
                if (webRequests.size == 2) throw failure
            }
            coEvery { context.tagRemoteDataSource.push(any()) } coAnswers { tagRequests += 1 }
            coEvery { context.placeRemoteDataSource.push(any()) } coAnswers { placeRequests += 1 }

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

            actual.message shouldBe failure.message
            webRequests.map { request -> request.size } shouldContainExactly listOf(100, 100)
            tagRequests.size shouldBe 3
            placeRequests.size shouldBe 3
        }

        test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 태그가 실패해도 웹 항목은 끝까지 업로드한다") {
            val context = context(tagList = tags(size = 201), webList = webs(size = 201))
            val failure = TestException(fixtureMonkey.giveMeOne())
            val tagRequests = mutableListOf<Int>()
            val webRequests = mutableListOf<List<WebRemoteEntity>>()
            coEvery { context.tagRemoteDataSource.push(any()) } coAnswers {
                tagRequests += 1
                if (tagRequests.size == 2) throw failure
            }
            coEvery { context.webRemoteDataSource.push(any()) } coAnswers {
                webRequests += firstArg<List<WebRemoteEntity>>()
            }

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

            actual.message shouldBe failure.message
            webRequests.map { request -> request.size } shouldContainExactly listOf(100, 100, 1)
        }

        listOf(
            "태그" to "메모",
            "메모" to "메모와 태그의 연결",
        ).forEach { (failed, _) ->
            test("TC-674eebce-610c-4b46-8dce-79aef5252327 웹 항목은 $failed 업로드가 실패해도 시작한다") {
                val context =
                    context(
                        tagList = tags(size = 1),
                        webList = webs(size = 1),
                        memoList = memos(size = 1),
                    )
                val failure = TestException(fixtureMonkey.giveMeOne())
                if (failed == "태그") {
                    coEvery { context.tagRemoteDataSource.push(any()) } throws failure
                } else {
                    coEvery { context.memoRemoteDataSource.push(any()) } throws failure
                }

                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

                coVerify(exactly = 1) { context.webRemoteDataSource.push(any()) }
            }
        }

        test("TC-d6ad6c31-e1e9-435a-ab90-64a6c654d457 웹 항목 내려받기가 지연되어도 나머지 종류의 내려받기를 먼저 시작한다") {
            runTest {
                val context = context()
                val callOrder = mutableListOf<String>()
                coEvery { context.webRemoteDataSource.pull(usn = 0L) } coAnswers {
                    delay(PULL_DELAY)
                    callOrder += "webPullEnd"
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
                coEvery { context.tagLinkRemoteDataSource.pull(usn = 0L) } coAnswers {
                    callOrder += "tagLinkPullEnd"
                    emptyList()
                }

                context.subject.doWork()

                callOrder.last() shouldBe "webPullEnd"
                callOrder.dropLast(1) shouldContainExactlyInAnyOrder
                    listOf("tagPullEnd", "placePullEnd", "memoPullEnd", "memoTagPullEnd", "memoPlacePullEnd", "tagLinkPullEnd")
            }
        }

        test("TC-319f896c-9872-4455-b4d1-a91dc09e1e9b 웹 항목 내려받기가 실패해도 태그는 빈 응답까지 내려받는다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val firstPullList = tagPulls(usnList = listOf(2L))
            val secondPullList = tagPulls(usnList = listOf(5L))
            coEvery { context.webRemoteDataSource.pull(any()) } throws failure
            coEvery { context.tagRemoteDataSource.pull(usn = 0L) } returns firstPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 2L) } returns secondPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 5L) } returns emptyList()

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 5L) }
        }

        test("TC-51b016e3-825d-4ef5-9968-ecbf31b4c8a4 웹 항목 내려받기가 실패하면 동기화가 실패한다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.webRemoteDataSource.pull(any()) } throws failure

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

            actual.message shouldBe failure.message
            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 0L) }
            coVerify(exactly = 1) { context.placeRemoteDataSource.pull(usn = 0L) }
        }

        listOf(
            1 to listOf(1),
            100 to listOf(100),
            101 to listOf(100, 1),
            201 to listOf(100, 100, 1),
        ).forEach { (itemCount, expectedRequestSizes) ->
            test("TC-400fe05d-3f17-47cf-b0bb-ca12fe6625c0 웹 항목 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val webList = webs(size = itemCount)
                val context = context(webList = webList)
                val requests = mutableListOf<List<WebRemoteEntity>>()
                coEvery { context.webRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<WebRemoteEntity>>()
                }

                context.subject.doWork()

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly webList.map { web -> web.toRemote() }
            }
        }

        test("TC-a228458a-3525-434f-9d8c-743f78654ba9 웹 항목 업로드에는 요청 헤더 목록 전체가 순서대로 담긴다") {
            val headerList =
                listOf(
                    WebHeaderLocalEntity(name = "Authorization", value = "token-${fixtureMonkey.giveMeOne<String>()}"),
                    WebHeaderLocalEntity(name = "Authorization", value = "token-${fixtureMonkey.giveMeOne<String>()}"),
                    WebHeaderLocalEntity(name = "X-Region", value = ""),
                )
            val web = web().let { web -> web.copy(detail = web.detail.copy(headerList = headerList)) }
            val context = context(webList = listOf(web))
            val requests = mutableListOf<List<WebRemoteEntity>>()
            coEvery { context.webRemoteDataSource.push(any()) } coAnswers {
                requests += firstArg<List<WebRemoteEntity>>()
            }

            context.subject.doWork()

            requests
                .flatten()
                .single()
                .detail.headerList shouldContainExactly
                headerList.map { header -> header.toRemote() }
        }

        test("TC-cce8783a-21bb-4316-bdb7-74d4d2fe8853 웹 항목은 기록된 순번으로 내려받기를 시작한다") {
            val context = context()
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.WEB) } returns 9L

            context.subject.doWork()

            coVerify(exactly = 1) { context.webRemoteDataSource.pull(usn = 9L) }
        }

        test("TC-5dd13cc9-5011-41cf-bfae-1314fe0084b6 웹 항목과 태그의 내려받기 위치는 서로 덮어쓰지 않는다") {
            val context = context()
            val webPullList = webPulls(usnList = listOf(7L))
            val tagPullList = tagPulls(usnList = listOf(11L))
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.WEB) } returns 3L
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.TAG) } returns 4L
            coEvery { context.webRemoteDataSource.pull(usn = 3L) } returns webPullList
            coEvery { context.webRemoteDataSource.pull(usn = 7L) } returns emptyList()
            coEvery { context.tagRemoteDataSource.pull(usn = 4L) } returns tagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 11L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountWebSyncTransaction.upsert(
                    accountId = context.accountId,
                    webList = webPullList.map { pull -> pull.web.toLocal() },
                    cursor = 7L,
                )
            }
            coVerify(exactly = 0) { context.webRemoteDataSource.pull(usn = 11L) }
        }

        test("TC-21ff0303-9f3c-4cb3-9f35-4251ec918ee8 웹 항목의 빈 응답을 받으면 내려받기를 끝낸다") {
            val context = context()
            val firstPullList = webPulls(usnList = listOf(2L))
            coEvery { context.webRemoteDataSource.pull(usn = 0L) } returns firstPullList
            coEvery { context.webRemoteDataSource.pull(usn = 2L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) { context.webRemoteDataSource.pull(usn = 0L) }
            coVerify(exactly = 1) { context.webRemoteDataSource.pull(usn = 2L) }
            coVerify(exactly = 1) {
                context.accountWebSyncTransaction.upsert(
                    accountId = context.accountId,
                    webList = firstPullList.map { pull -> pull.web.toLocal() },
                    cursor = 2L,
                )
            }
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 업로드 묶음이 성공하면 그 묶음의 웹 항목만 대기 해제를 요청한다") {
            val webList = webs(size = 101)
            val context = context(webList = webList)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountWebSyncTransaction.clearPending(context.accountId, webList.take(100))
            }
            coVerify(exactly = 1) {
                context.accountWebSyncTransaction.clearPending(context.accountId, webList.drop(100))
            }
        }
    })
