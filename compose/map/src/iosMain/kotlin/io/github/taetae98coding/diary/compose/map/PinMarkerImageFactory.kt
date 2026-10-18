package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import io.github.taetae98coding.diary.compose.map.di.MapDispatcher
import io.github.taetae98coding.diary.compose.map.provider.label
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
internal fun rememberPinMarkerImageFactory(): PinMarkerImageFactory {
    val dispatcher = koinInject<CoroutineDispatcher>(qualifier = named<MapDispatcher>())

    return remember(dispatcher) { PinMarkerImageFactory(dispatcher = dispatcher) }
}

internal data class PinMarkerImageKey(
    val color: Color,
    val label: String,
)

internal val DiaryMapPin.pinMarkerImageKey: PinMarkerImageKey
    get() = PinMarkerImageKey(color = color, label = label)

// 핀 그림은 비트맵에 그려 PNG로 바꾸므로, 화면을 그리는 메인 스레드 밖에서 색과 라벨마다 한 번만 만든다.
internal class PinMarkerImageFactory(
    private val dispatcher: CoroutineDispatcher,
) {
    suspend fun create(
        pinList: List<DiaryMapPin>,
        density: Float,
        textMeasurer: TextMeasurer,
    ): Map<PinMarkerImageKey, PinMarkerImage?> =
        withContext(dispatcher) {
            pinList
                .map { pin -> pin.pinMarkerImageKey }
                .distinct()
                .associateWith { key ->
                    ensureActive()
                    createPinMarkerImage(color = key.color, label = key.label, density = density, textMeasurer = textMeasurer)
                }
        }
}
