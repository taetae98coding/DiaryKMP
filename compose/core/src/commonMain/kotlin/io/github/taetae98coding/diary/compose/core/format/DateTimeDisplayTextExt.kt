package io.github.taetae98coding.diary.compose.core.format

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.date_display_format
import io.github.taetae98coding.diary.compose.core.date_display_month_names
import io.github.taetae98coding.diary.compose.core.time_display_am
import io.github.taetae98coding.diary.compose.core.time_display_format
import io.github.taetae98coding.diary.compose.core.time_display_pm
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

private const val NOON_HOUR = 12
private const val MINUTE_LENGTH = 2

@Composable
public fun LocalDate.toDisplayText(): String {
    // 웹에서는 리소스를 비동기로 로드해 첫 컴포지션의 월 이름 배열이 비어 있다.
    val monthName = stringArrayResource(Res.array.date_display_month_names).getOrNull(month.number - 1) ?: return ""

    return stringResource(
        Res.string.date_display_format,
        year.toString(),
        monthName,
        day.toString(),
    )
}

@Composable
public fun LocalTime.toDisplayText(): String {
    val amPmText =
        if (isBeforeNoon()) {
            stringResource(Res.string.time_display_am)
        } else {
            stringResource(Res.string.time_display_pm)
        }

    return stringResource(
        Res.string.time_display_format,
        displayHour().toString(),
        minute.toString().padStart(MINUTE_LENGTH, '0'),
        amPmText,
    )
}

internal fun LocalTime.isBeforeNoon(): Boolean = hour < NOON_HOUR

internal fun LocalTime.displayHour(): Int =
    when {
        hour == 0 -> NOON_HOUR
        hour > NOON_HOUR -> hour - NOON_HOUR
        else -> hour
    }
