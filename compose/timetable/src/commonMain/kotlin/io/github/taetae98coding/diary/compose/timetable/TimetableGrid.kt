package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

public const val TIMETABLE_NOW_INDICATOR_TEST_TAG: String = "TimetableNowIndicator"

@Composable
internal fun TimetableGrid(
    page: Int,
    dateRange: LocalDateRange,
    modifier: Modifier = Modifier,
    state: TimetableState = rememberTimetableState(),
    nowProvider: () -> LocalDateTime? = { null },
    timeItemList: List<TimetableTimeItem> = emptyList(),
) {
    val scrollState = rememberTimetableScrollState(page = page, state = state)
    val lineColor = DiaryTheme.colorScheme.outlineVariant
    val dayHeight = TimetableDefaults.HourHeight * HOURS_PER_DAY

    Row(modifier = modifier.verticalScroll(scrollState)) {
        TimetableTimeColumn(
            modifier =
                Modifier
                    .width(TimetableDefaults.TimeColumnWidth)
                    .height(dayHeight),
        )
        Row(
            modifier =
                Modifier
                    .weight(1F)
                    .height(dayHeight)
                    .drawBehind {
                        val hourHeight = size.height / HOURS_PER_DAY
                        val dayWidth = size.width / dateRange.count()

                        for (hour in 0 until HOURS_PER_DAY) {
                            val y = hourHeight * hour
                            drawLine(color = lineColor, start = Offset(0F, y), end = Offset(size.width, y))
                        }
                        for (index in 0 until dateRange.count()) {
                            val x = dayWidth * index
                            drawLine(color = lineColor, start = Offset(x, 0F), end = Offset(x, size.height))
                        }
                    },
        ) {
            dateRange.forEach { date ->
                TimetableDayColumn(
                    date = date,
                    modifier =
                        Modifier
                            .weight(1F)
                            .fillMaxHeight(),
                    nowProvider = nowProvider,
                    timeItemList = timeItemList,
                )
            }
        }
    }
}

@Composable
private fun rememberTimetableScrollState(
    page: Int,
    state: TimetableState,
): ScrollState {
    val density = LocalDensity.current
    val scrollState =
        remember(state) {
            ScrollState(
                initial =
                    state.verticalScrollOffset
                        ?: with(density) { (TimetableDefaults.HourHeight * INITIAL_SCROLL_HOUR).roundToPx() },
            )
        }

    LaunchedEffect(state, scrollState, page) {
        snapshotFlow { scrollState.value }
            .collect { value ->
                if (state.pagerState.currentPage == page) {
                    state.verticalScrollOffset = value
                }
            }
    }

    return scrollState
}

@Composable
private fun TimetableTimeColumn(modifier: Modifier = Modifier) {
    val color = DiaryTheme.colorScheme.onSurfaceVariant
    val style = DiaryTheme.typography.labelSmall

    Layout(
        content = {
            for (hour in 1 until HOURS_PER_DAY) {
                Text(
                    text = hourLabel(hour = hour),
                    color = color,
                    style = style,
                    maxLines = 1,
                )
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val endPadding = TimetableDefaults.TimeLabelEndPadding.roundToPx()
        val labelConstraints = Constraints(maxWidth = (constraints.maxWidth - endPadding).coerceAtLeast(0))
        val placeableList = measurables.map { it.measure(labelConstraints) }
        val hourHeight = constraints.maxHeight.toFloat() / HOURS_PER_DAY

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            placeableList.forEachIndexed { index, placeable ->
                val hour = index + 1

                placeable.place(
                    x = constraints.maxWidth - endPadding - placeable.width,
                    y = (hourHeight * hour).roundToInt() - placeable.height / 2,
                )
            }
        }
    }
}

@Composable
private fun hourLabel(hour: Int): String =
    when {
        hour < NOON_HOUR -> stringResource(Res.string.timetable_hour_am, hour)
        hour == NOON_HOUR -> stringResource(Res.string.timetable_hour_pm, hour)
        else -> stringResource(Res.string.timetable_hour_pm, hour - NOON_HOUR)
    }

@Composable
private fun TimetableDayColumn(
    date: LocalDate,
    modifier: Modifier = Modifier,
    nowProvider: () -> LocalDateTime? = { null },
    timeItemList: List<TimetableTimeItem> = emptyList(),
) {
    val placedList = remember(timeItemList, date) { timeItemList.placeOn(date = date) }
    val currentNowProvider by rememberUpdatedState(nowProvider)
    val isNowDate by remember(date) { derivedStateOf { currentNowProvider()?.date == date } }

    Layout(
        content = {
            placedList.forEach { placed ->
                key(placed.item.key) { placed.item.content() }
            }
            if (isNowDate) {
                TimetableNowIndicator(modifier = Modifier.testTag(TIMETABLE_NOW_INDICATOR_TEST_TAG))
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val horizontalSpacing = TimetableDefaults.TimeItemHorizontalSpacing.roundToPx()
        val verticalSpacing = TimetableDefaults.TimeItemVerticalSpacing.roundToPx()
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val minuteHeight = height.toFloat() / MINUTES_PER_DAY
        val itemPlaceableList =
            placedList.mapIndexed { index, placed ->
                val columnWidth = width.toFloat() / placed.columnCount
                val itemWidth = (columnWidth - horizontalSpacing).roundToInt().coerceAtLeast(0)
                val itemHeight = ((placed.endMinute - placed.startMinute) * minuteHeight - verticalSpacing * 2).roundToInt().coerceAtLeast(0)
                val placeable = measurables[index].measure(Constraints.fixed(width = itemWidth, height = itemHeight))

                Triple(placeable, (columnWidth * placed.column).roundToInt(), (placed.startMinute * minuteHeight).roundToInt() + verticalSpacing)
            }
        val nowPlaceable =
            measurables
                .getOrNull(placedList.size)
                ?.measure(Constraints.fixed(width = width, height = TimetableDefaults.NowDotSize.roundToPx()))

        layout(width = width, height = height) {
            itemPlaceableList.forEach { (placeable, x, y) -> placeable.place(x = x, y = y) }
            nowPlaceable?.let { placeable ->
                val nowMinute = currentNowProvider()?.time?.minuteOfDay ?: 0

                placeable.place(x = 0, y = (nowMinute * minuteHeight).roundToInt() - placeable.height / 2)
            }
        }
    }
}

@Composable
private fun TimetableNowIndicator(modifier: Modifier = Modifier) {
    val color = DiaryTheme.colorScheme.primary

    Spacer(
        modifier =
            modifier.drawBehind {
                val centerY = size.height / 2

                drawLine(
                    color = color,
                    start = Offset(0F, centerY),
                    end = Offset(size.width, centerY),
                    strokeWidth = TimetableDefaults.NowLineThickness.toPx(),
                )
                drawCircle(
                    color = color,
                    radius = size.height / 2,
                    center = Offset(0F, centerY),
                )
            },
    )
}

private const val NOON_HOUR = 12

@ScreenPreview
@Composable
private fun TimetableGridPreview() {
    val date = remember { LocalDate(year = 2026, month = 7, day = 19) }
    val timeItemList =
        remember {
            listOf(
                TimetableTimeItem(date = date, startTime = LocalTime(hour = 9, minute = 0), endTime = LocalTime(hour = 10, minute = 30), key = "회의") {
                    Text(text = "회의")
                },
            )
        }

    DiaryTheme {
        Surface {
            TimetableGrid(
                page = 0,
                dateRange = date..date,
                modifier = Modifier.fillMaxSize(),
                nowProvider = { LocalDateTime(date = date, time = LocalTime(hour = 9, minute = 30)) },
                timeItemList = timeItemList,
            )
        }
    }
}
