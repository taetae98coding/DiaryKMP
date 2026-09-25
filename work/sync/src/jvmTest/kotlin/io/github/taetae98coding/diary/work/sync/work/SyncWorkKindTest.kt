package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.MockKMatcherScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

// 열세 종류를 같은 방식으로 다루도록 종류마다 대기 항목, 서버 요청, 기기 저장을 묶는다.
private class SyncKindCase(
    val label: String,
    val push: suspend MockKMatcherScope.(TestContext) -> Unit,
    val pull: suspend MockKMatcherScope.(TestContext, Long) -> List<Any>,
    val clearPending: suspend MockKMatcherScope.(TestContext) -> Unit,
    val save: suspend MockKMatcherScope.(TestContext) -> Unit,
    val pulls: (List<Long>) -> Pair<List<Any>, List<Any>>,
)

private const val TAG = "태그"
private const val PLACE = "장소"
private const val WEB = "웹 항목"
private const val CONTACT = "연락처"
private const val MUSIC = "곡"
private const val MEMO = "메모"
private const val MEMO_TAG = "메모와 태그의 연결"
private const val MEMO_PLACE = "메모와 장소의 연결"
private const val MEMO_WEB = "메모와 웹 항목의 연결"
private const val MEMO_CONTACT = "메모와 연락처의 연결"
private const val TAG_LINK = "태그와 태그의 연결"
private const val WEB_TAG = "웹 항목과 태그의 연결"
private const val PLACE_TAG = "장소와 태그의 연결"

private val kindCaseList: List<SyncKindCase> =
    listOf(
        SyncKindCase(
            label = TAG,
            push = { context -> context.tagRemoteDataSource.push(any()) },
            pull = { context, usn -> context.tagRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountTagSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountTagSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList -> tagPulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.tag.toLocal() } } },
        ),
        SyncKindCase(
            label = PLACE,
            push = { context -> context.placeRemoteDataSource.push(any()) },
            pull = { context, usn -> context.placeRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountPlaceSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountPlaceSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList -> placePulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.place.toLocal() } } },
        ),
        SyncKindCase(
            label = WEB,
            push = { context -> context.webRemoteDataSource.push(any()) },
            pull = { context, usn -> context.webRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountWebSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountWebSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList -> webPulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.web.toLocal() } } },
        ),
        SyncKindCase(
            label = CONTACT,
            push = { context -> context.contactRemoteDataSource.push(any()) },
            pull = { context, usn -> context.contactRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountContactSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountContactSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList -> contactPulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.contact.toLocal() } } },
        ),
        SyncKindCase(
            label = MUSIC,
            push = { context -> context.musicRemoteDataSource.push(any()) },
            pull = { context, usn -> context.musicRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountMusicSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountMusicSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList -> musicPulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.music.toLocal() } } },
        ),
        SyncKindCase(
            label = MEMO,
            push = { context -> context.memoRemoteDataSource.push(any()) },
            pull = { context, usn -> context.memoRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountMemoSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountMemoSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList -> memoPulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.memo.toLocal() } } },
        ),
        SyncKindCase(
            label = MEMO_TAG,
            push = { context -> context.memoTagRemoteDataSource.push(any()) },
            pull = { context, usn -> context.memoTagRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountMemoTagSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountMemoTagSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList -> memoTagPulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.memoTag.toLocal() } } },
        ),
        SyncKindCase(
            label = MEMO_PLACE,
            push = { context -> context.memoPlaceRemoteDataSource.push(any()) },
            pull = { context, usn -> context.memoPlaceRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountMemoPlaceSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountMemoPlaceSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList -> memoPlacePulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.memoPlace.toLocal() } } },
        ),
        SyncKindCase(
            label = MEMO_WEB,
            push = { context -> context.memoWebRemoteDataSource.push(any()) },
            pull = { context, usn -> context.memoWebRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountMemoWebSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountMemoWebSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList -> memoWebPulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.memoWeb.toLocal() } } },
        ),
        SyncKindCase(
            label = MEMO_CONTACT,
            push = { context -> context.memoContactRemoteDataSource.push(any()) },
            pull = { context, usn -> context.memoContactRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountMemoContactSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountMemoContactSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList ->
                memoContactPulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.memoContact.toLocal() } }
            },
        ),
        SyncKindCase(
            label = TAG_LINK,
            push = { context -> context.tagLinkRemoteDataSource.push(any()) },
            pull = { context, usn -> context.tagLinkRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountTagLinkSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountTagLinkSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList -> tagLinkPulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.tagLink.toLocal() } } },
        ),
        SyncKindCase(
            label = WEB_TAG,
            push = { context -> context.webTagRemoteDataSource.push(any()) },
            pull = { context, usn -> context.webTagRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountWebTagSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountWebTagSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList -> webTagPulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.webTag.toLocal() } } },
        ),
        SyncKindCase(
            label = PLACE_TAG,
            push = { context -> context.placeTagRemoteDataSource.push(any()) },
            pull = { context, usn -> context.placeTagRemoteDataSource.pull(usn = usn) },
            clearPending = { context -> context.accountPlaceTagSyncTransaction.clearPending(any(), any()) },
            save = { context -> context.accountPlaceTagSyncTransaction.save(any(), any(), any()) },
            pulls = { usnList -> placeTagPulls(usnList = usnList).let { pullList -> pullList to pullList.map { pull -> pull.placeTag.toLocal() } } },
        ),
    )

private val kindCaseMap: Map<String, SyncKindCase> = kindCaseList.associateBy { kindCase -> kindCase.label }

private fun kindCase(label: String): SyncKindCase = kindCaseMap.getValue(label)

private fun kindContext(pendingCountMap: Map<String, Int>): TestContext {
    fun count(label: String): Int = pendingCountMap[label] ?: 0

    return context(
        tagList = tags(size = count(TAG)),
        placeList = places(size = count(PLACE)),
        webList = webs(size = count(WEB)),
        contactList = contacts(size = count(CONTACT)),
        musicList = musics(size = count(MUSIC)),
        memoList = memos(size = count(MEMO)),
        memoTagList = memoTags(size = count(MEMO_TAG)),
        memoPlaceList = memoPlaces(size = count(MEMO_PLACE)),
        memoWebList = memoWebs(size = count(MEMO_WEB)),
        memoContactList = memoContacts(size = count(MEMO_CONTACT)),
        tagLinkList = tagLinks(size = count(TAG_LINK)),
        webTagList = webTags(size = count(WEB_TAG)),
        placeTagList = placeTags(size = count(PLACE_TAG)),
    )
}

// 종류마다 보낸 묶음의 크기를 기록하고, 지정한 번째 묶음에서 실패시킨다.
private fun TestContext.recordPush(
    kindCase: SyncKindCase,
    failingRequestNumber: Int? = null,
    failure: Throwable? = null,
): MutableList<Int> {
    val requestSizeList = mutableListOf<Int>()
    coEvery { kindCase.push(this, this@recordPush) } coAnswers {
        requestSizeList += firstArg<List<Any>>().size
        if (requestSizeList.size == failingRequestNumber && failure != null) throw failure
    }
    return requestSizeList
}

private fun TestContext.verifyPushCount(
    label: String,
    exactly: Int,
) {
    val kindCase = kindCase(label = label)
    coVerify(exactly = exactly) { kindCase.push(this, this@verifyPushCount) }
}

private fun TestContext.verifyNoPull() {
    kindCaseList.forEach { kindCase ->
        coVerify(exactly = 0) { kindCase.pull(this, this@verifyNoPull, any()) }
    }
}

class SyncWorkKindTest :
    FunSpec({
        kindCaseList.forEach { kindCase ->
            test("TC-DATA-SYNC-DOMAIN-020 ${kindCase.label}만 대기하면 ${kindCase.label}만 요청하고 나머지 열두 종류는 요청하지 않는다") {
                val context = kindContext(pendingCountMap = mapOf(kindCase.label to 1))

                context.subject.doWork()

                kindCaseList.forEach { other ->
                    context.verifyPushCount(label = other.label, exactly = if (other == kindCase) 1 else 0)
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-020 대기 항목이 없으면 열세 종류 모두 요청하지 않는다") {
            val context = kindContext(pendingCountMap = emptyMap())

            context.subject.doWork()

            kindCaseList.forEach { kindCase -> context.verifyPushCount(label = kindCase.label, exactly = 0) }
        }

        test("TC-DATA-SYNC-DOMAIN-020 열세 종류 모두 대기하면 열세 종류 모두 요청한다") {
            val context = kindContext(pendingCountMap = kindCaseList.associate { kindCase -> kindCase.label to 1 })

            context.subject.doWork()

            kindCaseList.forEach { kindCase -> context.verifyPushCount(label = kindCase.label, exactly = 1) }
        }

        val independentLabelList = listOf(TAG, PLACE, WEB, CONTACT, MUSIC)
        independentLabelList.forEach { delayedLabel ->
            test("TC-DATA-SYNC-DOMAIN-055 $delayedLabel 업로드가 지연되어도 나머지 네 종류의 첫 요청이 먼저 시작된다") {
                runTest {
                    val context = kindContext(pendingCountMap = independentLabelList.associateWith { 1 })
                    val eventList = mutableListOf<String>()
                    independentLabelList.forEach { label ->
                        coEvery { kindCase(label = label).push(this, context) } coAnswers {
                            eventList += "start:$label"
                            if (label == delayedLabel) delay(PUSH_DELAY)
                            eventList += "end:$label"
                        }
                    }

                    context.subject.doWork()

                    val delayedEndIndex = eventList.indexOf("end:$delayedLabel")
                    independentLabelList
                        .filterNot { label -> label == delayedLabel }
                        .forEach { label -> (eventList.indexOf("start:$label") < delayedEndIndex) shouldBe true }
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-021 태그 묶음이 실패하면 이후 태그와 메모, 일곱 종류의 연결을 모두 시도하지 않는다") {
            val linkLabelList = listOf(MEMO_TAG, MEMO_PLACE, MEMO_WEB, MEMO_CONTACT, TAG_LINK, WEB_TAG, PLACE_TAG)
            val context =
                kindContext(
                    pendingCountMap = mapOf(TAG to 201, PLACE to 1, WEB to 1, CONTACT to 1, MEMO to 1) + linkLabelList.associateWith { 1 },
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            val tagRequestSizeList = context.recordPush(kindCase = kindCase(label = TAG), failingRequestNumber = 2, failure = failure)

            val actual = shouldThrowExactly<TestException> { context.subject.doWork() }

            actual.message shouldBe failure.message
            tagRequestSizeList shouldContainExactly listOf(100, 100)
            context.verifyPushCount(label = MEMO, exactly = 0)
            linkLabelList.forEach { label -> context.verifyPushCount(label = label, exactly = 0) }
        }

        test("TC-DATA-SYNC-DOMAIN-022 메모 묶음이 실패하면 이후 메모와 메모를 기다리는 네 종류의 연결을 시도하지 않는다") {
            val memoLinkLabelList = listOf(MEMO_TAG, MEMO_PLACE, MEMO_WEB, MEMO_CONTACT)
            val context =
                kindContext(
                    pendingCountMap = mapOf(TAG to 1, PLACE to 1, WEB to 1, CONTACT to 1, MEMO to 201) + memoLinkLabelList.associateWith { 1 },
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            val memoRequestSizeList = context.recordPush(kindCase = kindCase(label = MEMO), failingRequestNumber = 2, failure = failure)

            val actual = shouldThrowExactly<TestException> { context.subject.doWork() }

            actual.message shouldBe failure.message
            memoRequestSizeList shouldContainExactly listOf(100, 100)
            memoLinkLabelList.forEach { label -> context.verifyPushCount(label = label, exactly = 0) }
        }

        test("TC-DATA-SYNC-DOMAIN-053 장소 묶음이 실패하면 장소를 기다리는 연결만 시도하지 않고 나머지는 업로드한다") {
            val context =
                kindContext(
                    pendingCountMap =
                        mapOf(TAG to 1, PLACE to 201, MEMO to 1, MEMO_TAG to 1, TAG_LINK to 1, MEMO_PLACE to 1, PLACE_TAG to 1),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            val placeRequestSizeList = context.recordPush(kindCase = kindCase(label = PLACE), failingRequestNumber = 2, failure = failure)

            val actual = shouldThrowExactly<TestException> { context.subject.doWork() }

            actual.message shouldBe failure.message
            placeRequestSizeList shouldContainExactly listOf(100, 100)
            context.verifyPushCount(label = MEMO_PLACE, exactly = 0)
            context.verifyPushCount(label = PLACE_TAG, exactly = 0)
            listOf(TAG, MEMO, MEMO_TAG, TAG_LINK).forEach { label -> context.verifyPushCount(label = label, exactly = 1) }
        }

        listOf(
            WEB to listOf(MEMO_WEB, WEB_TAG),
            CONTACT to listOf(MEMO_CONTACT),
        ).forEach { (failingLabel, waitingLabelList) ->
            test("TC-DATA-SYNC-DOMAIN-079 $failingLabel 묶음이 실패하면 $failingLabel 을 기다리는 연결을 시도하지 않는다") {
                val context =
                    kindContext(
                        pendingCountMap = mapOf(TAG to 1, MEMO to 1, failingLabel to 201) + waitingLabelList.associateWith { 1 },
                    )
                val failure = TestException(fixtureMonkey.giveMeOne())
                val requestSizeList =
                    context.recordPush(kindCase = kindCase(label = failingLabel), failingRequestNumber = 2, failure = failure)

                val actual = shouldThrowExactly<TestException> { context.subject.doWork() }

                actual.message shouldBe failure.message
                requestSizeList shouldContainExactly listOf(100, 100)
                waitingLabelList.forEach { label -> context.verifyPushCount(label = label, exactly = 0) }
                context.verifyPushCount(label = TAG, exactly = 1)
                context.verifyPushCount(label = MEMO, exactly = 1)
            }
        }

        test("TC-DATA-SYNC-DOMAIN-080 한 묶음이 실패해도 앞서 성공한 묶음의 대기 해제만 남고 되돌리지 않는다") {
            val tagList = tags(size = 201)
            val context = context(tagList = tagList)
            val failure = TestException(fixtureMonkey.giveMeOne())
            context.recordPush(kindCase = kindCase(label = TAG), failingRequestNumber = 2, failure = failure)

            shouldThrowExactly<TestException> { context.subject.doWork() }

            coVerify(exactly = 1) {
                context.accountTagSyncTransaction.clearPending(accountId = context.accountId, tagList = tagList.take(100))
            }
            confirmVerified(context.accountTagSyncTransaction)
        }

        test("TC-DATA-SYNC-DOMAIN-084 메모 업로드가 성공하고 메모와 태그의 연결 업로드가 실패하면 메모의 대기 해제만 유지된다") {
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val primaryTagMemo = memo().copy(primaryTagId = tagId)
            val memoTag = memoTag().copy(memoId = primaryTagMemo.id, tagId = tagId)
            val context = context(memoList = listOf(primaryTagMemo), memoTagList = listOf(memoTag))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.memoTagRemoteDataSource.push(any()) } throws failure

            val actual = shouldThrowExactly<TestException> { context.subject.doWork() }

            actual.message shouldBe failure.message
            coVerify(exactly = 1) {
                context.accountMemoSyncTransaction.clearPending(accountId = context.accountId, memoList = listOf(primaryTagMemo))
            }
            coVerify(exactly = 0) { context.accountMemoTagSyncTransaction.clearPending(any(), any()) }
            context.verifyNoPull()
        }

        test("TC-DATA-SYNC-DOMAIN-082 계정 상태를 확인하지 못해 끝난 동기화도 원인을 담은 오류 보고가 한 번 남는다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val context =
                kindContext(pendingCountMap = mapOf(TAG to 1, MEMO to 1))
                    .copy(getAccountUseCase = accountFailureUseCase(failure = failure))
            val reportList = recordCrashlyticsLog()

            val actual = shouldThrowExactly<TestException> { context.subject.doWork() }

            actual.message shouldBe failure.message
            val report = reportList.single()
            report.throwable.shouldBeInstanceOf<TestException>()
            report.throwable.message shouldBe failure.message
            kindCaseList.forEach { kindCase -> context.verifyPushCount(label = kindCase.label, exactly = 0) }
            context.verifyNoPull()
        }

        kindCaseList.forEach { kindCase ->
            test("TC-DATA-SYNC-DATA-002 ${kindCase.label} 업로드가 실패하면 대기 해제와 내려받기 없이 동기화가 실패한다") {
                val context = kindContext(pendingCountMap = mapOf(kindCase.label to 1))
                val failure = TestException(fixtureMonkey.giveMeOne())
                coEvery { kindCase.push(this, context) } throws failure

                val actual = shouldThrowExactly<TestException> { context.subject.doWork() }

                actual.message shouldBe failure.message
                coVerify(exactly = 0) { kindCase.clearPending(this, context) }
                coVerify(exactly = 0) { kindCase.save(this, context) }
                context.verifyNoPull()
            }

            test("TC-DATA-SYNC-DATA-017 ${kindCase.label}의 기록된 순번이 없으면 가장 작은 내려받기 위치로 첫 묶음을 요청한다") {
                val context = kindContext(pendingCountMap = emptyMap())

                context.subject.doWork()

                coVerify(exactly = 1) { kindCase.pull(this, context, 0L) }
            }

            test("TC-DATA-SYNC-DATA-019 내려받은 ${kindCase.label}의 전체 상태와 서버 변경 순번을 기기 저장에 담는다") {
                val context = kindContext(pendingCountMap = emptyMap())
                val (pullList, localList) = kindCase.pulls(listOf(4L, 9L))
                val savedList = mutableListOf<Pair<List<Any>, Long>>()
                coEvery { kindCase.pull(this, context, 0L) } returns pullList
                coEvery { kindCase.pull(this, context, 9L) } returns emptyList()
                coEvery { kindCase.save(this, context) } coAnswers {
                    savedList += secondArg<List<Any>>() to thirdArg<Long>()
                }

                context.subject.doWork()

                savedList shouldContainExactly listOf(localList to 9L)
            }
        }
    })

private fun accountFailureUseCase(failure: Throwable): GetAccountUseCase {
    val useCase = mockk<GetAccountUseCase>()
    every { useCase(parameter = Unit) } returns flowOf(Result.failure(failure))
    return useCase
}
