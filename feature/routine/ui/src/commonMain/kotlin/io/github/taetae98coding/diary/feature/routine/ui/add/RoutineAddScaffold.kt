package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineForm
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineFormState
import io.github.taetae98coding.diary.feature.routine.ui.form.rememberRoutineAddFormState
import io.github.taetae98coding.diary.feature.routine.ui.routine_add_add_button_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_add_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_add_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineAddScaffold(
    onEvent: (RoutineAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: RoutineFormState = rememberRoutineAddFormState(),
    componentVisibleProvider: () -> RoutineAddScaffoldComponentVisible = { RoutineAddScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.routine_add_title),
                onNavigateUp = { onEvent(RoutineAddScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.routine_add_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
            )
        },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(RoutineAddScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.routine_add_add_button_content_description),
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        RoutineForm(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
        )
    }
}

@ScreenPreview
@Composable
private fun RoutineAddScaffoldPreview() {
    DiaryTheme {
        RoutineAddScaffold(onEvent = {})
    }
}
