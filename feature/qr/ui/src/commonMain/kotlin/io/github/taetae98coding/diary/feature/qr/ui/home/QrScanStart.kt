package io.github.taetae98coding.diary.feature.qr.ui.home

import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionResult

internal suspend fun startQrScan(
    permissionManager: PermissionManager,
    isSupported: Boolean,
    onGranted: () -> Unit,
    onDenied: () -> Unit,
) {
    if (!isSupported) return

    when (permissionManager.request(Permission.CAMERA)) {
        PermissionResult.ALREADY_GRANTED, PermissionResult.GRANTED -> onGranted()
        PermissionResult.DENIED -> onDenied()
    }
}
