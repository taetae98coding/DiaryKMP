package io.github.taetae98coding.diary.feature.more.ui.home.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.more.ui.home.MoreHomeScaffoldEvent

@Composable
internal fun MoreHomeAccountCard(
    onEvent: (MoreHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MoreHomeAccountUiState = { MoreHomeAccountUiState.Loading },
) {
    Card(modifier = modifier) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(MoreHomeAccountCardDefaults.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(MoreHomeAccountCardDefaults.ProfileToActionSpacing),
        ) {
            MoreHomeProfileRow(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
            )
            MoreHomeAccountChipRow(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
            )
        }
    }
}

@ComponentPreview
@Composable
private fun MoreHomeAccountCardPreview(
    @PreviewParameter(MoreHomeAccountUiStatePreviewParameter::class) uiState: MoreHomeAccountUiState,
) {
    DiaryTheme {
        MoreHomeAccountCard(
            onEvent = {},
            uiStateProvider = { uiState },
        )
    }
}
