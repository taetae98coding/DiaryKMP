package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_camera_permission_denied_message
import io.github.taetae98coding.diary.feature.qr.ui.scan.isQrScanSupported
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrHomeScreen(
    navigateUp: () -> Unit,
    navigateToScan: () -> Unit,
    permissionManager: PermissionManager,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val scanStarter = remember(coroutineScope, permissionManager) { QrScanStarter(coroutineScope = coroutineScope, permissionManager = permissionManager) }
    val permissionDeniedMessage = stringResource(Res.string.qr_camera_permission_denied_message)

    QrHomeScaffold(
        onEvent = { event ->
            when (event) {
                is QrHomeScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is QrHomeScaffoldEvent.ClickScan -> {
                    scanStarter.startIfIdle(
                        onGranted = navigateToScan,
                        onDenied = { coroutineScope.launch { hostState.showImmediate(message = permissionDeniedMessage) } },
                    )
                }
            }
        },
        modifier = modifier,
        hostState = hostState,
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
                    isSupported = isQrScanSupported,
                    onGranted = onGranted,
                    onDenied = onDenied,
                )
            }
    }
}
