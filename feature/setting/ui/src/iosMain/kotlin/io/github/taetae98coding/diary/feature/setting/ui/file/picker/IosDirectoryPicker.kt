package io.github.taetae98coding.diary.feature.setting.ui.file.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.darwin.NSObject

@Composable
internal actual fun rememberDirectoryPicker(onPick: (uri: String) -> Unit): DirectoryPicker {
    val viewController = LocalUIViewController.current
    val latestOnPick by rememberUpdatedState(onPick)

    return remember(viewController) { IosDirectoryPicker(viewController = viewController, onPick = { uri -> latestOnPick(uri) }) }
}

private class IosDirectoryPicker(
    private val viewController: UIViewController,
    onPick: (String) -> Unit,
) : DirectoryPicker {
    // UIDocumentPickerViewController가 delegate를 약한 참조로 잡으므로 picker가 대신 붙들고 있는다.
    private val delegate = PickDelegate(onPick = onPick)

    // 폴더는 사본으로 받을 수 없으므로 그 자리를 열어 두고, 다음 실행에도 쓸 수 있게 하는 일은 저장할 때 한다.
    override fun open() {
        val pickerViewController = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeFolder), asCopy = false)
        pickerViewController.delegate = delegate
        pickerViewController.allowsMultipleSelection = false

        viewController.presentViewController(pickerViewController, animated = true, completion = null)
    }

    private class PickDelegate(
        private val onPick: (String) -> Unit,
    ) : NSObject(),
        UIDocumentPickerDelegateProtocol {
        override fun documentPicker(
            controller: UIDocumentPickerViewController,
            didPickDocumentsAtURLs: List<*>,
        ) {
            (didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.absoluteString?.let(onPick)
        }
    }
}
