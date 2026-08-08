package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.holiday.ui.Res
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_error_description
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_retry
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoldenHolidayErrorDescription(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(DiaryTheme.dimens.screenPaddingValues),
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.holiday_error_description),
            style = DiaryTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(onClick = onRetry) {
            Text(text = stringResource(Res.string.holiday_retry))
        }
    }
}

@ScreenPreview
@Composable
private fun GoldenHolidayErrorDescriptionPreview() {
    DiaryTheme {
        Surface {
            GoldenHolidayErrorDescription(
                onRetry = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
