package io.github.taetae98coding.diary.feature.search.ui.home

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SearchHomeScaffoldInsetTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var view: View

    @Test
    fun `소프트 키보드가 열리면 결과 영역은 키보드 위에서 끝난다`() {
        setSearchHomeScaffold()

        dispatchInsets(imeHeight = IME_HEIGHT)

        resultBounds().bottom shouldBe (rootBounds().bottom - IME_HEIGHT.toPx() plusOrMinus PIXEL_TOLERANCE)
    }

    @Test
    fun `소프트 키보드가 열려도 유형 탭 행과 결과 영역의 위쪽은 자리를 옮기지 않는다`() {
        setSearchHomeScaffold()
        dispatchInsets(imeHeight = 0.dp)
        val tabRowTop = tabRowBounds().top
        val resultTop = resultBounds().top

        dispatchInsets(imeHeight = IME_HEIGHT)

        tabRowBounds().top shouldBe tabRowTop
        resultBounds().top shouldBe resultTop
    }

    @Test
    fun `소프트 키보드가 닫히면 결과 영역은 시스템 내비게이션 바 위까지 되돌아간다`() {
        setSearchHomeScaffold()
        dispatchInsets(imeHeight = IME_HEIGHT)

        dispatchInsets(imeHeight = 0.dp)

        resultBounds().bottom shouldBe (rootBounds().bottom - NAVIGATION_BAR_HEIGHT.toPx() plusOrMinus PIXEL_TOLERANCE)
    }

    private fun setSearchHomeScaffold() {
        composeRule.setContent {
            view = LocalView.current

            DiaryTheme {
                SearchHomeScaffold(onEvent = {}) {
                    Box(modifier = Modifier.fillMaxSize().testTag(RESULT_TAG))
                }
            }
        }
    }

    private fun dispatchInsets(imeHeight: Dp) {
        composeRule.runOnIdle {
            val insets =
                WindowInsetsCompat
                    .Builder()
                    .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, NAVIGATION_BAR_HEIGHT.roundToPx()))
                    .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, imeHeight.roundToPx()))
                    .setVisible(WindowInsetsCompat.Type.ime(), imeHeight > 0.dp)
                    .build()

            ViewCompat.dispatchApplyWindowInsets(view.rootView, insets)
        }
        composeRule.waitForIdle()
    }

    private fun resultBounds(): Rect = composeRule.onNodeWithTag(RESULT_TAG).fetchSemanticsNode().boundsInRoot

    private fun tabRowBounds(): Rect = composeRule.onNodeWithContentDescription(DEFAULT_TAB_ROW_DESCRIPTION).fetchSemanticsNode().boundsInRoot

    private fun rootBounds(): Rect = composeRule.onRoot().fetchSemanticsNode().boundsInRoot

    private fun Dp.toPx(): Float = with(composeRule.density) { toPx() }

    private fun Dp.roundToPx(): Int = with(composeRule.density) { roundToPx() }

    private companion object {
        private const val RESULT_TAG = "result"
        private const val DEFAULT_TAB_ROW_DESCRIPTION = "Search result type"
        private const val PIXEL_TOLERANCE = 1F

        private val NAVIGATION_BAR_HEIGHT = 48.dp
        private val IME_HEIGHT = 300.dp
    }
}
