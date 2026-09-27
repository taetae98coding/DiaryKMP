package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

internal object CalendarHomeTestFixture {
    const val DROP_DOWN_DESCRIPTION = "Show date picker"
    const val DROP_UP_DESCRIPTION = "Hide date picker"
    const val NEXT_MONTH_DESCRIPTION = "Change to next month"
    const val TODAY_BUTTON_DESCRIPTION = "Go to today"
    const val DEFAULT_CONFIRM = "OK"
    const val DEFAULT_CANCEL = "Cancel"
    const val KOREAN_CONFIRM = "확인"
    const val KOREAN_CANCEL = "취소"

    fun koreanTitle(yearMonth: YearMonth): String = "${yearMonth.year}년 ${yearMonth.month.number}월"

    fun englishTitle(yearMonth: YearMonth): String = "${englishMonthTitle(yearMonth)} ${yearMonth.year}"

    fun dayCellText(
        yearMonth: YearMonth,
        day: Int,
    ): String = "${englishMonthTitle(yearMonth)} $day, ${yearMonth.year}"

    // 상단 바 오늘 버튼도 일 숫자를 표시하므로 날짜 칸의 접근성 이름을 함께 가진 날짜 숫자만 대상으로 삼는다.
    fun dateCell(day: Int): SemanticsMatcher =
        hasText(day.toString()).and(
            SemanticsMatcher("date cell of day $day") { node ->
                node.config
                    .getOrNull(SemanticsProperties.ContentDescription)
                    .orEmpty()
                    .any { description -> description.endsWith(" $day") }
            },
        )

    private fun englishMonthTitle(yearMonth: YearMonth): String =
        yearMonth.month.name
            .lowercase()
            .replaceFirstChar(Char::titlecase)
}
