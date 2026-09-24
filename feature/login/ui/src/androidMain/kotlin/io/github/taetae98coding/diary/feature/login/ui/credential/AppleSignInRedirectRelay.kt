package io.github.taetae98coding.diary.feature.login.ui.credential

import android.net.Uri
import kotlinx.coroutines.CompletableDeferred

// 앱 링크는 별도 Activity로 도착하므로, 로그인을 기다리는 화면과 그 Activity가 프로세스 안에서 만나는 자리가 필요하다.
internal object AppleSignInRedirectRelay {
    private var pending: CompletableDeferred<Uri>? = null

    fun begin(): CompletableDeferred<Uri> =
        CompletableDeferred<Uri>().also { deferred ->
            pending?.cancel()
            pending = deferred
        }

    fun end(deferred: CompletableDeferred<Uri>) {
        if (pending === deferred) {
            pending = null
        }
    }

    fun deliver(uri: Uri) {
        pending?.complete(uri)
    }
}
