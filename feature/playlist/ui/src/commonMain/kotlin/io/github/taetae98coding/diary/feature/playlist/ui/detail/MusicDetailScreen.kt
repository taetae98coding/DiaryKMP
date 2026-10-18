package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.core.uri.tryOpenUri
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.form.rememberMusicDetailFormState
import io.github.taetae98coding.diary.feature.playlist.ui.music_detail_open_failed_message
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MusicDetailScreen(
    navigateUp: () -> Unit,
    componentVisibleProvider: () -> MusicDetailScaffoldComponentVisible,
    viewModel: MusicDetailViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val content = uiState as? MusicDetailUiState.Content
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    val openFailedMessage = stringResource(Res.string.music_detail_open_failed_message)

    key(content?.id) {
        val state = rememberMusicDetailFormState(initialDetail = content?.detail ?: MusicDetail.EMPTY)

        MusicDetailScreenEffect(
            navigateUp = navigateUp,
            effect = viewModel.effect,
            state = state,
        )

        MusicDetailScaffold(
            onEvent = { event ->
                when (event) {
                    is MusicDetailScaffoldEvent.ClickNavigateUp -> navigateUp()

                    is MusicDetailScaffoldEvent.ClickUpdate -> viewModel.update(detail = state.detail)

                    is MusicDetailScaffoldEvent.ClickFetchLink -> viewModel.fetchLink(link = state.link)

                    is MusicDetailScaffoldEvent.ClickOpenInNew ->
                        content
                            ?.detail
                            ?.link
                            ?.takeIf(String::isNotBlank)
                            ?.takeUnless(uriHandler::tryOpenUri)
                            ?.let { coroutineScope.launch { state.snackbarHostState.showImmediate(message = openFailedMessage) } }

                    is MusicDetailScaffoldEvent.ClickDelete -> viewModel.delete()
                }
            },
            modifier = modifier,
            state = state,
            uiStateProvider = { uiState },
            componentVisibleProvider = componentVisibleProvider,
        )
    }
}
