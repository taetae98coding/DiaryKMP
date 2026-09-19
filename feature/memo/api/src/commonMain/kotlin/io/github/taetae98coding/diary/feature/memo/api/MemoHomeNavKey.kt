package io.github.taetae98coding.diary.feature.memo.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object MemoHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "MemoHome"
}
