package io.github.taetae98coding.diary.data.browser.repository

import io.github.taetae98coding.diary.core.browsercookie.api.datasource.ChromeCookieLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.BrowserSettingLocalDataSource
import io.github.taetae98coding.diary.domain.browser.repository.ChromeSessionImportSettingRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
internal class ChromeSessionImportSettingRepositoryImpl(
    private val browserSettingLocalDataSource: BrowserSettingLocalDataSource,
    private val chromeCookieLocalDataSource: ChromeCookieLocalDataSource,
) : ChromeSessionImportSettingRepository {
    override val isSupported: Boolean
        get() = chromeCookieLocalDataSource.isSupported

    override fun getProfileDirectory(): Flow<String> = browserSettingLocalDataSource.getChromeSessionProfileDirectory()

    override suspend fun setProfileDirectory(directory: String) {
        browserSettingLocalDataSource.setChromeSessionProfileDirectory(directory = directory)
    }

    override suspend fun unsetProfileDirectory() {
        browserSettingLocalDataSource.setChromeSessionProfileDirectory(directory = "")
    }
}
