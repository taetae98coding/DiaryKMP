package io.github.taetae98coding.diary.feature.memo.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.memoNavKeys() {
    subclass(MemoHomeNavKey::class, MemoHomeNavKey.serializer())
    subclass(MemoHomeFilterNavKey::class, MemoHomeFilterNavKey.serializer())
    subclass(MemoFinishedListNavKey::class, MemoFinishedListNavKey.serializer())
    subclass(MemoAddNavKey::class, MemoAddNavKey.serializer())
    subclass(MemoDetailNavKey::class, MemoDetailNavKey.serializer())
}
