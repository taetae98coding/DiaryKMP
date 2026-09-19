package io.github.taetae98coding.diary.core.permission

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

public abstract class PermissionManager {
    private val grantedStateMap = Permission.entries.associateWith { MutableStateFlow(false) }

    public fun isGranted(permission: Permission): Flow<Boolean> = grantedState(permission)

    // 어느 플랫폼도 권한 변경을 앱에 알려 주지 않으므로, 앱 밖에서 권한이 바뀌었을 만한 계기에 호출자가 다시 확인하게 한다.
    public suspend fun refresh(permission: Permission) {
        grantedState(permission).value = readIsGranted(permission)
    }

    public suspend fun request(permission: Permission): PermissionResult {
        if (readIsGranted(permission)) {
            grantedState(permission).value = true

            return PermissionResult.ALREADY_GRANTED
        }

        val result = requestPermission(permission)

        refresh(permission)

        return result
    }

    protected abstract suspend fun readIsGranted(permission: Permission): Boolean

    protected abstract suspend fun requestPermission(permission: Permission): PermissionResult

    private fun grantedState(permission: Permission): MutableStateFlow<Boolean> = checkNotNull(grantedStateMap[permission])
}
