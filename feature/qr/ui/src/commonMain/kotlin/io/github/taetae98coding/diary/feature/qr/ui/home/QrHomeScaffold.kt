package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_home_title
import io.github.taetae98coding.diary.feature.qr.ui.qr_navigate_up_button_content_description
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrHomeScaffold(
    onEvent: (QrHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    qrPagingItems: LazyPagingItems<Qr> = remember { flowOf(PagingData.empty<Qr>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> QrHomeUiState = { QrHomeUiState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.qr_home_title),
                onNavigateUp = { onEvent(QrHomeScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.qr_navigate_up_button_content_description),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(QrHomeScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.qr_add_button_content_description),
            )
        },
    ) { paddingValues ->
        QrHomeList(
            onEvent = onEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            qrPagingItems = qrPagingItems,
            isRefreshingProvider = { uiStateProvider().isRefreshing },
        )
    }
}

@ScreenPreview
@Composable
private fun QrHomeScaffoldPreview() {
    DiaryTheme {
        QrHomeScaffold(onEvent = {})
    }
}
