package io.github.taetae98coding.diary.compose.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionResult

@Composable
public fun RequestPermissionEffect(
    permission: Permission,
    onResult: (PermissionResult) -> Unit,
    permissionManager: PermissionManager = rememberPermissionManager(),
) {
    val currentOnResult by rememberUpdatedState(onResult)

    LaunchedEffect(permissionManager, permission) { currentOnResult(permissionManager.request(permission)) }
}
