package io.github.taetae98coding.diary.feature.tag.ui.detail.qr

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.qr.QrListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailQrContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToQrDetail: (Uuid) -> Unit,
    scopeState: TagDetailScopeState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = TagDetailTab.QR, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val qrViewModel = koinViewModel<TagDetailQrViewModel> { parametersOf(id) }
        val syncViewModel = koinViewModel<SyncRefreshViewModel>()
        val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
        val qrPagingItems = qrViewModel.qrPagingData.collectAsLazyPagingItems()
        val scopeUiState by qrViewModel.scopeUiState.collectAsStateWithLifecycle()

        QrListUndoSnackbarEffect(
            onRestore = qrViewModel::restore,
            effect = qrViewModel.effect,
            snackbarHostState = snackbarHostState,
        )

        TagDetailScopeEffect(
            onSelect = { scope -> qrViewModel.select(scope = scope) },
            state = scopeState,
        )

        TagDetailQrTab(
            onEvent = { event ->
                when (event) {
                    is TagDetailQrContentEvent.ClickQr -> navigateToQrDetail(event.id)
                    is TagDetailQrContentEvent.Refresh -> syncViewModel.refresh()
                    is TagDetailQrContentEvent.DeleteQr -> qrViewModel.delete(id = event.id)
                }
            },
            modifier = modifier,
            qrPagingItems = qrPagingItems,
            isRefreshingProvider = { syncUiState.isRefreshing },
            scopeProvider = { scopeUiState.scope },
        )
    }
}
