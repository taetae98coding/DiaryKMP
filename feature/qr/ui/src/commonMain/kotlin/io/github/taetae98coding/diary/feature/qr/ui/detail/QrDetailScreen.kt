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
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.add.QrScannedResultEffect
import io.github.taetae98coding.diary.feature.qr.ui.add.form.ReflectQrCoordinateEffect
import io.github.taetae98coding.diary.feature.qr.ui.add.form.rememberQrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.rememberQrScanStarter
import io.github.taetae98coding.diary.feature.qr.ui.detail.action.rememberQrActionController
import io.github.taetae98coding.diary.feature.qr.ui.qr_camera_permission_denied_message
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrDetailScreen(
    navigateUp: () -> Unit,
    navigateToScan: () -> Unit,
    permissionManager: PermissionManager,
    resultEventBus: ResultEventBus,
    viewModel: QrDetailViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val content = uiState as? QrDetailUiState.Content
    val scanStarter = rememberQrScanStarter(permissionManager = permissionManager)
    val permissionDeniedMessage = stringResource(Res.string.qr_camera_permission_denied_message)
    val actionController = rememberQrActionController()

    key(content?.id) {
        val state =
            rememberQrAddFormState(
                initialDetail = content?.detail ?: QrDetail.EMPTY,
                defaultProvider = content?.defaultProvider,
            )

        QrDetailScreenEffect(
            navigateUp = navigateUp,
            effect = viewModel.effect,
            state = state,
        )
        val action by remember(state, actionController) {
            derivedStateOf { actionController.actionOf(qrValue = state.contentState.qrValue) }
        }

        ReflectQrCoordinateEffect(state = state.contentState)
        QrScannedResultEffect(
            state = state,
            resultEventBus = resultEventBus,
        )

        QrDetailScaffold(
            onEvent = { event ->
                when (event) {
                    is QrDetailScaffoldEvent.ClickNavigateUp -> {
                        navigateUp()
                    }

                    is QrDetailScaffoldEvent.ClickAction -> {
                        coroutineScope.launch { actionController.launch(action = event.action, snackbarHostState = state.snackbarHostState) }
                    }

                    is QrDetailScaffoldEvent.ClickUndo -> {
                        state.undo()
                    }

                    is QrDetailScaffoldEvent.ClickScan -> {
                        scanStarter.startIfIdle(
                            onGranted = navigateToScan,
                            onDenied = { coroutineScope.launch { state.snackbarHostState.showImmediate(message = permissionDeniedMessage) } },
                        )
                    }

                    is QrDetailScaffoldEvent.ClickDelete -> {
                        viewModel.delete()
                    }

                    is QrDetailScaffoldEvent.ClickUpdate -> {
                        viewModel.update(detail = state.detail)
                    }
                }
            },
            modifier = modifier,
            state = state,
            uiStateProvider = { uiState },
            actionProvider = { action },
        )
    }
}
