package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.feature.setting.ui.holiday.list.HolidaySettingList
import io.github.taetae98coding.diary.feature.setting.ui.holiday.search.SettingHolidaySearchEmpty
import io.github.taetae98coding.diary.feature.setting.ui.holiday.search.rememberSettingHolidaySearchResult
import io.github.taetae98coding.diary.feature.setting.ui.previewHolidaySettingList

// 검색어는 글자마다 바뀌므로 Scaffold가 아니라 걸러진 목록을 그리는 이곳에서 읽는다.
@Composable
internal fun HolidaySettingContent(
    onEvent: (SettingHolidayScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SettingHolidayScaffoldState = rememberSettingHolidayScaffoldState(),
    holidaySettingList: List<HolidaySetting> = emptyList(),
    listBottomPadding: Dp = DiaryTheme.dimens.screenVerticalPadding,
) {
    val searchResult =
        rememberSettingHolidaySearchResult(
            query = state.query,
            holidaySettingList = holidaySettingList,
        )
    val isFiltering = state.isFiltering
    val listContent =
        remember(isFiltering, searchResult) {
            HolidaySettingListContent(
                isFiltering = isFiltering,
                holidaySettingList = searchResult,
            )
        }

    DiaryCrossfade(
        targetState = listContent,
        modifier = modifier,
        contentKey = { content -> content.isSearchEmpty },
    ) { content ->
        if (content.isSearchEmpty) {
            SettingHolidaySearchEmpty(modifier = Modifier.fillMaxSize())
        } else {
            HolidaySettingList(
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
                holidaySettingList = content.holidaySettingList,
                bottomPadding = listBottomPadding,
            )
        }
    }
}

private data class HolidaySettingListContent(
    val isFiltering: Boolean,
    val holidaySettingList: List<HolidaySetting>,
) {
    val isSearchEmpty: Boolean = isFiltering && holidaySettingList.isEmpty()
}

@ScreenPreview
@Composable
private fun HolidaySettingContentPreview() {
    DiaryTheme {
        Surface {
            HolidaySettingContent(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                holidaySettingList = previewHolidaySettingList(),
            )
        }
    }
}
