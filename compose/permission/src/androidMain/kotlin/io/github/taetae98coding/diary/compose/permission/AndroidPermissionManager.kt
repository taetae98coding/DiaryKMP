package io.github.taetae98coding.diary.compose.permission

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionResult
import io.github.taetae98coding.diary.core.permission.isPermissionGranted
import io.github.taetae98coding.diary.core.permission.toAndroidPermissionList
import kotlinx.coroutines.CompletableDeferred

@Composable
public actual fun rememberPermissionManager(): PermissionManager {
    val context = LocalContext.current
    val permissionManager = remember(context) { AndroidPermissionManager(context = context) }
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = permissionManager::onRequestResult,
        )

    SideEffect {
        permissionManager.launcher = launcher
    }

    return permissionManager
}

// 요청은 composition이 소유한 launcher로만 시작할 수 있으므로 launcher 배선을 이 클래스 안에 둔다.
private class AndroidPermissionManager(
    private val context: Context,
) : PermissionManager() {
    var launcher: ActivityResultLauncher<Array<String>>? = null

    private val pendingResultMap = mutableMapOf<Permission, CompletableDeferred<Map<String, Boolean>>>()

    override suspend fun readIsGranted(permission: Permission): Boolean = context.isPermissionGranted(permission)

    override suspend fun requestPermission(permission: Permission): PermissionResult {
        val result =
            pendingResultMap[permission] ?: CompletableDeferred<Map<String, Boolean>>().also { deferred ->
                pendingResultMap[permission] = deferred
                checkNotNull(launcher).launch(permission.toAndroidPermissionList().toTypedArray())
            }

        return if (result.await().any { entry -> entry.value }) {
            PermissionResult.GRANTED
        } else {
            PermissionResult.DENIED
        }
    }

    // 시스템은 어느 권한의 결과인지 알려 주지 않으므로 돌려받은 권한 이름으로 요청을 되찾는다.
    fun onRequestResult(result: Map<String, Boolean>) {
        val permission =
            Permission.entries.firstOrNull { permission ->
                permission.toAndroidPermissionList().any { name -> name in result }
            } ?: return

        pendingResultMap.remove(permission)?.complete(result)
    }
}
