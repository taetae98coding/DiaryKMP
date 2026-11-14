package io.github.taetae98coding.diary.feature.routine.ui.home.viewmode

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class RoutineHomeViewModePreviewParameter : PreviewParameterProvider<RoutineHomeViewMode> {
    override val values: Sequence<RoutineHomeViewMode> = routineHomeViewModeList.asSequence()
}
