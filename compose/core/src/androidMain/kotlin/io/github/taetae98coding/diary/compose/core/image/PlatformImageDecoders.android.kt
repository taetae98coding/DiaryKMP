package io.github.taetae98coding.diary.compose.core.image

import coil3.ComponentRegistry

// Android의 기본 디코더는 촬영 방향을 스스로 반영하므로 더할 것이 없다.
internal actual fun ComponentRegistry.Builder.addPlatformDecoders(): Unit = Unit
