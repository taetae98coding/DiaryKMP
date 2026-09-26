package io.github.taetae98coding.diary.feature.qr.ui.add

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
