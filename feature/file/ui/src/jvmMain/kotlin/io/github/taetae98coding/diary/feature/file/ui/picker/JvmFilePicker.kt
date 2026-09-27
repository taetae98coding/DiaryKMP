package io.github.taetae98coding.diary.feature.file.ui.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import java.awt.FileDialog
import java.awt.Frame

@Composable
internal actual fun rememberFilePicker(onPick: (FileUri) -> Unit): FilePicker {
    val dispatcher = koinInject<CoroutineDispatcher>(qualifier = named<FilePickerDispatcher>())
    val coroutineScope = rememberCoroutineScope()
    val latestOnPick by rememberUpdatedState(onPick)

    return remember(dispatcher, coroutineScope) {
        JvmFilePicker(
            dispatcher = dispatcher,
            coroutineScope = coroutineScope,
            onPick = { uri -> latestOnPick(uri) },
        )
    }
}

internal class JvmFilePicker(
    private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    private val onPick: (FileUri) -> Unit,
) : FilePicker {
    // 모달 대화상자는 닫힐 때까지 호출한 스레드를 막는다. Compose 디스패처가 코루틴을 실행하는 도중에 띄우면
    // 중첩 이벤트 루프가 그 디스패처를 재진입해 실행 목록을 깨뜨리므로, 별도 스레드에서 열고 EDT는 그대로 둔다.
    override fun open() {
        coroutineScope.launch {
            withContext(dispatcher) { pick() }?.let(onPick)
        }
    }

    private fun pick(): FileUri? {
        // 소유 창 없이 여는 파일 선택 창은 Frame 오버로드로만 만들 수 있어 타입을 명시한다.
        val dialog =
            FileDialog(null as Frame?, "", FileDialog.LOAD)
                .apply { isVisible = true }

        return dialog.files.firstOrNull()?.let { file -> FileUri(file.toURI().toString()) }
    }
}
