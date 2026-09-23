package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.feature.playlist.ui.form.rememberMusicDetailFormState

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

    // 조회한 곡이 정해지면 그 값으로 입력을 한 번만 채운다.
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

                    // 앱 밖에서 여는 주소는 입력 중인 값이 아니라 저장된 링크다.
                    is MusicDetailScaffoldEvent.ClickOpenInNew -> content?.detail?.link?.let(uriHandler::openUri)

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
