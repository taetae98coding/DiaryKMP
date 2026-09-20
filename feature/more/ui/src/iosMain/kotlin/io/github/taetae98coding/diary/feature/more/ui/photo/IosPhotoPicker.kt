@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.feature.more.ui.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import kotlin.coroutines.resume

private const val IMAGE_TYPE_IDENTIFIER = "public.image"

@Composable
internal actual fun rememberPhotoPicker(): PhotoPicker {
    val viewController = LocalUIViewController.current

    return remember(viewController) { IosPhotoPicker(viewController = viewController) }
}

private class IosPhotoPicker(
    private val viewController: UIViewController,
) : PhotoPicker {
    // PHPickerViewController가 delegate를 약한 참조로 잡으므로 picker가 대신 붙들고 있는다.
    private val delegate = ResumeOnFinishDelegate()

    override suspend fun open(): FileUri? =
        suspendCancellableCoroutine { continuation ->
            delegate.continuation = continuation

            val configuration = PHPickerConfiguration()
            configuration.filter = PHPickerFilter.imagesFilter()

            val pickerViewController = PHPickerViewController(configuration = configuration)
            pickerViewController.delegate = delegate

            viewController.presentViewController(pickerViewController, animated = true, completion = null)
        }

    private class ResumeOnFinishDelegate :
        NSObject(),
        PHPickerViewControllerDelegateProtocol {
        var continuation: CancellableContinuation<FileUri?>? = null

        override fun picker(
            picker: PHPickerViewController,
            didFinishPicking: List<*>,
        ) {
            picker.dismissViewControllerAnimated(flag = true, completion = null)

            val itemProvider = (didFinishPicking.firstOrNull() as? PHPickerResult)?.itemProvider

            if (itemProvider == null) {
                resume(uri = null)
                return
            }

            itemProvider.loadFileRepresentationForTypeIdentifier(IMAGE_TYPE_IDENTIFIER) { url, _ ->
                // 시스템이 준 위치는 이 블록이 끝나면 사라지므로 앱 임시 디렉터리로 복사한 위치를 돌려준다.
                resume(uri = url?.copyToTemporary())
            }
        }

        private fun resume(uri: FileUri?) {
            val pendingContinuation = continuation
            continuation = null

            if (pendingContinuation?.isActive == true) {
                pendingContinuation.resume(uri)
            }
        }
    }
}

// 사진 내용은 뒤에서 변환기가 위치로 직접 읽으므로, 여기서는 메모리에 올리지 않고 파일 시스템에 복사만 맡긴다.
private fun NSURL.copyToTemporary(): FileUri? {
    val extension = pathExtension.orEmpty()
    val name = if (extension.isEmpty()) NSUUID().UUIDString else "${NSUUID().UUIDString}.$extension"
    val destination = NSURL.fileURLWithPath(NSTemporaryDirectory() + name)

    return if (NSFileManager.defaultManager.copyItemAtURL(this, destination, null)) {
        destination.absoluteString?.let { absoluteString -> FileUri(absoluteString) }
    } else {
        null
    }
}
