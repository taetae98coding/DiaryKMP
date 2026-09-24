package io.github.taetae98coding.diary.feature.more.ui.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
import javax.imageio.ImageIO

@Composable
internal actual fun rememberPhotoPicker(): PhotoPicker {
    val dispatcher = koinInject<CoroutineDispatcher>(qualifier = named<PhotoPickerDispatcher>())

    return remember(dispatcher) { JvmPhotoPicker(dispatcher = dispatcher) }
}

internal class JvmPhotoPicker(
    private val dispatcher: CoroutineDispatcher,
) : PhotoPicker {
    // 모달 대화상자는 닫힐 때까지 호출한 스레드를 막는다. Compose 디스패처가 코루틴을 실행하는 도중에 띄우면
    // 중첩 이벤트 루프가 그 디스패처를 재진입해 실행 목록을 깨뜨리므로, 별도 스레드에서 열고 EDT는 그대로 둔다.
    override suspend fun open(): FileUri? =
        withContext(dispatcher) {
            // 소유 창 없이 여는 파일 선택 창은 Frame 오버로드로만 만들 수 있어 타입을 명시한다.
            val dialog =
                FileDialog(null as Frame?, "", FileDialog.LOAD)
                    .apply {
                        filenameFilter = imageFilenameFilter()
                        isVisible = true
                    }

            dialog.files.firstOrNull()?.let { file -> FileUri(file.toURI().toString()) }
        }

    private fun imageFilenameFilter(): FilenameFilter =
        FilenameFilter { _, name ->
            IMAGE_SUFFIX_LIST.any { suffix -> name.endsWith(suffix, ignoreCase = true) }
        }

    companion object {
        private val IMAGE_SUFFIX_LIST = ImageIO.getReaderFileSuffixes().map { suffix -> ".$suffix" }
    }
}
