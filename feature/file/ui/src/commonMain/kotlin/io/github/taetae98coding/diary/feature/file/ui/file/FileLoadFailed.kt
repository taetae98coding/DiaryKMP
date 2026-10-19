package io.github.taetae98coding.diary.feature.file.ui.file

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
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_home_retry
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileLoadFailed(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(DiaryTheme.dimens.screenPaddingValues),
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            color = DiaryTheme.colorScheme.onSurfaceVariant,
            style = DiaryTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(onClick = onRetry) {
            Text(text = stringResource(Res.string.file_home_retry))
        }
    }
}

@ComponentPreview
@Composable
private fun FileLoadFailedPreview() {
    DiaryTheme {
        Surface {
            FileLoadFailed(
                message = "파일을 불러오지 못했습니다",
                onRetry = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
