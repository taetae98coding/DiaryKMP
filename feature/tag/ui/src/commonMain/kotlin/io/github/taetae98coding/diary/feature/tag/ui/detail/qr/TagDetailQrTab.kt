package io.github.taetae98coding.diary.feature.tag.ui.detail.qr

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.QrIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableEmptyBox
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.qr.QrListEvent
import io.github.taetae98coding.diary.compose.qr.SwipeQrCard
import io.github.taetae98coding.diary.compose.qr.previewQr
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_qr_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_qr_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailQrTab(
    onEvent: (TagDetailQrContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    qrPagingItems: LazyPagingItems<Qr> = remember { flowOf(PagingData.empty<Qr>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    scopeProvider: () -> TagScope = { TagScope.SELF },
) {
    ListQueryScrollEffect(
        gridState = gridState,
        filterProvider = scopeProvider,
        itemListProvider = { qrPagingItems.itemSnapshotList.items },
        isRefreshingProvider = { qrPagingItems.loadState.refresh is LoadState.Loading },
    )

    DiaryCrossfade(
        targetState = qrPagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            Empty(
                onEvent = onEvent,
                isRefreshingProvider = isRefreshingProvider,
            )
        } else {
            QrGrid(
                onEvent = onEvent,
                gridState = gridState,
                qrPagingItems = qrPagingItems,
                isRefreshingProvider = isRefreshingProvider,
            )
        }
    }
}

@Composable
private fun QrGrid(
    onEvent: (TagDetailQrContentEvent) -> Unit,
    gridState: LazyGridState,
    qrPagingItems: LazyPagingItems<Qr>,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryRefreshableGrid(
        onRefresh = { onEvent(TagDetailQrContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
        state = gridState,
        isRefreshingProvider = isRefreshingProvider,
    ) {
        items(
            count = qrPagingItems.itemCount,
            key = qrPagingItems.itemKey { item -> item.id },
        ) { index ->
            val qr = qrPagingItems[index]

            SwipeQrCard(
                onEvent = { event ->
                    when (event) {
                        is QrListEvent.ClickQr -> onEvent(TagDetailQrContentEvent.ClickQr(id = event.id))
                        is QrListEvent.SwipeDelete -> onEvent(TagDetailQrContentEvent.DeleteQr(id = event.id))
                    }
                },
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
                qr = qr,
            )
        }
    }
}

@Composable
private fun Empty(
    onEvent: (TagDetailQrContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryRefreshableEmptyBox(
        onRefresh = { onEvent(TagDetailQrContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
        isRefreshingProvider = isRefreshingProvider,
    ) {
        DiaryEmptyBox(
            title = stringResource(Res.string.tag_detail_qr_empty_title),
            description = stringResource(Res.string.tag_detail_qr_empty_description),
            icon = { QrIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
        )
    }
}

@ScreenPreview
@Composable
private fun TagDetailQrTabPreview() {
    val pagingData = remember { flowOf(PagingData.from(listOf(previewQr(title = "회사 출입")))) }

    DiaryTheme {
        Surface {
            TagDetailQrTab(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                qrPagingItems = pagingData.collectAsLazyPagingItems(),
            )
        }
    }
}
