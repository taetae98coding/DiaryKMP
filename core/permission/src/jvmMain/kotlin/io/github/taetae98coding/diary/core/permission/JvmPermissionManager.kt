package io.github.taetae98coding.diary.core.permission

import io.github.taetae98coding.diary.library.avfoundation.AVAuthorizationStatus

public class JvmPermissionManager internal constructor(
    private val cameraAuthorization: JvmCameraAuthorization,
) : PermissionManager() {
    public constructor() : this(cameraAuthorization = AVFoundationCameraAuthorization)

    override suspend fun readIsGranted(permission: Permission): Boolean =
        when (permission) {
            Permission.CAMERA -> cameraAuthorization.status() == AVAuthorizationStatus.AUTHORIZED
            Permission.NOTIFICATION, Permission.LOCATION -> false
        }

    override suspend fun requestPermission(permission: Permission): PermissionResult =
        when (permission) {
            Permission.CAMERA -> requestCameraPermission()
            Permission.NOTIFICATION, Permission.LOCATION -> PermissionResult.DENIED
        }

    // 이미 결정된 뒤에는 시스템이 요청을 표시하지 않으므로, 결정되지 않았을 때만 요청한다.
    private suspend fun requestCameraPermission(): PermissionResult {
        if (cameraAuthorization.status() != AVAuthorizationStatus.NOT_DETERMINED) {
            return PermissionResult.DENIED
        }

        return if (cameraAuthorization.requestAccess()) PermissionResult.GRANTED else PermissionResult.DENIED
    }
}
