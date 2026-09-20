package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource

private const val ACCOUNT_KEY = "account"

@Composable
internal fun MoreHomeScaffold(
    onEvent: (MoreHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    accountUiStateProvider: () -> MoreHomeAccountUiState = { MoreHomeAccountUiState.Loading },
    signOutUiStateProvider: () -> MoreHomeSignOutUiState = { MoreHomeSignOutUiState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = { MoreHomeTopBar(onEvent = onEvent) },
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 64.dp),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
        ) {
            item(
                key = ACCOUNT_KEY,
                span = { GridItemSpan(maxLineSpan) },
            ) {
                MoreHomeAccountCard(
                    onEvent = onEvent,
                    uiStateProvider = accountUiStateProvider,
                    modifier =
                        Modifier
                            .animateItem()
                            .fillMaxWidth(),
                )
            }

            items(
                items = moreHomeMenuList,
                key = { menu -> menu.name },
            ) { menu ->
                MoreHomeMenuCard(
                    onClick = { onEvent(MoreHomeScaffoldEvent.ClickMenu(menu)) },
                    icon = { MoreHomeMenuIcon(menu = menu) },
                    label = {
                        Text(
                            text = stringResource(menu.labelResource),
                            textAlign = TextAlign.Center,
                            style = DiaryTheme.typography.labelMedium,
                        )
                    },
                    modifier =
                        Modifier
                            .animateItem()
                            .aspectRatio(1f),
                )
            }
        }
    }

    MoreHomeSignOutDialogHost(
        onEvent = onEvent,
        uiStateProvider = signOutUiStateProvider,
    )
}

@ScreenPreview
@Composable
private fun MoreHomeScaffoldPreview(
    @PreviewParameter(MoreHomeAccountUiStatePreviewParameter::class) accountUiState: MoreHomeAccountUiState,
) {
    DiaryTheme {
        MoreHomeScaffold(
            accountUiStateProvider = { accountUiState },
            onEvent = {},
        )
    }
}
