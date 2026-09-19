package io.github.taetae98coding.diary.feature.routine.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object RoutineHomeNavKey : ScreenNavKey {
    override val screenName: String get() = "RoutineHome"
}
