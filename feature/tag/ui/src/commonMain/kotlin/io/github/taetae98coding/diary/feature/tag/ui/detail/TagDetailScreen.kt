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
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.snackbar.DismissUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.tag.TagDetail
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
            navigation =
                TagDetailNavigation(
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
                ),
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
    navigation: TagDetailNavigation,
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

    TagDetailScreenEffect(effect = detailViewModel.effect, scaffoldState = scaffoldState, navigateUp = navigation.navigateUp)
    DismissUndoSnackbarEffect(keyProvider = { tabState.tab }, hostState = scaffoldState.snackbarHostState)

    TagDetailScaffold(
        onEvent = { event -> handleTagDetailScaffoldEvent(event = event, viewModel = detailViewModel, scopeState = scopeState, navigateUp = navigation.navigateUp) },
        modifier =
            modifier.tagDetailTabShortcut(
                onUpdate = { detailViewModel.update(detail = scaffoldState.detail) },
                onMemoAdd = navigation.navigateToMemoAdd,
                onWebAdd = navigation.navigateToWebAdd,
                onPlaceAdd = { navigation.navigateToPlaceAdd(placeState.addCoordinate) },
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
                onMemoAdd = navigation.navigateToMemoAdd,
                onWebAdd = navigation.navigateToWebAdd,
                onPlaceAdd = { navigation.navigateToPlaceAdd(placeState.addCoordinate) },
                isUpdateVisible = isUpdateEnabled,
                isUpdateInProgressProvider = { content?.isInProgress == true },
            )
        },
    ) { tab ->
        TabContent(
            tab = tab,
            id = id,
            viewModelStoreProvider = viewModelStoreProvider,
            navigation = navigation,
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
    navigation: TagDetailNavigation,
    tagAddRequestKey: Uuid,
    placeState: TagDetailPlaceState,
    placeMapViewModel: TagDetailPlaceMapViewModel,
    placeMapState: DiaryMapState,
    scopeState: TagDetailScopeState,
    uiStateProvider: () -> TagDetailUiState,
    state: TagFormState,
) {
    when (tab) {
        TagDetailTab.DETAIL ->
            TagDetailFormContent(
                id = id,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToTagAdd = navigation.navigateToTagAdd,
                navigateToDetail = navigation.navigateToDetail,
                tagAddRequestKey = tagAddRequestKey,
                modifier = Modifier.fillMaxSize(),
                uiStateProvider = uiStateProvider,
                state = state,
            )

        TagDetailTab.MEMO ->
            TagDetailMemoContent(
                id = id,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToMemoDetail = navigation.navigateToMemoDetail,
                navigateToMemoFinishedList = navigation.navigateToMemoFinishedList,
                scopeState = scopeState,
                modifier = Modifier.fillMaxSize(),
                snackbarHostState = state.snackbarHostState,
            )

        TagDetailTab.WEB ->
            TagDetailWebContent(
                id = id,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToWebDetail = navigation.navigateToWebDetail,
                scopeState = scopeState,
                modifier = Modifier.fillMaxSize(),
                snackbarHostState = state.snackbarHostState,
            )

        TagDetailTab.PLACE ->
            TagDetailPlaceContent(
                id = id,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToPlaceDetail = navigation.navigateToPlaceDetail,
                state = placeState,
                mapViewModel = placeMapViewModel,
                mapState = placeMapState,
                scopeState = scopeState,
                modifier = Modifier.fillMaxSize(),
                snackbarHostState = state.snackbarHostState,
            )
    }
}
