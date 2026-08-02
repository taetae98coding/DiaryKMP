package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.form.TagFormState
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagDetailFormState
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailScaffold(
    onEvent: (TagDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> TagDetailUiState = { TagDetailUiState.Loading },
    state: TagFormState = rememberTagDetailFormState(initialDetail = TagDetail.EMPTY),
    tabState: TagDetailTabState = rememberTagDetailTabState(),
    scopeState: TagDetailScopeState = rememberTagDetailScopeState(),
    componentVisibleProvider: () -> TagDetailScaffoldComponentVisible = { TagDetailScaffoldComponentVisible() },
    tabFloatingActionButton: @Composable (TagDetailTab) -> Unit,
    tabContent: @Composable (TagDetailTab) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TagDetailTopBar(
                uiStateProvider = uiStateProvider,
                onEvent = onEvent,
                componentVisibleProvider = componentVisibleProvider,
                isScopeAppliedProvider = { scopeState.isApplied },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = { tabFloatingActionButton(tabState.tab) },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            TagDetailTabRow(
                modifier = Modifier.fillMaxWidth(),
                state = tabState,
            )
            TagDetailPager(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1F),
                state = tabState,
                tabContent = tabContent,
            )
        }
    }

    TagDetailScopeBottomSheetHost(
        onSelect = { scope -> scopeState.select(scope) },
        state = scopeState,
    )
}

private class TagDetailUiStatePreviewParameter : PreviewParameterProvider<TagDetailUiState> {
    override val values: Sequence<TagDetailUiState> =
        sequenceOf(
            TagDetailUiState.Loading,
            TagDetailUiState.Content(
                id = Uuid.NIL,
                detail = TagDetail.EMPTY.copy(title = "태그 제목"),
                isFinished = false,
            ),
        )
}

@ScreenPreview
@Composable
private fun TagDetailScaffoldPreview(
    @PreviewParameter(TagDetailUiStatePreviewParameter::class) uiState: TagDetailUiState,
) {
    val detail = (uiState as? TagDetailUiState.Content)?.detail ?: TagDetail.EMPTY
    val state = rememberTagDetailFormState(initialDetail = detail)

    DiaryTheme {
        TagDetailScaffold(
            onEvent = {},
            uiStateProvider = { uiState },
            state = state,
            tabFloatingActionButton = { tab ->
                when (tab) {
                    TagDetailTab.DETAIL -> TagDetailFormFloatingActionButton(onClick = {}, isVisible = true)
                    TagDetailTab.MEMO -> TagDetailMemoFloatingActionButton(onClick = {})
                    TagDetailTab.WEB -> TagDetailWebFloatingActionButton(onClick = {})
                    TagDetailTab.PLACE -> TagDetailPlaceFloatingActionButton(onClick = {})
                }
            },
        ) { tab ->
            when (tab) {
                TagDetailTab.DETAIL ->
                    TagDetailFormTab(
                        onEvent = {},
                        modifier = Modifier.fillMaxSize(),
                        uiStateProvider = { uiState },
                        state = state,
                    )

                TagDetailTab.MEMO -> TagDetailMemoTab(onEvent = {}, onMemoListEvent = {}, modifier = Modifier.fillMaxSize())

                TagDetailTab.WEB -> TagDetailWebTab(onEvent = {}, modifier = Modifier.fillMaxSize())

                TagDetailTab.PLACE -> TagDetailPlaceTab(onEvent = {}, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
