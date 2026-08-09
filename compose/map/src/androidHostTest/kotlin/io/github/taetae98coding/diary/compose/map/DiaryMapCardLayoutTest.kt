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
import androidx.compose.ui.unit.height
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapProviderTestFixture.DEFAULT_GOOGLE_LABEL
import io.github.taetae98coding.diary.compose.map.DiaryMapProviderTestFixture.DEFAULT_NAVER_LABEL
import io.github.taetae98coding.diary.compose.map.DiaryMapProviderTestFixture.DEFAULT_PROVIDER_CONTENT_DESCRIPTION
import io.github.taetae98coding.diary.compose.map.DiaryMapProviderTestFixture.LAYOUT_TEST_TAG
import io.github.taetae98coding.diary.compose.map.DiaryMapProviderTestFixture.MAP_TEST_TAG
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryMapCardLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-DIARY-MAP-FEATURE-019 전환 컨트롤과 지도가 겹치지 않는다`() {
        setDiaryMapCardLayout()

        val layoutBounds = composeRule.onNodeWithTag(LAYOUT_TEST_TAG).getUnclippedBoundsInRoot()
        val tabRowBounds = composeRule.onNodeWithContentDescription(DEFAULT_PROVIDER_CONTENT_DESCRIPTION).getUnclippedBoundsInRoot()
        val mapBounds = composeRule.onNodeWithTag(MAP_TEST_TAG).getUnclippedBoundsInRoot()

        tabRowBounds.top shouldBe layoutBounds.top
        tabRowBounds.height shouldBeGreaterThan 0.dp
        mapBounds.top shouldBe tabRowBounds.bottom
    }

    @Test
    fun `TC-DIARY-MAP-FEATURE-019 지도가 전환 컨트롤을 뺀 나머지 영역을 채운다`() {
        setDiaryMapCardLayout()

        val layoutBounds = composeRule.onNodeWithTag(LAYOUT_TEST_TAG).getUnclippedBoundsInRoot()
        val mapBounds = composeRule.onNodeWithTag(MAP_TEST_TAG).getUnclippedBoundsInRoot()

        mapBounds.left shouldBe layoutBounds.left
        mapBounds.right shouldBe layoutBounds.right
        mapBounds.bottom shouldBe layoutBounds.bottom
        mapBounds.height shouldBeGreaterThan 0.dp
    }

    @Test
    fun `TC-DIARY-MAP-FEATURE-018 겹치지 않는 배치에서 전환 컨트롤로 제공자를 선택한다`() {
        setDiaryMapCardLayout()

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).performClick()

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertIsNotSelected()
    }

    @Test
    fun `제공자를 전환해도 지도 영역은 그대로 유지된다`() {
        setDiaryMapCardLayout()
        val beforeBounds = composeRule.onNodeWithTag(MAP_TEST_TAG).getUnclippedBoundsInRoot()

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(MAP_TEST_TAG).getUnclippedBoundsInRoot() shouldBe beforeBounds
    }

    private fun setDiaryMapCardLayout() {
        composeRule.setContent {
            DiaryTheme {
                DiaryMapCardLayout(
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
