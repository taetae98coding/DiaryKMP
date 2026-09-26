package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.daysUntil
import kotlin.math.roundToInt

public const val TIMETABLE_ALL_DAY_TEST_TAG: String = "TimetableAllDay"

@Composable
internal fun TimetableAllDay(
    dateRange: LocalDateRange,
    modifier: Modifier = Modifier,
    itemList: List<TimetablePlacedAllDayItem> = emptyList(),
) {
    val spacing = DiaryTheme.dimens.calendarItemSpacing

    Row(modifier = modifier.testTag(TIMETABLE_ALL_DAY_TEST_TAG)) {
        Spacer(modifier = Modifier.width(TimetableDefaults.TimeColumnWidth))
        Layout(
            content = {
                itemList.forEach { placed ->
                    key(placed.item.key) { placed.item.content() }
                }
            },
            modifier =
                Modifier
                    .weight(1F)
                    .heightIn(max = TimetableDefaults.AllDayMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = spacing),
        ) { measurables, constraints ->
            val spacingPx = spacing.roundToPx()
            val dayCount = dateRange.count()
            val dayWidth = constraints.maxWidth.toFloat() / dayCount
            val horizontalBoundList =
                itemList.map { placed ->
                    val startIndex = dateRange.start.daysUntil(placed.dateRange.start)
                    val endIndex = dateRange.start.daysUntil(placed.dateRange.endInclusive) + 1
                    val left = (dayWidth * startIndex).roundToInt() + edgeInset(index = startIndex, dayCount = dayCount, spacing = spacingPx)
                    val right = (dayWidth * endIndex).roundToInt() - edgeInset(index = endIndex, dayCount = dayCount, spacing = spacingPx)

                    left to right
                }
            val placeableList =
                itemList.indices.map { index ->
                    val (left, right) = horizontalBoundList[index]

                    measurables[index].measure(Constraints.fixedWidth((right - left).coerceAtLeast(0)))
                }
            val rowCount = (itemList.maxOfOrNull { it.row } ?: -1) + 1
            val rowHeightList =
                List(rowCount) { row ->
                    itemList.indices
                        .filter { index -> itemList[index].row == row }
                        .maxOf { index -> placeableList[index].height }
                }
            val rowTopList = rowHeightList.runningFold(0) { top, height -> top + height + spacingPx }
            val height = (rowTopList.lastOrNull() ?: 0) - if (rowCount > 0) spacingPx else 0

            layout(width = constraints.maxWidth, height = height.coerceAtLeast(0)) {
                itemList.forEachIndexed { index, placed ->
                    placeableList[index].place(
                        x = horizontalBoundList[index].first,
                        y = rowTopList[placed.row],
                    )
                }
            }
        }
    }
}

private fun edgeInset(
    index: Int,
    dayCount: Int,
    spacing: Int,
): Int = if (index == 0 || index == dayCount) spacing else spacing / 2

@ComponentPreview
@Composable
private fun TimetableAllDayPreview() {
    val dateRange = remember { LocalDate(year = 2026, month = 7, day = 19)..LocalDate(year = 2026, month = 7, day = 25) }
    val itemList =
        remember {
            listOf(
                TimetableAllDayItem(dateRange = dateRange.start..dateRange.start, key = "휴가") { Text(text = "휴가") }
                    .let { item -> TimetablePlacedAllDayItem(item = item, dateRange = item.dateRange, row = 0) },
            )
        }

    DiaryTheme {
        Surface {
            TimetableAllDay(
                dateRange = dateRange,
                modifier = Modifier.fillMaxWidth(),
                itemList = itemList,
            )
        }
    }
}
