package io.github.taetae98coding.diary.core.permission

public class JvmPermissionManager : PermissionManager() {
    override suspend fun readIsGranted(permission: Permission): Boolean = false

    override suspend fun requestPermission(permission: Permission): PermissionResult = PermissionResult.DENIED
}
