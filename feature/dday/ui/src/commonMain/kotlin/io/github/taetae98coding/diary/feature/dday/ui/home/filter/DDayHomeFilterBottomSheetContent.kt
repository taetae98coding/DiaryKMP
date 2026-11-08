package io.github.taetae98coding.diary.feature.dday.ui.home.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.TagListPreviewParameter
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterBottomSheetContent
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_home_filter_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DDayHomeFilterBottomSheetContent(
    onEvent: (TagFilterEvent) -> Unit,
    modifier: Modifier = Modifier,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> DDayHomeFilterUiState = { DDayHomeFilterUiState() },
) {
    TagFilterBottomSheetContent(
        title = stringResource(Res.string.dday_home_filter_title),
        onEvent = onEvent,
        modifier = modifier,
        tagPagingItems = tagPagingItems,
        selectedTagIdSetProvider = { uiStateProvider().selectedTagIdSet },
    )
}

@ScreenPreview
@Composable
private fun DDayHomeFilterBottomSheetContentPreview(
    @PreviewParameter(TagListPreviewParameter::class) tagList: List<Tag>,
) {
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        DDayHomeFilterBottomSheetContent(
            onEvent = {},
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { DDayHomeFilterUiState(selectedTagIdSet = setOfNotNull(tagList.firstOrNull()?.id)) },
        )
    }
}
