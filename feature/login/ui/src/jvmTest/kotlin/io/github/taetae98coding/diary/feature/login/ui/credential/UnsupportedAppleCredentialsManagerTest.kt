package io.github.taetae98coding.diary.feature.login.ui.credential

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec

class UnsupportedAppleCredentialsManagerTest :
    FunSpec({
        test("Apple 로그인을 제공하지 않는 플랫폼에서는 인증 결과 획득 실패로 처리한다") {
            val manager = UnsupportedAppleCredentialsManager()

            shouldThrowExactly<AppleCredentialsException> {
                manager.signIn()
            }
        }
    })
