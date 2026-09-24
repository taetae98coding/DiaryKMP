package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.work.musicdownload.di.MusicDownloadScope
import io.github.taetae98coding.diary.work.musicdownload.work.MusicFilePath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

// 같은 영상을 두 곳에서 동시에 받으면 같은 파일에 함께 쓰게 되므로, 영상마다 작업을 하나만 두고 나머지는 합류한다.
// 작업은 요청한 쪽의 스코프가 아니라 모듈 스코프에서 돌아 요청이 끊겨도 끝까지 받는다.
@Single
internal class MusicVideoDownloadJobRegistry(
    private val ytDlpDownloader: YtDlpDownloader,
    @param:MusicDownloadScope private val scope: CoroutineScope,
) {
    private val mutex = Mutex()
    private val jobMap = mutableMapOf<String, MusicVideoDownloadJob>()

    suspend fun download(
        videoId: String,
        path: MusicFilePath,
        onProgress: suspend (Float) -> Unit,
    ): Boolean {
        val job = mutex.withLock { jobMap.getOrPut(videoId) { start(videoId = videoId, path = path) } }

        return coroutineScope {
            val collector = launch { job.progress.filterNotNull().collect { value -> onProgress(value) } }

            try {
                job.result.await()
            } finally {
                collector.cancel()
            }
        }
    }

    private fun start(
        videoId: String,
        path: MusicFilePath,
    ): MusicVideoDownloadJob {
        val progress = MutableStateFlow<Float?>(null)
        val result =
            scope.async {
                try {
                    ytDlpDownloader.download(videoId = videoId, path = path) { value -> progress.value = value }
                } finally {
                    mutex.withLock { jobMap.remove(videoId) }
                }
            }

        return MusicVideoDownloadJob(progress = progress, result = result)
    }
}

private class MusicVideoDownloadJob(
    val progress: MutableStateFlow<Float?>,
    val result: Deferred<Boolean>,
)
