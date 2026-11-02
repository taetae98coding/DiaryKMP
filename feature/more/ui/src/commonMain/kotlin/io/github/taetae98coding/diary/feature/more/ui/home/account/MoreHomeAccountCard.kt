package io.github.taetae98coding.diary.feature.more.ui.home.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.more.ui.Res
import io.github.taetae98coding.diary.feature.more.ui.home.MoreHomeScaffoldEvent
import io.github.taetae98coding.diary.feature.more.ui.more_account_card_click_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MoreHomeAccountCard(
    onEvent: (MoreHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MoreHomeAccountUiState = { MoreHomeAccountUiState.Loading },
) {
    val isUser = uiStateProvider() is MoreHomeAccountUiState.User
    val openProfileClickLabel = stringResource(Res.string.more_account_card_click_label)

    val clickableModifier =
        if (isUser) {
            Modifier
                .clip(CardDefaults.shape)
                .clickable(onClickLabel = openProfileClickLabel) { onEvent(MoreHomeScaffoldEvent.ClickAccountCard) }
        } else {
            Modifier
        }

    Card(modifier = modifier.then(clickableModifier)) {
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
