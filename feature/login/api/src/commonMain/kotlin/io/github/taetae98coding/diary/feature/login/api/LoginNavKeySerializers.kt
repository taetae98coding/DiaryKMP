package io.github.taetae98coding.diary.feature.login.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.loginNavKeys() {
    subclass(LoginHomeNavKey::class, LoginHomeNavKey.serializer())
}
