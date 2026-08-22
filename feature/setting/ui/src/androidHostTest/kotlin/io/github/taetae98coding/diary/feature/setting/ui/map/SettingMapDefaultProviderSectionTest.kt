package io.github.taetae98coding.diary.feature.setting.ui.map

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingMapDefaultProviderSectionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SETTING-MAP-FEATURE-005 한국어 환경에서 기본 지도 설정 문구를 표시한다`() {
        setSettingMapDefaultProviderSection()

        composeRule.onNodeWithText("기본 지도").assertExists()
        composeRule.onNodeWithText("네이버").assertExists()
        composeRule.onNodeWithText("Google").assertExists()
    }

    @Test
    fun `TC-SETTING-MAP-FEATURE-005 기본 환경에서 기본 지도 설정 문구를 표시한다`() {
        setSettingMapDefaultProviderSection()

        composeRule.onNodeWithText(DEFAULT_DEFAULT_PROVIDER_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertExists()
    }

    @Test
    fun `TC-SETTING-MAP-FEATURE-006 저장된 기본 지도만 선택된 상태로 표시한다`() {
        val cases =
            mapOf(
                MapProvider.NAVER to (DEFAULT_NAVER_LABEL to DEFAULT_GOOGLE_LABEL),
                MapProvider.GOOGLE to (DEFAULT_GOOGLE_LABEL to DEFAULT_NAVER_LABEL),
            )
        val defaultProvider = mutableStateOf(MapProvider.NAVER)
        setSettingMapDefaultProviderSection(defaultProviderProvider = { defaultProvider.value })

        cases.forEach { (provider, labels) ->
            composeRule.runOnIdle { defaultProvider.value = provider }

            val (selectedLabel, notSelectedLabel) = labels
            composeRule.onNodeWithText(selectedLabel).assertIsSelected()
            composeRule.onNodeWithText(notSelectedLabel).assertIsNotSelected()
        }
    }

    @Test
    fun `TC-SETTING-MAP-FEATURE-007 각 지도 줄을 선택할 수 있다`() {
        setSettingMapDefaultProviderSection()

        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assert(hasClickAction())
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 2
        displayedTexts() shouldContainExactlyInAnyOrder
            listOf(
                DEFAULT_DEFAULT_PROVIDER_LABEL,
                DEFAULT_NAVER_LABEL,
                DEFAULT_GOOGLE_LABEL,
            )
    }

    @Test
    fun `네이버 지도 줄을 Google 지도 줄보다 위에 표시한다`() {
        setSettingMapDefaultProviderSection()

        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).getUnclippedBoundsInRoot().top shouldBeLessThan
            composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).getUnclippedBoundsInRoot().top
    }

    @Test
    fun `TC-SETTING-MAP-FEATURE-008 선택되어 있지 않은 줄을 선택하면 그 지도를 기본 지도로 요청한다`() {
        val selectedProviders = mutableListOf<MapProvider>()
        setSettingMapDefaultProviderSection(onSelect = { provider -> selectedProviders.add(provider) })

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).performClick()
        composeRule.waitForIdle()

        selectedProviders shouldBe listOf(MapProvider.GOOGLE)
    }

    private fun setSettingMapDefaultProviderSection(
        defaultProviderProvider: () -> MapProvider = { MapProvider.NAVER },
        onSelect: (MapProvider) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingMapDefaultProviderSection(
                    defaultProvider = defaultProviderProvider(),
                    onSelect = onSelect,
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
        private const val DEFAULT_DEFAULT_PROVIDER_LABEL = "Default Map"
        private const val DEFAULT_NAVER_LABEL = "Naver"
        private const val DEFAULT_GOOGLE_LABEL = "Google"
    }
}
