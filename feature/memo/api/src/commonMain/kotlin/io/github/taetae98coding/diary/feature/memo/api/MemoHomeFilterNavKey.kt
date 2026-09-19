package io.github.taetae98coding.diary.feature.memo.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object MemoHomeFilterNavKey : ScreenNavKey {
    override val screenName: String get() = "MemoHomeFilter"
}
