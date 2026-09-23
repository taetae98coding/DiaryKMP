package io.github.taetae98coding.diary.feature.contact.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.contactNavKeys() {
    subclass(ContactHomeNavKey::class, ContactHomeNavKey.serializer())
    subclass(ContactAddNavKey::class, ContactAddNavKey.serializer())
    subclass(ContactDetailNavKey::class, ContactDetailNavKey.serializer())
}
