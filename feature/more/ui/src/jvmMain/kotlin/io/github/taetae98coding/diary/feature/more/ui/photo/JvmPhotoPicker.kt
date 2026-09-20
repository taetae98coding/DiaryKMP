package io.github.taetae98coding.diary.feature.more.ui.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.core.model.file.FileUri
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
import javax.imageio.ImageIO

@Composable
internal actual fun rememberPhotoPicker(): PhotoPicker = remember { JvmPhotoPicker() }

internal class JvmPhotoPicker : PhotoPicker {
    override suspend fun open(): FileUri? {
        // 소유 창 없이 여는 파일 선택 창은 Frame 오버로드로만 만들 수 있어 타입을 명시한다.
        val dialog =
            FileDialog(null as Frame?, "", FileDialog.LOAD)
                .apply {
                    filenameFilter = imageFilenameFilter()
                    isVisible = true
                }

        return dialog.files.firstOrNull()?.let { file -> FileUri(file.toURI().toString()) }
    }

    private fun imageFilenameFilter(): FilenameFilter =
        FilenameFilter { _, name ->
            IMAGE_SUFFIX_LIST.any { suffix -> name.endsWith(suffix, ignoreCase = true) }
        }

    companion object {
        private val IMAGE_SUFFIX_LIST = ImageIO.getReaderFileSuffixes().map { suffix -> ".$suffix" }
    }
}
