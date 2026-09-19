package io.github.taetae98coding.diary.feature.checklist.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object ChecklistHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "ChecklistHome"
}
