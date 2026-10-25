@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.ui.memoEntry
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.ui.placeEntry
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.ui.tagEntry
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.ui.webEntry
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// 완료된 태그 목록과 태그 상세가 어떻게 놓이는지는 기능들이 앱에 등록한 배치 정보가 정한다.
// 실제 배치 정보와 선택 전 상세를 그대로 쓰고, 화면 본문만 이름이 보이는 대역으로 바꿔 창 크기별로 검증한다.
// 태그 목록과 메모 목록의 선택 전 상세는 실제 추가 화면이라 이 대역 환경에서 띄울 수 없으므로, 그 목록으로 돌아가는 경우에는 목록에 상세를 열어 둔다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagFinishedListLayoutTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-3dfe87fa-52c8-441e-939d-4fccba789c09 TC-3b4e1e05-b0bb-48f5-a781-63426a76ac3b 넓은 창에서 완료된 태그 목록을 열면 TagHome 목록과 태그 추가 없이 선택 전 상세와 함께 표시한다`() {
        setAppNavDisplay(backStack = NavBackStack(TagHomeNavKey, TagFinishedListNavKey))

        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(HOME_CONTENT).assertDoesNotExist()
        composeRule.onNodeWithText(ADD_CONTENT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-d40a0f5e-925a-48c7-8997-4adfd0fc9b84 넓은 창에서 태그를 선택하면 목록을 유지한 채 상세 영역에 그 태그의 상세가 놓인다`() {
        val detailKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagFinishedListNavKey)
        setAppNavDisplay(backStack = backStack)

        composeRule.runOnIdle { backStack.add(detailKey) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(detailContentOf(detailKey)).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-8175338b-308e-40e9-8d8f-c50793db9466 넓은 창에서 상세 영역의 태그 상세에서 연결 입력으로 연 화면도 목록과 함께 상세 영역에 놓인다`() {
        val listSelectedKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val linkedKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagFinishedListNavKey, listSelectedKey)
        setAppNavDisplay(backStack = backStack)

        composeRule.runOnIdle { backStack.add(linkedKey) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(detailContentOf(linkedKey)).assertIsDisplayed()

        composeRule.runOnIdle { backStack.add(TagAddNavKey(requestKey = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(ADD_CONTENT).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun `TC-f2c0b78a-b7c0-4294-b799-ab758a5ac0ce 한 영역만 쓰는 창에서는 현재 화면만 단독으로 표시한다`() {
        val detailKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagFinishedListNavKey)
        setAppNavDisplay(backStack = backStack)
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertDoesNotExist()

        composeRule.runOnIdle { backStack.add(detailKey) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(detailContentOf(detailKey)).assertIsDisplayed()
        composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-d5e3c2e4-5f87-4e71-813b-d01eb612c6d3 넓은 창에서 목록에서 고른 태그 상세에서 뒤로가면 목록을 유지하고 선택 전 상태로 돌아간다`() {
        val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagFinishedListNavKey, TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))
        setAppNavDisplay(backStack = backStack)

        pressBack()

        backStack.toList() shouldBe listOf(TagHomeNavKey, TagFinishedListNavKey)
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-d5e3c2e4-5f87-4e71-813b-d01eb612c6d3 넓은 창에서 연결한 태그의 상세에서 뒤로가면 목록을 유지하고 목록에서 고른 태그 상세로 돌아간다`() {
        val listSelectedKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val linkedKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagFinishedListNavKey, listSelectedKey, linkedKey)
        setAppNavDisplay(backStack = backStack)

        pressBack()

        backStack.toList() shouldBe listOf(TagHomeNavKey, TagFinishedListNavKey, listSelectedKey)
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(detailContentOf(listSelectedKey)).assertIsDisplayed()
        composeRule.onNodeWithText(detailContentOf(linkedKey)).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-846bf001-f201-4e72-820e-f2fa3c3bbac8 TC-dd898457-7fbf-406f-96eb-0c8b52a870dc 넓은 창 태그 상세가 탭으로 연 화면은 목록 없이 놓이고 돌아오면 그 상세가 놓인다`() {
        val listSelectedKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagFinishedListNavKey, listSelectedKey)
        val openedKeyList: List<ScreenNavKey> =
            listOf(
                MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                MemoAddNavKey(primaryTagId = listSelectedKey.id),
                WebDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                PlaceDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                TagMemoFinishedListNavKey(tagId = listSelectedKey.id),
            )
        setAppNavDisplay(backStack = backStack)

        openedKeyList.forEach { openedKey ->
            composeRule.runOnIdle { backStack.add(openedKey) }
            composeRule.waitForIdle()

            composeRule.onNodeWithText(contentOf(openedKey)).assertIsDisplayed()
            composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
            composeRule.onNodeWithText(detailContentOf(listSelectedKey)).assertDoesNotExist()

            pressBack()

            composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
            composeRule.onNodeWithText(detailContentOf(listSelectedKey)).assertIsDisplayed()
        }
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-6887f2d0-17fd-4046-99ad-aaac6d1ba535 넓은 창에서 상세가 선택 전 상태일 때 뒤로가면 완료된 태그 목록 전체를 떠나 TagHome 목록으로 돌아간다`() {
        val homeDetailKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val backStack = NavBackStack(TagHomeNavKey, homeDetailKey, TagFinishedListNavKey)
        setAppNavDisplay(backStack = backStack)

        pressBack()

        backStack.toList() shouldBe listOf(TagHomeNavKey, homeDetailKey)
        composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
        composeRule.onNodeWithText(HOME_CONTENT).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-62fc4ea3-f7fa-45c9-9f11-d138d4563e88 넓은 창에서 완료된 태그 목록에 다녀와도 태그 목록 배치의 상세가 그대로다`() {
        val homeDetailKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val linkedKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, homeDetailKey, linkedKey)
        setAppNavDisplay(backStack = backStack)
        composeRule.onNodeWithText(detailContentOf(linkedKey)).assertIsDisplayed()

        composeRule.runOnIdle {
            backStack.add(TagFinishedListNavKey)
            backStack.add(TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        pressBack()
        pressBack()

        backStack.toList() shouldBe listOf(TagHomeNavKey, homeDetailKey, linkedKey)
        composeRule.onNodeWithText(HOME_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(detailContentOf(linkedKey)).assertIsDisplayed()

        pressBack()

        composeRule.onNodeWithText(HOME_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(detailContentOf(homeDetailKey)).assertIsDisplayed()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun setAppNavDisplay(backStack: NavBackStack<ScreenNavKey>) {
        composeRule.setContent {
            val appEntryProvider =
                remember {
                    entryProvider<ScreenNavKey> {
                        memoEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                        tagEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                        webEntry(backStack = backStack)
                        placeEntry(backStack = backStack)
                    }
                }

            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider = { key ->
                        NavEntry(key = key, metadata = appEntryProvider(key).metadata) { Text(text = contentOf(key)) }
                    },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun contentOf(key: ScreenNavKey): String =
        when (key) {
            is TagFinishedListNavKey -> LIST_CONTENT
            is TagDetailNavKey -> detailContentOf(key)
            is TagHomeNavKey -> HOME_CONTENT
            is TagAddNavKey -> ADD_CONTENT
            else -> key.screenName
        }

    private fun detailContentOf(key: TagDetailNavKey): String = "TagDetailContent:${key.id}"

    private companion object {
        const val LIST_CONTENT = "TagFinishedListContent"
        const val HOME_CONTENT = "TagHomeContent"
        const val ADD_CONTENT = "TagAddContent"
        const val DEFAULT_PLACEHOLDER_TEXT = "Choose a tag"
    }
}
