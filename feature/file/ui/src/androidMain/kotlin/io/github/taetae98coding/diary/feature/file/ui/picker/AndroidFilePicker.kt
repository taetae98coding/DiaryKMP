package io.github.taetae98coding.diary.feature.file.ui.picker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.github.taetae98coding.diary.core.model.file.FileUri

private const val ANY_MIME_TYPE = "*/*"

@Composable
internal actual fun rememberFilePicker(onPick: (FileUri) -> Unit): FilePicker {
    val latestOnPick by rememberUpdatedState(onPick)
    // 결과 전달 수단이 Activity를 다시 만들 때도 결과를 이 launcher에 넘겨 주므로, 재생성 뒤에도 고른 파일을 받는다.
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { value -> latestOnPick(FileUri(value.toString())) }
        }

    return remember(launcher) { AndroidFilePicker(launcher = launcher) }
}

private class AndroidFilePicker(
    private val launcher: ActivityResultLauncher<Array<String>>,
) : FilePicker {
    override fun open() {
        launcher.launch(arrayOf(ANY_MIME_TYPE))
    }
}
