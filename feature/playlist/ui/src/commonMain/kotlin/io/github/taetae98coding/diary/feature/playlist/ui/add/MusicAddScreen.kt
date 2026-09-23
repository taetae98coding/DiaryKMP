package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.effect.RequestFocusEffect
import io.github.taetae98coding.diary.feature.playlist.ui.form.rememberMusicAddFormState

@Composable
internal fun MusicAddScreen(
    navigateUp: () -> Unit,
    componentVisibleProvider: () -> MusicAddScaffoldComponentVisible,
    viewModel: MusicAddViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberMusicAddFormState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RequestFocusEffect(focusRequester = state.linkState.focusRequester)
    MusicAddScreenEffect(
        effect = viewModel.effect,
        state = state,
    )

    MusicAddScaffold(
        onEvent = { event ->
            when (event) {
                is MusicAddScaffoldEvent.ClickNavigateUp -> navigateUp()
                is MusicAddScaffoldEvent.ClickAdd -> viewModel.add(detail = state.detail)
                is MusicAddScaffoldEvent.ClickFetchLink -> viewModel.fetchLink(link = state.link)
            }
        },
        modifier = modifier,
        state = state,
        uiStateProvider = { uiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
