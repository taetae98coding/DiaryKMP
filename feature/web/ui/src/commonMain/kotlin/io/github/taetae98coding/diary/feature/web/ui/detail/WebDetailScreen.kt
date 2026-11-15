package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.snackbar.DismissUndoSnackbarEffect
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import io.github.taetae98coding.diary.feature.web.ui.detail.memo.WebDetailMemoContent
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageViewModel
import io.github.taetae98coding.diary.feature.web.ui.form.handleWebFormEvent
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebDetailFormState
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Composable
internal fun WebDetailScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    navigateToMemoAdd: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToMemoFinishedList: () -> Unit,
    id: Uuid,
    tagAddRequestKey: Uuid,
    webViewModel: WebDetailViewModel,
    pageViewModel: WebDetailPageViewModel,
    tagViewModel: EntityDetailTagViewModel,
    contentSharerFactory: ContentSharerFactory,
    modifier: Modifier = Modifier,
) {
    val viewModelStoreProvider = rememberViewModelStoreProvider()
    val uiState by webViewModel.uiState.collectAsStateWithLifecycle()
    val pageUiState by pageViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val content = uiState as? WebDetailUiState.Content
    val coroutineScope = rememberCoroutineScope()
    val contentSharer = contentSharerFactory.rememberContentSharer()

    EntityTagAddedResultEffect(requestKey = tagAddRequestKey, onTagAdded = tagViewModel::add)

    val scaffoldState = key(id) { rememberWebDetailScaffoldState() }

    key(content?.id) {
        val formState = rememberWebDetailFormState(initialDetail = content?.detail ?: WebDetail.EMPTY)

        LoadWebPageEffect(pageViewModel = pageViewModel, state = scaffoldState)

        val outsideAction = rememberWebDetailOutsideAction(contentSharer = contentSharer, snackbarHostState = formState.snackbarHostState)

        WebDetailScreenEffect(effect = webViewModel.effect, formState = formState, navigateUp = navigateUp)
        DismissUndoSnackbarEffect(keyProvider = { scaffoldState.tab }, hostState = formState.snackbarHostState)

        WebDetailScaffold(
            onEvent = { event ->
                when (event) {
                    is WebDetailScaffoldEvent.ClickNavigateUp -> navigateUp()
                    is WebDetailScaffoldEvent.ClickMemoAdd -> navigateToMemoAdd()
                    is WebDetailScaffoldEvent.ClickRetry -> pageViewModel.retry()
                    is WebDetailScaffoldEvent.ClickUpdate -> webViewModel.update(detail = formState.detail)
                    is WebDetailScaffoldEvent.ClickShare -> coroutineScope.launch { outsideAction.share(input = formState.detail, saved = content?.detail) }
                    is WebDetailScaffoldEvent.ClickOpenInNew -> coroutineScope.launch { outsideAction.openInNew(url = content?.detail?.url) }
                    is WebDetailScaffoldEvent.ClickDelete -> webViewModel.delete()
                    is WebDetailScaffoldEvent.ClickViewMode -> scaffoldState.viewModeSheetState.show()
                    is WebDetailScaffoldEvent.SelectTab -> scaffoldState.select(tab = event.tab)
                    is WebDetailScaffoldEvent.SelectViewMode -> scaffoldState.select(viewMode = event.viewMode)
                }
            },
            onFormEvent = { event -> handleWebFormEvent(event = event, state = formState, selectableTagPagingItems = selectableTagPagingItems, navigateToTagAdd = navigateToTagAdd, navigateToTagDetail = navigateToTagDetail) },
            onTagPickerEvent = { event -> handleEntityTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
            modifier = modifier,
            isShareVisible = contentSharer.isTextSupported,
            state = scaffoldState,
            formState = formState,
            tagPagingItems = tagPagingItems,
            uiStateProvider = { uiState },
            pageUiStateProvider = { pageUiState },
            tagUiStateProvider = { tagUiState },
        ) {
            WebDetailMemoContent(
                id = id,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToMemoDetail = navigateToMemoDetail,
                navigateToMemoFinishedList = navigateToMemoFinishedList,
                modifier = Modifier.fillMaxSize(),
                snackbarHostState = formState.snackbarHostState,
            )
        }
    }
}
