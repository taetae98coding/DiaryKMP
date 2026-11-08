package io.github.taetae98coding.diary.compose.dday

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.core.model.dday.DDayCount
import io.github.taetae98coding.diary.core.model.dday.DDayPeriod
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
public fun DDayCount.toCardText(): String =
    when (this) {
        is DDayCount.Upcoming -> {
            withPeriod(
                text = stringResource(Res.string.dday_card_upcoming, remainingDays.toString()),
                period = period,
            )
        }

        is DDayCount.Today -> {
            stringResource(Res.string.dday_card_today)
        }

        is DDayCount.Passed -> {
            withPeriod(
                text = pluralStringResource(Res.plurals.dday_card_passed, passedDays, passedDays.toString()),
                period = period,
            )
        }
    }

@Composable
private fun withPeriod(
    text: String,
    period: DDayPeriod,
): String {
    val periodText = period.toPeriodText() ?: return text

    return stringResource(Res.string.dday_card_with_period, text, periodText)
}

@Composable
private fun DDayPeriod.toPeriodText(): String? {
    val yearText = if (years > 0) pluralStringResource(Res.plurals.dday_card_period_year, years, years.toString()) else null
    val monthText = if (months > 0) pluralStringResource(Res.plurals.dday_card_period_month, months, months.toString()) else null

    return if (yearText != null && monthText != null) {
        stringResource(Res.string.dday_card_period_year_month, yearText, monthText)
    } else {
        yearText ?: monthText
    }
}
