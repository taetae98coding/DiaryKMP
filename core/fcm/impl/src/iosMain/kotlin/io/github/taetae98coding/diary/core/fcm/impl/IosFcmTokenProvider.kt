@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.core.fcm.impl

import io.github.taetae98coding.diary.core.fcm.api.FcmTokenProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Factory
import platform.Foundation.NSError
import swiftPMImport.DiaryKmp.core.fcm.core.fcm.impl.FIRMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Factory
internal class IosFcmTokenProvider : FcmTokenProvider {
    override suspend fun getToken(): String? {
        val messaging = FIRMessaging.messaging()

        // 수신 정보는 APNs 기기 토큰이 앱 델리게이트로 전달된 뒤에야 발급되고, 그 전에 요청하면 오류로 답한다.
        if (messaging.APNSToken == null) return null

        return suspendCancellableCoroutine { continuation ->
            messaging.tokenWithCompletion { token: String?, error: NSError? ->
                if (!continuation.isActive) return@tokenWithCompletion

                if (error == null) {
                    continuation.resume(token)
                } else {
                    continuation.resumeWithException(FcmTokenException(message = error.localizedDescription))
                }
            }
        }
    }
}

internal class FcmTokenException(
    message: String,
) : RuntimeException(message)
