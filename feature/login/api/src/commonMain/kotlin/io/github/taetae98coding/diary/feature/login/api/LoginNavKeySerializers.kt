package io.github.taetae98coding.diary.feature.login.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.loginNavKeys() {
    subclass(LoginHomeNavKey::class, LoginHomeNavKey.serializer())
}
