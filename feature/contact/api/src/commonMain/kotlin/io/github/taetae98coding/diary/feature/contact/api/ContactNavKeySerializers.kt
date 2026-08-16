package io.github.taetae98coding.diary.feature.contact.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.contactNavKeys() {
    subclass(ContactHomeNavKey::class, ContactHomeNavKey.serializer())
    subclass(ContactAddNavKey::class, ContactAddNavKey.serializer())
    subclass(ContactDetailNavKey::class, ContactDetailNavKey.serializer())
}
