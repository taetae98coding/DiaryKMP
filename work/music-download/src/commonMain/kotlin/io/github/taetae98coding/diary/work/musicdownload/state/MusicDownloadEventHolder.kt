package io.github.taetae98coding.diary.work.musicdownload.state

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Single

@Single
internal class MusicDownloadEventHolder {
    private val _event = Channel<MusicDownloadEvent>(Channel.BUFFERED)

    val event: Flow<MusicDownloadEvent> = _event.receiveAsFlow()

    fun send(event: MusicDownloadEvent) {
        _event.trySend(event)
    }
}
