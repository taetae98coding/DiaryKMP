package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.contact.ui.detail.memo.ContactDetailMemoTab
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTab
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTabState
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.contactDetailTabList
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.rememberContactDetailTabState

internal const val CONTACT_DETAIL_PAGER_TEST_TAG: String = "ContactDetailPager"

@Composable
internal fun ContactDetailPager(
    modifier: Modifier = Modifier,
    state: ContactDetailTabState = rememberContactDetailTabState(),
    tabContent: @Composable (ContactDetailTab) -> Unit,
) {
    HorizontalPager(
        state = state.pagerState,
        modifier = modifier.testTag(CONTACT_DETAIL_PAGER_TEST_TAG),
    ) { page ->
        tabContent(contactDetailTabList[page])
    }
}

@ScreenPreview
@Composable
private fun ContactDetailPagerPreview() {
    DiaryTheme {
        ContactDetailPager(modifier = Modifier.fillMaxSize()) { tab ->
            when (tab) {
                ContactDetailTab.DETAIL -> ContactDetailScaffoldContent(modifier = Modifier.fillMaxSize())
                ContactDetailTab.MEMO -> ContactDetailMemoTab(onEvent = {}, onMemoListEvent = {}, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
