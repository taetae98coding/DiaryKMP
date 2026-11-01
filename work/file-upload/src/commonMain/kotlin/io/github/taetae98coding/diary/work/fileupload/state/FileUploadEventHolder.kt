package io.github.taetae98coding.diary.work.fileupload.state

import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

// 결과는 그 순간 보이는 화면만 알리므로 쌓아 두지 않고, 받는 동안의 결과만 받는 쪽마다 따로 전한다.
// 목록과 함께 FileHome과 FileAdd가 동시에 받을 수 있으므로 Channel 하나를 나눠 받지 않는다.
// FileHome은 FileAdd나 FileDetail에 가려진 동안 받지 못한 성공도 돌아와서 목록에 반영해야 하므로 성공은 따로 쌓아 둔다.
@Single
internal class FileUploadEventHolder {
    private val eventChannelSet = MutableStateFlow<Set<Channel<FileUploadEvent>>>(emptySet())
    private val succeededChannel = Channel<FileUploadEvent.Succeeded>(Channel.CONFLATED)

    val event: Flow<FileUploadEvent> =
        flow {
            val eventChannel = Channel<FileUploadEvent>(Channel.BUFFERED)
            eventChannelSet.update { channelSet -> channelSet + eventChannel }
            try {
                emitAll(eventChannel)
            } finally {
                eventChannelSet.update { channelSet -> channelSet - eventChannel }
            }
        }

    val succeededEvent: Flow<FileUploadEvent.Succeeded> = succeededChannel.receiveAsFlow()

    fun send(event: FileUploadEvent) {
        eventChannelSet.value.forEach { eventChannel -> eventChannel.trySend(event) }
        if (event is FileUploadEvent.Succeeded) succeededChannel.trySend(event)
    }
}
