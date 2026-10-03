package io.github.taetae98coding.diary.compose.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
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
    var isRequested by retain(permission) { mutableStateOf(false) }

    LaunchedEffect(permissionManager, permission) {
        if (isRequested) return@LaunchedEffect

        isRequested = true
        currentOnResult(permissionManager.request(permission))
    }
}
