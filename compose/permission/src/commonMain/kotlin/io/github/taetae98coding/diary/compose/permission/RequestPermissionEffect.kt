package io.github.taetae98coding.diary.compose.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionResult
import kotlin.random.Random

@Composable
public fun RequestPermissionEffect(
    permission: Permission,
    onResult: (PermissionResult) -> Unit,
    permissionManager: PermissionManager = rememberPermissionManager(),
) {
    val currentOnResult by rememberUpdatedState(onResult)
    val requestPointId = rememberSaveable { Random.nextLong() }
    val localHistory = remember { PermissionRequestHistory() }
    val history = LocalPermissionRequestHistory.current ?: localHistory

    LaunchedEffect(permissionManager, permission, history, requestPointId) {
        if (!history.markRequested(requestPointId = requestPointId, permission = permission)) return@LaunchedEffect

        currentOnResult(permissionManager.request(permission))
    }
}
