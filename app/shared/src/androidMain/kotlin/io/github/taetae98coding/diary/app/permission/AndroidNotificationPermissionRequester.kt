package io.github.taetae98coding.diary.app.permission

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
internal actual fun rememberNotificationPermissionRequester(): NotificationPermissionRequester {
    val context = LocalContext.current
    val requester = remember(context) { AndroidNotificationPermissionRequester(context = context) }
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = requester::onRequestResult,
        )

    SideEffect {
        requester.launcher = launcher
    }

    return requester
}

private class AndroidNotificationPermissionRequester(
    private val context: Context,
) : NotificationPermissionRequester {
    var launcher: ActivityResultLauncher<String>? = null

    private var pendingResult: CompletableDeferred<Boolean>? = null

    override suspend fun request(): NotificationPermissionRequestResult {
        if (hasNotificationPermission()) return NotificationPermissionRequestResult.GRANTED

        val result =
            pendingResult ?: CompletableDeferred<Boolean>().also { deferred ->
                pendingResult = deferred
                checkNotNull(launcher).launch(Manifest.permission.POST_NOTIFICATIONS)
            }

        return if (result.await()) {
            NotificationPermissionRequestResult.GRANTED
        } else {
            NotificationPermissionRequestResult.DENIED
        }
    }

    fun onRequestResult(isGranted: Boolean) {
        pendingResult?.complete(isGranted)
        pendingResult = null
    }

    private fun hasNotificationPermission(): Boolean = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}
