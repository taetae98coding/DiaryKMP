package io.github.taetae98coding.diary.feature.calendar.ui.home.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import io.github.taetae98coding.diary.compose.core.icon.WeatherIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.weather.WeatherCondition
import io.github.taetae98coding.diary.feature.calendar.ui.previewWeatherCondition

@Composable
internal fun WeatherConditionImage(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
) {
    val painter = rememberAsyncImagePainter(model = condition.imageUrl)
    val state by painter.state.collectAsStateWithLifecycle()

    Box(
        modifier =
            modifier
                .size(CalendarWeatherItemDefaults.IconSize)
                .semantics { contentDescription = condition.description },
    ) {
        if (state is AsyncImagePainter.State.Success) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            WeatherIcon(modifier = Modifier.fillMaxSize())
        }
    }
}

@ComponentPreview
@Composable
private fun WeatherConditionImagePreview() {
    val condition = remember { previewWeatherCondition() }

    DiaryTheme {
        Surface {
            WeatherConditionImage(condition = condition)
        }
    }
}
