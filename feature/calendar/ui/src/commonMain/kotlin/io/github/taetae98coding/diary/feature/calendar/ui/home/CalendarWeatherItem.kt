package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridGroupScope
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.model.weather.WeatherCondition
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarWeather

internal const val CALENDAR_HOME_WEATHER_ICONS_TEST_TAG = "CalendarHomeWeatherIcons"

internal fun CalendarWeekOfMonthGridGroupScope.weatherItem(
    onWeatherClick: () -> Unit,
    weatherProvider: () -> List<CalendarWeather>,
) {
    weatherProvider().forEach { weather ->
        item(
            dateRange = weather.date..weather.date,
            key = weather.date,
        ) {
            CalendarWeatherItem(
                weather = weather,
                onClick = onWeatherClick,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
internal fun CalendarWeatherItem(
    weather: CalendarWeather,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag(CALENDAR_HOME_WEATHER_ICONS_TEST_TAG)
                    .horizontalScroll(state = rememberScrollState()),
            horizontalArrangement =
                Arrangement.spacedBy(
                    space = 2.dp,
                    alignment = Alignment.CenterHorizontally,
                ),
        ) {
            weather.weatherList
                .consecutiveDistinctConditionList()
                .forEach { condition ->
                    WeatherConditionImage(condition = condition)
                }
        }
        weather.temperature.toText()?.let { temperature ->
            Text(
                text = temperature,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                style = DiaryTheme.typography.labelSmall.copy(fontSize = 10.sp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun List<Weather>.consecutiveDistinctConditionList(): List<WeatherCondition> =
    buildList {
        this@consecutiveDistinctConditionList.forEach { weather ->
            weather.conditionList.firstOrNull()?.let { condition ->
                if (lastOrNull()?.imageUrl != condition.imageUrl) {
                    add(condition)
                }
            }
        }
    }

@ComponentPreview
@Composable
private fun CalendarWeatherItemPreview() {
    val weather = remember { previewCalendarWeather() }

    DiaryTheme {
        Surface {
            CalendarWeatherItem(
                weather = weather,
                onClick = {},
            )
        }
    }
}
