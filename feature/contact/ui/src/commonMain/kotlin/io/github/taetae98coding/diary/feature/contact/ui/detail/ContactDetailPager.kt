package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_memo_empty_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_memo_empty_title
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTab
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTabState
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.contactDetailTabList
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.rememberContactDetailTabState
import io.github.taetae98coding.diary.feature.core.memo.EntityDetailMemoTab
import org.jetbrains.compose.resources.stringResource

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
                ContactDetailTab.MEMO -> EntityDetailMemoTab(emptyTitle = stringResource(Res.string.contact_detail_memo_empty_title), emptyDescription = stringResource(Res.string.contact_detail_memo_empty_description), onEvent = {}, onMemoListEvent = {}, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
