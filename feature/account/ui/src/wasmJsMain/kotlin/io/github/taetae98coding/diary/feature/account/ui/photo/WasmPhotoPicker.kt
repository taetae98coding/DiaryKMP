@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.feature.account.ui.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import kotlin.coroutines.resume

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<PhotoPickerFactory>())
internal class WasmPhotoPickerFactory : PhotoPickerFactory {
    @Composable
    override fun rememberPhotoPicker(): PhotoPicker = remember { WasmPhotoPicker() }
}

internal class WasmPhotoPicker : PhotoPicker {
    // 브라우저는 사용자 조작 안에서 연 파일 선택 창만 허용하므로 미리 만들어 두지 않고 열 때 만든다.
    override suspend fun open(): FileUri? =
        suspendCancellableCoroutine { continuation ->
            val input = document.createElement("input") as HTMLInputElement

            input.type = "file"
            input.accept = "image/*"
            input.addEventListener(
                type = "change",
                callback = {
                    if (continuation.isActive) {
                        continuation.resume(input.files?.item(0)?.let { file -> FileUri(objectUrl(file)) })
                    }
                },
            )
            // 파일 선택 창을 닫기만 하면 change가 오지 않으므로 cancel도 함께 듣는다.
            input.addEventListener(
                type = "cancel",
                callback = {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                },
            )

            input.click()
        }
}

// 고른 파일은 경로가 없어 브라우저가 만들어 주는 blob URL로 가리킨다.
@Suppress("UnusedParameter")
private fun objectUrl(file: File): String = js("URL.createObjectURL(file)")
