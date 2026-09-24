package io.github.taetae98coding.diary.work.musicdownload

import io.github.taetae98coding.diary.core.file.api.datasource.AppFileLocalDataSource
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicDownloadTargetUseCase
import io.github.taetae98coding.diary.work.musicdownload.di.MusicDownloadDispatcher
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadEventHolder
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadStateHolder
import io.github.taetae98coding.diary.work.musicdownload.tool.DownloadToolPreparer
import io.github.taetae98coding.diary.work.musicdownload.tool.MusicDownloader
import io.github.taetae98coding.diary.work.musicdownload.work.MusicDownloadWork
import io.github.taetae98coding.diary.work.musicdownload.work.MusicDownloadWorkImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class JvmWorkMusicDownloadModule {
    @Factory
    @MusicDownloadDispatcher
    internal fun providesMusicDownloadDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Factory
    internal fun providesMusicDownloadWork(
        downloadToolPreparer: DownloadToolPreparer,
        musicDownloader: MusicDownloader,
        findMusicDownloadTargetUseCase: FindMusicDownloadTargetUseCase,
        appFileLocalDataSource: AppFileLocalDataSource,
        musicDownloadStateHolder: MusicDownloadStateHolder,
        musicDownloadEventHolder: MusicDownloadEventHolder,
    ): MusicDownloadWork =
        MusicDownloadWorkImpl(
            downloadToolPreparer = downloadToolPreparer,
            musicDownloader = musicDownloader,
            findMusicDownloadTargetUseCase = findMusicDownloadTargetUseCase,
            appFileLocalDataSource = appFileLocalDataSource,
            musicDownloadStateHolder = musicDownloadStateHolder,
            musicDownloadEventHolder = musicDownloadEventHolder,
        )
}
