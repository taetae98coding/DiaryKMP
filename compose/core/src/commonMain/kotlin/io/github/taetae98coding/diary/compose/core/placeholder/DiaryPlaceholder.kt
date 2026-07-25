package io.github.taetae98coding.diary.compose.core.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryPlaceholder(
    icon: @Composable () -> Unit,
    message: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(DiaryTheme.dimens.screenPaddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(
                space = DiaryTheme.dimens.itemSpacing,
                alignment = Alignment.CenterVertically,
            ),
    ) {
        CompositionLocalProvider(LocalContentColor provides DiaryTheme.colorScheme.onSurfaceVariant) {
            Box(
                modifier =
                    Modifier
                        .size(DiaryPlaceholderDefaults.ContainerSize)
                        .background(
                            color = DiaryTheme.colorScheme.surfaceContainerHighest,
                            shape = DiaryPlaceholderDefaults.containerShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
            ProvideTextStyle(value = DiaryTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center)) {
                message()
            }
        }
    }
}

@ScreenPreview
@Composable
private fun DiaryPlaceholderPreview() {
    DiaryTheme {
        Surface {
            DiaryPlaceholder(
                icon = { MemoIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                message = { Text(text = "메모를 선택하세요") },
            )
        }
    }
}
