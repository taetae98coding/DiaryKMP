package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_camera_permission_denied_message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrAddScreen(
    navigateUp: () -> Unit,
    navigateToScan: () -> Unit,
    permissionManager: PermissionManager,
    resultEventBus: ResultEventBus,
    viewModel: QrAddViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val state = rememberQrAddFormState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scanStarter = remember(coroutineScope, permissionManager) { QrScanStarter(coroutineScope = coroutineScope, permissionManager = permissionManager) }
    val permissionDeniedMessage = stringResource(Res.string.qr_camera_permission_denied_message)

    DiaryTitleInputFocusEffect(state = state.titleState)
    QrAddScreenEffect(
        effect = viewModel.effect,
        state = state,
    )
    QrScannedResultEffect(
        state = state.valueState,
        resultEventBus = resultEventBus,
    )

    QrAddScaffold(
        onEvent = { event ->
            when (event) {
                is QrAddScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is QrAddScaffoldEvent.ClickScan -> {
                    scanStarter.startIfIdle(
                        onGranted = navigateToScan,
                        onDenied = { coroutineScope.launch { state.hostState.showImmediate(message = permissionDeniedMessage) } },
                    )
                }

                is QrAddScaffoldEvent.ClickAdd -> {
                    viewModel.add(detail = state.detail)
                }
            }
        },
        modifier = modifier,
        state = state,
        uiStateProvider = { uiState },
    )
}

private class QrScanStarter(
    private val coroutineScope: CoroutineScope,
    private val permissionManager: PermissionManager,
) {
    private var job: Job? = null

    fun startIfIdle(
        onGranted: () -> Unit,
        onDenied: () -> Unit,
    ) {
        if (job?.isActive == true) return

        job =
            coroutineScope.launch {
                startQrScan(
                    permissionManager = permissionManager,
                    onGranted = onGranted,
                    onDenied = onDenied,
                )
            }
    }
}
