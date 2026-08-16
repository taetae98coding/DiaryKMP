package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType

@Stable
internal class SearchHomeScaffoldState(
    val queryState: TextFieldState,
    val pagerState: PagerState,
    val focusRequester: FocusRequester,
) {
    val type: SearchHomeType
        get() = searchHomeTypeList[pagerState.currentPage]

    suspend fun select(type: SearchHomeType) {
        pagerState.animateScrollToPage(searchHomeTypeList.indexOf(type))
    }
}

@Composable
internal fun rememberSearchHomeScaffoldState(initialType: SearchHomeType = SearchHomeType.MEMO): SearchHomeScaffoldState {
    val queryState = rememberTextFieldState()
    val pagerState = rememberPagerState(initialPage = searchHomeTypeList.indexOf(initialType)) { searchHomeTypeList.size }
    val focusRequester = remember { FocusRequester() }

    return remember(queryState, pagerState, focusRequester) {
        SearchHomeScaffoldState(
            queryState = queryState,
            pagerState = pagerState,
            focusRequester = focusRequester,
        )
    }
}
