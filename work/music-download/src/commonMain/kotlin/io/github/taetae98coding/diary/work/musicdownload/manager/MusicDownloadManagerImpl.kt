package io.github.taetae98coding.diary.work.musicdownload.manager

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadEvent
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.domain.playlist.MusicDownloadManager
import io.github.taetae98coding.diary.work.musicdownload.scheduler.MusicDownloadWorkScheduler
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadEventHolder
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadStateHolder
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
internal class MusicDownloadManagerImpl(
    private val musicDownloadWorkScheduler: MusicDownloadWorkScheduler,
    private val musicDownloadStateHolder: MusicDownloadStateHolder,
    musicDownloadEventHolder: MusicDownloadEventHolder,
) : MusicDownloadManager {
    override val stateMap: Flow<Map<Uuid, MusicDownloadState>> = musicDownloadStateHolder.stateMap

    override val event: Flow<MusicDownloadEvent> = musicDownloadEventHolder.event

    override fun requestDownload(sort: ListSort) {
        if (musicDownloadStateHolder.hasUnfinished) return

        musicDownloadWorkScheduler.download(sort = sort)
    }
}
