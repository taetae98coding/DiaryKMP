package io.github.taetae98coding.diary.work.musicdownload.work

import io.github.taetae98coding.diary.core.file.api.datasource.AppFileLocalDataSource
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadEvent
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicDownloadTargetUseCase
import io.github.taetae98coding.diary.logger.console.api.ConsoleLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadEventHolder
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadStateHolder
import io.github.taetae98coding.diary.work.musicdownload.tool.DownloadToolPrepareResult
import io.github.taetae98coding.diary.work.musicdownload.tool.DownloadToolPreparer
import io.github.taetae98coding.diary.work.musicdownload.tool.MusicDownloader
import kotlinx.coroutines.CancellationException

internal class MusicDownloadWorkImpl(
    private val downloadToolPreparer: DownloadToolPreparer,
    private val musicDownloader: MusicDownloader,
    private val findMusicDownloadTargetUseCase: FindMusicDownloadTargetUseCase,
    private val appFileLocalDataSource: AppFileLocalDataSource,
    private val musicDownloadStateHolder: MusicDownloadStateHolder,
    private val musicDownloadEventHolder: MusicDownloadEventHolder,
) : MusicDownloadWork {
    override suspend fun doWork(sort: ListSort) {
        val ytDlpPath = prepareOrNull() ?: return
        val targetList = findMusicDownloadTargetUseCase(parameter = sort).getOrThrow()

        musicDownloadStateHolder.submitPending(idList = targetList.map { target -> target.id })

        targetList.forEach { target -> download(ytDlpPath = ytDlpPath, target = target) }
    }

    private suspend fun prepareOrNull(): String? =
        when (val result = downloadToolPreparer.prepare()) {
            is DownloadToolPrepareResult.Prepared -> result.ytDlpPath

            is DownloadToolPrepareResult.NotInstalled -> {
                musicDownloadEventHolder.send(event = MusicDownloadEvent.TOOL_NOT_INSTALLED)
                null
            }

            is DownloadToolPrepareResult.Failed -> {
                DiaryLogger.log(log = ConsoleLog(tag = TAG, message = "내려받기 도구 준비 실패"))
                musicDownloadEventHolder.send(event = MusicDownloadEvent.TOOL_PREPARE_FAILED)
                null
            }
        }

    private suspend fun download(
        ytDlpPath: String,
        target: MusicDownloadTarget,
    ) {
        val name = target.id.toMusicFileName()

        try {
            if (appFileLocalDataSource.exists(directory = MUSIC_FILE_DIRECTORY, name = name)) {
                musicDownloadStateHolder.update(id = target.id, state = MusicDownloadState.Done)
                return
            }

            musicDownloadStateHolder.update(id = target.id, state = MusicDownloadState.Running(progress = 0F))

            val path = appFileLocalDataSource.resolve(directory = MUSIC_FILE_DIRECTORY, name = name)
            val isDownloaded =
                musicDownloader.download(
                    ytDlpPath = ytDlpPath,
                    link = target.link,
                    path = path,
                    onProgress = { progress ->
                        musicDownloadStateHolder.update(id = target.id, state = MusicDownloadState.Running(progress = progress))
                    },
                )

            if (isDownloaded) {
                musicDownloadStateHolder.update(id = target.id, state = MusicDownloadState.Done)
            } else {
                appFileLocalDataSource.delete(directory = MUSIC_FILE_DIRECTORY, name = name)
                musicDownloadStateHolder.update(id = target.id, state = MusicDownloadState.Failed)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            DiaryLogger.log(log = ConsoleLog(tag = TAG, message = "곡 다운로드 실패", throwable = throwable))
            musicDownloadStateHolder.update(id = target.id, state = MusicDownloadState.Failed)
        }
    }

    private companion object {
        const val TAG: String = "MusicDownloadWork"
    }
}
