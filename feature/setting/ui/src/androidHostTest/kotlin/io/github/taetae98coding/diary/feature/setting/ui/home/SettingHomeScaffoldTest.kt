package io.github.taetae98coding.diary.feature.setting.ui.home

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목은 설정이다`() {
        setSettingHomeScaffold()

        composeRule.onNodeWithText("설정").assertExists()
    }

    @Test
    fun `기본 환경에서 제목은 Settings다`() {
        setSettingHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setSettingHomeScaffold()

        composeRule.onNodeWithContentDescription("뒤로가기").assert(hasClickAction())
    }

    @Test
    fun `기본 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setSettingHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SETTING-HOME-FEATURE-005 한국어 환경에서 설정 항목 이름을 표시한다`() {
        setSettingHomeScaffold()

        composeRule.onNodeWithText("공휴일").assertExists()
        composeRule.onNodeWithText("지도").assertExists()
        composeRule.onNodeWithText(DEFAULT_GEMINI_ITEM_LABEL).assertExists()
        composeRule.onNodeWithText("브라우저").assertExists()
    }

    @Test
    fun `TC-SETTING-HOME-FEATURE-005 기본 환경에서 설정 항목 이름을 표시한다`() {
        setSettingHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_HOLIDAY_ITEM_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_MAP_ITEM_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_GEMINI_ITEM_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_BROWSER_ITEM_LABEL).assertExists()
    }

    @Test
    fun `TC-SETTING-HOME-FEATURE-006 각 설정 항목을 선택할 수 있다`() {
        setSettingHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_HOLIDAY_ITEM_LABEL).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_MAP_ITEM_LABEL).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_GEMINI_ITEM_LABEL).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_BROWSER_ITEM_LABEL).assert(hasClickAction())
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe CLICKABLE_NODE_COUNT
    }

    @Test
    fun `TC-SETTING-HOME-DOMAIN-001 설정 항목을 선언된 순서대로 배치한다`() {
        setSettingHomeScaffold()

        displayedItemLabels() shouldBe
            listOf(DEFAULT_HOLIDAY_ITEM_LABEL, DEFAULT_MAP_ITEM_LABEL, DEFAULT_GEMINI_ITEM_LABEL, DEFAULT_BROWSER_ITEM_LABEL)
    }

    @Test
    fun `TC-SETTING-HOME-FEATURE-011 브라우저 항목을 제공하지 않는 환경에서는 그 항목을 표시하지 않는다`() {
        setSettingHomeScaffold(
            uiState = SettingHomeUiState.Loaded(itemList = listOf(SettingHomeItem.HOLIDAY, SettingHomeItem.MAP, SettingHomeItem.GEMINI)),
        )

        composeRule.onNodeWithText(DEFAULT_BROWSER_ITEM_LABEL).assertDoesNotExist()
        displayedItemLabels() shouldBe listOf(DEFAULT_HOLIDAY_ITEM_LABEL, DEFAULT_MAP_ITEM_LABEL, DEFAULT_GEMINI_ITEM_LABEL)
    }

    @Test
    fun `TC-SETTING-HOME-FEATURE-012 제공하는 항목을 확인하기 전에는 항목과 진행 표시를 두지 않는다`() {
        setSettingHomeScaffold(uiState = SettingHomeUiState.Loading)

        displayedItemLabels() shouldBe emptyList()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    private fun setSettingHomeScaffold(
        uiState: SettingHomeUiState = SettingHomeUiState.Loaded(itemList = settingHomeItemList),
        onEvent: (SettingHomeScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingHomeScaffold(
                    onEvent = onEvent,
                    uiStateProvider = { uiState },
                )
            }
        }
    }

    private fun displayedItemLabels(): List<String> =
        composeRule
            .onAllNodes(hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.Text] }
            .map { text -> text.text }

    public companion object {
        private const val DEFAULT_TITLE = "Settings"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_HOLIDAY_ITEM_LABEL = "Holiday"
        private const val DEFAULT_MAP_ITEM_LABEL = "Map"
        private const val DEFAULT_GEMINI_ITEM_LABEL = "Gemini"
        private const val DEFAULT_BROWSER_ITEM_LABEL = "Browser"

        private const val NAVIGATE_UP_CLICKABLE_COUNT = 1
        private const val SETTING_ITEM_CLICKABLE_COUNT = 4
        private const val CLICKABLE_NODE_COUNT = NAVIGATE_UP_CLICKABLE_COUNT + SETTING_ITEM_CLICKABLE_COUNT
    }
}
