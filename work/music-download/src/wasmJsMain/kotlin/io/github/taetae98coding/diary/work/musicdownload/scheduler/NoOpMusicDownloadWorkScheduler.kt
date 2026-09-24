package io.github.taetae98coding.diary.work.musicdownload.scheduler

import io.github.taetae98coding.diary.core.model.list.ListSort
import org.koin.core.annotation.Single

@Single
internal class NoOpMusicDownloadWorkScheduler : MusicDownloadWorkScheduler {
    override val isSupported: Boolean = false

    override fun download(sort: ListSort): Unit = Unit
}
