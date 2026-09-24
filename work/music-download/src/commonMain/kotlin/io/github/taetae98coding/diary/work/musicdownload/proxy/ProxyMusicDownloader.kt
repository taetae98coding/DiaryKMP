package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.domain.setting.repository.MusicDownloadProxySettingRepository
import io.github.taetae98coding.diary.work.musicdownload.tool.MusicDownloader
import io.github.taetae98coding.diary.work.musicdownload.work.MusicFilePath
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

private const val COPY_BUFFER_SIZE = 64 * 1024

internal class ProxyMusicDownloader(
    private val httpClient: HttpClient,
    private val musicDownloadProxySettingRepository: MusicDownloadProxySettingRepository,
    private val dispatcher: CoroutineDispatcher,
) : MusicDownloader {
    override suspend fun download(
        target: MusicDownloadTarget,
        path: MusicFilePath,
        onProgress: suspend (Float) -> Unit,
    ): Boolean {
        val address = musicDownloadProxySettingRepository.get().first().address
        val url = address.toMusicDownloadProxyMusicUrl(videoId = target.videoId)

        val isDownloaded =
            try {
                httpClient
                    .prepareGet(urlString = url) {
                        onDownload { bytesSentTotal, contentLength ->
                            if (contentLength != null && contentLength > 0) {
                                onProgress((bytesSentTotal.toFloat() / contentLength).coerceIn(0F, 1F))
                            }
                        }
                    }.execute { response ->
                        if (!response.status.isSuccess()) return@execute false

                        val written = withContext(dispatcher) { response.bodyAsChannel().writeTo(path = path.downloading) }
                        val expected = response.contentLength()

                        expected == null || expected == written
                    }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Throwable) {
                false
            }

        return withContext(dispatcher) { path.complete(isDownloaded = isDownloaded) }
    }
}

private suspend fun ByteReadChannel.writeTo(path: String): Long {
    val buffer = ByteArray(COPY_BUFFER_SIZE)
    var written = 0L

    SystemFileSystem.sink(Path(path)).buffered().use { sink: Sink ->
        while (true) {
            val count = readAvailable(buffer)
            if (count < 0) break

            sink.write(buffer, 0, count)
            written += count
        }
    }

    return written
}

private fun MusicFilePath.complete(isDownloaded: Boolean): Boolean {
    val downloadingPath = Path(downloading)

    if (isDownloaded && SystemFileSystem.exists(downloadingPath)) {
        SystemFileSystem.atomicMove(source = downloadingPath, destination = Path(completed))
        return true
    }

    SystemFileSystem.delete(path = downloadingPath, mustExist = false)
    return false
}
