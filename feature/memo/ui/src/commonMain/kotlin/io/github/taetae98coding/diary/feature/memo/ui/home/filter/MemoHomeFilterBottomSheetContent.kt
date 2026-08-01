package io.github.taetae98coding.diary.feature.memo.ui.home.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.TagFilterEvent
import io.github.taetae98coding.diary.compose.tag.TagFilterFlexBox
import io.github.taetae98coding.diary.compose.tag.TagFilterTitleRow
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_tag_inactive_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_tag_title
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_title
import io.github.taetae98coding.diary.feature.memo.ui.previewTag
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

private val CONTENT_HORIZONTAL_PADDING = 24.dp
private val CONTENT_BOTTOM_PADDING = 16.dp

@Composable
internal fun MemoHomeFilterBottomSheetContent(
    onEvent: (MemoHomeFilterBottomSheetEvent) -> Unit,
    onTagFilterEvent: (TagFilterEvent) -> Unit,
    modifier: Modifier = Modifier,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoHomeFilterUiState = { MemoHomeFilterUiState() },
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.memo_home_filter_title),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CONTENT_HORIZONTAL_PADDING, vertical = 12.dp),
            style = DiaryTheme.typography.titleLargeEmphasized,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = CONTENT_BOTTOM_PADDING),
        ) {
            MemoHomeExistenceFilterSection(
                onEvent = onEvent,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CONTENT_HORIZONTAL_PADDING),
                uiStateProvider = uiStateProvider,
            )

            HorizontalDivider(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CONTENT_HORIZONTAL_PADDING, vertical = DiaryTheme.dimens.componentSpacing),
            )

            MemoHomeTagFilterSection(
                onEvent = onTagFilterEvent,
                modifier = Modifier.fillMaxWidth(),
                tagPagingItems = tagPagingItems,
                uiStateProvider = uiStateProvider,
            )
        }
    }
}

@Composable
private fun MemoHomeTagFilterSection(
    onEvent: (TagFilterEvent) -> Unit,
    modifier: Modifier = Modifier,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoHomeFilterUiState = { MemoHomeFilterUiState() },
) {
    Column(modifier = modifier) {
        TagFilterTitleRow(
            title = stringResource(Res.string.memo_home_filter_tag_title),
            onEvent = onEvent,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CONTENT_HORIZONTAL_PADDING),
            titleStyle = DiaryTheme.typography.titleMedium,
            selectedTagIdSetProvider = { uiStateProvider().selectedTagIdSet },
            isEnabledProvider = { uiStateProvider().isTagFilterEnabled() },
        )

        MemoHomeTagFilterInactiveDescription(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CONTENT_HORIZONTAL_PADDING),
            uiStateProvider = uiStateProvider,
        )

        TagFilterFlexBox(
            onEvent = onEvent,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CONTENT_HORIZONTAL_PADDING)
                    .padding(top = DiaryTheme.dimens.itemSpacing),
            tagPagingItems = tagPagingItems,
            selectedTagIdSetProvider = { uiStateProvider().selectedTagIdSet },
            isEnabledProvider = { uiStateProvider().isTagFilterEnabled() },
        )
    }
}

@Composable
private fun MemoHomeTagFilterInactiveDescription(
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoHomeFilterUiState = { MemoHomeFilterUiState() },
) {
    if (uiStateProvider().isTagFilterEnabled()) return

    Text(
        text = stringResource(Res.string.memo_home_filter_tag_inactive_description),
        modifier = modifier.padding(top = DiaryTheme.dimens.itemSpacing),
        color = DiaryTheme.colorScheme.onSurfaceVariant,
        style = DiaryTheme.typography.bodySmall,
    )
}

private data class MemoHomeFilterPreview(
    val tagList: List<Tag>,
    val tagExistence: MemoFilterExistence,
)

private class MemoHomeFilterPreviewParameter : PreviewParameterProvider<MemoHomeFilterPreview> {
    private val tagList =
        listOf(
            previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5),
            previewTag(emoji = "🏃", title = "운동", color = 0xFFE57373),
        )

    override val values: Sequence<MemoHomeFilterPreview> =
        sequenceOf(
            MemoHomeFilterPreview(tagList = tagList, tagExistence = MemoFilterExistence.ALL),
            MemoHomeFilterPreview(tagList = tagList, tagExistence = MemoFilterExistence.NOT_EXIST),
        )
}

@ScreenPreview
@Composable
private fun MemoHomeFilterBottomSheetContentPreview(
    @PreviewParameter(MemoHomeFilterPreviewParameter::class) preview: MemoHomeFilterPreview,
) {
    val tagPagingData = remember(preview) { flowOf(PagingData.from(preview.tagList)) }

    DiaryTheme {
        MemoHomeFilterBottomSheetContent(
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            uiStateProvider = {
                MemoHomeFilterUiState(
                    selectedTagIdSet = setOfNotNull(preview.tagList.firstOrNull()?.id),
                    existence =
                        MemoExistenceFilter(
                            date = MemoFilterExistence.EXIST,
                            tag = preview.tagExistence,
                            place = MemoFilterExistence.NOT_EXIST,
                        ),
                )
            },
            onEvent = {},
            onTagFilterEvent = {},
        )
    }
}
