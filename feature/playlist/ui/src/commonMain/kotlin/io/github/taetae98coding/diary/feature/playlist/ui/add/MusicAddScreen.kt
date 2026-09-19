package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.feature.playlist.ui.form.rememberMusicAddFormState

@Composable
internal fun MusicAddScreen(
    navigateUp: () -> Unit,
    viewModel: MusicAddViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberMusicAddFormState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DiaryTitleInputFocusEffect(state = state.titleState)
    MusicAddScreenEffect(
        effect = viewModel.effect,
        state = state,
    )

    MusicAddScaffold(
        onEvent = { event ->
            when (event) {
                is MusicAddScaffoldEvent.ClickNavigateUp -> navigateUp()
                is MusicAddScaffoldEvent.ClickAdd -> viewModel.add(detail = state.detail)
            }
        },
        modifier = modifier,
        state = state,
        uiStateProvider = { uiState },
    )
}
