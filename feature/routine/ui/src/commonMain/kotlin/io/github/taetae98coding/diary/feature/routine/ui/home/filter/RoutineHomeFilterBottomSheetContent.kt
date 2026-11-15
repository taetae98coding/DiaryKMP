@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.home.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
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
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterEvent
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterFlexBox
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterTitleRow
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_filter_tag_title
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_filter_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineHomeFilterBottomSheetContent(
    onEvent: (RoutineHomeFilterBottomSheetEvent) -> Unit,
    onTagFilterEvent: (TagFilterEvent) -> Unit,
    modifier: Modifier = Modifier,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> RoutineHomeFilterUiState = { RoutineHomeFilterUiState() },
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.routine_home_filter_title),
            modifier = Modifier.styleable(style = DiaryTheme.styles.bottomSheetTitle),
            style = DiaryTheme.typography.titleLargeEmphasized,
        )

        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .styleable(style = DiaryTheme.styles.bottomSheetContent),
        ) {
            RoutineHomeViewScopeSection(
                onEvent = onEvent,
                modifier = Modifier.styleable(style = DiaryTheme.styles.bottomSheetSection),
                viewScopeProvider = { uiStateProvider().viewScope },
            )

            HorizontalDivider(
                modifier =
                    Modifier
                        .styleable(style = DiaryTheme.styles.bottomSheetSection)
                        .padding(vertical = DiaryTheme.dimens.componentSpacing),
            )

            RoutineHomeTagFilterSection(
                onEvent = onTagFilterEvent,
                modifier = Modifier.fillMaxWidth(),
                tagPagingItems = tagPagingItems,
                uiStateProvider = uiStateProvider,
            )
        }
    }
}

@Composable
private fun RoutineHomeTagFilterSection(
    onEvent: (TagFilterEvent) -> Unit,
    modifier: Modifier = Modifier,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> RoutineHomeFilterUiState = { RoutineHomeFilterUiState() },
) {
    Column(modifier = modifier) {
        TagFilterTitleRow(
            title = stringResource(Res.string.routine_home_filter_tag_title),
            onEvent = onEvent,
            modifier = Modifier.styleable(style = DiaryTheme.styles.bottomSheetSection),
            titleStyle = DiaryTheme.typography.titleMedium,
            selectedTagIdSetProvider = { uiStateProvider().selectedTagIdSet },
        )

        TagFilterFlexBox(
            onEvent = onEvent,
            modifier =
                Modifier
                    .styleable(style = DiaryTheme.styles.bottomSheetSection)
                    .padding(top = DiaryTheme.dimens.itemSpacing),
            tagPagingItems = tagPagingItems,
            selectedTagIdSetProvider = { uiStateProvider().selectedTagIdSet },
        )
    }
}

@ScreenPreview
@Composable
private fun RoutineHomeFilterBottomSheetContentPreview(
    @PreviewParameter(TagListPreviewParameter::class) tagList: List<Tag>,
) {
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        RoutineHomeFilterBottomSheetContent(
            onEvent = {},
            onTagFilterEvent = {},
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { RoutineHomeFilterUiState(selectedTagIdSet = setOfNotNull(tagList.firstOrNull()?.id)) },
        )
    }
}
