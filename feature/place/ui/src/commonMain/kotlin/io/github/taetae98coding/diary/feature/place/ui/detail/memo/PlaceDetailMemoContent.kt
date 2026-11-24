package io.github.taetae98coding.diary.feature.place.ui.detail.memo

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.taetae98coding.diary.feature.core.memo.EntityDetailMemoConnector
import io.github.taetae98coding.diary.feature.core.memo.EntityDetailMemoFinishedListButton
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.detail.tab.PlaceDetailTab
import io.github.taetae98coding.diary.feature.place.ui.place_detail_memo_empty_description
import io.github.taetae98coding.diary.feature.place.ui.place_detail_memo_empty_title
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun PlaceDetailMemoContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToMemoFinishedList: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = PlaceDetailTab.MEMO, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        EntityDetailMemoConnector(
            memoViewModel = assistedMetroViewModel<PlaceDetailMemoViewModel, PlaceDetailMemoViewModel.Factory> { create(placeId = id) },
            syncViewModel = metroViewModel(),
            navigateToMemoDetail = navigateToMemoDetail,
            emptyTitle = stringResource(Res.string.place_detail_memo_empty_title),
            emptyDescription = stringResource(Res.string.place_detail_memo_empty_description),
            modifier = modifier,
            snackbarHostState = snackbarHostState,
            trailing = { EntityDetailMemoFinishedListButton(onClick = navigateToMemoFinishedList) },
        )
    }
}
