package io.github.taetae98coding.diary.feature.memo.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.memoNavKeys() {
    subclass(MemoHomeNavKey::class, MemoHomeNavKey.serializer())
    subclass(MemoHomeFilterNavKey::class, MemoHomeFilterNavKey.serializer())
    subclass(MemoFinishedListNavKey::class, MemoFinishedListNavKey.serializer())
    subclass(MemoAddNavKey::class, MemoAddNavKey.serializer())
    subclass(MemoDetailNavKey::class, MemoDetailNavKey.serializer())
}
