package io.github.taetae98coding.diary.core.datastore.api.setting.datasource

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.FileDownloadDirectoryLocalEntity
import kotlinx.coroutines.flow.Flow

public interface FileDownloadSettingLocalDataSource {
    public fun getDirectory(): Flow<FileDownloadDirectoryLocalEntity?>

    public suspend fun upsertDirectory(directory: FileDownloadDirectoryLocalEntity?)
}
