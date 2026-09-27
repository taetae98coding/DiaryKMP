package io.github.taetae98coding.diary.feature.file.ui.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import io.github.taetae98coding.diary.core.model.file.FileUri
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.darwin.NSObject

@Composable
internal actual fun rememberFilePicker(onPick: (FileUri) -> Unit): FilePicker {
    val viewController = LocalUIViewController.current
    val latestOnPick by rememberUpdatedState(onPick)

    return remember(viewController) { IosFilePicker(viewController = viewController, onPick = { uri -> latestOnPick(uri) }) }
}

private class IosFilePicker(
    private val viewController: UIViewController,
    onPick: (FileUri) -> Unit,
) : FilePicker {
    // UIDocumentPickerViewController가 delegate를 약한 참조로 잡으므로 picker가 대신 붙들고 있는다.
    private val delegate = PickDelegate(onPick = onPick)

    // 사본으로 열면 시스템이 앱 임시 디렉터리에 복사한 위치를 주므로, 보안 범위 접근 없이 나중에 읽을 수 있다.
    override fun open() {
        val pickerViewController = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeItem), asCopy = true)
        pickerViewController.delegate = delegate
        pickerViewController.allowsMultipleSelection = false

        viewController.presentViewController(pickerViewController, animated = true, completion = null)
    }

    private class PickDelegate(
        private val onPick: (FileUri) -> Unit,
    ) : NSObject(),
        UIDocumentPickerDelegateProtocol {
        override fun documentPicker(
            controller: UIDocumentPickerViewController,
            didPickDocumentsAtURLs: List<*>,
        ) {
            (didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.absoluteString?.let { absoluteString -> onPick(FileUri(absoluteString)) }
        }
    }
}
