package io.github.taetae98coding.diary.domain.browser.repository

import kotlinx.coroutines.flow.Flow

public interface ChromeSessionImportSettingRepository {
    public val isSupported: Boolean

    public fun getProfileDirectory(): Flow<String>

    public suspend fun setProfileDirectory(directory: String)

    public suspend fun unsetProfileDirectory()
}
