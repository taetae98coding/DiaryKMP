package io.github.taetae98coding.diary.feature.qr.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.qrNavKeys() {
    subclass(QrHomeNavKey::class, QrHomeNavKey.serializer())
}
