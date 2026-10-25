package io.github.taetae98coding.diary.data.file.repository

import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.FileDownloadSettingLocalDataSource
import io.github.taetae98coding.diary.core.file.api.datasource.FileDownloadLocalDataSource
import io.github.taetae98coding.diary.core.model.file.FileDownloadSetting
import io.github.taetae98coding.diary.data.file.mapper.toDomain
import io.github.taetae98coding.diary.data.file.mapper.toLocal
import io.github.taetae98coding.diary.domain.file.repository.FileDownloadSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class FileDownloadSettingRepositoryImpl(
    private val fileDownloadSettingLocalDataSource: FileDownloadSettingLocalDataSource,
    private val fileDownloadLocalDataSource: FileDownloadLocalDataSource,
) : FileDownloadSettingRepository {
    override fun get(): Flow<FileDownloadSetting> =
        fileDownloadSettingLocalDataSource
            .getDirectory()
            .map { directory -> FileDownloadSetting(directory = directory?.toDomain()) }

    override fun isDirectorySelectable(): Boolean = fileDownloadLocalDataSource.isDirectorySelectable

    // 시스템이 앱에 허락하는 폴더 수에는 한도가 있어, 새 폴더로 바꾸면 앞서 고른 폴더의 허락은 돌려준다.
    override suspend fun setDirectory(uri: String) {
        val previous = fileDownloadSettingLocalDataSource.getDirectory().first()
        val directory = fileDownloadLocalDataSource.retainDirectory(uri = uri)

        fileDownloadSettingLocalDataSource.upsertDirectory(directory = directory.toLocal())
        if (previous != null && previous.uri != directory.uri) {
            fileDownloadLocalDataSource.releaseDirectory(directory = previous.toDomain())
        }
    }

    // 허락을 먼저 돌려주면 설정을 지우지 못했을 때 고른 폴더가 남은 채 쓸 수 없게 되므로, 설정을 먼저 지운다.
    override suspend fun unsetDirectory() {
        val directory = fileDownloadSettingLocalDataSource.getDirectory().first() ?: return

        fileDownloadSettingLocalDataSource.upsertDirectory(directory = null)
        fileDownloadLocalDataSource.releaseDirectory(directory = directory.toDomain())
    }
}
