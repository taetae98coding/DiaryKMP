package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class DiaryRefreshableEmptyBoxTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `영역을 채우는 내용은 Crossfade 안에 있어도 영역 전체를 채운다`() {
        setEmptyBox {
            DiaryCrossfade(targetState = Unit) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .testTag(CONTENT_TAG),
                )
            }
        }

        val root = composeRule.onRoot().getUnclippedBoundsInRoot()
        val content = composeRule.onNodeWithTag(CONTENT_TAG).getUnclippedBoundsInRoot()

        content.top shouldBe root.top
        content.bottom shouldBe root.bottom
    }

    @Test
    fun `크기가 정해진 내용은 Crossfade 안에 있어도 영역의 가로세로 가운데에 놓인다`() {
        setEmptyBox {
            DiaryCrossfade(targetState = Unit) {
                Box(
                    modifier =
                        Modifier
                            .size(CONTENT_SIZE)
                            .testTag(CONTENT_TAG),
                )
            }
        }

        val root = composeRule.onRoot().getUnclippedBoundsInRoot()
        val content = composeRule.onNodeWithTag(CONTENT_TAG).getUnclippedBoundsInRoot()

        // 홀수 픽셀 폭에서는 가운데가 반 픽셀 단위로 반올림되므로 1dp 안에서 비교한다.
        content.centerX().value shouldBe (root.centerX().value plusOrMinus CENTER_TOLERANCE.value)
        content.centerY().value shouldBe (root.centerY().value plusOrMinus CENTER_TOLERANCE.value)
    }

    @Test
    fun `스크롤할 내용이 없어도 아래로 당기면 새로고침을 한 번 요청한다`() {
        var refreshCount = 0
        setEmptyBox(onRefresh = { refreshCount++ }) {
            Box(modifier = Modifier.size(CONTENT_SIZE))
        }

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        refreshCount shouldBe 1
    }

    private fun setEmptyBox(
        onRefresh: () -> Unit = {},
        content: @Composable () -> Unit,
    ) {
        composeRule.setContent {
            DiaryTheme {
                DiaryRefreshableEmptyBox(
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    content()
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun DpRect.centerX(): Dp = (left + right) / 2

    private fun DpRect.centerY(): Dp = (top + bottom) / 2

    private companion object {
        const val CONTENT_TAG = "Content"
        val CONTENT_SIZE = 100.dp
        val CENTER_TOLERANCE = 1.dp
    }
}
