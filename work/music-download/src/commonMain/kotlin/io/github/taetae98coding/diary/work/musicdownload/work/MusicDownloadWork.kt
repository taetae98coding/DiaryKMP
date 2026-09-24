package io.github.taetae98coding.diary.work.musicdownload.work

import io.github.taetae98coding.diary.core.model.list.ListSort

internal interface MusicDownloadWork {
    suspend fun doWork(sort: ListSort)
}
