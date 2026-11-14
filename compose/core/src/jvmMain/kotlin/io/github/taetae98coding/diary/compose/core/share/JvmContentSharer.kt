@file:OptIn(ExperimentalComposeUiApi::class)

package io.github.taetae98coding.diary.compose.core.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import java.awt.datatransfer.StringSelection

public object JvmContentSharerFactory : ContentSharerFactory {
    @Composable
    override fun rememberContentSharer(): ContentSharer {
        val clipboard = LocalClipboard.current

        return remember(clipboard) { JvmContentSharer(clipboard = clipboard) }
    }
}

internal class JvmContentSharer(
    private val clipboard: Clipboard,
) : ContentSharer {
    override val isTextSupported: Boolean = true

    override fun isFileSupported(type: ShareFileType): Boolean = false

    override suspend fun share(content: ShareContent): ShareResult =
        when (content) {
            is ShareContent.Text -> copy(text = content.text)
            is ShareContent.File -> ShareResult.FAILED
        }

    // 다른 프로그램이 시스템 클립보드를 붙잡고 있으면 AWT가 IllegalStateException을 던진다.
    private suspend fun copy(text: String): ShareResult =
        try {
            clipboard.setClipEntry(ClipEntry(StringSelection(text)))
            ShareResult.COPIED
        } catch (_: IllegalStateException) {
            ShareResult.FAILED
        }
}
