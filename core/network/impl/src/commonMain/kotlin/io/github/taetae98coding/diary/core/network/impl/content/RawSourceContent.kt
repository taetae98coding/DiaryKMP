package io.github.taetae98coding.diary.core.network.impl.content

import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.io.Buffer
import kotlinx.io.RawSource

private const val CHUNK_BYTES = 8 * 1024

internal class RawSourceContent(
    override val contentType: ContentType,
    override val contentLength: Long,
    private val openContent: suspend () -> RawSource,
) : OutgoingContent.WriteChannelContent() {
    // 한 덩어리씩 읽어 보내고 그때마다 flush해, 상대가 받는 속도만큼만 원본을 읽는다.
    override suspend fun writeTo(channel: ByteWriteChannel) {
        openContent().use { source ->
            val buffer = Buffer()
            val chunk = ByteArray(CHUNK_BYTES)

            while (source.readAtMostTo(buffer, CHUNK_BYTES.toLong()) >= 0) {
                while (!buffer.exhausted()) {
                    val read = buffer.readAtMostTo(chunk, 0, chunk.size)

                    channel.writeFully(chunk, 0, read)
                }

                channel.flush()
            }
        }
    }
}
