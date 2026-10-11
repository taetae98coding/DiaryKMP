@file:OptIn(ExperimentalPathApi::class)

package io.github.taetae98coding.diary.core.browser.cookie.impl

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.browser.cookie.api.entity.BrowserCookieLocalEntity
import io.github.taetae98coding.diary.core.browser.cookie.api.entity.BrowserCookieSameSiteLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private const val PROFILE_DIRECTORY = "Default"

class ChromeCookieLocalDataSourceImplTest :
    FunSpec({
        lateinit var snapshotParentDirectory: Path

        beforeTest {
            snapshotParentDirectory = createTempDirectory("diary-chrome-snapshot-test")
        }

        afterTest {
            snapshotParentDirectory.deleteRecursively()
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-001 암호화된 쿠키 값을 풀어 제공한다") {
            runTest {
                val key = randomAesKey()
                val value = "value-${fixtureMonkey.giveMeOne<String>()}"
                val database = createDatabase(ChromeCookieTestRow(hostKey = ".example.com", name = "session", encryptedValue = encryptChromeCookieValue(value, ".example.com", key)))
                val dataSource = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = database, key = key, dispatcher = StandardTestDispatcher(testScheduler))

                val cookieList = dataSource.readCookieList(profileDirectory = PROFILE_DIRECTORY)

                cookieList.map { cookie -> cookie.name to cookie.value } shouldBe listOf("session" to value)
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-013 암호화되지 않은 쿠키 값은 그대로 제공하고 키체인을 읽지 않는다") {
            runTest {
                val database = createDatabase(ChromeCookieTestRow(hostKey = "example.com", name = "plain", value = "plain-value"))
                val dataSource = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = database, keyProvider = { error("keychain must not be read") }, dispatcher = StandardTestDispatcher(testScheduler))

                dataSource.readCookieList(profileDirectory = PROFILE_DIRECTORY).map { cookie -> cookie.value } shouldBe listOf("plain-value")
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-012 저장소의 쿠키를 도메인과 관계없이 모두 제공한다") {
            runTest {
                val key = randomAesKey()
                val database =
                    createDatabase(
                        ChromeCookieTestRow(hostKey = "example.com", name = "host", encryptedValue = encryptChromeCookieValue("1", "example.com", key)),
                        ChromeCookieTestRow(hostKey = ".example.com", name = "domain", encryptedValue = encryptChromeCookieValue("2", ".example.com", key)),
                        ChromeCookieTestRow(hostKey = "other.example.org", name = "other", encryptedValue = encryptChromeCookieValue("3", "other.example.org", key)),
                    )
                val dataSource = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = database, key = key, dispatcher = StandardTestDispatcher(testScheduler))

                val cookieList = dataSource.readCookieList(profileDirectory = PROFILE_DIRECTORY)

                cookieList.map { cookie -> cookie.domain to cookie.name } shouldContainExactlyInAnyOrder
                    listOf("example.com" to "host", ".example.com" to "domain", "other.example.org" to "other")
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-003 특정 상위 사이트 안에서만 쓰는 쿠키는 제공하지 않는다") {
            runTest {
                val key = randomAesKey()
                val database =
                    createDatabase(
                        ChromeCookieTestRow(hostKey = ".example.com", name = "normal", encryptedValue = encryptChromeCookieValue("1", ".example.com", key)),
                        ChromeCookieTestRow(
                            hostKey = ".example.com",
                            name = "partitioned",
                            encryptedValue = encryptChromeCookieValue("2", ".example.com", key),
                            topFrameSiteKey = "https://top.example.org",
                        ),
                    )
                val dataSource = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = database, key = key, dispatcher = StandardTestDispatcher(testScheduler))

                dataSource.readCookieList(profileDirectory = PROFILE_DIRECTORY).map { cookie -> cookie.name } shouldBe listOf("normal")
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-004 쿠키 속성을 Chrome 저장소의 값대로 제공한다") {
            runTest {
                val key = randomAesKey()
                val expiresAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Instant>().toEpochMilliseconds())
                val cases =
                    listOf(
                        ChromeCookieTestRow(hostKey = ".example.com", name = "a", path = "/a", expiresAt = expiresAt, isSecure = true, isHttpOnly = true, sameSite = -1L) to
                            BrowserCookieSameSiteLocalEntity.UNSPECIFIED,
                        ChromeCookieTestRow(hostKey = ".example.com", name = "b", path = "/b", expiresAt = null, isSecure = false, isHttpOnly = false, sameSite = 2L) to
                            BrowserCookieSameSiteLocalEntity.STRICT,
                        ChromeCookieTestRow(hostKey = ".example.com", name = "c", path = "/c", expiresAt = expiresAt, isSecure = true, isHttpOnly = false, sameSite = 1L) to
                            BrowserCookieSameSiteLocalEntity.LAX,
                        ChromeCookieTestRow(hostKey = ".example.com", name = "d", path = "/d", expiresAt = expiresAt, isSecure = false, isHttpOnly = true, sameSite = 0L) to
                            BrowserCookieSameSiteLocalEntity.NONE,
                    )
                val database =
                    createDatabase(
                        *cases
                            .map { (row, _) -> row.copy(encryptedValue = encryptChromeCookieValue(row.name, row.hostKey, key)) }
                            .toTypedArray(),
                    )
                val dataSource = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = database, key = key, dispatcher = StandardTestDispatcher(testScheduler))

                val cookieList = dataSource.readCookieList(profileDirectory = PROFILE_DIRECTORY)

                cookieList shouldContainExactlyInAnyOrder
                    cases.map { (row, sameSite) ->
                        BrowserCookieLocalEntity(
                            name = row.name,
                            value = row.name,
                            domain = row.hostKey,
                            path = row.path,
                            expiresAt = row.expiresAt,
                            isSecure = row.isSecure,
                            isHttpOnly = row.isHttpOnly,
                            sameSite = sameSite,
                        )
                    }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-015 사이트가 확인되지 않는 쿠키는 실패 없이 빼고 제공한다") {
            runTest {
                val key = randomAesKey()
                val database =
                    createDatabase(
                        ChromeCookieTestRow(hostKey = ".example.com", name = "valid", encryptedValue = encryptChromeCookieValue("1", ".example.com", key)),
                        ChromeCookieTestRow(hostKey = ".example.com", name = "moved", encryptedValue = encryptChromeCookieValue("2", ".other.com", key)),
                    )
                val dataSource = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = database, key = key, dispatcher = StandardTestDispatcher(testScheduler))

                dataSource.readCookieList(profileDirectory = PROFILE_DIRECTORY).map { cookie -> cookie.name } shouldBe listOf("valid")
            }
        }

        test("해시를 붙이기 전 버전의 저장소는 값을 그대로 제공한다") {
            runTest {
                val key = randomAesKey()
                val database =
                    createDatabase(
                        ChromeCookieTestRow(hostKey = ".example.com", name = "legacy", encryptedValue = encryptChromeCookieValue("1", ".example.com", key, withDomainHash = false)),
                        version = CHROME_DATABASE_VERSION - 1,
                    )
                val dataSource = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = database, key = key, dispatcher = StandardTestDispatcher(testScheduler))

                dataSource.readCookieList(profileDirectory = PROFILE_DIRECTORY).map { cookie -> cookie.value } shouldBe listOf("1")
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-005 암호화 키를 얻지 못하면 실패로 알린다") {
            runTest {
                val key = randomAesKey()
                val database = createDatabase(ChromeCookieTestRow(hostKey = ".example.com", name = "session", encryptedValue = encryptChromeCookieValue("1", ".example.com", key)))
                val dataSource = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = database, keyProvider = { error("keychain denied") }, dispatcher = StandardTestDispatcher(testScheduler))

                shouldThrow<IllegalStateException> {
                    dataSource.readCookieList(profileDirectory = PROFILE_DIRECTORY)
                }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-006 쿠키 저장소가 없으면 실패로 알린다") {
            runTest {
                val dataSource = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = createTempDirectory("diary-missing"), key = randomAesKey(), dispatcher = StandardTestDispatcher(testScheduler))

                shouldThrow<IllegalStateException> {
                    dataSource.readCookieList(profileDirectory = PROFILE_DIRECTORY)
                }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DATA-007 읽기가 끝나면 복사본을 남기지 않고 원래 저장소를 바꾸지 않는다") {
            runTest {
                val key = randomAesKey()
                val database = createDatabase(ChromeCookieTestRow(hostKey = ".example.com", name = "session", encryptedValue = encryptChromeCookieValue("1", ".example.com", key)))
                val originalBytes = database.resolve(PROFILE_DIRECTORY).resolve("Cookies").readBytes()
                val dataSource = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = database, key = key, dispatcher = StandardTestDispatcher(testScheduler))

                dataSource.readCookieList(profileDirectory = PROFILE_DIRECTORY) shouldHaveSize 1

                snapshotParentDirectory.listDirectoryEntries().shouldBeEmpty()
                database.resolve(PROFILE_DIRECTORY).resolve("Cookies").readBytes() shouldBe originalBytes
            }
        }

        test("제공 여부는 저장소 위치의 제공 여부를 그대로 따른다") {
            runTest {
                listOf(true, false).forEach { isSupported ->
                    val dataSource =
                        ChromeCookieLocalDataSourceImpl(
                            location = ChromeCookieLocation(isSupported = isSupported, userDataDirectory = Paths.get("/nonexistent"), snapshotParentDirectory = snapshotParentDirectory),
                            keyProvider = { randomAesKey() },
                            dispatcher = StandardTestDispatcher(testScheduler),
                        )

                    dataSource.isSupported shouldBe isSupported
                }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-012 요청한 프로필 폴더의 저장소만 읽는다") {
            runTest {
                val userDataDirectory = createDatabase(ChromeCookieTestRow(hostKey = "example.com", name = "default", value = "1"))
                val otherPath = userDataDirectory.resolve("Profile 1").resolve("Cookies")
                Files.createDirectories(otherPath.parent)
                createChromeCookieDatabase(path = otherPath, rowList = listOf(ChromeCookieTestRow(hostKey = "example.com", name = "other", value = "2")))
                val dataSource = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = userDataDirectory, key = randomAesKey(), dispatcher = StandardTestDispatcher(testScheduler))

                dataSource.readCookieList(profileDirectory = "Profile 1").map { cookie -> cookie.name } shouldBe listOf("other")
            }
        }

        test("만료 시각이 없는 쿠키는 만료 시각 없음으로 제공한다") {
            runTest {
                val database = createDatabase(ChromeCookieTestRow(hostKey = "example.com", name = "session", value = "1", expiresAt = null))
                val dataSource = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = database, key = randomAesKey(), dispatcher = StandardTestDispatcher(testScheduler))

                dataSource
                    .readCookieList(profileDirectory = PROFILE_DIRECTORY)
                    .single()
                    .expiresAt
                    .shouldBeNull()
            }
        }
    })

// 저장소 파일이 놓인 프로필 폴더의 상위, 즉 Chrome 사용자 데이터 폴더를 돌려준다.
private fun createDatabase(
    vararg rowList: ChromeCookieTestRow,
    version: Long = CHROME_DATABASE_VERSION,
): Path {
    val userDataDirectory = createTempDirectory("diary-chrome-test")
    val path = userDataDirectory.resolve(PROFILE_DIRECTORY).resolve("Cookies")

    Files.createDirectories(path.parent)
    createChromeCookieDatabase(path = path, rowList = rowList.toList(), version = version)

    return userDataDirectory
}

private fun dataSource(
    snapshotParentDirectory: Path,
    userDataDirectory: Path,
    key: ByteArray,
    dispatcher: CoroutineDispatcher,
): ChromeCookieLocalDataSourceImpl = dataSource(snapshotParentDirectory = snapshotParentDirectory, userDataDirectory = userDataDirectory, keyProvider = { key }, dispatcher = dispatcher)

private fun dataSource(
    snapshotParentDirectory: Path,
    userDataDirectory: Path,
    keyProvider: ChromeCookieKeyProvider,
    dispatcher: CoroutineDispatcher,
): ChromeCookieLocalDataSourceImpl =
    ChromeCookieLocalDataSourceImpl(
        location = ChromeCookieLocation(isSupported = true, userDataDirectory = userDataDirectory, snapshotParentDirectory = snapshotParentDirectory),
        keyProvider = keyProvider,
        dispatcher = dispatcher,
    )
