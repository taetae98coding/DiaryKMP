package io.github.taetae98coding.diary.compose.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.WasmPermissionManager

@Composable
public actual fun rememberPermissionManager(): PermissionManager = remember { WasmPermissionManager() }
