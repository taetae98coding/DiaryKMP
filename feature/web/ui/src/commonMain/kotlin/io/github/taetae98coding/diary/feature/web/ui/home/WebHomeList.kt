package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.WebIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGrid
import io.github.taetae98coding.diary.compose.core.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.WebCard
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.previewWeb
import io.github.taetae98coding.diary.feature.web.ui.web_home_empty_description
import io.github.taetae98coding.diary.feature.web.ui.web_home_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val WEB_HOME_LIST_TEST_TAG: String = "WebHomeList"

@Composable
internal fun WebHomeList(
    onEvent: (WebHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    webPagingItems: LazyPagingItems<Web> = remember { flowOf(PagingData.empty<Web>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    ListQueryScrollEffect(
        gridState = gridState,
        sortProvider = sortProvider,
        itemListProvider = { webPagingItems.itemSnapshotList.items },
    )

    DiaryCrossfade(
        targetState = webPagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            DiaryPullToRefreshBox(
                isRefreshingProvider = isRefreshingProvider,
                onRefresh = { onEvent(WebHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                DiaryEmptyBox(
                    title = stringResource(Res.string.web_home_empty_title),
                    // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    description = stringResource(Res.string.web_home_empty_description),
                    icon = { WebIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            }
        } else {
            DiaryRefreshableGrid(
                onRefresh = { onEvent(WebHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                isRefreshingProvider = isRefreshingProvider,
                listTestTag = WEB_HOME_LIST_TEST_TAG,
            ) {
                items(
                    count = webPagingItems.itemCount,
                    key = webPagingItems.itemKey { web -> web.id },
                ) { index ->
                    val web = webPagingItems[index]

                    WebCard(
                        onClick = { web?.let { value -> onEvent(WebHomeScaffoldEvent.ClickWeb(id = value.id)) } },
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
}

@ScreenPreview
@Composable
private fun WebHomeListPreview() {
    val webList =
        remember {
            listOf(
                previewWeb(title = "안드로이드 개발자", url = "https://developer.android.com"),
                previewWeb(title = "Kotlin", url = "https://kotlinlang.org"),
            )
        }
    val webPagingData = remember(webList) { flowOf(PagingData.from(webList)) }

    DiaryTheme {
        WebHomeList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            webPagingItems = webPagingData.collectAsLazyPagingItems(),
        )
    }
}
