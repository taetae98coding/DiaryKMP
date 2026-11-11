package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.core.uri.tryOpenUri
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.form.handleMusicFormEvent
import io.github.taetae98coding.diary.feature.playlist.ui.form.rememberMusicDetailFormState
import io.github.taetae98coding.diary.feature.playlist.ui.music_detail_open_failed_message
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MusicDetailScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> MusicDetailScaffoldComponentVisible,
    musicViewModel: MusicDetailViewModel,
    tagViewModel: EntityDetailTagViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by musicViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val content = uiState as? MusicDetailUiState.Content
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    val openFailedMessage = stringResource(Res.string.music_detail_open_failed_message)

    EntityTagAddedResultEffect(requestKey = tagAddRequestKey, onTagAdded = tagViewModel::add)

    key(content?.id) {
        val state = rememberMusicDetailFormState(initialDetail = content?.detail ?: MusicDetail.EMPTY)

        MusicDetailScreenEffect(
            navigateUp = navigateUp,
            effect = musicViewModel.effect,
            state = state,
        )

        MusicDetailScaffold(
            onEvent = { event ->
                when (event) {
                    is MusicDetailScaffoldEvent.ClickNavigateUp -> navigateUp()

                    is MusicDetailScaffoldEvent.ClickUpdate -> musicViewModel.update(detail = state.detail)

                    is MusicDetailScaffoldEvent.ClickFetchLink -> musicViewModel.fetchLink(link = state.link)

                    is MusicDetailScaffoldEvent.ClickOpenInNew ->
                        content
                            ?.detail
                            ?.link
                            ?.takeIf(String::isNotBlank)
                            ?.let { link ->
                                coroutineScope.launch {
                                    if (!uriHandler.tryOpenUri(link)) {
                                        state.snackbarHostState.showImmediate(message = openFailedMessage)
                                    }
                                }
                            }

                    is MusicDetailScaffoldEvent.ClickDelete -> musicViewModel.delete()
                }
            },
            onFormEvent = { event ->
                handleMusicFormEvent(
                    event = event,
                    state = state,
                    selectableTagPagingItems = selectableTagPagingItems,
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToTagDetail = navigateToTagDetail,
                )
            },
            onTagPickerEvent = { event -> handleEntityTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
            modifier = modifier,
            state = state,
            tagPagingItems = tagPagingItems,
            uiStateProvider = { uiState },
            tagUiStateProvider = { tagUiState },
            componentVisibleProvider = componentVisibleProvider,
        )
    }
}
