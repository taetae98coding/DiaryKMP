package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal suspend fun startQrScan(
    permissionManager: PermissionManager,
    onGranted: () -> Unit,
    onDenied: () -> Unit,
) {
    when (permissionManager.request(Permission.CAMERA)) {
        PermissionResult.ALREADY_GRANTED, PermissionResult.GRANTED -> onGranted()
        PermissionResult.DENIED -> onDenied()
    }
}

internal class QrScanStarter(
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

@Composable
internal fun rememberQrScanStarter(permissionManager: PermissionManager): QrScanStarter {
    val coroutineScope = rememberCoroutineScope()

    return remember(coroutineScope, permissionManager) { QrScanStarter(coroutineScope = coroutineScope, permissionManager = permissionManager) }
}
