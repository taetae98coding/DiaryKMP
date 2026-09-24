package io.github.taetae98coding.diary.domain.browser.usecase

import io.github.taetae98coding.diary.core.model.browser.BrowserCookie
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.domain.browser.repository.ChromeCookieRepository
import io.github.taetae98coding.diary.domain.browser.repository.InAppBrowserCookieRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.Instant

@Factory
public class ImportChromeSessionUseCase internal constructor(
    private val chromeCookieRepository: ChromeCookieRepository,
    private val inAppBrowserCookieRepository: InAppBrowserCookieRepository,
    private val clock: Clock,
) : UseCase<ChromeProfile, Unit>() {
    override suspend fun execute(parameter: ChromeProfile) {
        val now = clock.now()
        val cookieList =
            chromeCookieRepository
                .findAll(profileDirectory = parameter.directory)
                .filter { cookie -> !cookie.isExpired(now = now) }

        if (cookieList.isNotEmpty()) {
            inAppBrowserCookieRepository.upsert(cookieList = cookieList)
        }
    }

    private fun BrowserCookie.isExpired(now: Instant): Boolean = expiresAt?.let { expiresAt -> expiresAt <= now } == true
}
