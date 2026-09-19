package io.github.taetae98coding.diary.compose.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.core.permission.JvmPermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionManager

@Composable
public actual fun rememberPermissionManager(): PermissionManager = remember { JvmPermissionManager() }
