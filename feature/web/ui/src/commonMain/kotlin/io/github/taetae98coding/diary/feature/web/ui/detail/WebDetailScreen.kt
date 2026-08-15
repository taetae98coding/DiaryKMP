package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.tag.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormState
import io.github.taetae98coding.diary.feature.web.ui.form.handleWebFormEvent
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebDetailFormState
import io.github.taetae98coding.diary.feature.web.ui.tag.WebTagAddedResultEffect
import io.github.taetae98coding.diary.feature.web.ui.web_detail_update_succeeded_message
import io.github.taetae98coding.diary.feature.web.ui.web_header_name_blank_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun WebDetailScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    webViewModel: WebDetailViewModel,
    pageViewModel: WebDetailPageViewModel,
    tagViewModel: WebDetailTagViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by webViewModel.uiState.collectAsStateWithLifecycle()
    val pageUiState by pageViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val content = uiState as? WebDetailUiState.Content
    val uriHandler = LocalUriHandler.current

    WebTagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::add,
    )

    key(content?.id) {
        val scaffoldState = rememberWebDetailScaffoldState()
        val formState = rememberWebDetailFormState(initialDetail = content?.detail ?: WebDetail.EMPTY)

        LoadWebPageEffect(pageViewModel = pageViewModel, state = scaffoldState)

        WebDetailScreenEffect(
            effect = webViewModel.effect,
            formState = formState,
            pageViewModel = pageViewModel,
            navigateUp = navigateUp,
        )

        WebDetailScaffold(
            onEvent = { event ->
                handleWebDetailScaffoldEvent(
                    event = event,
                    webViewModel = webViewModel,
                    pageViewModel = pageViewModel,
                    scaffoldState = scaffoldState,
                    formState = formState,
                    uriHandler = uriHandler,
                    url = content?.detail?.url,
                    navigateUp = navigateUp,
                )
            },
            onFormEvent = { event ->
                handleWebFormEvent(
                    event = event,
                    state = formState,
                    tagPagingItems = tagPagingItems,
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToTagDetail = navigateToTagDetail,
                )
            },
            onTagPickerEvent = { event ->
                handleWebDetailTagPickerEvent(
                    event = event,
                    tagViewModel = tagViewModel,
                    navigateToTagAdd = navigateToTagAdd,
                )
            },
            modifier = modifier,
            state = scaffoldState,
            formState = formState,
            tagPagingItems = tagPagingItems,
            uiStateProvider = { uiState },
            pageUiStateProvider = { pageUiState },
            tagUiStateProvider = { tagUiState },
        )
    }
}

private fun handleWebDetailScaffoldEvent(
    event: WebDetailScaffoldEvent,
    webViewModel: WebDetailViewModel,
    pageViewModel: WebDetailPageViewModel,
    scaffoldState: WebDetailScaffoldState,
    formState: WebFormState,
    uriHandler: UriHandler,
    url: String?,
    navigateUp: () -> Unit,
) {
    when (event) {
        is WebDetailScaffoldEvent.ClickNavigateUp -> navigateUp()

        is WebDetailScaffoldEvent.ClickRetry -> pageViewModel.retry()

        is WebDetailScaffoldEvent.ClickUpdate -> webViewModel.update(detail = formState.detail)

        // 앱 밖에서 열지 못해도 화면을 그대로 유지하고 사용자에게 알리지 않는다.
        is WebDetailScaffoldEvent.ClickOpenInNew -> url?.let { value -> runCatching { uriHandler.openUri(value) } }

        is WebDetailScaffoldEvent.ClickDelete -> webViewModel.delete()

        is WebDetailScaffoldEvent.ClickViewMode -> scaffoldState.viewModeSheetState.show()

        is WebDetailScaffoldEvent.SelectTab -> scaffoldState.select(tab = event.tab)

        is WebDetailScaffoldEvent.SelectViewMode -> scaffoldState.select(viewMode = event.viewMode)
    }
}

private fun handleWebDetailTagPickerEvent(
    event: EntityTagPickerEvent,
    tagViewModel: WebDetailTagViewModel,
    navigateToTagAdd: () -> Unit,
) {
    when (event) {
        is EntityTagPickerEvent.ClickAdd -> navigateToTagAdd()
        is EntityTagPickerEvent.Add -> tagViewModel.add(tagId = event.id)
        is EntityTagPickerEvent.Remove -> tagViewModel.remove(tagId = event.id)
        is EntityTagPickerEvent.ChangeQuery -> tagViewModel.updateQuery(query = event.query)
    }
}

// URL 방식에서는 앱이 웹 페이지를 요청하지 않으므로 응답 본문 방식이 될 때만 불러온다.
@Composable
private fun LoadWebPageEffect(
    pageViewModel: WebDetailPageViewModel,
    state: WebDetailScaffoldState,
) {
    LaunchedEffect(state, pageViewModel) {
        snapshotFlow { state.viewMode }
            .filter { viewMode -> viewMode == WebDetailViewMode.RESPONSE }
            .collect { pageViewModel.load() }
    }
}

@Composable
private fun WebDetailScreenEffect(
    formState: WebFormState,
    pageViewModel: WebDetailPageViewModel,
    navigateUp: () -> Unit,
    effect: Flow<WebDetailEffect> = emptyFlow(),
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.web_detail_update_succeeded_message)
    val headerNameBlankMessage = stringResource(Res.string.web_header_name_blank_message)

    CollectEffect(effect) { value ->
        when (value) {
            is WebDetailEffect.UpdateSucceeded -> {
                pageViewModel.refresh()
                coroutineScope.launch { formState.hostState.showImmediate(message = updateSucceededMessage) }
            }

            is WebDetailEffect.HeaderNameBlank -> {
                coroutineScope.launch { formState.hostState.showImmediate(message = headerNameBlankMessage) }
            }

            is WebDetailEffect.DeleteSucceeded -> navigateUp()
        }
    }
}
