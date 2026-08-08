package io.github.taetae98coding.diary.feature.calendar.ui.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CompletableDeferred

@Composable
internal actual fun rememberLocationPermissionRequester(): LocationPermissionRequester {
    val context = LocalContext.current
    val requester = remember(context) { AndroidLocationPermissionRequester(context = context) }
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = requester::onRequestResult,
        )

    SideEffect {
        requester.launcher = launcher
    }

    return requester
}

private class AndroidLocationPermissionRequester(
    private val context: Context,
) : LocationPermissionRequester {
    var launcher: ActivityResultLauncher<Array<String>>? = null

    private var pendingResult: CompletableDeferred<Map<String, Boolean>>? = null

    override suspend fun request(): LocationPermissionRequestResult {
        if (hasLocationPermission()) return LocationPermissionRequestResult.ALREADY_GRANTED

        val result =
            pendingResult ?: CompletableDeferred<Map<String, Boolean>>().also { deferred ->
                pendingResult = deferred
                checkNotNull(launcher).launch(LOCATION_PERMISSION_LIST.toTypedArray())
            }

        return if (result.await().any { entry -> entry.value }) {
            LocationPermissionRequestResult.GRANTED
        } else {
            LocationPermissionRequestResult.DENIED
        }
    }

    fun onRequestResult(result: Map<String, Boolean>) {
        pendingResult?.complete(result)
        pendingResult = null
    }

    private fun hasLocationPermission(): Boolean = LOCATION_PERMISSION_LIST.any { permission -> context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED }

    companion object {
        private val LOCATION_PERMISSION_LIST =
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
    }
}
