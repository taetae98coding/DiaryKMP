package io.github.taetae98coding.diary.feature.routine.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object RoutineAddNavKey : ScreenNavKey {
    override val screenName: String get() = "RoutineAdd"
}
