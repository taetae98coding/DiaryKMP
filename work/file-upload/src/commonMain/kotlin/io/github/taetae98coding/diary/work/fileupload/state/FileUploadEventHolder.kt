package io.github.taetae98coding.diary.work.fileupload.state

import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Single

@Single
internal class FileUploadEventHolder {
    private val channel = Channel<FileUploadEvent>(Channel.BUFFERED)

    val event: Flow<FileUploadEvent> = channel.receiveAsFlow()

    fun send(event: FileUploadEvent) {
        channel.trySend(event)
    }
}
