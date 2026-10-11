@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.core.fcm.impl

import io.github.taetae98coding.diary.core.fcm.api.FcmTokenProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Factory
import platform.Foundation.NSError
import swiftPMImport.DiaryKmp.core.fcm.core.fcm.impl.FIRInstallations
import swiftPMImport.DiaryKmp.core.fcm.core.fcm.impl.FIRMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Factory
internal class IosFcmTokenProvider : FcmTokenProvider {
    override suspend fun getInstallationId(): String? {
        val messaging = FIRMessaging.messaging()

        // 설치 ID 등록은 APNs 기기 토큰이 앱 델리게이트로 전달된 뒤에야 알림으로 이어지므로, 그 전에는 등록하지 않는다.
        if (messaging.APNSToken == null) return null

        messaging.register()

        // register는 등록이 끝난 설치 ID를 돌려주지 않고 메시징 델리게이트로만 알리므로, 등록이 끝난 뒤 설치 ID를 따로 읽는다.
        return suspendCancellableCoroutine { continuation ->
            FIRInstallations.installations().installationIDWithCompletion { installationId: String?, error: NSError? ->
                if (!continuation.isActive) return@installationIDWithCompletion

                if (error == null) {
                    continuation.resume(installationId)
                } else {
                    continuation.resumeWithException(FcmTokenException(message = error.localizedDescription))
                }
            }
        }
    }

    private suspend fun FIRMessaging.register() {
        suspendCancellableCoroutine { continuation ->
            registerWithCompletion { error: NSError? ->
                if (!continuation.isActive) return@registerWithCompletion

                if (error == null) {
                    continuation.resume(Unit)
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
