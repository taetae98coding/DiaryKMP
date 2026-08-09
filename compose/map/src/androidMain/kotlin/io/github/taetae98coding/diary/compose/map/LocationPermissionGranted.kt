package io.github.taetae98coding.diary.compose.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

@Composable
internal fun rememberIsLocationPermissionGranted(): Boolean {
    val context = LocalContext.current
    var isGranted by remember(context) { mutableStateOf(context.isLocationPermissionGranted()) }

    LifecycleResumeEffect(context) {
        isGranted = context.isLocationPermissionGranted()

        onPauseOrDispose { }
    }

    return isGranted
}

private fun Context.isLocationPermissionGranted(): Boolean = LOCATION_PERMISSION_LIST.any { permission -> checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED }

private val LOCATION_PERMISSION_LIST =
    listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
