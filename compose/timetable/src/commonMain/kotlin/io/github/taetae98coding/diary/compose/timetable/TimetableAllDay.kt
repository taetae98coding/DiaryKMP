package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.daysUntil
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

public const val TIMETABLE_ALL_DAY_TEST_TAG: String = "TimetableAllDay"

@Composable
internal fun TimetableAllDay(
    dateRange: LocalDateRange,
    modifier: Modifier = Modifier,
    selectState: TimetableSelectState = rememberTimetableSelectState(),
    itemList: List<TimetablePlacedAllDayItem> = emptyList(),
) {
    val spacing = DiaryTheme.dimens.calendarItemSpacing

    Layout(
        content = {
            itemList.forEach { placed ->
                key(placed.item.key) { placed.item.content() }
            }
            Text(
                text = stringResource(Res.string.timetable_all_day),
                color = DiaryTheme.colorScheme.onSurfaceVariant,
                style = DiaryTheme.typography.labelSmall,
                maxLines = 1,
            )
        },
        modifier =
            modifier
                .testTag(TIMETABLE_ALL_DAY_TEST_TAG)
                .timetableDateSelectBackground(
                    dateRange = dateRange,
                    startInset = TimetableDefaults.TimeColumnWidth,
                    state = selectState,
                    color = CalendarDefaults.selectBackgroundColor(),
                ),
    ) { measurables, constraints ->
        val spacingPx = spacing.roundToPx()
        val startInset = TimetableDefaults.TimeColumnWidth.roundToPx()
        val endPadding = TimetableDefaults.TimeLabelEndPadding.roundToPx()
        val width = constraints.maxWidth
        val dayAreaWidth = (width - startInset).coerceAtLeast(0)
        val horizontalBoundList = itemList.horizontalBoundList(dateRange = dateRange, width = dayAreaWidth, spacing = spacingPx)
        val placeableList =
            itemList.indices.map { index ->
                val (left, right) = horizontalBoundList[index]

                measurables[index].measure(Constraints.fixedWidth((right - left).coerceAtLeast(0)))
            }
        val labelPlaceable = measurables[itemList.size].measure(Constraints(maxWidth = (startInset - endPadding).coerceAtLeast(0)))
        val rowCount = (itemList.maxOfOrNull { it.row } ?: -1) + 1
        val rowHeightList =
            List(rowCount) { row ->
                itemList.indices
                    .filter { index -> itemList[index].row == row }
                    .maxOf { index -> placeableList[index].height }
            }
        val rowTopList = rowHeightList.runningFold(spacingPx) { top, height -> top + height + spacingPx }
        val height = rowTopList.last()
        val labelCenterY = spacingPx + (rowHeightList.firstOrNull() ?: 0) / 2

        layout(width = width, height = height) {
            itemList.forEachIndexed { index, placed ->
                placeableList[index].place(
                    x = startInset + horizontalBoundList[index].first,
                    y = rowTopList[placed.row],
                )
            }
            labelPlaceable.place(
                x = startInset - endPadding - labelPlaceable.width,
                y = labelCenterY - labelPlaceable.height / 2,
            )
        }
    }
}

private fun List<TimetablePlacedAllDayItem>.horizontalBoundList(
    dateRange: LocalDateRange,
    width: Int,
    spacing: Int,
): List<Pair<Int, Int>> {
    val dayCount = dateRange.count()
    val dayWidth = width.toFloat() / dayCount

    return map { placed ->
        val startIndex = dateRange.start.daysUntil(placed.dateRange.start)
        val endIndex = dateRange.start.daysUntil(placed.dateRange.endInclusive) + 1
        val left = (dayWidth * startIndex).roundToInt() + edgeInset(index = startIndex, dayCount = dayCount, spacing = spacing)
        val right = (dayWidth * endIndex).roundToInt() - edgeInset(index = endIndex, dayCount = dayCount, spacing = spacing)

        left to right
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
