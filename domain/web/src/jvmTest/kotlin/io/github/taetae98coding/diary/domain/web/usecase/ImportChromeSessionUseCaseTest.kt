package io.github.taetae98coding.diary.domain.web.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.browser.BrowserCookie
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.domain.setting.repository.ChromeProfileRepository
import io.github.taetae98coding.diary.domain.setting.repository.ChromeSessionImportSettingRepository
import io.github.taetae98coding.diary.domain.web.repository.ChromeCookieRepository
import io.github.taetae98coding.diary.domain.web.repository.InAppBrowserCookieRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private val now: Instant = Instant.fromEpochMilliseconds(1_800_000_000_000L)

private const val PROFILE_DIRECTORY = "Profile 1"
private const val OTHER_PROFILE_DIRECTORY = "Default"

private val profileList: List<ChromeProfile> =
    listOf(
        ChromeProfile(directory = OTHER_PROFILE_DIRECTORY, name = "TaeJong"),
        ChromeProfile(directory = PROFILE_DIRECTORY, name = "Work"),
    )

private val fixedClock: Clock =
    object : Clock {
        override fun now(): Instant = now
    }

class ImportChromeSessionUseCaseTest :
    BehaviorSpec({
        Given("프로필을 고르지 않았다") {
            val chromeCookieRepository = mockk<ChromeCookieRepository>()
            val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
            val useCase = useCase(isSupported = true, profileDirectory = "", chromeCookieRepository, inAppBrowserCookieRepository)

            When("로그인 정보를 가져온다") {
                val result = useCase(parameter = "https://mail.example.com/inbox")

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-001 Chrome 쿠키를 읽지 않고 아무것도 넘기지 않으며 성공으로 끝난다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 0) { chromeCookieRepository.findByDomain(any(), any()) }
                    coVerify(exactly = 0) { inAppBrowserCookieRepository.upsert(any()) }
                }
            }
        }

        Given("고른 프로필이 목록에 없다") {
            val chromeCookieRepository = mockk<ChromeCookieRepository>()
            val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
            val useCase = useCase(isSupported = true, profileDirectory = "Profile 9", chromeCookieRepository, inAppBrowserCookieRepository)

            When("로그인 정보를 가져온다") {
                val result = useCase(parameter = "https://mail.example.com/inbox")

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-011 Chrome 쿠키를 읽지 않고 아무것도 넘기지 않으며 성공으로 끝난다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 0) { chromeCookieRepository.findByDomain(any(), any()) }
                    coVerify(exactly = 0) { inAppBrowserCookieRepository.upsert(any()) }
                }
            }
        }

        Given("프로필을 골라 두었지만 프로필 목록을 읽을 수 없다") {
            val chromeCookieRepository = mockk<ChromeCookieRepository>()
            val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
            val useCase =
                useCase(
                    isSupported = true,
                    profileDirectory = PROFILE_DIRECTORY,
                    chromeCookieRepository,
                    inAppBrowserCookieRepository,
                    chromeProfileRepository = mockk { coEvery { findAll() } throws IllegalStateException("local state unreadable") },
                )

            When("로그인 정보를 가져온다") {
                val result = useCase(parameter = "https://mail.example.com/inbox")

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-014 Chrome 쿠키를 읽지 않고 아무것도 넘기지 않으며 실패로 끝난다") {
                    result.shouldBeFailure()
                    coVerify(exactly = 0) { chromeCookieRepository.findByDomain(any(), any()) }
                    coVerify(exactly = 0) { inAppBrowserCookieRepository.upsert(any()) }
                }
            }
        }

        Given("제공하지 않는 환경이다") {
            listOf(PROFILE_DIRECTORY, "").forEach { profileDirectory ->
                val chromeCookieRepository = mockk<ChromeCookieRepository>()
                val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
                val useCase = useCase(isSupported = false, profileDirectory = profileDirectory, chromeCookieRepository, inAppBrowserCookieRepository)

                When("선택이 $profileDirectory 인 상태에서 로그인 정보를 가져온다") {
                    val result = useCase(parameter = "https://mail.example.com/inbox")

                    Then("TC-CHROME-SESSION-IMPORT-DOMAIN-002 Chrome 쿠키를 읽지 않고 아무것도 넘기지 않으며 성공으로 끝난다") {
                        result.shouldBeSuccess()
                        coVerify(exactly = 0) { chromeCookieRepository.findByDomain(any(), any()) }
                        coVerify(exactly = 0) { inAppBrowserCookieRepository.upsert(any()) }
                    }
                }
            }
        }

        Given("제공 환경이고 목록에 있는 프로필을 골라 두었다") {
            When("주소로 로그인 정보를 가져온다") {
                val cases =
                    mapOf(
                        "https://mail.example.com/inbox" to setOf("mail.example.com", ".mail.example.com", "example.com", ".example.com"),
                        "https://example.com" to setOf("example.com", ".example.com"),
                        "https://a.b.example.co.kr/path?q=1" to
                            setOf(
                                "a.b.example.co.kr",
                                ".a.b.example.co.kr",
                                "b.example.co.kr",
                                ".b.example.co.kr",
                                "example.co.kr",
                                ".example.co.kr",
                                "co.kr",
                                ".co.kr",
                            ),
                    )

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-003 TC-CHROME-SESSION-IMPORT-DOMAIN-012 고른 프로필에서 호스트와 상위 도메인의 쿠키만 요청한다") {
                    cases.forEach { (url, domainSet) ->
                        val chromeCookieRepository = mockk<ChromeCookieRepository>()
                        coEvery { chromeCookieRepository.findByDomain(any(), any()) } returns emptyList()
                        val useCase = useCase(isSupported = true, profileDirectory = PROFILE_DIRECTORY, chromeCookieRepository, mockk())

                        useCase(parameter = url).shouldBeSuccess()

                        coVerify(exactly = 1) { chromeCookieRepository.findByDomain(profileDirectory = PROFILE_DIRECTORY, domainSet = domainSet) }
                        coVerify(exactly = 0) { chromeCookieRepository.findByDomain(profileDirectory = OTHER_PROFILE_DIRECTORY, domainSet = any()) }
                    }
                }
            }

            When("만료 시각이 지난 쿠키와 남은 쿠키, 없는 쿠키가 함께 온다") {
                val expired = cookie(domain = ".example.com", expiresAt = now - 1.hours)
                val alive = cookie(domain = ".example.com", expiresAt = now + 1.hours)
                val session = cookie(domain = ".example.com", expiresAt = null)
                val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
                val upserted = slot<List<BrowserCookie>>()
                coEvery { inAppBrowserCookieRepository.upsert(capture(upserted)) } just runs
                val useCase = useCase(isSupported = true, profileDirectory = PROFILE_DIRECTORY, chromeCookieRepository(expired, alive, session), inAppBrowserCookieRepository)

                val result = useCase(parameter = "https://example.com")

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-004 만료 시각이 남은 쿠키와 없는 쿠키만 넘긴다") {
                    result.shouldBeSuccess()
                    upserted.captured shouldBe listOf(alive, session)
                }
            }

            When("Google 계정 도메인과 다른 도메인의 쿠키가 함께 온다") {
                val cases =
                    mapOf(
                        "google.com" to false,
                        ".google.com" to false,
                        "accounts.google.com" to false,
                        ".accounts.google.com" to false,
                        "notgoogle.com" to true,
                        "google.com.example.com" to true,
                    )

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-005 Google 계정 도메인의 쿠키만 넘기지 않는다") {
                    cases.forEach { (domain, isForwarded) ->
                        val cookie = cookie(domain = domain, expiresAt = null)
                        val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
                        coEvery { inAppBrowserCookieRepository.upsert(any()) } just runs
                        val useCase = useCase(isSupported = true, profileDirectory = PROFILE_DIRECTORY, chromeCookieRepository(cookie), inAppBrowserCookieRepository)

                        useCase(parameter = "https://example.com").shouldBeSuccess()

                        coVerify(exactly = if (isForwarded) 1 else 0) { inAppBrowserCookieRepository.upsert(cookieList = listOf(cookie)) }
                    }
                }
            }

            When("속성을 가진 쿠키가 온다") {
                val cookieList = List(size = 3) { cookie(domain = ".example.com", expiresAt = now + 1.hours) }
                val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
                val upserted = slot<List<BrowserCookie>>()
                coEvery { inAppBrowserCookieRepository.upsert(capture(upserted)) } just runs
                val useCase = useCase(isSupported = true, profileDirectory = PROFILE_DIRECTORY, chromeCookieRepository(*cookieList.toTypedArray()), inAppBrowserCookieRepository)

                val result = useCase(parameter = "https://example.com")

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-006 쿠키의 속성을 그대로 넘긴다") {
                    result.shouldBeSuccess()
                    upserted.captured shouldBe cookieList
                }
            }

            When("해당 사이트의 쿠키가 하나도 없다") {
                val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
                val useCase = useCase(isSupported = true, profileDirectory = PROFILE_DIRECTORY, chromeCookieRepository(), inAppBrowserCookieRepository)

                val result = useCase(parameter = "https://example.com")

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-007 아무것도 넘기지 않고 성공으로 끝난다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 0) { inAppBrowserCookieRepository.upsert(any()) }
                }
            }

            When("Chrome 쿠키 저장소 읽기가 실패한다") {
                val chromeCookieRepository = mockk<ChromeCookieRepository>()
                coEvery { chromeCookieRepository.findByDomain(any(), any()) } throws IllegalStateException("read failed")
                val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
                val useCase = useCase(isSupported = true, profileDirectory = PROFILE_DIRECTORY, chromeCookieRepository, inAppBrowserCookieRepository)

                val result = useCase(parameter = "https://example.com")

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-008 아무것도 넘기지 않고 실패로 끝난다") {
                    result.shouldBeFailure()
                    coVerify(exactly = 0) { inAppBrowserCookieRepository.upsert(any()) }
                }
            }

            When("앱 안 웹 표시 수단에 넘기기가 실패한다") {
                val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
                coEvery { inAppBrowserCookieRepository.upsert(any()) } throws IllegalStateException("store failed")
                val useCase = useCase(isSupported = true, profileDirectory = PROFILE_DIRECTORY, chromeCookieRepository(cookie(domain = ".example.com", expiresAt = null)), inAppBrowserCookieRepository)

                val result = useCase(parameter = "https://example.com")

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-009 실패로 끝난다") {
                    result.shouldBeFailure()
                }
            }

            When("호스트를 알 수 없는 주소로 가져온다") {
                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-010 Chrome 쿠키를 읽지 않고 아무것도 넘기지 않으며 성공으로 끝난다") {
                    listOf("", "not a url", "mailto:someone@example.com").forEach { url ->
                        val chromeCookieRepository = mockk<ChromeCookieRepository>()
                        val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
                        val useCase = useCase(isSupported = true, profileDirectory = PROFILE_DIRECTORY, chromeCookieRepository, inAppBrowserCookieRepository)

                        useCase(parameter = url).shouldBeSuccess()

                        coVerify(exactly = 0) { chromeCookieRepository.findByDomain(any(), any()) }
                        coVerify(exactly = 0) { inAppBrowserCookieRepository.upsert(any()) }
                    }
                }
            }
        }
    })

private fun useCase(
    isSupported: Boolean,
    profileDirectory: String,
    chromeCookieRepository: ChromeCookieRepository,
    inAppBrowserCookieRepository: InAppBrowserCookieRepository,
    chromeProfileRepository: ChromeProfileRepository = mockk { coEvery { findAll() } returns profileList },
): ImportChromeSessionUseCase =
    ImportChromeSessionUseCase(
        chromeSessionImportSettingRepository =
            mockk {
                every { this@mockk.isSupported } returns isSupported
                every { getProfileDirectory() } returns flowOf(profileDirectory)
            },
        chromeProfileRepository = chromeProfileRepository,
        chromeCookieRepository = chromeCookieRepository,
        inAppBrowserCookieRepository = inAppBrowserCookieRepository,
        clock = fixedClock,
    )

private fun chromeCookieRepository(vararg cookieList: BrowserCookie): ChromeCookieRepository =
    mockk {
        coEvery { findByDomain(any(), any()) } returns cookieList.toList()
    }

// FixtureMonkey가 Instant를 다루는 값은 setExp로 고정하고 도메인은 판정 대상이라 직접 정한다.
private fun cookie(
    domain: String,
    expiresAt: Instant?,
): BrowserCookie =
    fixtureMonkey
        .giveMeKotlinBuilder<BrowserCookie>()
        .setExp(BrowserCookie::name, "name-${fixtureMonkey.giveMeOne<String>()}")
        .setExp(BrowserCookie::domain, domain)
        .setExp(BrowserCookie::expiresAt, expiresAt)
        .sample()
