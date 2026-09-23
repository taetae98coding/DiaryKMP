package io.github.taetae98coding.diary.feature.holiday.ui.home

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_ERROR_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_RETRY_LABEL
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.february
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.holiday
import io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday.GoldenHolidayYear
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HolidayHomeScaffoldInsetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `황금연휴 목록은 시스템 내비게이션 바 뒤까지 그린다`() {
        setHolidayHomeScaffold(uiState = loadedUiState())

        goldenHolidayListBounds().bottom shouldBe rootBounds().bottom
    }

    @Test
    fun `황금연휴 목록을 끝까지 스크롤하면 마지막 카드 아래에 화면 세로 여백과 시스템 내비게이션 바 높이만큼 비운다`() {
        setHolidayHomeScaffold(uiState = loadedUiState())

        // 지그재그 격자는 마지막 항목으로 이동해도 끝까지 내려가지 않을 수 있어 남은 거리보다 크게 더 스크롤한다.
        goldenHolidayList().performScrollToIndex(GROUP_COUNT - 1)
        goldenHolidayList().performSemanticsAction(SemanticsActions.ScrollBy) { scrollBy -> scrollBy(0F, Float.MAX_VALUE) }

        val lastCardBottom =
            goldenHolidayList()
                .fetchSemanticsNode()
                .children
                .maxOf { card -> card.boundsInRoot.bottom }
        val expectedGap = with(composeRule.density) { (SCREEN_VERTICAL_PADDING + NAVIGATION_BAR_HEIGHT).toPx() }

        goldenHolidayListBounds().bottom - lastCardBottom shouldBe (expectedGap plusOrMinus PIXEL_TOLERANCE)
    }

    @Test
    fun `오류 안내와 재시도 버튼은 시스템 내비게이션 바를 피한 본문 가운데에 놓인다`() {
        setHolidayHomeScaffold(uiState = HolidayHomeYearUiState.Error)

        val pagerTop =
            composeRule
                .onNode(yearPagerMatcher)
                .fetchSemanticsNode()
                .boundsInRoot.top
        val bodyBottom = rootBounds().bottom - with(composeRule.density) { NAVIGATION_BAR_HEIGHT.toPx() }
        val errorTop = composeRule.onNodeWithText(DEFAULT_ERROR_DESCRIPTION).getBoundsInRoot().top
        val retryBottom = composeRule.onNodeWithText(DEFAULT_RETRY_LABEL).getBoundsInRoot().bottom

        with(composeRule.density) {
            (retryBottom.toPx() <= bodyBottom) shouldBe true
            (errorTop.toPx() + retryBottom.toPx()) / 2 shouldBe ((pagerTop + bodyBottom) / 2 plusOrMinus PIXEL_TOLERANCE)
        }
    }

    private fun setHolidayHomeScaffold(uiState: HolidayHomeYearUiState) {
        lateinit var view: View

        composeRule.setContent {
            view = LocalView.current

            DiaryTheme {
                HolidayHomeScaffold(
                    onEvent = {},
                    state = rememberHolidayHomeScaffoldState(initialYear = YEAR),
                    yearContent = { _, contentPadding ->
                        GoldenHolidayYear(
                            uiStateProvider = { uiState },
                            onEvent = {},
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = contentPadding,
                        )
                    },
                )
            }
        }
        composeRule.runOnIdle {
            val navigationBarHeight = with(composeRule.density) { NAVIGATION_BAR_HEIGHT.roundToPx() }
            val insets =
                WindowInsetsCompat
                    .Builder()
                    .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, navigationBarHeight))
                    .build()

            ViewCompat.dispatchApplyWindowInsets(view, insets)
        }
        composeRule.waitForIdle()
    }

    private fun loadedUiState(): HolidayHomeYearUiState.Loaded =
        HolidayHomeYearUiState.Loaded(
            goldenHolidayGroupList =
                List(GROUP_COUNT) { index ->
                    val start = february(day = index * 3 + 1)

                    goldenHolidayGroup(
                        optionList =
                            listOf(
                                goldenHoliday(
                                    holidayList = listOf(holiday(name = "$HOLIDAY_NAME_PREFIX$index", start = start)),
                                    start = start,
                                    endInclusive = start,
                                ),
                            ),
                    )
                },
        )

    // 주별 날짜 안에도 세로 스크롤 노드가 있으므로 트리에서 가장 바깥인 첫 노드가 황금연휴 목록이다.
    private fun goldenHolidayList(): SemanticsNodeInteraction = composeRule.onAllNodes(goldenHolidayListMatcher).onFirst()

    private fun goldenHolidayListBounds(): Rect = goldenHolidayList().fetchSemanticsNode().boundsInRoot

    private fun rootBounds(): Rect = composeRule.onRoot().fetchSemanticsNode().boundsInRoot

    private companion object {
        private const val GROUP_COUNT = 8
        private const val HOLIDAY_NAME_PREFIX = "공휴일"
        private const val PIXEL_TOLERANCE = 1F

        private val NAVIGATION_BAR_HEIGHT = 48.dp
        private val SCREEN_VERTICAL_PADDING = 16.dp

        private val goldenHolidayListMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        private val yearPagerMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange)
    }
}
