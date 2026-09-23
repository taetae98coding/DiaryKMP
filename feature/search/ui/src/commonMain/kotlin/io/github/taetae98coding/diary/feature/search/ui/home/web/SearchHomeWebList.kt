package io.github.taetae98coding.diary.feature.search.ui.home.web

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.icon.WebIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.WebCard
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResult
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultEvent
import io.github.taetae98coding.diary.feature.search.ui.previewWeb
import kotlinx.coroutines.flow.flowOf

internal const val SEARCH_HOME_WEB_LIST_TEST_TAG: String = "SearchHomeWebList"

@Composable
internal fun SearchHomeWebList(
    onEvent: (SearchHomeResultEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    sortSheetState: DialogState = rememberDialogState(),
    webPagingItems: LazyPagingItems<Web> = remember { flowOf(PagingData.empty<Web>()) }.collectAsLazyPagingItems(),
    query: String = "",
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    SearchHomeResult(
        onEvent = onEvent,
        modifier = modifier,
        sortSheetState = sortSheetState,
        query = query,
        pagingItems = webPagingItems,
        sortProvider = sortProvider,
        emptyIcon = { WebIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
    ) {
        LazyColumn(
            modifier = Modifier.testTag(SEARCH_HOME_WEB_LIST_TEST_TAG),
            state = listState,
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        ) {
            items(
                count = webPagingItems.itemCount,
                key = webPagingItems.itemKey { web -> web.id },
            ) { index ->
                val web = webPagingItems[index]

                WebCard(
                    onClick = { web?.let { value -> onEvent(SearchHomeResultEvent.ClickResult(id = value.id)) } },
                    modifier =
                        Modifier
                            .animateItem()
                            .fillMaxWidth(),
                    web = web,
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun SearchHomeWebListPreview() {
    val webPagingData =
        remember {
            flowOf(
                PagingData.from(
                    listOf(
                        previewWeb(title = "여행 준비 정보", url = "https://developer.android.com"),
                        previewWeb(title = "여행 숙소 예약", url = "https://kotlinlang.org"),
                    ),
                ),
            )
        }

    DiaryTheme {
        SearchHomeWebList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            webPagingItems = webPagingData.collectAsLazyPagingItems(),
        )
    }
}
