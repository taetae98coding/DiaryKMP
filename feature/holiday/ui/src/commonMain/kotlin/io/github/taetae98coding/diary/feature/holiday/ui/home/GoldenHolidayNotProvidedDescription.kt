package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.holiday.ui.Res
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_not_provided_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoldenHolidayNotProvidedDescription(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(DiaryTheme.dimens.screenPaddingValues),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.holiday_not_provided_description),
            style = DiaryTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@ScreenPreview
@Composable
private fun GoldenHolidayNotProvidedDescriptionPreview() {
    DiaryTheme {
        Surface {
            GoldenHolidayNotProvidedDescription(modifier = Modifier.fillMaxSize())
        }
    }
}
