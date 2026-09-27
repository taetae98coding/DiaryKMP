@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.feature.file.ui.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File

@Composable
internal actual fun rememberFilePicker(onPick: (FileUri) -> Unit): FilePicker {
    val latestOnPick by rememberUpdatedState(onPick)

    return remember { WasmFilePicker(onPick = { uri -> latestOnPick(uri) }) }
}

internal class WasmFilePicker(
    private val onPick: (FileUri) -> Unit,
) : FilePicker {
    // 브라우저는 사용자 조작 안에서 연 파일 선택 창만 허용하므로 미리 만들어 두지 않고 열 때 만든다.
    override fun open() {
        val input = document.createElement("input") as HTMLInputElement

        input.type = "file"
        input.addEventListener(
            type = "change",
            callback = {
                input.files?.item(0)?.let { file -> onPick(FileUri(namedObjectUrl(file))) }
            },
        )

        input.click()
    }
}

// 고른 파일은 경로가 없어 브라우저가 만들어 주는 blob URL로 가리키고, blob URL이 잃는 파일 이름은 fragment에 담는다.
@Suppress("UnusedParameter")
private fun namedObjectUrl(file: File): String = js("URL.createObjectURL(file) + '#' + encodeURIComponent(file.name)")
