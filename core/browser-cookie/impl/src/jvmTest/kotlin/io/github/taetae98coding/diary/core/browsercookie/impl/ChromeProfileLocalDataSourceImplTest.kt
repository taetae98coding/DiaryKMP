package io.github.taetae98coding.diary.core.browsercookie.impl

import io.github.taetae98coding.diary.core.browsercookie.api.entity.ChromeProfileLocalEntity
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

class ChromeProfileLocalDataSourceImplTest :
    FunSpec({
        test("TC-CHROME-SESSION-IMPORT-DATA-009 프로필 목록을 Chrome이 기록한 이름과 순서로 제공한다") {
            val dataSource =
                dataSource(
                    localState = """{"profile":{"info_cache":{"Default":{"name":"TaeJong"},"Profile 1":{"name":"Work"}},"last_used":"Default"}}""",
                )

            dataSource.findAll() shouldBe
                listOf(
                    ChromeProfileLocalEntity(directory = "Default", name = "TaeJong"),
                    ChromeProfileLocalEntity(directory = "Profile 1", name = "Work"),
                )
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-010 이름이 없는 프로필은 폴더 이름으로 제공한다") {
            val dataSource = dataSource(localState = """{"profile":{"info_cache":{"Profile 2":{"name":""},"Profile 3":{}}}}""")

            dataSource.findAll() shouldBe
                listOf(
                    ChromeProfileLocalEntity(directory = "Profile 2", name = "Profile 2"),
                    ChromeProfileLocalEntity(directory = "Profile 3", name = "Profile 3"),
                )
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-011 프로필 정보 파일이 없으면 실패로 알린다") {
            val dataSource =
                ChromeProfileLocalDataSourceImpl(
                    location = ChromeCookieLocation(isSupported = true, userDataDirectory = createTempDirectory("diary-chrome-missing")),
                    dispatcher = Dispatchers.Default,
                )

            shouldThrowAny { dataSource.findAll() }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-011 프로필 정보 파일을 해석할 수 없으면 실패로 알린다") {
            val dataSource = dataSource(localState = "not json")

            shouldThrowAny { dataSource.findAll() }
        }
    })

private fun dataSource(localState: String): ChromeProfileLocalDataSourceImpl {
    val userDataDirectory: Path = createTempDirectory("diary-chrome-profile")

    userDataDirectory.resolve("Local State").writeText(localState)

    return ChromeProfileLocalDataSourceImpl(
        location = ChromeCookieLocation(isSupported = true, userDataDirectory = userDataDirectory),
        dispatcher = Dispatchers.Default,
    )
}
