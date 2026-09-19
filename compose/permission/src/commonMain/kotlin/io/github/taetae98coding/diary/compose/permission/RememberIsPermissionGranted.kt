package io.github.taetae98coding.diary.compose.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager

@Composable
public fun rememberIsPermissionGranted(
    permission: Permission,
    permissionManager: PermissionManager = rememberPermissionManager(),
): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current
    val isGranted by permissionManager.isGranted(permission).collectAsStateWithLifecycle(initialValue = false)

    // 사용자가 앱 밖에서 권한을 바꾸고 돌아오는 것이 요청 외의 유일한 변경 계기이므로 화면이 다시 앞으로 올 때마다 확인한다.
    LaunchedEffect(permissionManager, permission, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            permissionManager.refresh(permission)
        }
    }

    return isGranted
}
