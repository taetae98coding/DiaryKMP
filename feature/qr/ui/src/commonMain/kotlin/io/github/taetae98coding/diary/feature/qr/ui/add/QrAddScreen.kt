package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.form.ReflectQrCoordinateEffect
import io.github.taetae98coding.diary.feature.qr.ui.add.form.handleQrFormEvent
import io.github.taetae98coding.diary.feature.qr.ui.add.form.rememberQrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.qr_camera_permission_denied_message
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun QrAddScreen(
    navigateUp: () -> Unit,
    navigateToScan: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    permissionManager: PermissionManager,
    resultEventBus: ResultEventBus,
    componentVisibleProvider: () -> QrAddScaffoldComponentVisible,
    addViewModel: QrAddViewModel,
    tagViewModel: EntityAddTagViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val state = rememberQrAddFormState(defaultProvider = uiState.defaultProvider)
    val scanStarter = rememberQrScanStarter(permissionManager = permissionManager)
    val permissionDeniedMessage = stringResource(Res.string.qr_camera_permission_denied_message)
    val showPermissionDenied: () -> Unit = { coroutineScope.launch { state.snackbarHostState.showImmediate(message = permissionDeniedMessage) } }

    DiaryTitleInputFocusEffect(state = state.titleState)
    EntityTagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::add,
        resultEventBus = resultEventBus,
    )
    QrAddScreenEffect(
        effect = addViewModel.effect,
        state = state,
    )
    ReflectQrCoordinateEffect(state = state.contentState)
    QrScannedResultEffect(
        state = state,
        resultEventBus = resultEventBus,
    )

    QrAddScaffold(
        onEvent = { event ->
            handleQrAddScaffoldEvent(
                event = event,
                state = state,
                tagIdSetProvider = { tagViewModel.selectionUiState.value.tagIdSet },
                addViewModel = addViewModel,
                startScan = { coroutineScope.launch { scanStarter.startIfIdle(onGranted = navigateToScan, onDenied = showPermissionDenied) } },
                navigateUp = navigateUp,
            )
        },
        onFormEvent = { event ->
            handleQrFormEvent(
                event = event,
                state = state,
                selectableTagPagingItems = selectableTagPagingItems,
                navigateToTagAdd = navigateToTagAdd,
                navigateToTagDetail = navigateToTagDetail,
            )
        },
        onTagPickerEvent = { event -> handleEntityTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
        modifier = modifier,
        state = state,
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        tagUiStateProvider = { tagUiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}

private fun handleQrAddScaffoldEvent(
    event: QrAddScaffoldEvent,
    state: QrAddFormState,
    tagIdSetProvider: () -> Set<Uuid>,
    addViewModel: QrAddViewModel,
    startScan: () -> Unit,
    navigateUp: () -> Unit,
) {
    when (event) {
        is QrAddScaffoldEvent.ClickNavigateUp -> navigateUp()
        is QrAddScaffoldEvent.ClickUndo -> state.undo()
        is QrAddScaffoldEvent.ClickScan -> startScan()
        is QrAddScaffoldEvent.ClickAdd -> addViewModel.add(detail = state.detail, tagIdSet = tagIdSetProvider())
    }
}
