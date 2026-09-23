package io.github.taetae98coding.diary.compose.core.image

import coil3.ComponentRegistry

internal actual fun ComponentRegistry.Builder.addPlatformDecoders() {
    add(ExifOrientedSkiaImageDecoder.Factory())
}
