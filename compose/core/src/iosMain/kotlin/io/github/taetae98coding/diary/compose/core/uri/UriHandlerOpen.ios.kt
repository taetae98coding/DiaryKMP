package io.github.taetae98coding.diary.compose.core.uri

import androidx.compose.ui.platform.UriHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL.Companion.URLWithString
import platform.UIKit.UIApplication
import kotlin.coroutines.resume

// iOS는 주소를 열지 못한 것을 예외가 아니라 완료 결과로 알려 주고, UriHandler는 그 결과를 받지 않으므로 직접 연다.
public actual suspend fun UriHandler.tryOpenUri(uri: String): Boolean {
    val url = URLWithString(uri) ?: return false

    return suspendCancellableCoroutine { continuation ->
        UIApplication.sharedApplication.openURL(
            url = url,
            options = emptyMap<Any?, Any>(),
            completionHandler = { isOpened -> continuation.resume(isOpened) },
        )
    }
}
