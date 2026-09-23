package io.github.taetae98coding.diary.compose.core.image

import androidx.compose.runtime.Composable
import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory

/**
 * 앱의 이미지 로더를 한 번 정한다. 어떤 이미지 컴포저블보다 먼저 호출되어야 하므로 앱 루트에 둔다.
 */
@Composable
public fun DiaryImageLoaderEffect() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader
            .Builder(context)
            .components { addPlatformDecoders() }
            .build()
    }
}

internal expect fun ComponentRegistry.Builder.addPlatformDecoders()
