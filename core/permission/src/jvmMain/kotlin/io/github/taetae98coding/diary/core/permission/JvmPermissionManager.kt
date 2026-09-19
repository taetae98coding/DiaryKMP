package io.github.taetae98coding.diary.core.permission

public class JvmPermissionManager : PermissionManager() {
    // JVM 데스크톱은 시스템 권한 개념을 제공하지 않으므로 확인 결과를 허용되지 않음으로 두고 요청 없이 거부로 처리한다.
    override suspend fun readIsGranted(permission: Permission): Boolean = false

    override suspend fun requestPermission(permission: Permission): PermissionResult = PermissionResult.DENIED
}
