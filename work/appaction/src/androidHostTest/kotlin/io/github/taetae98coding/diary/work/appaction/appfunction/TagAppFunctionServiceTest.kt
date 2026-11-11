package io.github.taetae98coding.diary.work.appaction.appfunction

import android.content.Context
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.testing.AppFunctionTestRule
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.appfunction.checklist.AppFunctionChecklistSummaryPage
import io.github.taetae98coding.diary.work.appaction.appfunction.contact.AppFunctionContactPage
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueAs
import io.github.taetae98coding.diary.work.appaction.appfunction.dday.AppFunctionDDayPage
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionWeb
import io.github.taetae98coding.diary.work.appaction.appfunction.music.AppFunctionMusicPage
import io.github.taetae98coding.diary.work.appaction.appfunction.qr.AppFunctionQrPage
import io.github.taetae98coding.diary.work.appaction.appfunction.tag.AppFunctionTagDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.tag.AppFunctionTagPage
import io.github.taetae98coding.diary.work.appaction.appfunction.tag.AppFunctionTagWebPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionSortValue
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTagDetail
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTagScopeValue
import io.github.taetae98coding.diary.work.appaction.tag.TagAppAction
import io.github.taetae98coding.diary.work.appaction.tag.TagItemAppAction
import io.github.taetae98coding.diary.work.appaction.tag.TagLinkAppAction
import io.github.taetae98coding.diary.work.appaction.tag.appActionTag
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val tagAppAction = mockk<TagAppAction>()
    private val tagLinkAppAction = mockk<TagLinkAppAction>()
    private val tagItemAppAction = mockk<TagItemAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single { tagAppAction }
                    single { tagLinkAppAction }
                    single { tagItemAppAction }
                },
            )
        }
        tester = AppFunctionServiceTester(context = context, appFunctionManager = appFunctionTestRule.getAppFunctionManager())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `태그 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList().filter { name -> name.startsWith("tag") } shouldContainExactlyInAnyOrder
                listOf(
                    "tagList",
                    "tagFinishedList",
                    TAG_GET,
                    "tagAdd",
                    "tagUpdate",
                    "tagFinish",
                    "tagRestart",
                    "tagDelete",
                    "tagRestore",
                    "tagLinkAdd",
                    "tagLinkRemove",
                    "tagMemoList",
                    "tagMemoFinishedList",
                    "tagWebList",
                    "tagPlaceList",
                    "tagContactList",
                    "tagChecklistList",
                    "tagMusicList",
                    "tagQrList",
                    "tagDdayList",
                )
        }

    @Test
    fun `태그 목록은 공통 동작의 페이지를 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val tag = Arb.appActionTag().next()
            val nextCursor = Arb.int(1..1000).next().toString()
            coEvery { tagAppAction.list(sort = any(), cursor = any()) } returns AppActionPage(items = listOf(tag), nextCursor = nextCursor)

            val response = tester.execute(name = "tagList") { setString(SORT, AppActionSortValue.RECENTLY_UPDATED) }

            response.returnValueAs<AppFunctionTagPage>() shouldBe AppFunctionTagPage(items = listOf(tag.expected()), nextCursor = nextCursor)
            coVerify(exactly = 1) { tagAppAction.list(sort = AppActionSortValue.RECENTLY_UPDATED, cursor = null) }
        }

    @Test
    fun `태그 조회는 하위 태그와 함께 돌려준다`() =
        runBlocking<Unit> {
            val tag = Arb.appActionTag().next()
            val linkedTag = Arb.appActionTag().next()
            coEvery { tagAppAction.get(tagId = tag.id) } returns AppActionTagDetail(tag = tag, linkedTags = listOf(linkedTag))

            val response = tester.execute(name = TAG_GET) { setString(TAG_ID, tag.id) }

            response.returnValueAs<AppFunctionTagDetail>() shouldBe AppFunctionTagDetail(tag = tag.expected(), linkedTags = listOf(linkedTag.expected()))
        }

    @Test
    fun `없는 태그를 조회하면 값이 없는 결과를 돌려준다`() =
        runBlocking<Unit> {
            val tagId = Arb.kotlinUuid().next().toString()
            coEvery { tagAppAction.get(tagId = tagId) } returns null

            val response = tester.execute(name = TAG_GET) { setString(TAG_ID, tagId) }

            response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Success>().returnValue.containsKey(ExecuteAppFunctionResponse.Success.PROPERTY_RETURN_VALUE) shouldBe false
        }

    @Test
    fun `태그 추가는 받은 값을 공통 동작에 넘기고 id를 돌려준다`() =
        runBlocking<Unit> {
            val title = Arb.nonBlankString().next()
            val linkedTagId = Arb.kotlinUuid().next().toString()
            val addedId = Arb.kotlinUuid().next().toString()
            coEvery { tagAppAction.add(any(), any(), any(), any(), any()) } returns AppActionId(id = addedId)

            val response =
                tester.execute(name = "tagAdd") {
                    setString("title", title)
                    setStringList("linkedTagIds", listOf(linkedTagId))
                }

            response.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId)
            coVerify(exactly = 1) { tagAppAction.add(title = title, emoji = null, description = null, color = null, linkedTagIds = listOf(linkedTagId)) }
        }

    @Test
    fun `태그 수정은 담은 값만 넘기고 바뀐 수를 돌려준다`() =
        runBlocking<Unit> {
            val tagId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..1).next()
            coEvery { tagAppAction.update(any(), any(), any(), any(), any()) } returns AppActionChangedCount(changedCount = changedCount)

            val response =
                tester.execute(name = "tagUpdate") {
                    setString(TAG_ID, tagId)
                    setString("emoji", "")
                }

            response.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
            coVerify(exactly = 1) { tagAppAction.update(tagId = tagId, emoji = "", title = null, description = null, color = null) }
        }

    @Test
    fun `태그 삭제는 바뀐 수를 돌려준다`() =
        runBlocking<Unit> {
            val tagId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..1).next()
            coEvery { tagAppAction.delete(tagId = tagId) } returns AppActionChangedCount(changedCount = changedCount)

            val response = tester.execute(name = "tagDelete") { setString(TAG_ID, tagId) }

            response.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
        }

    @Test
    fun `태그 연결은 완료를 돌려준다`() =
        runBlocking<Unit> {
            val tagId = Arb.kotlinUuid().next().toString()
            val linkedTagId = Arb.kotlinUuid().next().toString()
            coEvery { tagLinkAppAction.add(tagId = tagId, linkedTagId = linkedTagId) } returns AppActionDone(done = true)

            val response =
                tester.execute(name = "tagLinkAdd") {
                    setString(TAG_ID, tagId)
                    setString(LINKED_TAG_ID, linkedTagId)
                }

            response.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
        }

    @Test
    fun `태그의 웹 목록은 범위와 정렬을 넘기고 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val tagId = Arb.kotlinUuid().next().toString()
            val web =
                AppActionWeb(
                    id = Arb.kotlinUuid().next().toString(),
                    title = Arb.string().next(),
                    description = Arb.string().next(),
                    url = Arb.string().next(),
                    createdAt = Arb.string().next(),
                    updatedAt = Arb.string().next(),
                )
            coEvery { tagItemAppAction.webList(tagId = any(), scope = any(), sort = any(), cursor = any()) } returns AppActionPage(items = listOf(web), nextCursor = null)

            val response =
                tester.execute(name = "tagWebList") {
                    setString(TAG_ID, tagId)
                    setString(SCOPE, AppActionTagScopeValue.CHILD)
                }

            response.returnValueAs<AppFunctionTagWebPage>() shouldBe
                AppFunctionTagWebPage(
                    items = listOf(AppFunctionWeb(id = web.id, title = web.title, description = web.description, url = web.url, createdAt = web.createdAt, updatedAt = web.updatedAt)),
                    nextCursor = null,
                )
            coVerify(exactly = 1) { tagItemAppAction.webList(tagId = tagId, scope = AppActionTagScopeValue.CHILD, sort = null, cursor = null) }
        }

    @Test
    fun `태그의 연락처와 곡 목록은 범위, 정렬, 위치를 넘기고 다음 페이지 위치를 돌려준다`() =
        runBlocking<Unit> {
            val tagId = Arb.kotlinUuid().next().toString()
            val cursor = Arb.int(1..1000).next().toString()
            val nextCursor = Arb.int(1001..2000).next().toString()
            coEvery { tagItemAppAction.contactList(tagId = any(), scope = any(), sort = any(), cursor = any()) } returns AppActionPage(items = emptyList(), nextCursor = nextCursor)
            coEvery { tagItemAppAction.musicList(tagId = any(), scope = any(), sort = any(), cursor = any()) } returns AppActionPage(items = emptyList(), nextCursor = nextCursor)

            val contactResponse =
                tester.execute(name = "tagContactList") {
                    setString(TAG_ID, tagId)
                    setString(SCOPE, AppActionTagScopeValue.DESCENDANT)
                    setString(SORT, AppActionSortValue.NAME)
                    setString(CURSOR, cursor)
                }
            val musicResponse =
                tester.execute(name = "tagMusicList") {
                    setString(TAG_ID, tagId)
                    setString(SORT, AppActionSortValue.TITLE)
                }

            contactResponse.returnValueAs<AppFunctionContactPage>() shouldBe AppFunctionContactPage(items = emptyList(), nextCursor = nextCursor)
            musicResponse.returnValueAs<AppFunctionMusicPage>() shouldBe AppFunctionMusicPage(items = emptyList(), nextCursor = nextCursor)
            coVerify(exactly = 1) { tagItemAppAction.contactList(tagId = tagId, scope = AppActionTagScopeValue.DESCENDANT, sort = AppActionSortValue.NAME, cursor = cursor) }
            coVerify(exactly = 1) { tagItemAppAction.musicList(tagId = tagId, scope = null, sort = AppActionSortValue.TITLE, cursor = null) }
        }

    @Test
    fun `태그의 체크리스트, QR, 디데이 목록은 범위와 위치를 넘기고 다음 페이지 위치를 돌려준다`() =
        runBlocking<Unit> {
            val tagId = Arb.kotlinUuid().next().toString()
            val cursor = Arb.int(1..1000).next().toString()
            val nextCursor = Arb.int(1001..2000).next().toString()
            coEvery { tagItemAppAction.checklistList(tagId = any(), scope = any(), cursor = any()) } returns AppActionPage(items = emptyList(), nextCursor = nextCursor)
            coEvery { tagItemAppAction.qrList(tagId = any(), scope = any(), cursor = any()) } returns AppActionPage(items = emptyList(), nextCursor = nextCursor)
            coEvery { tagItemAppAction.ddayList(tagId = any(), scope = any(), cursor = any()) } returns AppActionPage(items = emptyList(), nextCursor = nextCursor)

            val checklistResponse =
                tester.execute(name = "tagChecklistList") {
                    setString(TAG_ID, tagId)
                    setString(SCOPE, AppActionTagScopeValue.CHILD)
                    setString(CURSOR, cursor)
                }
            val qrResponse = tester.execute(name = "tagQrList") { setString(TAG_ID, tagId) }
            val ddayResponse = tester.execute(name = "tagDdayList") { setString(TAG_ID, tagId) }

            checklistResponse.returnValueAs<AppFunctionChecklistSummaryPage>() shouldBe AppFunctionChecklistSummaryPage(items = emptyList(), nextCursor = nextCursor)
            qrResponse.returnValueAs<AppFunctionQrPage>() shouldBe AppFunctionQrPage(items = emptyList(), nextCursor = nextCursor)
            ddayResponse.returnValueAs<AppFunctionDDayPage>() shouldBe AppFunctionDDayPage(items = emptyList(), nextCursor = nextCursor)
            coVerify(exactly = 1) { tagItemAppAction.checklistList(tagId = tagId, scope = AppActionTagScopeValue.CHILD, cursor = cursor) }
            coVerify(exactly = 1) { tagItemAppAction.qrList(tagId = tagId, scope = null, cursor = null) }
            coVerify(exactly = 1) { tagItemAppAction.ddayList(tagId = tagId, scope = null, cursor = null) }
        }

    @Test
    fun `인자 오류는 잘못된 인자 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val message = Arb.nonBlankString().next()
            coEvery { tagItemAppAction.memoFinishedList(tagId = any(), sort = any(), cursor = any()) } throws AppActionException.InvalidArgument(message = message)

            val response = tester.execute(name = "tagMemoFinishedList") { setString(TAG_ID, "not-id") }

            val error = response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            error.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            error.errorMessage shouldBe message
        }

    @Test
    fun `앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val message = Arb.nonBlankString().next()
            coEvery { tagLinkAppAction.remove(tagId = any(), linkedTagId = any()) } throws AppActionException.Failure(message = message)

            val response =
                tester.execute(name = "tagLinkRemove") {
                    setString(TAG_ID, Arb.kotlinUuid().next().toString())
                    setString(LINKED_TAG_ID, Arb.kotlinUuid().next().toString())
                }

            val error = response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            error.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            error.errorMessage shouldBe message
        }
}

private const val TAG_ID = "tagId"
private const val TAG_GET = "tagGet"
private const val LINKED_TAG_ID = "linkedTagId"
private const val SCOPE = "scope"
private const val SORT = "sort"
private const val CURSOR = "cursor"

private fun AppActionTag.expected(): AppFunctionTag =
    AppFunctionTag(
        id = id,
        emoji = emoji,
        title = title,
        description = description,
        color = color,
        isFinished = isFinished,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
