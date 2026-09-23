package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.feature.routine.ui.form.rememberRoutineAddFormState

@Composable
internal fun RoutineAddScreen(
    navigateUp: () -> Unit,
    componentVisibleProvider: () -> RoutineAddScaffoldComponentVisible,
    modifier: Modifier = Modifier,
) {
    val state = rememberRoutineAddFormState()

    DiaryTitleInputFocusEffect(state = state.titleState)

    RoutineAddScaffold(
        onEvent = { event ->
            when (event) {
                is RoutineAddScaffoldEvent.ClickNavigateUp -> navigateUp()
                is RoutineAddScaffoldEvent.ClickAdd -> Unit
            }
        },
        modifier = modifier,
        state = state,
        componentVisibleProvider = componentVisibleProvider,
    )
}
