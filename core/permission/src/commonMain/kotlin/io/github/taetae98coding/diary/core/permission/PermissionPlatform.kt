package io.github.taetae98coding.diary.core.permission

public interface PermissionPlatform {
    public suspend fun readIsGranted(permission: Permission): Boolean

    public suspend fun requestPermission(permission: Permission): PermissionResult
}
