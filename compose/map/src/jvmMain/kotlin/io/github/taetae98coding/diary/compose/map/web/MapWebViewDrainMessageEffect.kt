package io.github.taetae98coding.diary.compose.map.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.library.webkit.WebKitWebViewPanel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

private val MESSAGE_DRAIN_INTERVAL = 200.milliseconds

@Composable
internal fun MapWebViewDrainMessageEffect(
    webViewPanel: WebKitWebViewPanel,
    isDocumentReady: MutableStateFlow<Boolean> = MutableStateFlow(false),
    state: DiaryMapState = rememberDiaryMapState(),
    onSpotClick: ((DiaryMapCoordinate) -> Unit)? = null,
    onPinClick: ((Uuid) -> Unit)? = null,
) {
    LaunchedEffect(webViewPanel, state, onSpotClick, onPinClick, isDocumentReady) {
        while (true) {
            delay(MESSAGE_DRAIN_INTERVAL)
            webViewPanel
                .drainIpcMessages()
                .forEach { message ->
                    state.handleMapMessage(
                        message = message,
                        onReady = { isDocumentReady.value = true },
                        onSpotClick = onSpotClick,
                        onPinClick = onPinClick,
                    )
                }
        }
    }
}

internal fun DiaryMapState.handleMapMessage(
    message: String,
    onReady: () -> Unit,
    onSpotClick: ((DiaryMapCoordinate) -> Unit)?,
    onPinClick: ((Uuid) -> Unit)?,
) {
    when (val value = message.toMapMessageOrNull()) {
        is MapMessage.Ready -> onReady()
        is MapMessage.Camera -> moveCamera(value.camera)
        is MapMessage.Click -> onSpotClick?.invoke(value.coordinate)
        is MapMessage.PinClick -> if (pins.any { pin -> pin.id == value.id }) onPinClick?.invoke(value.id)
        null -> Unit
    }
}
