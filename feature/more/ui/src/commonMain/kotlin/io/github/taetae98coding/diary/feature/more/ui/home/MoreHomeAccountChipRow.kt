package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.chip.DiaryAssistChip
import io.github.taetae98coding.diary.compose.core.icon.SignInIcon
import io.github.taetae98coding.diary.compose.core.icon.SignOutIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.more.ui.Res
import io.github.taetae98coding.diary.feature.more.ui.more_sign_in_chip_label
import io.github.taetae98coding.diary.feature.more.ui.more_sign_out_chip_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MoreHomeAccountChipRow(
    onEvent: (MoreHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MoreHomeAccountUiState = { MoreHomeAccountUiState.Loading },
) {
    Row(modifier = modifier) {
        when (uiStateProvider()) {
            is MoreHomeAccountUiState.Loading -> {
                DiaryAssistChip(
                    onClick = {},
                    label = "",
                    modifier = Modifier.width(80.dp),
                )
            }

            is MoreHomeAccountUiState.Guest -> {
                DiaryAssistChip(
                    onClick = { onEvent(MoreHomeScaffoldEvent.ClickSignIn) },
                    label = stringResource(Res.string.more_sign_in_chip_label),
                    leadingIcon = { SignInIcon() },
                )
            }

            is MoreHomeAccountUiState.User -> {
                DiaryAssistChip(
                    onClick = { onEvent(MoreHomeScaffoldEvent.ClickSignOut) },
                    label = stringResource(Res.string.more_sign_out_chip_label),
                    leadingIcon = { SignOutIcon() },
                )
            }
        }
    }
}

@ComponentPreview
@Composable
private fun MoreHomeAccountChipRowPreview(
    @PreviewParameter(MoreHomeAccountUiStatePreviewParameter::class) uiState: MoreHomeAccountUiState,
) {
    DiaryTheme {
        Surface {
            MoreHomeAccountChipRow(
                onEvent = {},
                uiStateProvider = { uiState },
            )
        }
    }
}
