package io.github.taetae98coding.diary.feature.setting.ui.file.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

private const val MAC_DIRECTORY_DIALOG_PROPERTY = "apple.awt.fileDialogForDirectories"
private const val OS_NAME_PROPERTY = "os.name"
private const val MAC_OS_NAME_PREFIX = "Mac"

@Composable
internal actual fun rememberDirectoryPicker(onPick: (uri: String) -> Unit): DirectoryPicker {
    val dispatcher = koinInject<CoroutineDispatcher>(qualifier = named<DirectoryPickerDispatcher>())
    val coroutineScope = rememberCoroutineScope()
    val latestOnPick by rememberUpdatedState(onPick)

    return remember(dispatcher, coroutineScope) {
        JvmDirectoryPicker(
            dispatcher = dispatcher,
            coroutineScope = coroutineScope,
            onPick = { uri -> latestOnPick(uri) },
        )
    }
}

internal class JvmDirectoryPicker(
    private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    private val onPick: (String) -> Unit,
) : DirectoryPicker {
    // 모달 대화상자는 닫힐 때까지 호출한 스레드를 막는다. Compose 디스패처가 코루틴을 실행하는 도중에 띄우면
    // 중첩 이벤트 루프가 그 디스패처를 재진입해 실행 목록을 깨뜨리므로, 별도 스레드에서 열고 EDT는 그대로 둔다.
    override fun open() {
        coroutineScope.launch {
            withContext(dispatcher) { pick() }?.let { directory -> onPick(directory.absolutePath) }
        }
    }

    // macOS의 기본 대화상자는 속성을 켜야 폴더를 고를 수 있고, 그 밖의 운영체제는 Swing의 폴더 선택기를 쓴다.
    private fun pick(): File? =
        if (System.getProperty(OS_NAME_PROPERTY).orEmpty().startsWith(MAC_OS_NAME_PREFIX)) {
            pickWithFileDialog()
        } else {
            pickWithFileChooser()
        }

    private fun pickWithFileDialog(): File? {
        System.setProperty(MAC_DIRECTORY_DIALOG_PROPERTY, true.toString())

        return try {
            // 소유 창 없이 여는 파일 선택 창은 Frame 오버로드로만 만들 수 있어 타입을 명시한다.
            val dialog =
                FileDialog(null as Frame?, "", FileDialog.LOAD)
                    .apply { isVisible = true }

            dialog.files.firstOrNull()
        } finally {
            System.setProperty(MAC_DIRECTORY_DIALOG_PROPERTY, false.toString())
        }
    }

    private fun pickWithFileChooser(): File? {
        val chooser = JFileChooser().apply { fileSelectionMode = JFileChooser.DIRECTORIES_ONLY }

        return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
    }
}
