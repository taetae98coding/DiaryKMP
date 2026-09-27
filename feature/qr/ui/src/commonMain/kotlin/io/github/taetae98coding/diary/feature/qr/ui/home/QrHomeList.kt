package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import io.github.taetae98coding.diary.compose.core.icon.QrIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.card.SwipeToDeleteQrCard
import io.github.taetae98coding.diary.feature.qr.ui.previewQr
import io.github.taetae98coding.diary.feature.qr.ui.qr_home_empty_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_home_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val QR_HOME_LIST_TEST_TAG: String = "QrHomeList"

@Composable
internal fun QrHomeList(
    onEvent: (QrHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    qrPagingItems: LazyPagingItems<Qr> = remember { flowOf(PagingData.empty<Qr>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryCrossfade(
        targetState = qrPagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            DiaryPullToRefreshBox(
                isRefreshingProvider = isRefreshingProvider,
                onRefresh = { onEvent(QrHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                DiaryEmptyBox(
                    title = stringResource(Res.string.qr_home_empty_title),
                    // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    description = stringResource(Res.string.qr_home_empty_description),
                    icon = { QrIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            }
        } else {
            DiaryRefreshableGrid(
                onRefresh = { onEvent(QrHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                isRefreshingProvider = isRefreshingProvider,
                listTestTag = QR_HOME_LIST_TEST_TAG,
            ) {
                items(
                    count = qrPagingItems.itemCount,
                    key = qrPagingItems.itemKey { qr -> qr.id },
                ) { index ->
                    val qr = qrPagingItems[index]

                    SwipeToDeleteQrCard(
                        onDelete = { qr?.let { value -> onEvent(QrHomeScaffoldEvent.DeleteQr(id = value.id)) } },
                        modifier =
                            Modifier
                                .animateItem()
                                .fillMaxWidth(),
                        qr = qr,
                    )
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun QrHomeListPreview() {
    val qrList =
        remember {
            listOf(
                previewQr(title = "회사 출입", value = "https://example.com/office"),
                previewQr(title = "와이파이", value = "WIFI:S:diary;T:WPA;P:password;;"),
            )
        }
    val qrPagingData = remember(qrList) { flowOf(PagingData.from(qrList)) }

    DiaryTheme {
        QrHomeList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            qrPagingItems = qrPagingData.collectAsLazyPagingItems(),
        )
    }
}
