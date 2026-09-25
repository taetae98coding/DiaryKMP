package io.github.taetae98coding.diary.core.permission

public class IosPermissionManager : PermissionManager() {
    override suspend fun readIsGranted(permission: Permission): Boolean =
        when (permission) {
            Permission.NOTIFICATION -> isNotificationPermissionGranted()
            Permission.LOCATION -> isLocationPermissionGranted()
            Permission.CAMERA -> isCameraPermissionGranted()
        }

    override suspend fun requestPermission(permission: Permission): PermissionResult =
        when (permission) {
            Permission.NOTIFICATION -> requestNotificationPermission()
            Permission.LOCATION -> requestLocationPermission()
            Permission.CAMERA -> requestCameraPermission()
        }
}
