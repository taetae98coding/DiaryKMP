package io.github.taetae98coding.diary.core.browsercookie.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Paths

class ChromeCookieLocationTest :
    FunSpec({
        test("macOS에서만 Chrome 로그인 이어받기를 제공한다") {
            val cases =
                mapOf(
                    "Mac OS X" to true,
                    "macOS" to true,
                    "Linux" to false,
                    "Windows 11" to false,
                )

            cases.forEach { (osName, isSupported) ->
                ChromeCookieLocation.current(osName = osName, userHome = Paths.get("/Users/user")).isSupported shouldBe isSupported
            }
        }

        test("프로필 폴더의 쿠키 저장소와 프로필 정보 파일을 가리킨다") {
            val location = ChromeCookieLocation.current(osName = "Mac OS X", userHome = Paths.get("/Users/user"))

            location.cookiesPath(profileDirectory = "Profile 1") shouldBe
                Paths.get("/Users/user/Library/Application Support/Google/Chrome/Profile 1/Cookies")
            location.localStatePath shouldBe Paths.get("/Users/user/Library/Application Support/Google/Chrome/Local State")
        }
    })
