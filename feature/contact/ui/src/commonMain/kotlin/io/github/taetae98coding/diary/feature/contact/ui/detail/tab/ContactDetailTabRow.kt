package io.github.taetae98coding.diary.feature.contact.ui.detail.tab

import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.ContactIcon
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_tab_detail_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_tab_memo_content_description
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactDetailTabRow(
    modifier: Modifier = Modifier,
    state: ContactDetailTabState = rememberContactDetailTabState(),
) {
    val coroutineScope = rememberCoroutineScope()

    PrimaryTabRow(
        selectedTabIndex = contactDetailTabList.indexOf(state.tab),
        modifier = modifier,
    ) {
        contactDetailTabList.forEach { tab ->
            Tab(
                selected = tab == state.tab,
                onClick = { coroutineScope.launch { state.select(tab) } },
                icon = { ContactDetailTabIcon(tab = tab) },
            )
        }
    }
}

@Composable
private fun ContactDetailTabIcon(tab: ContactDetailTab) {
    when (tab) {
        ContactDetailTab.DETAIL -> ContactIcon(contentDescription = stringResource(Res.string.contact_detail_tab_detail_content_description))
        ContactDetailTab.MEMO -> MemoIcon(contentDescription = stringResource(Res.string.contact_detail_tab_memo_content_description))
    }
}

@ComponentPreview
@Composable
private fun ContactDetailTabRowPreview() {
    DiaryTheme {
        ContactDetailTabRow()
    }
}
