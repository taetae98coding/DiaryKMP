@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
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
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.ui.contactEntry
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.navigateToMemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.memoEntry
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.ui.placeEntry
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.ui.tagEntry
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
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

// 완료된 메모 목록과 메모 상세가 어떻게 놓이는지는 메모 기능이 앱에 등록한 배치 정보가 정한다.
// 실제 배치 정보와 선택 전 상세를 그대로 쓰고, 화면 본문만 이름이 보이는 대역으로 바꿔 창 크기별로 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoFinishedListLayoutTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-d5d2c870-c1a8-4977-bd89-f6f37f88fd13 TC-ea6f386d-f6a3-4d7d-9d96-6ff5cb3b582e 넓은 창에서 완료된 메모 목록을 열면 MemoHome 목록 없이 선택 전 상세와 함께 표시한다`() {
        val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, MemoFinishedListNavKey)
        setAppNavDisplay(backStack = backStack)

        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(HOME_CONTENT).assertDoesNotExist()
        composeRule.onNodeWithText(ADD_CONTENT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-37b0df93-d613-4ee7-bbaa-7179a3201cfc 기본 환경의 선택 전 안내만 표시하고 실행할 동작을 두지 않는다`() {
        setAppNavDisplay(backStack = NavBackStack(MemoHomeNavKey, MemoFinishedListNavKey))

        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertIsDisplayed()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 0
    }

    @Test
    @Config(qualifiers = "ko-w1280dp-h1600dp")
    fun `TC-37b0df93-d613-4ee7-bbaa-7179a3201cfc 한국어 선택 전 안내만 표시하고 실행할 동작을 두지 않는다`() {
        setAppNavDisplay(backStack = NavBackStack(MemoHomeNavKey, MemoFinishedListNavKey))

        composeRule.onNodeWithText(KOREAN_PLACEHOLDER_TEXT).assertIsDisplayed()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 0
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-dd8970a8-0084-4755-9ece-aade2474e3d2 넓은 창에서 메모를 선택하면 목록을 유지한 채 상세 영역에 그 메모의 상세가 놓인다`() {
        val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, MemoFinishedListNavKey)
        setAppNavDisplay(backStack = backStack)

        composeRule.runOnIdle { backStack.navigateToMemoDetail(fixtureMonkey.giveMeOne<Uuid>()) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(HOME_CONTENT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun `TC-60b76d30-508b-4b3b-a5a7-b67e0dc2ef08 한 영역만 쓰는 창에서는 현재 화면만 단독으로 표시한다`() {
        val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, MemoFinishedListNavKey)
        setAppNavDisplay(backStack = backStack)
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertDoesNotExist()

        composeRule.runOnIdle { backStack.navigateToMemoDetail(fixtureMonkey.giveMeOne<Uuid>()) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-446a7b3f-b8d8-47b7-8101-e05eb4a53f27 넓은 창에서 메모 상세를 연 뒤 뒤로가면 목록을 유지하고 선택 전 상태로 돌아간다`() {
        val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, MemoFinishedListNavKey)
        setAppNavDisplay(backStack = backStack)
        composeRule.runOnIdle { backStack.navigateToMemoDetail(fixtureMonkey.giveMeOne<Uuid>()) }
        composeRule.waitForIdle()

        pressBack()

        backStack.toList() shouldBe listOf(MemoHomeNavKey, MemoFinishedListNavKey)
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-446a7b3f-b8d8-47b7-8101-e05eb4a53f27 넓은 창에서 다른 메모의 상세로 바꾼 뒤 뒤로가면 이전 메모가 아닌 선택 전 상태로 돌아간다`() {
        val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, MemoFinishedListNavKey)
        setAppNavDisplay(backStack = backStack)
        composeRule.runOnIdle { backStack.navigateToMemoDetail(fixtureMonkey.giveMeOne<Uuid>()) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.navigateToMemoDetail(fixtureMonkey.giveMeOne<Uuid>()) }
        composeRule.waitForIdle()

        pressBack()

        backStack.toList() shouldBe listOf(MemoHomeNavKey, MemoFinishedListNavKey)
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-0523c77b-f821-4c55-b2d8-2603bccde3fb 넓은 창에서 상세가 선택 전 상태일 때 뒤로가면 완료된 메모 목록 전체를 떠나 MemoHome 목록으로 돌아간다`() {
        // MemoHome의 선택 전 상세는 실제 메모 추가 화면이라 이 대역 환경에서 띄울 수 없으므로, 돌아갈 MemoHome에 메모 상세를 열어 둔다.
        val homeDetailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val backStack = NavBackStack(MemoHomeNavKey, homeDetailKey, MemoFinishedListNavKey)
        setAppNavDisplay(backStack = backStack)

        pressBack()

        backStack.toList() shouldBe listOf(MemoHomeNavKey, homeDetailKey)
        composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
        composeRule.onNodeWithText(HOME_CONTENT).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-17db916b-14c5-4630-b776-0acc114d266e 넓은 창에서 상세 영역의 메모에서 연 연결 항목 화면은 단독으로 표시하고 돌아오면 떠나기 전의 메모가 놓인다`() {
        val homeDetailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val backStack = NavBackStack(MemoHomeNavKey, homeDetailKey, MemoFinishedListNavKey)
        val openedKeyList: List<ScreenNavKey> =
            listOf(
                TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                TagAddNavKey(requestKey = fixtureMonkey.giveMeOne<Uuid>()),
                WebDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                WebAddNavKey(requestKey = fixtureMonkey.giveMeOne<Uuid>()),
                ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                ContactAddNavKey(requestKey = fixtureMonkey.giveMeOne<Uuid>()),
                PlaceDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                PlaceAddNavKey(requestKey = fixtureMonkey.giveMeOne<Uuid>()),
            )
        setAppNavDisplay(backStack = backStack)
        composeRule.runOnIdle { backStack.navigateToMemoDetail(fixtureMonkey.giveMeOne<Uuid>()) }
        composeRule.waitForIdle()
        val selectedDetailKey = backStack.last()

        openedKeyList.forEach { openedKey ->
            composeRule.runOnIdle { backStack.add(openedKey) }
            composeRule.waitForIdle()

            composeRule.onNodeWithText(contentOf(openedKey)).assertIsDisplayed()
            composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
            composeRule.onNodeWithText(DETAIL_CONTENT).assertDoesNotExist()

            pressBack()

            backStack.last() shouldBe selectedDetailKey
            composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
            composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        }
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-4b6fb492-81b6-4c57-8992-1c7a884b53c6 넓은 창에서 완료된 메모 목록에 다녀와도 메모 목록 배치의 상세가 그대로다`() {
        val homeDetailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, homeDetailKey)
        setAppNavDisplay(backStack = backStack)
        composeRule.onNodeWithText(HOME_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()

        composeRule.runOnIdle {
            backStack.add(MemoFinishedListNavKey)
            backStack.navigateToMemoDetail(fixtureMonkey.giveMeOne<Uuid>())
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        pressBack()
        pressBack()

        backStack.toList() shouldBe listOf(MemoHomeNavKey, homeDetailKey)
        composeRule.onNodeWithText(HOME_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
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
                        contactEntry(backStack = backStack)
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
            is MemoFinishedListNavKey -> LIST_CONTENT
            is MemoDetailNavKey -> DETAIL_CONTENT
            is MemoHomeNavKey -> HOME_CONTENT
            is MemoAddNavKey -> ADD_CONTENT
            else -> key.screenName
        }

    private companion object {
        const val LIST_CONTENT = "MemoFinishedListContent"
        const val DETAIL_CONTENT = "MemoDetailContent"
        const val HOME_CONTENT = "MemoHomeContent"
        const val ADD_CONTENT = "MemoAddContent"
        const val DEFAULT_PLACEHOLDER_TEXT = "Choose a memo"
        const val KOREAN_PLACEHOLDER_TEXT = "메모를 선택하세요"
    }
}
