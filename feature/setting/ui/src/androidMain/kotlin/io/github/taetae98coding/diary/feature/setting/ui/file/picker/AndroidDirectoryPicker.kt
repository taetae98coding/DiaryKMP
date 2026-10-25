package io.github.taetae98coding.diary.feature.setting.ui.file.picker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
internal actual fun rememberDirectoryPicker(onPick: (uri: String) -> Unit): DirectoryPicker {
    val latestOnPick by rememberUpdatedState(onPick)
    // 결과 전달 수단이 Activity를 다시 만들 때도 결과를 이 launcher에 넘겨 주므로, 재생성 뒤에도 고른 폴더를 받는다.
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let { value -> latestOnPick(value.toString()) }
        }

    return remember(launcher) { AndroidDirectoryPicker(launcher = launcher) }
}

private class AndroidDirectoryPicker(
    private val launcher: ActivityResultLauncher<Uri?>,
) : DirectoryPicker {
    override fun open() {
        launcher.launch(null)
    }
}
