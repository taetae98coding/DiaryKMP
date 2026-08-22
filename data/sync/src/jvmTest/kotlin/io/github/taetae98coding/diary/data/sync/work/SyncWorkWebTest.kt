package io.github.taetae98coding.diary.data.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.mapper.web.toLocal
import io.github.taetae98coding.diary.core.mapper.web.toRemote
import io.github.taetae98coding.diary.core.network.api.web.entity.WebRemoteEntity
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
            test("TC-DATA-SYNC-DOMAIN-055 $delayed 업로드가 지연되어도 $other 업로드를 먼저 시작한다") {
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

                    context.subject.doWork(accountId = context.accountId)

                    val delayedEnd = if (delayed == "태그") "tagPushEnd" else "webPushEnd"
                    callOrder.last() shouldBe delayedEnd
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-020 웹 항목만 대기하면 웹 항목 요청만 발생한다") {
            val context = context(webList = webs(size = 1))

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) { context.webRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.placeRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoTagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoPlaceRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagLinkRemoteDataSource.push(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-054 웹 항목이 실패해도 태그와 장소는 끝까지 업로드한다") {
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
                    context.subject.doWork(accountId = context.accountId)
                }

            actual.message shouldBe failure.message
            webRequests.map { request -> request.size } shouldContainExactly listOf(100, 100)
            tagRequests.size shouldBe 3
            placeRequests.size shouldBe 3
        }

        test("TC-DATA-SYNC-DOMAIN-054 태그가 실패해도 웹 항목은 끝까지 업로드한다") {
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
                    context.subject.doWork(accountId = context.accountId)
                }

            actual.message shouldBe failure.message
            webRequests.map { request -> request.size } shouldContainExactly listOf(100, 100, 1)
        }

        listOf(
            "태그" to "메모",
            "메모" to "메모와 태그의 연결",
        ).forEach { (failed, _) ->
            test("TC-DATA-SYNC-DOMAIN-019 웹 항목은 $failed 업로드가 실패해도 시작한다") {
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
                    context.subject.doWork(accountId = context.accountId)
                }

                coVerify(exactly = 1) { context.webRemoteDataSource.push(any()) }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-038 웹 항목 내려받기가 지연되어도 나머지 종류의 내려받기를 먼저 시작한다") {
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

                context.subject.doWork(accountId = context.accountId)

                callOrder.last() shouldBe "webPullEnd"
                callOrder.dropLast(1) shouldContainExactlyInAnyOrder
                    listOf("tagPullEnd", "placePullEnd", "memoPullEnd", "memoTagPullEnd", "memoPlacePullEnd", "tagLinkPullEnd")
            }
        }

        test("TC-DATA-SYNC-DOMAIN-039 웹 항목 내려받기가 실패해도 태그는 빈 응답까지 내려받는다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val firstPullList = tagPulls(usnList = listOf(2L))
            val secondPullList = tagPulls(usnList = listOf(5L))
            coEvery { context.webRemoteDataSource.pull(any()) } throws failure
            coEvery { context.tagRemoteDataSource.pull(usn = 0L) } returns firstPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 2L) } returns secondPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 5L) } returns emptyList()

            shouldThrowExactly<TestException> {
                context.subject.doWork(accountId = context.accountId)
            }

            coVerify(exactly = 1) { context.tagRemoteDataSource.pull(usn = 5L) }
        }

        test("TC-DATA-SYNC-DOMAIN-040 웹 항목 내려받기가 실패하면 동기화가 실패한다") {
            val context = context()
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.webRemoteDataSource.pull(any()) } throws failure

            val actual =
                shouldThrowExactly<TestException> {
                    context.subject.doWork(accountId = context.accountId)
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
            test("TC-DATA-SYNC-DATA-005 웹 항목 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val webList = webs(size = itemCount)
                val context = context(webList = webList)
                val requests = mutableListOf<List<WebRemoteEntity>>()
                coEvery { context.webRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<WebRemoteEntity>>()
                }

                context.subject.doWork(accountId = context.accountId)

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly webList.map { web -> web.toRemote() }
            }
        }

        test("TC-DATA-SYNC-DATA-030 웹 항목 업로드에는 요청 헤더 목록 전체가 순서대로 담긴다") {
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

            context.subject.doWork(accountId = context.accountId)

            requests
                .flatten()
                .single()
                .detail.headerList shouldContainExactly
                headerList.map { header -> header.toRemote() }
        }

        test("TC-DATA-SYNC-DATA-017 웹 항목은 기록된 순번으로 내려받기를 시작한다") {
            val context = context()
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.WEB) } returns 9L

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) { context.webRemoteDataSource.pull(usn = 9L) }
        }

        test("TC-DATA-SYNC-DATA-028 웹 항목과 태그의 내려받기 위치는 서로 덮어쓰지 않는다") {
            val context = context()
            val webPullList = webPulls(usnList = listOf(7L))
            val tagPullList = tagPulls(usnList = listOf(11L))
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.WEB) } returns 3L
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.TAG) } returns 4L
            coEvery { context.webRemoteDataSource.pull(usn = 3L) } returns webPullList
            coEvery { context.webRemoteDataSource.pull(usn = 7L) } returns emptyList()
            coEvery { context.tagRemoteDataSource.pull(usn = 4L) } returns tagPullList
            coEvery { context.tagRemoteDataSource.pull(usn = 11L) } returns emptyList()

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) {
                context.accountWebSyncTransaction.save(
                    accountId = context.accountId,
                    webList = webPullList.map { pull -> pull.web.toLocal() },
                    cursor = 7L,
                )
            }
            coVerify(exactly = 0) { context.webRemoteDataSource.pull(usn = 11L) }
        }

        test("TC-DATA-SYNC-DATA-020 웹 항목의 빈 응답을 받으면 내려받기를 끝낸다") {
            val context = context()
            val firstPullList = webPulls(usnList = listOf(2L))
            coEvery { context.webRemoteDataSource.pull(usn = 0L) } returns firstPullList
            coEvery { context.webRemoteDataSource.pull(usn = 2L) } returns emptyList()

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) { context.webRemoteDataSource.pull(usn = 0L) }
            coVerify(exactly = 1) { context.webRemoteDataSource.pull(usn = 2L) }
            coVerify(exactly = 1) {
                context.accountWebSyncTransaction.save(
                    accountId = context.accountId,
                    webList = firstPullList.map { pull -> pull.web.toLocal() },
                    cursor = 2L,
                )
            }
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드 묶음이 성공하면 그 묶음의 웹 항목만 대기 해제를 요청한다") {
            val webList = webs(size = 101)
            val context = context(webList = webList)

            context.subject.doWork(accountId = context.accountId)

            coVerify(exactly = 1) {
                context.accountWebSyncTransaction.clearPending(context.accountId, webList.take(100))
            }
            coVerify(exactly = 1) {
                context.accountWebSyncTransaction.clearPending(context.accountId, webList.drop(100))
            }
        }
    })
