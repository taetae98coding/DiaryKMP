package io.github.taetae98coding.diary.work.musicdownload.scheduler

import io.github.taetae98coding.diary.core.model.list.ListSort

internal interface MusicDownloadWorkScheduler {
    val isSupported: Boolean

    fun download(sort: ListSort)
}
