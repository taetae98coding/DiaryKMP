package io.github.taetae98coding.diary.library.avfoundation

import io.github.taetae98coding.diary.library.objc.ObjCRuntime
import io.github.taetae98coding.diary.library.objc.release
import io.github.taetae98coding.diary.library.objc.send
import io.github.taetae98coding.diary.library.objc.sendBoolean
import io.github.taetae98coding.diary.library.objc.sendVoid
import java.lang.foreign.MemorySegment

/**
 * 시스템 기본 카메라로 AVCaptureSession을 구성하고 QR 코드를 인식한다. 인식한 값은 dispatch 메인 큐(AppKit 메인 스레드)에서
 * onDetect로 전달된다. [startRunning]과 [stopRunning]은 끝날 때까지 호출한 스레드를 막으므로 메인 스레드 밖에서 부르고,
 * 세션을 멈춘 뒤 [close]로 해제한다.
 */
public class AVCaptureQrScanner private constructor(
    internal val session: MemorySegment,
    private val output: MemorySegment,
    private val delegate: AVCaptureMetadataDelegate,
) : AutoCloseable {
    public fun startRunning() {
        ObjCRuntime.withAutoreleasePool { session.sendVoid(ObjCRuntime.selector("startRunning")) }
    }

    public fun stopRunning() {
        ObjCRuntime.withAutoreleasePool { session.sendVoid(ObjCRuntime.selector("stopRunning")) }
    }

    // 대리 객체 호출은 메인 큐에 쌓이므로, 떼어 내고 해제하는 일도 메인 큐에서 해 이미 쌓인 호출이 먼저 끝나게 한다.
    override fun close() {
        ObjCRuntime.performOnMainThread {
            output.sendVoid(ObjCRuntime.selector("setMetadataObjectsDelegate:queue:"), MemorySegment.NULL, MemorySegment.NULL)
            delegate.dispose()
            session.release()
        }
    }

    public companion object {
        /**
         * 기본 카메라가 없거나 입력·출력을 세션에 붙일 수 없으면 null을 돌려준다. 카메라 장치를 여는 동안 호출한 스레드를 막으므로
         * 메인 스레드 밖에서 부른다.
         */
        public fun create(onDetect: (String) -> Unit): AVCaptureQrScanner? =
            ObjCRuntime.withAutoreleasePool {
                defaultVideoInput()?.let { input -> createSession(input = input, onDetect = onDetect) }
            }

        // deviceInputWithDevice:error:는 autorelease된 객체를 반환하므로 withAutoreleasePool 안에서 쓴다.
        private fun defaultVideoInput(): MemorySegment? {
            val device =
                avFoundationClass("AVCaptureDevice")
                    .send(ObjCRuntime.selector("defaultDeviceWithMediaType:"), avFoundationString("AVMediaTypeVideo"))
                    .takeUnless { segment -> segment.isNil() }
                    ?: return null

            return avFoundationClass("AVCaptureDeviceInput")
                .send(ObjCRuntime.selector("deviceInputWithDevice:error:"), device, MemorySegment.NULL)
                .takeUnless { segment -> segment.isNil() }
        }

        private fun createSession(
            input: MemorySegment,
            onDetect: (String) -> Unit,
        ): AVCaptureQrScanner? {
            val session = avFoundationClass("AVCaptureSession").send(ObjCRuntime.selector("new"))
            val output = avFoundationClass("AVCaptureMetadataOutput").send(ObjCRuntime.selector("new"))
            val canAdd =
                session.sendBoolean(ObjCRuntime.selector("canAddInput:"), input) &&
                    session.sendBoolean(ObjCRuntime.selector("canAddOutput:"), output)

            if (!canAdd) {
                output.release()
                session.release()
                return null
            }

            val delegate = AVCaptureMetadataDelegate(onDetect = onDetect)

            session.sendVoid(ObjCRuntime.selector("addInput:"), input)
            session.sendVoid(ObjCRuntime.selector("addOutput:"), output)
            output.sendVoid(ObjCRuntime.selector("setMetadataObjectsDelegate:queue:"), delegate.pointer, ObjCRuntime.mainQueue)
            output.selectQrCodeType()
            // 세션이 출력을 붙잡고 있으므로 여기서 만든 참조는 놓는다.
            output.release()

            return AVCaptureQrScanner(session = session, output = output, delegate = delegate)
        }

        // 인식할 코드 종류는 출력을 세션에 붙인 뒤에야 고를 수 있고, 지원하지 않는 종류를 고르면 NSException이 나 JVM이 중단된다.
        private fun MemorySegment.selectQrCodeType() {
            val qrCodeType = avFoundationString("AVMetadataObjectTypeQRCode")
            val availableTypes = send(ObjCRuntime.selector("availableMetadataObjectTypes"))

            if (!availableTypes.sendBoolean(ObjCRuntime.selector("containsObject:"), qrCodeType)) return

            val types = ObjCRuntime.objcClass("NSArray").send(ObjCRuntime.selector("arrayWithObject:"), qrCodeType)

            sendVoid(ObjCRuntime.selector("setMetadataObjectTypes:"), types)
        }
    }
}
