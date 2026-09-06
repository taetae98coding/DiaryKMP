package io.github.taetae98coding.diary.app.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
internal fun RequestNotificationPermissionEffect() {
    val requester = rememberNotificationPermissionRequester()

    LaunchedEffect(requester) { requester.request() }
}
