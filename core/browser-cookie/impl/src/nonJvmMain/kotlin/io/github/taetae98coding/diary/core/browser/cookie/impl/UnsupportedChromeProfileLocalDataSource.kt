package io.github.taetae98coding.diary.core.browser.cookie.impl

import io.github.taetae98coding.diary.core.browser.cookie.api.datasource.ChromeProfileLocalDataSource
import io.github.taetae98coding.diary.core.browser.cookie.api.entity.ChromeProfileLocalEntity
import org.koin.core.annotation.Factory

@Factory
internal class UnsupportedChromeProfileLocalDataSource : ChromeProfileLocalDataSource {
    override suspend fun readProfileList(): List<ChromeProfileLocalEntity> = error("Chrome profiles are not supported on this platform.")
}
