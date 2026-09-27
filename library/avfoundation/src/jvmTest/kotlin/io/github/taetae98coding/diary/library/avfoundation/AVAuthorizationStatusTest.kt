package io.github.taetae98coding.diary.library.avfoundation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AVAuthorizationStatusTest :
    FunSpec({
        test("AVAuthorizationStatus의 NSInteger 값을 같은 의미의 상태로 옮긴다") {
            mapOf(
                0L to AVAuthorizationStatus.NOT_DETERMINED,
                1L to AVAuthorizationStatus.RESTRICTED,
                2L to AVAuthorizationStatus.DENIED,
                3L to AVAuthorizationStatus.AUTHORIZED,
            ).forEach { (rawValue, status) ->
                avAuthorizationStatus(rawValue) shouldBe status
            }
        }

        test("알 수 없는 값은 카메라를 쓸 수 없는 거부로 본다") {
            listOf(-1L, 4L, Long.MAX_VALUE).forEach { rawValue ->
                avAuthorizationStatus(rawValue) shouldBe AVAuthorizationStatus.DENIED
            }
        }

        test("시스템이 알려 주는 카메라 권한 상태를 읽을 수 있다") {
            AVAuthorizationStatus.entries.contains(AVCaptureVideoAuthorization.status()) shouldBe true
        }
    })
