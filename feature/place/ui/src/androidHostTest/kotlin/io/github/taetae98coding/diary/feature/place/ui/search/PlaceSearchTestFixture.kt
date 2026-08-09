package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlin.uuid.Uuid

internal const val DEFAULT_QUERY_PLACEHOLDER = "Search for a place"
internal const val DEFAULT_EMPTY_MESSAGE = "No search results"
internal const val DEFAULT_FAILED_MESSAGE = "Couldn't search for places"
internal const val DEFAULT_QUERY_INPUT_DESCRIPTION = "Search query"
internal const val DEFAULT_CLEAR_BUTTON_DESCRIPTION = "Clear search query"
internal const val KOREAN_QUERY_INPUT_DESCRIPTION = "검색어"
internal const val KOREAN_CLEAR_BUTTON_DESCRIPTION = "검색어 지우기"
internal const val SEARCH_DELAY_MILLIS = 200L

internal val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal fun searchedPlace(
    name: String,
    address: String,
): SearchedPlace =
    fixtureMonkey
        .giveMeKotlinBuilder<SearchedPlace>()
        .setExp(SearchedPlace::id, Uuid.random())
        .setExp(SearchedPlace::name, name)
        .setExp(SearchedPlace::address, address)
        .setExp(SearchedPlace::coordinate, Coordinate(latitude = 37.5665, longitude = 126.9780))
        .sample()

/**
 * 검색어 입력과 결과 목록은 다이얼로그가 위아래로 배치하는 두 조각이다.
 * 다이얼로그 전체는 지도를 포함해 호스트 테스트에서 구성할 수 없으므로 두 조각만 함께 구성한다.
 */
internal fun ComposeContentTestRule.setPlaceSearchContent(
    uiStateProvider: () -> PlaceSearchUiState = { PlaceSearchUiState.Idle },
    initialProvider: DiaryMapProvider = DiaryMapProvider.NAVER,
    onSelect: (SearchedPlace) -> Unit = {},
) {
    setContent {
        DiaryTheme {
            val state = rememberPlaceSearchDialogState(initialProvider = initialProvider)

            Column {
                PlaceSearchResult(
                    state = state,
                    uiStateProvider = uiStateProvider,
                    onSelect = onSelect,
                    modifier = Modifier.fillMaxWidth(),
                )
                PlaceSearchQueryInput(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

internal fun ComposeContentTestRule.setPlaceSearchEffect(
    initialProvider: DiaryMapProvider = DiaryMapProvider.NAVER,
    onSearch: (PlaceSearchRequest) -> Unit = {},
    onClear: () -> Unit = {},
): () -> PlaceSearchDialogState {
    lateinit var state: PlaceSearchDialogState

    setContent {
        DiaryTheme {
            state = rememberPlaceSearchDialogState(initialProvider = initialProvider)

            PlaceSearchEffect(state = state, onSearch = onSearch, onClear = onClear)
            PlaceSearchQueryInput(
                state = state,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    return { state }
}
