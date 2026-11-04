package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionResult

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

@Stable
internal class QrScanStarter(
    private val permissionManager: PermissionManager,
) {
    private var isStarting = false

    suspend fun startIfIdle(
        onGranted: () -> Unit,
        onDenied: () -> Unit,
    ) {
        if (isStarting) return

        isStarting = true
        try {
            startQrScan(
                permissionManager = permissionManager,
                onGranted = onGranted,
                onDenied = onDenied,
            )
        } finally {
            isStarting = false
        }
    }
}

@Composable
internal fun rememberQrScanStarter(permissionManager: PermissionManager): QrScanStarter = remember(permissionManager) { QrScanStarter(permissionManager = permissionManager) }
