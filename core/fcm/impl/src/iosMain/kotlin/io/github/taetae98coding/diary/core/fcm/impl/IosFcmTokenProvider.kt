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
    override suspend fun getToken(): String? =
        suspendCancellableCoroutine { continuation ->
            FIRMessaging.messaging().tokenWithCompletion { token: String?, error: NSError? ->
                if (!continuation.isActive) return@tokenWithCompletion

                if (error == null) {
                    continuation.resume(token)
                } else {
                    continuation.resumeWithException(FcmTokenException(message = error.localizedDescription))
                }
            }
        }
}

internal class FcmTokenException(
    message: String,
) : RuntimeException(message)
