package io.github.taetae98coding.diary.work.musicdownload.scheduler

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.work.musicdownload.di.MusicDownloadScope
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadStateHolder
import io.github.taetae98coding.diary.work.musicdownload.work.MusicDownloadWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
internal class CoroutineMusicDownloadWorkScheduler(
    private val musicDownloadWork: MusicDownloadWork,
    private val musicDownloadStateHolder: MusicDownloadStateHolder,
    @param:MusicDownloadScope private val scope: CoroutineScope,
) : MusicDownloadWorkScheduler {
    private var job: Job? = null

    override fun download(sort: ListSort) {
        if (job?.isActive == true) return

        job =
            scope.launch {
                try {
                    musicDownloadWork.doWork(sort = sort)
                } finally {
                    // 시작하지 못한 곡의 대기 상태가 남으면 다음 요청이 막힌다.
                    musicDownloadStateHolder.clearUnfinished()
                }
            }
    }
}
