package io.github.taetae98coding.diary.library.applicationsupport

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.nio.file.Paths

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// 생성 문자열에 경로 구분자나 NUL이 섞이면 경로를 만들 수 없어 경로에 쓸 수 있는 문자만 남긴다.
private fun pathSegment(prefix: String): String = prefix + fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)

class ApplicationSupportDirectoryTest :
    FunSpec({
        test("TC-0e9a5c1b-a8e3-49c7-9135-ca01551db260 Mac App Store로 배포한 앱은 앱 전용 보관 공간의 홈 폴더를 기준으로 삼는다") {
            val userHome = "/Users/" + pathSegment(prefix = "home-")
            val containerHome = "$userHome/Library/Containers/" + pathSegment(prefix = "app-") + "/Data"
            val cases =
                listOf(
                    mapOf("APP_SANDBOX_CONTAINER_ID" to pathSegment(prefix = "app-"), "HOME" to containerHome) to containerHome,
                    mapOf("HOME" to userHome) to userHome,
                )

            cases.forEach { (environment, expectedHome) ->
                userHomeDirectory(environment = environment, userHomeProperty = userHome) shouldBe Paths.get(expectedHome)
            }
        }

        test("사용자 홈의 Application Support 아래 이름이 같은 영역을 가리킨다") {
            val userHome = Paths.get("/Users", pathSegment(prefix = "home-"))
            val directoryName = pathSegment(prefix = "Diary-")

            val directory = applicationSupportDirectory(directoryName = directoryName, userHome = userHome)

            directory.parent shouldBe userHome.resolve("Library/Application Support")
            directory.fileName.toString() shouldBe directoryName
        }

        test("영역 이름이 다르면 서로 다른 경로를 가리킨다") {
            val userHome = Paths.get("/Users", pathSegment(prefix = "home-"))
            val directoryName = pathSegment(prefix = "Diary-")

            applicationSupportDirectory(directoryName = "$directoryName-dev", userHome = userHome) shouldNotBe
                applicationSupportDirectory(directoryName = directoryName, userHome = userHome)
        }

        test("사용자 홈은 실행 중인 사용자의 홈 디렉터리를 가리킨다") {
            userHomeDirectory() shouldBe Paths.get(System.getProperty("user.home"))
        }

        test("영역 이름만 넘기면 실행 중인 사용자 홈 아래 영역을 가리킨다") {
            val directoryName = pathSegment(prefix = "Diary-")

            applicationSupportDirectory(directoryName = directoryName) shouldBe
                applicationSupportDirectory(directoryName = directoryName, userHome = userHomeDirectory())
        }
    })
