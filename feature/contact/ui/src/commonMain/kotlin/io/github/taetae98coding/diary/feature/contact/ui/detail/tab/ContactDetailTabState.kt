package io.github.taetae98coding.diary.feature.contact.ui.detail.tab

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
internal class ContactDetailTabState(
    val pagerState: PagerState,
) {
    val tab: ContactDetailTab
        get() = contactDetailTabList[pagerState.currentPage]

    suspend fun select(tab: ContactDetailTab) {
        pagerState.animateScrollToPage(contactDetailTabList.indexOf(tab))
    }
}

@Composable
internal fun rememberContactDetailTabState(initialTab: ContactDetailTab = ContactDetailTab.DETAIL): ContactDetailTabState {
    val pagerState = rememberPagerState(initialPage = contactDetailTabList.indexOf(initialTab)) { contactDetailTabList.size }

    return remember(pagerState) { ContactDetailTabState(pagerState = pagerState) }
}
