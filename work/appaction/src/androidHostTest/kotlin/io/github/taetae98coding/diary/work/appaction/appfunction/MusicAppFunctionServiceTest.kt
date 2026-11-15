package io.github.taetae98coding.diary.work.appaction.appfunction

import android.content.Context
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.testing.AppFunctionTestRule
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.github.taetae98coding.diary.work.appaction.appfunction.core.expectedTag
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueAs
import io.github.taetae98coding.diary.work.appaction.appfunction.music.AppFunctionMusic
import io.github.taetae98coding.diary.work.appaction.appfunction.music.AppFunctionMusicDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.music.AppFunctionMusicPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionSortValue
import io.github.taetae98coding.diary.work.appaction.music.AppActionMusic
import io.github.taetae98coding.diary.work.appaction.music.AppActionMusicDetail
import io.github.taetae98coding.diary.work.appaction.music.MusicAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
import io.github.taetae98coding.diary.work.appaction.tag.appActionTag
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
class MusicAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val musicAppAction = mockk<MusicAppAction>()
    private val entityTagAppAction = mockk<EntityTagAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single { musicAppAction }
                    single { entityTagAppAction }
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
    fun `곡 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList().filter { name -> name.startsWith("music") } shouldContainExactlyInAnyOrder
                listOf(MUSIC_LIST, MUSIC_GET, MUSIC_ADD, MUSIC_UPDATE, MUSIC_DELETE, MUSIC_RESTORE, MUSIC_TAG_ADD, MUSIC_TAG_REMOVE)
        }

    @Test
    fun `곡 목록은 공통 동작의 페이지를 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val music = arbAppActionMusic()
            val nextCursor = Arb.int(1..1000).next().toString()
            coEvery { musicAppAction.list(sort = any(), cursor = any()) } returns AppActionPage(items = listOf(music), nextCursor = nextCursor)

            val response = tester.execute(name = MUSIC_LIST) { setString("sort", AppActionSortValue.RECENTLY_UPDATED) }

            response.returnValueAs<AppFunctionMusicPage>() shouldBe AppFunctionMusicPage(items = listOf(music.expected()), nextCursor = nextCursor)
            coVerify(exactly = 1) { musicAppAction.list(sort = AppActionSortValue.RECENTLY_UPDATED, cursor = null) }
        }

    @Test
    fun `TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 곡 조회는 연결된 태그와 함께 돌려주고 없으면 값이 없는 결과를 돌려준다`() =
        runBlocking<Unit> {
            val music = arbAppActionMusic()
            val tag = Arb.appActionTag().next()
            val missingId = Arb.kotlinUuid().next().toString()
            coEvery { musicAppAction.get(musicId = music.id) } returns AppActionMusicDetail(music = music, tags = listOf(tag))
            coEvery { musicAppAction.get(musicId = missingId) } returns null

            tester.execute(name = MUSIC_GET) { setString(MUSIC_ID, music.id) }.returnValueAs<AppFunctionMusicDetail>() shouldBe AppFunctionMusicDetail(music = music.expected(), tags = listOf(tag.expectedTag()))
            tester
                .execute(name = MUSIC_GET) { setString(MUSIC_ID, missingId) }
                .shouldBeInstanceOf<ExecuteAppFunctionResponse.Success>()
                .returnValue
                .containsKey(ExecuteAppFunctionResponse.Success.PROPERTY_RETURN_VALUE) shouldBe false
        }

    @Test
    fun `TC-edf2f510-aac0-4a31-afdb-2f9368763906 곡 추가는 받은 값과 태그를 공통 동작에 넘기고 id를 돌려준다`() =
        runBlocking<Unit> {
            val title = Arb.nonBlankString().next()
            val link = Arb.string().next()
            val tagIdList = listOf(Arb.kotlinUuid().next().toString(), Arb.kotlinUuid().next().toString())
            val addedId = Arb.kotlinUuid().next().toString()
            coEvery { musicAppAction.add(title = any(), artist = any(), link = any(), tagIds = any()) } returns AppActionId(id = addedId)

            val response =
                tester.execute(name = MUSIC_ADD) {
                    setString("title", title)
                    setString("link", link)
                    setStringList("tagIds", tagIdList)
                }

            response.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId)
            coVerify(exactly = 1) { musicAppAction.add(title = title, artist = null, link = link, tagIds = tagIdList) }
        }

    @Test
    fun `곡 수정, 삭제, 되돌리기는 바뀐 수를 돌려준다`() =
        runBlocking<Unit> {
            val musicId = Arb.kotlinUuid().next().toString()
            val artist = Arb.string().next()
            val changedCount = Arb.int(0..1).next()
            coEvery { musicAppAction.update(musicId = any(), title = any(), artist = any(), link = any()) } returns AppActionChangedCount(changedCount = changedCount)
            coEvery { musicAppAction.delete(musicId = musicId) } returns AppActionChangedCount(changedCount = changedCount)
            coEvery { musicAppAction.restore(musicId = musicId) } returns AppActionChangedCount(changedCount = changedCount)
            val expected = AppFunctionChangedCount(changedCount = changedCount)

            tester
                .execute(name = MUSIC_UPDATE) {
                    setString(MUSIC_ID, musicId)
                    setString("artist", artist)
                }.returnValueAs<AppFunctionChangedCount>() shouldBe expected
            tester.execute(name = MUSIC_DELETE) { setString(MUSIC_ID, musicId) }.returnValueAs<AppFunctionChangedCount>() shouldBe expected
            tester.execute(name = MUSIC_RESTORE) { setString(MUSIC_ID, musicId) }.returnValueAs<AppFunctionChangedCount>() shouldBe expected
            coVerify(exactly = 1) { musicAppAction.update(musicId = musicId, title = null, artist = artist, link = null) }
        }

    @Test
    fun `TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc 곡 태그 연결과 해제는 완료를 돌려준다`() =
        runBlocking<Unit> {
            val musicId = Arb.kotlinUuid().next().toString()
            val tagId = Arb.kotlinUuid().next().toString()
            coEvery { entityTagAppAction.add(kind = AppActionEntityTagKind.MUSIC, entityId = any(), tagId = any()) } returns AppActionDone(done = true)
            coEvery { entityTagAppAction.remove(kind = AppActionEntityTagKind.MUSIC, entityId = any(), tagId = any()) } returns AppActionDone(done = true)

            val addResponse =
                tester.execute(name = MUSIC_TAG_ADD) {
                    setString(MUSIC_ID, musicId)
                    setString("tagId", tagId)
                }
            val removeResponse =
                tester.execute(name = MUSIC_TAG_REMOVE) {
                    setString(MUSIC_ID, musicId)
                    setString("tagId", tagId)
                }

            addResponse.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            removeResponse.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
            coVerify(exactly = 1) { entityTagAppAction.add(kind = AppActionEntityTagKind.MUSIC, entityId = musicId, tagId = tagId) }
            coVerify(exactly = 1) { entityTagAppAction.remove(kind = AppActionEntityTagKind.MUSIC, entityId = musicId, tagId = tagId) }
        }

    @Test
    fun `인자 오류는 잘못된 인자 실패로, 앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val invalidMessage = Arb.nonBlankString().next()
            val failureMessage = Arb.nonBlankString().next()
            coEvery { musicAppAction.list(sort = any(), cursor = any()) } throws AppActionException.InvalidArgument(message = invalidMessage)
            coEvery { musicAppAction.add(title = any(), artist = any(), link = any(), tagIds = any()) } throws AppActionException.Failure(message = failureMessage)

            val invalidError = tester.execute(name = MUSIC_LIST) { setString("cursor", "abc") }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            val failureError = tester.execute(name = MUSIC_ADD) { setString("title", Arb.nonBlankString().next()) }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error

            invalidError.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            invalidError.errorMessage shouldBe invalidMessage
            failureError.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            failureError.errorMessage shouldBe failureMessage
        }
}

private const val MUSIC_LIST = "musicList"
private const val MUSIC_GET = "musicGet"
private const val MUSIC_ADD = "musicAdd"
private const val MUSIC_UPDATE = "musicUpdate"
private const val MUSIC_DELETE = "musicDelete"
private const val MUSIC_RESTORE = "musicRestore"
private const val MUSIC_TAG_ADD = "musicTagAdd"
private const val MUSIC_TAG_REMOVE = "musicTagRemove"
private const val MUSIC_ID = "musicId"

private fun arbAppActionMusic(): AppActionMusic =
    AppActionMusic(
        id = Arb.kotlinUuid().next().toString(),
        title = Arb.nonBlankString().next(),
        artist = Arb.string().next(),
        link = Arb.string().next(),
        createdAt = Arb.string().next(),
        updatedAt = Arb.string().next(),
    )

private fun AppActionMusic.expected(): AppFunctionMusic =
    AppFunctionMusic(
        id = id,
        title = title,
        artist = artist,
        link = link,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
