package io.github.taetae98coding.diary.compose.tag.entity

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.previewTag
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.flowOf

public const val ENTITY_TAG_PICKER_LIST_TEST_TAG: String = "EntityTagPickerList"

@Composable
internal fun EntityTagPickerList(
    onEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    LazyColumn(modifier = modifier.testTag(ENTITY_TAG_PICKER_LIST_TEST_TAG)) {
        items(
            count = tagPagingItems.itemCount,
            key = tagPagingItems.itemKey { tag -> tag.id },
        ) { index ->
            val tag = tagPagingItems[index]
            val uiState = uiStateProvider()

            EntityTagPickerRow(
                onEvent = onEvent,
                tag = tag,
                isSelected = tag != null && uiState.tagList.any { selectedTag -> selectedTag.id == tag.id },
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
            )
        }
    }
}

@ComponentPreview
@Composable
private fun EntityTagPickerListPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        Surface {
            EntityTagPickerList(
                onEvent = {},
                tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
                uiStateProvider = { EntityTagInputUiState(tagList = tagList) },
            )
        }
    }
}
