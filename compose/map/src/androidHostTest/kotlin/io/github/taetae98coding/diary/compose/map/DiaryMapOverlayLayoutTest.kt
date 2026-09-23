package io.github.taetae98coding.diary.compose.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProviderTestFixture.DEFAULT_GOOGLE_LABEL
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProviderTestFixture.DEFAULT_NAVER_LABEL
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProviderTestFixture.DEFAULT_PROVIDER_CONTENT_DESCRIPTION
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProviderTestFixture.LAYOUT_TEST_TAG
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProviderTestFixture.MAP_TEST_TAG
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryMapOverlayLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-DIARY-MAP-FEATURE-018 겹쳐 표시하는 배치에서 전환 컨트롤로 제공자를 선택한다`() {
        setDiaryMapOverlayLayout()

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).performClick()

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertIsNotSelected()
    }

    @Test
    fun `지도가 영역 전체를 채우고 전환 컨트롤이 그 위에 겹쳐 표시된다`() {
        setDiaryMapOverlayLayout()

        val layoutBounds = composeRule.onNodeWithTag(LAYOUT_TEST_TAG).getUnclippedBoundsInRoot()
        val mapBounds = composeRule.onNodeWithTag(MAP_TEST_TAG).getUnclippedBoundsInRoot()
        val selectorBounds = composeRule.onNodeWithContentDescription(DEFAULT_PROVIDER_CONTENT_DESCRIPTION).getUnclippedBoundsInRoot()

        mapBounds.left shouldBe layoutBounds.left
        mapBounds.top shouldBe layoutBounds.top
        mapBounds.right shouldBe layoutBounds.right
        mapBounds.bottom shouldBe layoutBounds.bottom

        selectorBounds.top shouldBeGreaterThan mapBounds.top
        selectorBounds.bottom shouldBeLessThan mapBounds.bottom
    }

    private fun setDiaryMapOverlayLayout() {
        composeRule.setContent {
            DiaryTheme {
                DiaryMapOverlayLayout(
                    state = rememberDiaryMapState(),
                    modifier =
                        Modifier
                            .requiredSize(width = COMPONENT_WIDTH, height = COMPONENT_HEIGHT)
                            .testTag(LAYOUT_TEST_TAG),
                    map = { Box(modifier = Modifier.fillMaxSize().testTag(MAP_TEST_TAG)) },
                )
            }
        }
    }

    private companion object {
        val COMPONENT_WIDTH = 300.dp
        val COMPONENT_HEIGHT = 400.dp
    }
}
