@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.tag

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

@Composable
public fun TagFilterBottomSheetContent(
    title: String,
    onEvent: (TagFilterEvent) -> Unit,
    modifier: Modifier = Modifier,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    selectedTagIdSetProvider: () -> Set<Uuid> = { emptySet() },
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TagFilterTitleRow(
            title = title,
            onEvent = onEvent,
            modifier = Modifier.styleable(style = DiaryTheme.styles.bottomSheetTitle),
            selectedTagIdSetProvider = selectedTagIdSetProvider,
        )

        TagFilterFlexBox(
            onEvent = onEvent,
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .styleable(null, DiaryTheme.styles.bottomSheetContent, DiaryTheme.styles.bottomSheetSection),
            tagPagingItems = tagPagingItems,
            selectedTagIdSetProvider = selectedTagIdSetProvider,
        )
    }
}

@ScreenPreview
@Composable
private fun TagFilterBottomSheetContentPreview(
    @PreviewParameter(TagListPreviewParameter::class) tagList: List<Tag>,
) {
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        TagFilterBottomSheetContent(
            title = "태그 필터",
            onEvent = {},
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            selectedTagIdSetProvider = { setOfNotNull(tagList.firstOrNull()?.id) },
        )
    }
}
