package io.github.taetae98coding.diary.work.fileupload.state

import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Single

// FileAdd가 FileHome 위에 열리면 FileHome은 화면에서 내려가 이벤트를 받지 않으므로, 화면마다 따로 쌓아 두고 돌아왔을 때 받게 한다.
@Single
internal class FileUploadEventHolder {
    private val channelMap = FileScreen.entries.associateWith { Channel<FileUploadEvent>(Channel.BUFFERED) }

    fun getEvent(screen: FileScreen): Flow<FileUploadEvent> = channelMap.getValue(screen).receiveAsFlow()

    fun send(
        screen: FileScreen,
        event: FileUploadEvent,
    ) {
        channelMap.getValue(screen).trySend(event)
    }
}
