package io.github.taetae98coding.diary.feature.qr.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.qrNavKeys() {
    subclass(QrHomeNavKey::class, QrHomeNavKey.serializer())
}
