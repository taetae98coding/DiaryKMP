package io.github.taetae98coding.diary.feature.more.ui.photo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.CompletableDeferred

@Composable
internal actual fun rememberPhotoPicker(): PhotoPicker {
    val picker = remember { AndroidPhotoPicker() }
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = picker::onPickResult,
        )

    SideEffect {
        picker.launcher = launcher
    }

    return picker
}

private class AndroidPhotoPicker : PhotoPicker {
    var launcher: ActivityResultLauncher<PickVisualMediaRequest>? = null

    private var pendingResult: CompletableDeferred<Uri?>? = null

    override suspend fun open(): FileUri? {
        val result =
            pendingResult ?: CompletableDeferred<Uri?>().also { deferred ->
                pendingResult = deferred
                checkNotNull(launcher).launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

        return result.await()?.let { uri -> FileUri(uri.toString()) }
    }

    fun onPickResult(uri: Uri?) {
        pendingResult?.complete(uri)
        pendingResult = null
    }
}
