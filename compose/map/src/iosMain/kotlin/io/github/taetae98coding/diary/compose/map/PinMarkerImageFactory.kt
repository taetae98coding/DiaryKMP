package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.map.di.MapDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import platform.UIKit.UIImage

@Composable
internal fun rememberPinMarkerImageFactory(): PinMarkerImageFactory {
    val dispatcher = koinInject<CoroutineDispatcher>(qualifier = named<MapDispatcher>())

    return remember(dispatcher) { PinMarkerImageFactory(dispatcher = dispatcher) }
}

// 핀 그림은 비트맵에 그려 PNG로 바꾸므로, 화면을 그리는 메인 스레드 밖에서 색마다 한 번만 만든다.
internal class PinMarkerImageFactory(
    private val dispatcher: CoroutineDispatcher,
) {
    suspend fun create(
        colorList: List<Color>,
        density: Float,
    ): Map<Color, UIImage?> =
        withContext(dispatcher) {
            colorList
                .distinct()
                .associateWith { color -> createPinMarkerImage(color = color, density = density) }
        }
}
