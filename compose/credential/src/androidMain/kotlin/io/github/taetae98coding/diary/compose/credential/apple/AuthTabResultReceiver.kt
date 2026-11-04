package io.github.taetae98coding.diary.compose.credential.apple

import android.net.Uri
import kotlinx.coroutines.CompletableDeferred

// 결과 런처는 컴포지션에 등록되고 그 런처를 매니저가 쓰므로, 둘이 서로를 참조하지 않도록 결과는 이 수신자를 거쳐 기다리는 로그인에 전달한다.
internal class AuthTabResultReceiver {
    private var pending: CompletableDeferred<AuthTabResult>? = null

    fun begin(): CompletableDeferred<AuthTabResult> =
        CompletableDeferred<AuthTabResult>().also { deferred ->
            pending?.cancel()
            pending = deferred
        }

    fun end(deferred: CompletableDeferred<AuthTabResult>) {
        if (pending === deferred) pending = null
    }

    fun deliver(
        resultCode: Int,
        resultUri: Uri?,
    ) {
        pending?.complete(AuthTabResult(resultCode = resultCode, resultUri = resultUri))
    }
}

internal class AuthTabResult(
    val resultCode: Int,
    val resultUri: Uri?,
)
