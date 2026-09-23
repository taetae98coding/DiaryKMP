package io.github.taetae98coding.diary.feature.tag.ui.detail.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailSyncViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailWebContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToWebDetail: (Uuid) -> Unit,
    scopeState: TagDetailScopeState,
    modifier: Modifier = Modifier,
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = TagDetailTab.WEB, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val webViewModel = koinViewModel<TagDetailWebViewModel> { parametersOf(id) }
        val syncViewModel = koinViewModel<TagDetailSyncViewModel>()
        val isRefreshing by syncViewModel.isRefreshing.collectAsStateWithLifecycle()
        val webPagingItems = webViewModel.webPagingData.collectAsLazyPagingItems()
        val sort by webViewModel.sort.collectAsStateWithLifecycle()
        val sortSheetState = rememberDialogState()

        TagDetailScopeEffect(
            onSelect = { scope -> webViewModel.select(scope = scope) },
            state = scopeState,
        )

        TagDetailWebTab(
            onEvent = { event ->
                when (event) {
                    is TagDetailWebContentEvent.ClickWeb -> navigateToWebDetail(event.id)
                    is TagDetailWebContentEvent.Refresh -> syncViewModel.refresh()
                    is TagDetailWebContentEvent.ClickSort -> sortSheetState.show()
                    is TagDetailWebContentEvent.SelectSort -> webViewModel.select(sort = event.sort)
                }
            },
            modifier = modifier,
            sortSheetState = sortSheetState,
            webPagingItems = webPagingItems,
            isRefreshingProvider = { isRefreshing },
            sortProvider = { sort },
        )
    }
}
