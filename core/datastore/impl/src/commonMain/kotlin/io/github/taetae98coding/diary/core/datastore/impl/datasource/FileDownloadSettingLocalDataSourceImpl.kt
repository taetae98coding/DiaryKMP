package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.FileDownloadSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.FileDownloadDirectoryLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.FileDownloadSettingData
import io.github.taetae98coding.diary.core.datastore.impl.di.FileDownloadSettingDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class FileDownloadSettingLocalDataSourceImpl(
    @FileDownloadSettingDataStore
    private val dataStore: DataStore<FileDownloadSettingData>,
) : FileDownloadSettingLocalDataSource {
    override fun getDirectory(): Flow<FileDownloadDirectoryLocalEntity?> =
        dataStore
            .data
            .map { setting ->
                setting.directoryUri
                    .takeIf { uri -> uri.isNotEmpty() }
                    ?.let { uri -> FileDownloadDirectoryLocalEntity(uri = uri, name = setting.directoryName) }
            }

    override suspend fun upsertDirectory(directory: FileDownloadDirectoryLocalEntity?) {
        dataStore.updateData { setting ->
            setting.copy(
                directoryUri = directory?.uri.orEmpty(),
                directoryName = directory?.name.orEmpty(),
            )
        }
    }
}
