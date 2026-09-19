@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.tag

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.layout.DiaryFlexBox
import io.github.taetae98coding.diary.compose.core.layout.animatePlacement
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.theme.dimmed
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

@Composable
public fun TagFilterFlexBox(
    onEvent: (TagFilterEvent) -> Unit,
    modifier: Modifier = Modifier,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    selectedTagIdSetProvider: () -> Set<Uuid> = { emptySet() },
    isEnabledProvider: () -> Boolean = { true },
) {
    val isEnabled = isEnabledProvider()
    val styleState = rememberUpdatedStyleState(interactionSource = null) { it.isEnabled = isEnabled }

    DiaryFlexBox(
        modifier =
            modifier.styleable(styleState) {
                disabled { dimmed() }
            },
    ) {
        repeat(tagPagingItems.itemCount) { index ->
            val tag = tagPagingItems[index] ?: return@repeat

            key(tag.id) {
                TagFilterChip(
                    tag = tag,
                    onEvent = onEvent,
                    modifier = Modifier.animatePlacement(lookaheadScope = this),
                    isSelected = tag.id in selectedTagIdSetProvider(),
                    isEnabled = isEnabled,
                )
            }
        }
    }
}

@ComponentPreview
@Composable
private fun TagFilterFlexBoxPreview() {
    val tagList =
        remember {
            listOf(
                previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5),
                previewTag(emoji = "🏃", title = "운동", color = 0xFFE57373),
            )
        }
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        Surface {
            TagFilterFlexBox(
                onEvent = {},
                tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
                selectedTagIdSetProvider = { setOf(tagList.first().id) },
            )
        }
    }
}
