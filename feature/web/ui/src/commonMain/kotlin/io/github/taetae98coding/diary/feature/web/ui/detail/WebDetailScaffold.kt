package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.isAddShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerDialogHost
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.web.ui.detail.memo.WebDetailMemoTab
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.WebDetailTab
import io.github.taetae98coding.diary.feature.web.ui.detail.viewmode.WebDetailViewModeBottomSheetHost
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormEvent
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormState
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebDetailFormState
import io.github.taetae98coding.diary.feature.web.ui.previewWebDetail
import io.github.taetae98coding.diary.feature.web.ui.previewWebPage
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

@Composable
internal fun WebDetailScaffold(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    onFormEvent: (WebFormEvent) -> Unit,
    onTagPickerEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: WebDetailScaffoldState = rememberWebDetailScaffoldState(),
    formState: WebFormState = rememberWebDetailFormState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> WebDetailUiState = { WebDetailUiState.Loading },
    pageUiStateProvider: () -> WebDetailPageUiState = { WebDetailPageUiState.Loading },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
    memoContent: @Composable () -> Unit,
) {
    val isChanged by remember(formState) {
        derivedStateOf {
            val content = uiStateProvider() as? WebDetailUiState.Content
            content != null && formState.detail != content.detail
        }
    }

    Scaffold(
        modifier =
            modifier
                .submitShortcut(isEnabledProvider = { isChanged && state.tab != WebDetailTab.MEMO }) { onEvent(WebDetailScaffoldEvent.ClickUpdate) }
                .keyShortcut(isEnableProvider = { state.tab == WebDetailTab.MEMO }) { keyEvent ->
                    if (keyEvent.isAddShortcut()) {
                        onEvent(WebDetailScaffoldEvent.ClickMemoAdd)
                        true
                    } else {
                        false
                    }
                },
        topBar = {
            WebDetailTopBar(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = formState.hostState) },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        WebDetailScaffoldContent(
            onEvent = onEvent,
            onFormEvent = onFormEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            formState = formState,
            isChangedProvider = { isChanged },
            uiStateProvider = uiStateProvider,
            pageUiStateProvider = pageUiStateProvider,
            tagUiStateProvider = tagUiStateProvider,
            memoContent = memoContent,
        )
    }

    WebDetailViewModeBottomSheetHost(
        onEvent = onEvent,
        state = state,
    )

    EntityTagPickerDialogHost(
        dialogState = formState.tagPickerDialogState,
        onEvent = onTagPickerEvent,
        tagPagingItems = tagPagingItems,
        uiStateProvider = tagUiStateProvider,
    )
}

@ScreenPreview
@Composable
private fun WebDetailScaffoldPreview() {
    DiaryTheme {
        WebDetailScaffold(
            onEvent = {},
            onFormEvent = {},
            onTagPickerEvent = {},
            formState = rememberWebDetailFormState(initialDetail = previewWebDetail()),
            uiStateProvider = { WebDetailUiState.Content(id = Uuid.NIL, detail = previewWebDetail()) },
            pageUiStateProvider = { WebDetailPageUiState.Content(page = previewWebPage()) },
        ) {
            WebDetailMemoTab(onEvent = {}, onMemoListEvent = {}, modifier = Modifier.fillMaxSize())
        }
    }
}
