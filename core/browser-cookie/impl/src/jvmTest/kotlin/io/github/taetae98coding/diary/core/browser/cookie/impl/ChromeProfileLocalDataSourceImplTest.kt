package io.github.taetae98coding.diary.core.browser.cookie.impl

import io.github.taetae98coding.diary.core.browser.cookie.api.entity.ChromeProfileLocalEntity
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectory
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

// 프로필 목록은 쿠키 저장소를 복사하지 않으므로 복사본 위치를 쓰지 않는다.
private val UNUSED_SNAPSHOT_PARENT_DIRECTORY: Path = Paths.get("/nonexistent")

class ChromeProfileLocalDataSourceImplTest :
    FunSpec({
        test("TC-CHROME-SESSION-IMPORT-DATA-009 프로필 목록을 Chrome이 기록한 이름과 순서로 제공한다") {
            runTest {
                val dataSource =
                    dataSource(
                        localState = """{"profile":{"info_cache":{"Default":{"name":"TaeJong"},"Profile 1":{"name":"Work"}},"last_used":"Default"}}""",
                        dispatcher = StandardTestDispatcher(testScheduler),
                    )

                dataSource.readProfileList() shouldBe
                    listOf(
                        ChromeProfileLocalEntity(directory = "Default", name = "TaeJong"),
                        ChromeProfileLocalEntity(directory = "Profile 1", name = "Work"),
                    )
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-010 이름이 없는 프로필은 폴더 이름으로 제공한다") {
            runTest {
                val dataSource = dataSource(localState = """{"profile":{"info_cache":{"Profile 2":{"name":""},"Profile 3":{}}}}""", dispatcher = StandardTestDispatcher(testScheduler))

                dataSource.readProfileList() shouldBe
                    listOf(
                        ChromeProfileLocalEntity(directory = "Profile 2", name = "Profile 2"),
                        ChromeProfileLocalEntity(directory = "Profile 3", name = "Profile 3"),
                    )
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-011 프로필 정보 파일이 없으면 실패로 알린다") {
            runTest {
                val dataSource =
                    ChromeProfileLocalDataSourceImpl(
                        location = ChromeCookieLocation(isSupported = true, userDataDirectory = createTempDirectory("diary-chrome-missing"), snapshotParentDirectory = UNUSED_SNAPSHOT_PARENT_DIRECTORY),
                        dispatcher = StandardTestDispatcher(testScheduler),
                    )

                shouldThrowAny { dataSource.readProfileList() }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-011 프로필 정보 파일을 읽을 수 없으면 실패로 알린다") {
            runTest {
                val userDataDirectory = createTempDirectory("diary-chrome-unreadable")
                // 같은 이름의 폴더를 두어 파일로 읽을 수 없게 한다.
                userDataDirectory.resolve("Local State").createDirectory()
                val dataSource =
                    ChromeProfileLocalDataSourceImpl(
                        location = ChromeCookieLocation(isSupported = true, userDataDirectory = userDataDirectory, snapshotParentDirectory = UNUSED_SNAPSHOT_PARENT_DIRECTORY),
                        dispatcher = StandardTestDispatcher(testScheduler),
                    )

                shouldThrowAny { dataSource.readProfileList() }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-011 프로필 정보 파일을 해석할 수 없으면 실패로 알린다") {
            runTest {
                val dataSource = dataSource(localState = "not json", dispatcher = StandardTestDispatcher(testScheduler))

                shouldThrowAny { dataSource.readProfileList() }
            }
        }
    })

private fun dataSource(
    localState: String,
    dispatcher: CoroutineDispatcher,
): ChromeProfileLocalDataSourceImpl {
    val userDataDirectory: Path = createTempDirectory("diary-chrome-profile")

    userDataDirectory.resolve("Local State").writeText(localState)

    return ChromeProfileLocalDataSourceImpl(
        location = ChromeCookieLocation(isSupported = true, userDataDirectory = userDataDirectory, snapshotParentDirectory = UNUSED_SNAPSHOT_PARENT_DIRECTORY),
        dispatcher = dispatcher,
    )
}
