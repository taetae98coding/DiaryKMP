package io.github.taetae98coding.diary.core.network.impl.file.transport

import io.github.taetae98coding.diary.core.network.impl.content.writeSource
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.io.RawSource

internal class FileUploadMultipartContent(
    private val multipart: FileUploadMultipart,
    private val openContent: suspend () -> RawSource,
    private val onSent: (sentBytes: Long) -> Unit,
) : OutgoingContent.WriteChannelContent() {
    override val contentType: ContentType
        get() = multipart.contentType

    override val contentLength: Long
        get() = multipart.totalLength

    override suspend fun writeTo(channel: ByteWriteChannel) {
        channel.writeFully(multipart.head)
        openContent().use { source -> channel.writeSource(source = source, onSent = onSent) }
        channel.writeFully(multipart.tail)
        channel.flush()
    }
}
