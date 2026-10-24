package io.github.taetae98coding.diary.domain.file.repository

import io.github.taetae98coding.diary.core.model.file.FileDownloadSetting
import kotlinx.coroutines.flow.Flow

public interface FileDownloadSettingRepository {
    public fun get(): Flow<FileDownloadSetting>

    public fun isDirectorySelectable(): Boolean

    public suspend fun setDirectory(uri: String)

    public suspend fun unsetDirectory()
}
