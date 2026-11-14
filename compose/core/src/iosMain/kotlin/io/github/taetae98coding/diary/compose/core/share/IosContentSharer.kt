@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.core.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarDefaults
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityItemSourceProtocol
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIPopoverArrowDirectionUp
import platform.UIKit.UIPopoverPresentationController
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController
import platform.darwin.NSObject
import kotlin.coroutines.resume

public class IosContentSharerFactory(
    private val dispatcher: CoroutineDispatcher,
) : ContentSharerFactory {
    @Composable
    override fun rememberContentSharer(): ContentSharer {
        val viewController = LocalUIViewController.current

        return remember(viewController) { IosContentSharer(viewController = viewController, dispatcher = dispatcher) }
    }
}

internal class IosContentSharer(
    private val viewController: UIViewController,
    private val dispatcher: CoroutineDispatcher,
) : ContentSharer {
    override val isTextSupported: Boolean = true

    override fun isFileSupported(type: ShareFileType): Boolean = true

    override suspend fun share(content: ShareContent): ShareResult {
        val items = content.activityItems()
        // 화면 위에 이미 띄운 창이 있으면 그 창이 새 화면을 띄워야 한다. 창에 붙지 않은 화면은 띄워도 완료를 알리지 않는다.
        val presenter = viewController.topPresentedViewController()

        return if (items == null || presenter.view.window == null) ShareResult.FAILED else present(presenter = presenter, items = items)
    }

    // 고른 앱이 실패를 알려도 그 앱이 직접 결과를 보여 주므로 공유 시트를 연 뒤의 결과는 닫은 것과 같이 다룬다.
    private suspend fun present(
        presenter: UIViewController,
        items: List<Any>,
    ): ShareResult {
        val activityViewController = UIActivityViewController(activityItems = items, applicationActivities = null)
        activityViewController.popoverPresentationController?.anchorToTopBarTrailing(view = viewController.view)

        return suspendCancellableCoroutine { continuation ->
            activityViewController.completionWithItemsHandler = { _, isCompleted, _, _ ->
                if (continuation.isActive) {
                    continuation.resume(if (isCompleted) ShareResult.COMPLETED else ShareResult.DISMISSED)
                }
            }
            presenter.presentViewController(activityViewController, animated = true, completion = null)
        }
    }

    private suspend fun ShareContent.activityItems(): List<Any>? =
        when (this) {
            is ShareContent.Text -> listOf(withSubject(item = text, subject = title))

            is ShareContent.File -> {
                val file = write(content = this) ?: return null

                listOfNotNull(withSubject(item = file, subject = title), text.takeIf(String::isNotEmpty))
            }
        }

    private suspend fun write(content: ShareContent.File): NSURL? =
        withContext(dispatcher) {
            val fileManager = NSFileManager.defaultManager
            val directory = NSURL.fileURLWithPath(path = NSTemporaryDirectory(), isDirectory = true).URLByAppendingPathComponent(SHARE_DIRECTORY_NAME)
            val file = directory?.URLByAppendingPathComponent(content.fileName)
            if (directory == null || file == null) return@withContext null

            fileManager.removeItemAtURL(directory, error = null)
            val isCreated = fileManager.createDirectoryAtURL(directory, withIntermediateDirectories = true, attributes = null, error = null)

            file.takeIf { isCreated && content.bytes.toNSData().writeToURL(file, atomically = true) }
        }
}

private fun withSubject(
    item: Any,
    subject: String,
): Any = if (subject.isEmpty()) item else SubjectActivityItemSource(item = item, subject = subject)

private tailrec fun UIViewController.topPresentedViewController(): UIViewController {
    val presented = presentedViewController ?: return this
    return presented.topPresentedViewController()
}

// iPad는 공유 시트를 말풍선으로 띄우므로 가리킬 자리가 있어야 한다. 상단 바 버튼은 시스템 버튼이라 그 자리를 알 수 없어, 끝 쪽 버튼 묶음의 자리를 가리킨다.
private fun UIPopoverPresentationController.anchorToTopBarTrailing(view: UIView) {
    val width = view.bounds.useContents { size.width }
    val (safeTop, safeRight) = view.safeAreaInsets.useContents { top to right }
    val buttonSize = DiaryTopBarDefaults.IosButtonSize.value.toDouble()
    val horizontalPadding = DiaryTopBarDefaults.IosHorizontalPadding.value.toDouble()
    val barHeight = DiaryTopBarDefaults.IosBarHeight.value.toDouble()

    sourceView = view
    sourceRect = CGRectMake(x = width - safeRight - horizontalPadding - buttonSize, y = safeTop, width = buttonSize, height = barHeight)
    permittedArrowDirections = UIPopoverArrowDirectionUp
}

private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()

    return usePinned { pinned -> NSData.create(bytes = pinned.addressOf(0), length = size.convert()) }
}

// 메일처럼 제목을 받는 앱에 공유 제목을 넘기려면 항목을 직접 내주는 대리 객체가 있어야 한다.
private class SubjectActivityItemSource(
    private val item: Any,
    private val subject: String,
) : NSObject(),
    UIActivityItemSourceProtocol {
    override fun activityViewControllerPlaceholderItem(activityViewController: UIActivityViewController): Any = item

    @ObjCSignatureOverride
    override fun activityViewController(
        activityViewController: UIActivityViewController,
        itemForActivityType: String?,
    ): Any = item

    @ObjCSignatureOverride
    override fun activityViewController(
        activityViewController: UIActivityViewController,
        subjectForActivityType: String?,
    ): String = subject
}

private const val SHARE_DIRECTORY_NAME = "share"
