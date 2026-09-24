package io.github.taetae98coding.diary.feature.setting.ui.holiday

import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
class SettingHolidayScaffoldInsetTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var view: View

    @Test
    fun `소프트 키보드가 열리면 공휴일 목록은 키보드 위에서 끝난다`() {
        setSettingHolidayScaffold()

        dispatchInsets(imeHeight = IME_HEIGHT)

        listBounds().bottom shouldBe (rootBounds().bottom - IME_HEIGHT.toPx() plusOrMinus PIXEL_TOLERANCE)
    }

    @Test
    fun `소프트 키보드가 열리면 일괄 선택 동작 버튼은 본문 아래쪽과의 간격을 유지한 채 키보드 위로 올라간다`() {
        setSettingHolidayScaffold()
        dispatchInsets(imeHeight = 0.dp)
        val bulkActionBottom = bulkActionBounds().bottom

        dispatchInsets(imeHeight = IME_HEIGHT)

        bulkActionBottom - bulkActionBounds().bottom shouldBe ((IME_HEIGHT - NAVIGATION_BAR_HEIGHT).toPx() plusOrMinus PIXEL_TOLERANCE)
    }

    @Test
    fun `소프트 키보드가 열려도 상단 바와 목록의 위쪽은 자리를 옮기지 않는다`() {
        setSettingHolidayScaffold()
        dispatchInsets(imeHeight = 0.dp)
        val navigateUpTop = navigateUpBounds().top
        val listTop = listBounds().top

        dispatchInsets(imeHeight = IME_HEIGHT)

        navigateUpBounds().top shouldBe navigateUpTop
        listBounds().top shouldBe listTop
    }

    @Test
    fun `소프트 키보드가 닫히면 공휴일 목록은 시스템 내비게이션 바 위까지 되돌아간다`() {
        setSettingHolidayScaffold()
        dispatchInsets(imeHeight = IME_HEIGHT)

        dispatchInsets(imeHeight = 0.dp)

        listBounds().bottom shouldBe (rootBounds().bottom - NAVIGATION_BAR_HEIGHT.toPx() plusOrMinus PIXEL_TOLERANCE)
    }

    private fun setSettingHolidayScaffold() {
        val holidaySettingList = List(HOLIDAY_COUNT) { holidaySetting(isHoliday = true, isVisible = true) }

        composeRule.setContent {
            view = LocalView.current

            DiaryTheme {
                SettingHolidayScaffold(
                    onEvent = {},
                    uiStateProvider = { SettingHolidayUiState.Loaded(holidaySettingList = holidaySettingList) },
                )
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

    private fun listBounds(): Rect = composeRule.onNode(holidayListMatcher).fetchSemanticsNode().boundsInRoot

    private fun bulkActionBounds(): Rect = composeRule.onNodeWithContentDescription(DEFAULT_BULK_ACTION_DESCRIPTION).fetchSemanticsNode().boundsInRoot

    private fun navigateUpBounds(): Rect = composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).fetchSemanticsNode().boundsInRoot

    private fun rootBounds(): Rect = composeRule.onRoot().fetchSemanticsNode().boundsInRoot

    private fun Dp.toPx(): Float = with(composeRule.density) { toPx() }

    private fun Dp.roundToPx(): Int = with(composeRule.density) { roundToPx() }

    private companion object {
        private const val HOLIDAY_COUNT = 20
        private const val PIXEL_TOLERANCE = 1F

        private val NAVIGATION_BAR_HEIGHT = 48.dp
        private val IME_HEIGHT = 200.dp

        private val holidayListMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
    }
}
