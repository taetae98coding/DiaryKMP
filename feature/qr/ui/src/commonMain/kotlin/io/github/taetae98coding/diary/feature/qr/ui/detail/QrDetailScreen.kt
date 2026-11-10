package io.github.taetae98coding.diary.feature.qr.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.add.QrScannedResultEffect
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.form.ReflectQrCoordinateEffect
import io.github.taetae98coding.diary.feature.qr.ui.add.form.handleQrFormEvent
import io.github.taetae98coding.diary.feature.qr.ui.add.form.rememberQrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.rememberQrScanStarter
import io.github.taetae98coding.diary.feature.qr.ui.detail.action.QrActionLauncherFactory
import io.github.taetae98coding.diary.feature.qr.ui.detail.action.rememberQrActionController
import io.github.taetae98coding.diary.feature.qr.ui.qr_camera_permission_denied_message
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun QrDetailScreen(
    navigateUp: () -> Unit,
    navigateToScan: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    permissionManager: PermissionManager,
    qrActionLauncherFactory: QrActionLauncherFactory,
    resultEventBus: ResultEventBus,
    componentVisibleProvider: () -> QrDetailScaffoldComponentVisible,
    qrViewModel: QrDetailViewModel,
    tagViewModel: EntityDetailTagViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by qrViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val content = uiState as? QrDetailUiState.Content
    val scanStarter = rememberQrScanStarter(permissionManager = permissionManager)
    val permissionDeniedMessage = stringResource(Res.string.qr_camera_permission_denied_message)
    val actionController = rememberQrActionController(launcherFactory = qrActionLauncherFactory)

    EntityTagAddedResultEffect(requestKey = tagAddRequestKey, onTagAdded = tagViewModel::add, resultEventBus = resultEventBus)

    key(content?.id) {
        val state =
            rememberQrAddFormState(
                initialDetail = content?.detail ?: QrDetail.EMPTY,
                defaultProvider = content?.defaultProvider,
            )

        QrDetailScreenEffect(
            navigateUp = navigateUp,
            effect = qrViewModel.effect,
            state = state,
        )
        val action by remember(state, actionController) {
            derivedStateOf { actionController.actionOf(qrValue = state.contentState.qrValue) }
        }
        val showPermissionDenied: () -> Unit = { coroutineScope.launch { state.snackbarHostState.showImmediate(message = permissionDeniedMessage) } }

        ReflectQrCoordinateEffect(state = state.contentState)
        QrScannedResultEffect(
            state = state,
            resultEventBus = resultEventBus,
        )

        QrDetailScaffold(
            onEvent = { event ->
                handleQrDetailScaffoldEvent(
                    event = event,
                    state = state,
                    qrViewModel = qrViewModel,
                    launchAction = { action -> coroutineScope.launch { actionController.launch(action = action, snackbarHostState = state.snackbarHostState) } },
                    startScan = { coroutineScope.launch { scanStarter.startIfIdle(onGranted = navigateToScan, onDenied = showPermissionDenied) } },
                    navigateUp = navigateUp,
                )
            },
            onFormEvent = { event -> handleQrFormEvent(event = event, state = state, selectableTagPagingItems = selectableTagPagingItems, navigateToTagAdd = navigateToTagAdd, navigateToTagDetail = navigateToTagDetail) },
            onTagPickerEvent = { event -> handleEntityTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
            modifier = modifier,
            state = state,
            tagPagingItems = tagPagingItems,
            uiStateProvider = { uiState },
            tagUiStateProvider = { tagUiState },
            actionProvider = { action },
            componentVisibleProvider = componentVisibleProvider,
        )
    }
}

private fun handleQrDetailScaffoldEvent(
    event: QrDetailScaffoldEvent,
    state: QrAddFormState,
    qrViewModel: QrDetailViewModel,
    launchAction: (QrAction) -> Unit,
    startScan: () -> Unit,
    navigateUp: () -> Unit,
) {
    when (event) {
        is QrDetailScaffoldEvent.ClickNavigateUp -> navigateUp()
        is QrDetailScaffoldEvent.ClickAction -> launchAction(event.action)
        is QrDetailScaffoldEvent.ClickUndo -> state.undo()
        is QrDetailScaffoldEvent.ClickScan -> startScan()
        is QrDetailScaffoldEvent.ClickDelete -> qrViewModel.delete()
        is QrDetailScaffoldEvent.ClickUpdate -> qrViewModel.update(detail = state.detail)
    }
}
