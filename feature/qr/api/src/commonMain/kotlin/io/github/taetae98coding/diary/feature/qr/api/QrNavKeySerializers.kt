package io.github.taetae98coding.diary.feature.qr.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.qrNavKeys() {
    subclass(QrHomeNavKey::class, QrHomeNavKey.serializer())
    subclass(QrScanNavKey::class, QrScanNavKey.serializer())
}
