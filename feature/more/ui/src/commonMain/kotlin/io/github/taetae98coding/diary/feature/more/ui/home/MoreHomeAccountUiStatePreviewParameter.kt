package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class MoreHomeAccountUiStatePreviewParameter : PreviewParameterProvider<MoreHomeAccountUiState> {
    override val values: Sequence<MoreHomeAccountUiState> =
        sequenceOf(
            MoreHomeAccountUiState.Loading,
            MoreHomeAccountUiState.Guest,
            MoreHomeAccountUiState.User(profileImage = null, email = "diary@example.com"),
        )
}
