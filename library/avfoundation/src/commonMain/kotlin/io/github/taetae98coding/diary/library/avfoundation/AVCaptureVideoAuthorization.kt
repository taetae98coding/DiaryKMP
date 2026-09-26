package io.github.taetae98coding.diary.library.avfoundation

import io.github.taetae98coding.diary.library.objc.ObjCBlock
import io.github.taetae98coding.diary.library.objc.ObjCRuntime
import io.github.taetae98coding.diary.library.objc.sendLong
import io.github.taetae98coding.diary.library.objc.sendVoid
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 기본 영상 입력(카메라)에 대한 시스템 접근 권한을 읽고 요청한다. 요청은 권한이 결정되지 않았을 때만 시스템 요청을 표시하고,
 * 이미 결정된 뒤에는 표시 없이 그 결정을 돌려준다.
 */
public object AVCaptureVideoAuthorization {
    public fun status(): AVAuthorizationStatus =
        ObjCRuntime.withAutoreleasePool {
            val rawValue =
                avFoundationClass("AVCaptureDevice")
                    .sendLong(ObjCRuntime.selector("authorizationStatusForMediaType:"), avFoundationString("AVMediaTypeVideo"))

            avAuthorizationStatus(rawValue)
        }

    // completion handler는 임의의 스레드에서 호출된다.
    public suspend fun requestAccess(): Boolean =
        suspendCancellableCoroutine { continuation ->
            val completion = ObjCBlock.createBoolean { isGranted -> if (continuation.isActive) continuation.resume(isGranted) }

            ObjCRuntime.withAutoreleasePool {
                avFoundationClass("AVCaptureDevice").sendVoid(
                    ObjCRuntime.selector("requestAccessForMediaType:completionHandler:"),
                    avFoundationString("AVMediaTypeVideo"),
                    completion,
                )
            }
        }
}
