package io.github.taetae98coding.diary.feature.memo.ui.web

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
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.memo.ui.previewWeb
import kotlinx.coroutines.flow.flowOf

internal const val MEMO_WEB_PICKER_LIST_TEST_TAG: String = "MemoWebPickerList"

@Composable
internal fun MemoWebPickerList(
    onEvent: (MemoWebPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    webPagingItems: LazyPagingItems<Web> = remember { flowOf(PagingData.empty<Web>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoWebInputUiState = { MemoWebInputUiState() },
) {
    LazyColumn(modifier = modifier.testTag(MEMO_WEB_PICKER_LIST_TEST_TAG)) {
        items(
            count = webPagingItems.itemCount,
            key = webPagingItems.itemKey { web -> web.id },
        ) { index ->
            val web = webPagingItems[index]
            val uiState = uiStateProvider()

            MemoWebPickerRow(
                onEvent = onEvent,
                web = web,
                isSelected = web != null && uiState.selectedWebList.any { selectedWeb -> selectedWeb.id == web.id },
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
private fun MemoWebPickerListPreview() {
    val webList = remember { listOf(previewWeb(title = "사내 위키", url = "https://wiki.example.com")) }
    val webPagingData = remember(webList) { flowOf(PagingData.from(webList)) }

    DiaryTheme {
        Surface {
            MemoWebPickerList(
                onEvent = {},
                webPagingItems = webPagingData.collectAsLazyPagingItems(),
                uiStateProvider = { MemoWebInputUiState(selectedWebList = webList) },
            )
        }
    }
}
