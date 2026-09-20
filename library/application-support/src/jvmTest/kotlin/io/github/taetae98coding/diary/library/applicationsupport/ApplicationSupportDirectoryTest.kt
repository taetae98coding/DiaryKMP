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
    })
