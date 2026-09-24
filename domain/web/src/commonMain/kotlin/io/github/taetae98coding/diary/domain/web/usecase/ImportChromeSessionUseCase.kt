package io.github.taetae98coding.diary.domain.web.usecase

import io.github.taetae98coding.diary.core.model.browser.BrowserCookie
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.setting.repository.ChromeProfileRepository
import io.github.taetae98coding.diary.domain.setting.repository.ChromeSessionImportSettingRepository
import io.github.taetae98coding.diary.domain.web.repository.ChromeCookieRepository
import io.github.taetae98coding.diary.domain.web.repository.InAppBrowserCookieRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.Instant

private const val GOOGLE_ACCOUNT_DOMAIN = "google.com"

@Factory
public class ImportChromeSessionUseCase internal constructor(
    private val chromeSessionImportSettingRepository: ChromeSessionImportSettingRepository,
    private val chromeProfileRepository: ChromeProfileRepository,
    private val chromeCookieRepository: ChromeCookieRepository,
    private val inAppBrowserCookieRepository: InAppBrowserCookieRepository,
    private val clock: Clock,
) : UseCase<String, Unit>() {
    override suspend fun execute(parameter: String) {
        val profileDirectory = findSelectedProfileDirectory()
        val cookieList =
            if (profileDirectory.isEmpty()) {
                emptyList()
            } else {
                parameter
                    .uriHostOrNull()
                    ?.let { host -> findImportableCookieList(profileDirectory = profileDirectory, host = host) }
                    .orEmpty()
            }

        if (cookieList.isNotEmpty()) {
            inAppBrowserCookieRepository.upsert(cookieList = cookieList)
        }
    }

    private suspend fun findSelectedProfileDirectory(): String {
        if (!chromeSessionImportSettingRepository.isSupported) return ""

        val directory = chromeSessionImportSettingRepository.getProfileDirectory().first()

        return if (directory.isNotEmpty() && isListedProfile(directory = directory)) directory else ""
    }

    private suspend fun isListedProfile(directory: String): Boolean =
        chromeProfileRepository
            .findAll()
            .any { profile -> profile.directory == directory }

    private suspend fun findImportableCookieList(
        profileDirectory: String,
        host: String,
    ): List<BrowserCookie> {
        val now = clock.now()

        return chromeCookieRepository
            .findByDomain(profileDirectory = profileDirectory, domainSet = host.cookieDomainSet())
            .filter { cookie -> !cookie.isExpired(now = now) && !cookie.isGoogleAccountDomain() }
    }

    private fun BrowserCookie.isExpired(now: Instant): Boolean = expiresAt?.let { expiresAt -> expiresAt <= now } == true

    private fun BrowserCookie.isGoogleAccountDomain(): Boolean {
        val host = domain.removePrefix(".")

        return host == GOOGLE_ACCOUNT_DOMAIN || host.endsWith(".$GOOGLE_ACCOUNT_DOMAIN")
    }
}

// 호스트와 그 상위 도메인마다 host-only 쿠키와 도메인 쿠키를 모두 찾도록 점 없는 이름과 점으로 시작하는 이름을 함께 둔다.
// 최상위 도메인 하나만 남는 이름은 쿠키를 가질 수 없으므로 제외한다.
internal fun String.cookieDomainSet(): Set<String> {
    val labelList = split('.')

    return buildSet {
        for (index in 0 until labelList.size - 1) {
            val domain = labelList.subList(index, labelList.size).joinToString(separator = ".")

            add(domain)
            add(".$domain")
        }
    }
}
