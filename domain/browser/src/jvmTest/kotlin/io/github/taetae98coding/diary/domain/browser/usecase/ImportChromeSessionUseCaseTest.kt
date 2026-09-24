package io.github.taetae98coding.diary.domain.browser.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.browser.BrowserCookie
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.domain.browser.repository.ChromeCookieRepository
import io.github.taetae98coding.diary.domain.browser.repository.InAppBrowserCookieRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private val now: Instant = Instant.fromEpochMilliseconds(1_800_000_000_000L)

private val profile: ChromeProfile = ChromeProfile(directory = "Profile 1", name = "Work")

private val fixedClock: Clock =
    object : Clock {
        override fun now(): Instant = now
    }

class ImportChromeSessionUseCaseTest :
    BehaviorSpec({
        Given("프로필에 여러 사이트의 쿠키가 있다") {
            val cookieList =
                listOf("mail.example.com", ".example.com", "other.example.org", ".google.com", "accounts.google.com")
                    .map { domain -> cookie(domain = domain, expiresAt = null) }
            val chromeCookieRepository = mockk<ChromeCookieRepository>()
            coEvery { chromeCookieRepository.findAll(profileDirectory = profile.directory) } returns cookieList
            val inAppBrowserCookieRepository = relaxedInAppRepository()
            val useCase = useCase(chromeCookieRepository, inAppBrowserCookieRepository)

            When("로그인 정보를 가져온다") {
                val result = useCase(parameter = profile)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-012 TC-CHROME-SESSION-IMPORT-DOMAIN-015 고른 프로필 전체의 쿠키를 요청하고 모두 넘긴다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { chromeCookieRepository.findAll(profileDirectory = profile.directory) }
                    coVerify(exactly = 1) { inAppBrowserCookieRepository.upsert(cookieList = cookieList) }
                }
            }
        }

        Given("만료 시각이 지난 쿠키와 남은 쿠키, 없는 쿠키가 섞여 있다") {
            val expired = cookie(domain = "example.com", expiresAt = now - 1.hours)
            val alive = cookie(domain = "example.com", expiresAt = now + 1.hours)
            val session = cookie(domain = "example.com", expiresAt = null)
            val chromeCookieRepository = mockk<ChromeCookieRepository>()
            coEvery { chromeCookieRepository.findAll(any()) } returns listOf(expired, alive, session)
            val inAppBrowserCookieRepository = relaxedInAppRepository()
            val useCase = useCase(chromeCookieRepository, inAppBrowserCookieRepository)

            When("로그인 정보를 가져온다") {
                useCase(parameter = profile)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-004 만료 시각이 남은 쿠키와 없는 쿠키만 넘긴다") {
                    coVerify(exactly = 1) { inAppBrowserCookieRepository.upsert(cookieList = listOf(alive, session)) }
                }
            }
        }

        Given("쿠키가 모든 속성을 가진다") {
            val cookie = fixtureMonkey.giveMeKotlinBuilder<BrowserCookie>().setExp(BrowserCookie::expiresAt, now + 1.hours).sample()
            val chromeCookieRepository = mockk<ChromeCookieRepository>()
            coEvery { chromeCookieRepository.findAll(any()) } returns listOf(cookie)
            val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
            val delivered = slot<List<BrowserCookie>>()
            coEvery { inAppBrowserCookieRepository.upsert(cookieList = capture(delivered)) } just runs
            val useCase = useCase(chromeCookieRepository, inAppBrowserCookieRepository)

            When("로그인 정보를 가져온다") {
                useCase(parameter = profile)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-006 여덟 속성을 그대로 넘긴다") {
                    delivered.captured shouldBe listOf(cookie)
                }
            }
        }

        Given("프로필에 쿠키가 하나도 없다") {
            val chromeCookieRepository = mockk<ChromeCookieRepository>()
            coEvery { chromeCookieRepository.findAll(any()) } returns emptyList()
            val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
            val useCase = useCase(chromeCookieRepository, inAppBrowserCookieRepository)

            When("로그인 정보를 가져온다") {
                val result = useCase(parameter = profile)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-007 아무것도 넘기지 않고 성공으로 끝난다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 0) { inAppBrowserCookieRepository.upsert(any()) }
                }
            }
        }

        Given("Chrome 쿠키 저장소를 읽을 수 없다") {
            val chromeCookieRepository = mockk<ChromeCookieRepository>()
            coEvery { chromeCookieRepository.findAll(any()) } throws IllegalStateException("cookie store unreadable")
            val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
            val useCase = useCase(chromeCookieRepository, inAppBrowserCookieRepository)

            When("로그인 정보를 가져온다") {
                val result = useCase(parameter = profile)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-008 아무것도 넘기지 않고 실패로 끝난다") {
                    result.shouldBeFailure()
                    coVerify(exactly = 0) { inAppBrowserCookieRepository.upsert(any()) }
                }
            }
        }

        Given("앱 안 웹 표시 수단에 넘기지 못한다") {
            val chromeCookieRepository = mockk<ChromeCookieRepository>()
            coEvery { chromeCookieRepository.findAll(any()) } returns listOf(cookie(domain = "example.com", expiresAt = null))
            val inAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>()
            coEvery { inAppBrowserCookieRepository.upsert(any()) } throws IllegalStateException("store failed")
            val useCase = useCase(chromeCookieRepository, inAppBrowserCookieRepository)

            When("로그인 정보를 가져온다") {
                val result = useCase(parameter = profile)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-009 실패로 끝난다") {
                    result.shouldBeFailure()
                }
            }
        }
    })

private fun cookie(
    domain: String,
    expiresAt: Instant?,
): BrowserCookie =
    fixtureMonkey
        .giveMeKotlinBuilder<BrowserCookie>()
        .setExp(BrowserCookie::name, "cookie-${fixtureMonkey.giveMeOne<Int>()}")
        .setExp(BrowserCookie::domain, domain)
        .setExp(BrowserCookie::expiresAt, expiresAt)
        .sample()

private fun relaxedInAppRepository(): InAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>().also { repository -> coEvery { repository.upsert(any()) } just runs }

private fun useCase(
    chromeCookieRepository: ChromeCookieRepository,
    inAppBrowserCookieRepository: InAppBrowserCookieRepository,
): ImportChromeSessionUseCase =
    ImportChromeSessionUseCase(
        chromeCookieRepository = chromeCookieRepository,
        inAppBrowserCookieRepository = inAppBrowserCookieRepository,
        clock = fixedClock,
    )
