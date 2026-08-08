package io.github.taetae98coding.diary.feature.calendar.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
internal interface LocationPermissionRequester {
    suspend fun request(): LocationPermissionRequestResult
}

@Composable
internal expect fun rememberLocationPermissionRequester(): LocationPermissionRequester
