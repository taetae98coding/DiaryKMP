package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.DismissUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.detail.form.TagDetailFormContent
import io.github.taetae98coding.diary.feature.tag.ui.detail.form.TagDetailFormFloatingActionButton
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TagDetailMemoContent
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TagDetailMemoFloatingActionButton
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceContent
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceFloatingActionButton
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceMapViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceState
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceTargetHost
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.rememberTagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.rememberTagDetailTabState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.tagDetailTabShortcut
import io.github.taetae98coding.diary.feature.tag.ui.detail.web.TagDetailWebContent
import io.github.taetae98coding.diary.feature.tag.ui.detail.web.TagDetailWebFloatingActionButton
import io.github.taetae98coding.diary.feature.tag.ui.form.TagFormState
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagDetailFormState
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_update_succeeded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    navigateToMemoAdd: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToMemoFinishedList: () -> Unit,
    navigateToWebAdd: () -> Unit,
    navigateToWebDetail: (Uuid) -> Unit,
    navigateToPlaceAdd: (Coordinate?) -> Unit,
    navigateToPlaceDetail: (Uuid) -> Unit,
    id: Uuid,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> TagDetailScaffoldComponentVisible,
    detailViewModel: TagDetailViewModel,
    placeMapViewModel: TagDetailPlaceMapViewModel,
    modifier: Modifier = Modifier,
) {
    TagDetailPlaceTargetHost(
        id = id,
        placeMapViewModel = placeMapViewModel,
    ) { placeState, placeMapState ->
        TagDetailScreenContent(
            navigateUp = navigateUp,
            navigateToTagAdd = navigateToTagAdd,
            navigateToDetail = navigateToDetail,
            navigateToMemoAdd = navigateToMemoAdd,
            navigateToMemoDetail = navigateToMemoDetail,
            navigateToMemoFinishedList = navigateToMemoFinishedList,
            navigateToWebAdd = navigateToWebAdd,
            navigateToWebDetail = navigateToWebDetail,
            navigateToPlaceAdd = navigateToPlaceAdd,
            navigateToPlaceDetail = navigateToPlaceDetail,
            id = id,
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = componentVisibleProvider,
            detailViewModel = detailViewModel,
            placeState = placeState,
            placeMapViewModel = placeMapViewModel,
            placeMapState = placeMapState,
            modifier = modifier,
        )
    }
}

@Composable
private fun TagDetailScreenContent(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    navigateToMemoAdd: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToMemoFinishedList: () -> Unit,
    navigateToWebAdd: () -> Unit,
    navigateToWebDetail: (Uuid) -> Unit,
    navigateToPlaceAdd: (Coordinate?) -> Unit,
    navigateToPlaceDetail: (Uuid) -> Unit,
    id: Uuid,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> TagDetailScaffoldComponentVisible,
    detailViewModel: TagDetailViewModel,
    placeState: TagDetailPlaceState,
    placeMapViewModel: TagDetailPlaceMapViewModel,
    placeMapState: DiaryMapState,
    modifier: Modifier = Modifier,
) {
    val tabState = rememberTagDetailTabState()
    val scopeState = rememberTagDetailScopeState()
    val viewModelStoreProvider = rememberViewModelStoreProvider()
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val content = uiState as? TagDetailUiState.Content

    val scaffoldState = key(content?.id) { rememberTagDetailFormState(initialDetail = content?.detail ?: TagDetail.EMPTY) }

    val isUpdateEnabled by rememberIsUpdateEnabled(scaffoldState = scaffoldState, uiStateProvider = { uiState })

    TagDetailScreenEffect(effect = detailViewModel.effect, scaffoldState = scaffoldState, navigateUp = navigateUp)
    DismissUndoSnackbarEffect(keyProvider = { tabState.tab }, hostState = scaffoldState.hostState)

    TagDetailScaffold(
        onEvent = { event -> handleTagDetailScaffoldEvent(event = event, viewModel = detailViewModel, scopeState = scopeState, navigateUp = navigateUp) },
        modifier =
            modifier.tagDetailTabShortcut(
                onUpdate = { detailViewModel.update(detail = scaffoldState.detail) },
                onMemoAdd = navigateToMemoAdd,
                onWebAdd = navigateToWebAdd,
                onPlaceAdd = { navigateToPlaceAdd(placeState.addCoordinate) },
                state = tabState,
                isUpdateEnabledProvider = { isUpdateEnabled },
            ),
        uiStateProvider = { uiState },
        state = scaffoldState,
        tabState = tabState,
        scopeState = scopeState,
        componentVisibleProvider = componentVisibleProvider,
        tabFloatingActionButton = { tab ->
            TabFloatingActionButton(
                tab = tab,
                onUpdate = { detailViewModel.update(detail = scaffoldState.detail) },
                onMemoAdd = navigateToMemoAdd,
                onWebAdd = navigateToWebAdd,
                onPlaceAdd = { navigateToPlaceAdd(placeState.addCoordinate) },
                isUpdateVisible = isUpdateEnabled,
                isUpdateInProgressProvider = { content?.isInProgress == true },
            )
        },
    ) { tab ->
        TabContent(
            tab = tab,
            id = id,
            viewModelStoreProvider = viewModelStoreProvider,
            navigateToTagAdd = navigateToTagAdd,
            navigateToDetail = navigateToDetail,
            navigateToMemoDetail = navigateToMemoDetail,
            navigateToMemoFinishedList = navigateToMemoFinishedList,
            navigateToWebDetail = navigateToWebDetail,
            navigateToPlaceDetail = navigateToPlaceDetail,
            tagAddRequestKey = tagAddRequestKey,
            placeState = placeState,
            placeMapViewModel = placeMapViewModel,
            placeMapState = placeMapState,
            scopeState = scopeState,
            uiStateProvider = { uiState },
            state = scaffoldState,
        )
    }
}

@Composable
private fun rememberIsUpdateEnabled(
    scaffoldState: TagFormState,
    uiStateProvider: () -> TagDetailUiState,
): State<Boolean> =
    remember(scaffoldState) {
        derivedStateOf {
            val loaded = uiStateProvider() as? TagDetailUiState.Content
            loaded != null && scaffoldState.detail != loaded.detail
        }
    }

@Composable
private fun TabFloatingActionButton(
    tab: TagDetailTab,
    onUpdate: () -> Unit,
    onMemoAdd: () -> Unit,
    onWebAdd: () -> Unit,
    onPlaceAdd: () -> Unit,
    isUpdateVisible: Boolean = false,
    isUpdateInProgressProvider: () -> Boolean = { false },
) {
    // 태그 디테일 탭으로 옮겨 추가 버튼이 사라지는 동안에도 떠나기 전 목록 탭의 버튼으로 그린다.
    val lastAddTab = remember { mutableStateOf(TagDetailTab.MEMO) }
    val addTab = if (tab == TagDetailTab.DETAIL) lastAddTab.value else tab

    SideEffect { lastAddTab.value = addTab }

    Box {
        DiaryScaleVisibility(visible = tab == TagDetailTab.DETAIL && isUpdateVisible) {
            TagDetailFormFloatingActionButton(
                onClick = onUpdate,
                isInProgressProvider = isUpdateInProgressProvider,
            )
        }

        DiaryScaleVisibility(visible = tab != TagDetailTab.DETAIL) {
            when (addTab) {
                TagDetailTab.DETAIL, TagDetailTab.MEMO -> TagDetailMemoFloatingActionButton(onClick = onMemoAdd)
                TagDetailTab.WEB -> TagDetailWebFloatingActionButton(onClick = onWebAdd)
                TagDetailTab.PLACE -> TagDetailPlaceFloatingActionButton(onClick = onPlaceAdd)
            }
        }
    }
}

@Composable
private fun TabContent(
    tab: TagDetailTab,
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToTagAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToMemoFinishedList: () -> Unit,
    navigateToWebDetail: (Uuid) -> Unit,
    navigateToPlaceDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    placeState: TagDetailPlaceState,
    placeMapViewModel: TagDetailPlaceMapViewModel,
    placeMapState: DiaryMapState,
    scopeState: TagDetailScopeState,
    uiStateProvider: () -> TagDetailUiState = { TagDetailUiState.Loading },
    state: TagFormState = rememberTagDetailFormState(initialDetail = TagDetail.EMPTY),
) {
    when (tab) {
        TagDetailTab.DETAIL ->
            TagDetailFormContent(
                id = id,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToTagAdd = navigateToTagAdd,
                navigateToDetail = navigateToDetail,
                tagAddRequestKey = tagAddRequestKey,
                modifier = Modifier.fillMaxSize(),
                uiStateProvider = uiStateProvider,
                state = state,
            )

        TagDetailTab.MEMO ->
            TagDetailMemoContent(
                id = id,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToMemoDetail = navigateToMemoDetail,
                navigateToMemoFinishedList = navigateToMemoFinishedList,
                scopeState = scopeState,
                modifier = Modifier.fillMaxSize(),
                snackbarHostState = state.hostState,
            )

        TagDetailTab.WEB ->
            TagDetailWebContent(
                id = id,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToWebDetail = navigateToWebDetail,
                scopeState = scopeState,
                modifier = Modifier.fillMaxSize(),
                snackbarHostState = state.hostState,
            )

        TagDetailTab.PLACE ->
            TagDetailPlaceContent(
                id = id,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToPlaceDetail = navigateToPlaceDetail,
                state = placeState,
                mapViewModel = placeMapViewModel,
                mapState = placeMapState,
                scopeState = scopeState,
                modifier = Modifier.fillMaxSize(),
                snackbarHostState = state.hostState,
            )
    }
}

@Composable
private fun TagDetailScreenEffect(
    navigateUp: () -> Unit,
    effect: Flow<TagDetailEffect> = emptyFlow(),
    scaffoldState: TagFormState = rememberTagDetailFormState(initialDetail = TagDetail.EMPTY),
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.tag_detail_update_succeeded_message)

    CollectEffect(effect) { value ->
        when (value) {
            is TagDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { scaffoldState.hostState.showImmediate(message = updateSucceededMessage) }
            }

            is TagDetailEffect.DeleteSucceeded -> navigateUp()
        }
    }
}
