package io.github.taetae98coding.diary.feature.memo.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object MemoHomeFilterNavKey : ScreenNavKey {
    override val screenName: String get() = "MemoHomeFilter"
}
