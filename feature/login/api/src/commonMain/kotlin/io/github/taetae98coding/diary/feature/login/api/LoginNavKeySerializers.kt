package io.github.taetae98coding.diary.feature.login.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.loginNavKeys() {
    subclass(LoginHomeNavKey::class, LoginHomeNavKey.serializer())
}
