package io.github.taetae98coding.diary.feature.place.ui.detail.tab

import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.icon.PlaceIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.place_detail_tab_detail_content_description
import io.github.taetae98coding.diary.feature.place.ui.place_detail_tab_memo_content_description
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceDetailTabRow(
    modifier: Modifier = Modifier,
    state: PlaceDetailTabState = rememberPlaceDetailTabState(),
) {
    val coroutineScope = rememberCoroutineScope()

    PrimaryTabRow(
        selectedTabIndex = placeDetailTabList.indexOf(state.tab),
        modifier = modifier,
    ) {
        placeDetailTabList.forEach { tab ->
            Tab(
                selected = tab == state.tab,
                onClick = { coroutineScope.launch { state.select(tab) } },
                icon = { PlaceDetailTabIcon(tab = tab) },
            )
        }
    }
}

@Composable
private fun PlaceDetailTabIcon(tab: PlaceDetailTab) {
    when (tab) {
        PlaceDetailTab.DETAIL -> PlaceIcon(contentDescription = stringResource(Res.string.place_detail_tab_detail_content_description))
        PlaceDetailTab.MEMO -> MemoIcon(contentDescription = stringResource(Res.string.place_detail_tab_memo_content_description))
    }
}

@ComponentPreview
@Composable
private fun PlaceDetailTabRowPreview() {
    DiaryTheme {
        PlaceDetailTabRow()
    }
}
