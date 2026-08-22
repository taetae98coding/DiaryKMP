package io.github.taetae98coding.diary.feature.setting.ui.map

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingMapScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목은 지도 설정이다`() {
        setSettingMapScaffold()

        composeRule.onNodeWithText("지도 설정").assertExists()
    }

    @Test
    fun `기본 환경에서 제목은 Map Settings다`() {
        setSettingMapScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setSettingMapScaffold()

        composeRule.onNodeWithContentDescription("뒤로가기").assert(hasClickAction())
    }

    @Test
    fun `기본 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setSettingMapScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-SETTING-MAP-FEATURE-011 기본 지도를 확인하기 전에는 설정 영역을 표시하지 않는다`() {
        setSettingMapScaffold(uiStateProvider = { SettingMapUiState.Loading })

        composeRule.onNodeWithText(DEFAULT_DEFAULT_PROVIDER_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertDoesNotExist()
        displayedTexts() shouldBe listOf(DEFAULT_TITLE)
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 1
    }

    @Test
    fun `TC-SETTING-MAP-FEATURE-012 본문에 지도를 표시하지 않는다`() {
        val uiState = mutableStateOf<SettingMapUiState>(SettingMapUiState.Loaded(defaultProvider = MapProvider.NAVER))
        setSettingMapScaffold(uiStateProvider = { uiState.value })

        composeRule.runOnIdle { uiState.value = SettingMapUiState.Loaded(defaultProvider = MapProvider.GOOGLE) }

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_PROVIDER_DESCRIPTION).assertDoesNotExist()
        displayedTexts() shouldContainExactlyInAnyOrder
            listOf(
                DEFAULT_TITLE,
                DEFAULT_DEFAULT_PROVIDER_LABEL,
                DEFAULT_NAVER_LABEL,
                DEFAULT_GOOGLE_LABEL,
            )
    }

    private fun setSettingMapScaffold(
        uiStateProvider: () -> SettingMapUiState = { SettingMapUiState.Loaded(defaultProvider = MapProvider.NAVER) },
        onEvent: (SettingMapScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingMapScaffold(
                    uiStateProvider = uiStateProvider,
                    onEvent = onEvent,
                )
            }
        }
    }

    private fun displayedTexts(): List<String> =
        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.Text] }
            .map { text -> text.text }

    public companion object {
        private const val DEFAULT_TITLE = "Map Settings"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_DEFAULT_PROVIDER_LABEL = "Default Map"
        private const val DEFAULT_NAVER_LABEL = "Naver"
        private const val DEFAULT_GOOGLE_LABEL = "Google"
        private const val DEFAULT_MAP_PROVIDER_DESCRIPTION = "Map provider"
    }
}
