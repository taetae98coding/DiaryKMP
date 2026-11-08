package io.github.taetae98coding.diary.feature.tag.ui.detail.tab

import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.ChecklistIcon
import io.github.taetae98coding.diary.compose.core.icon.ContactIcon
import io.github.taetae98coding.diary.compose.core.icon.DDayIcon
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.icon.PlaceIcon
import io.github.taetae98coding.diary.compose.core.icon.PlaylistIcon
import io.github.taetae98coding.diary.compose.core.icon.QrIcon
import io.github.taetae98coding.diary.compose.core.icon.TagIcon
import io.github.taetae98coding.diary.compose.core.icon.WebIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_tab_checklist_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_tab_contact_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_tab_dday_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_tab_detail_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_tab_memo_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_tab_music_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_tab_place_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_tab_qr_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_tab_web_content_description
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailTabRow(
    modifier: Modifier = Modifier,
    state: TagDetailTabState = rememberTagDetailTabState(),
) {
    val coroutineScope = rememberCoroutineScope()

    // 스크롤 탭 행은 선택된 탭이 바뀌면 그 탭이 보이도록 스스로 스크롤한다.
    PrimaryScrollableTabRow(
        selectedTabIndex = tagDetailTabList.indexOf(state.tab),
        modifier = modifier,
    ) {
        tagDetailTabList.forEach { tab ->
            Tab(
                selected = tab == state.tab,
                onClick = { coroutineScope.launch { state.select(tab) } },
                icon = { TagDetailTabIcon(tab = tab) },
            )
        }
    }
}

@Composable
private fun TagDetailTabIcon(tab: TagDetailTab) {
    when (tab) {
        TagDetailTab.DETAIL -> TagIcon(contentDescription = stringResource(Res.string.tag_detail_tab_detail_content_description))
        TagDetailTab.MEMO -> MemoIcon(contentDescription = stringResource(Res.string.tag_detail_tab_memo_content_description))
        TagDetailTab.WEB -> WebIcon(contentDescription = stringResource(Res.string.tag_detail_tab_web_content_description))
        TagDetailTab.PLACE -> PlaceIcon(contentDescription = stringResource(Res.string.tag_detail_tab_place_content_description))
        TagDetailTab.CONTACT -> ContactIcon(contentDescription = stringResource(Res.string.tag_detail_tab_contact_content_description))
        TagDetailTab.CHECKLIST -> ChecklistIcon(contentDescription = stringResource(Res.string.tag_detail_tab_checklist_content_description))
        TagDetailTab.MUSIC -> PlaylistIcon(contentDescription = stringResource(Res.string.tag_detail_tab_music_content_description))
        TagDetailTab.QR -> QrIcon(contentDescription = stringResource(Res.string.tag_detail_tab_qr_content_description))
        TagDetailTab.DDAY -> DDayIcon(contentDescription = stringResource(Res.string.tag_detail_tab_dday_content_description))
    }
}

@ComponentPreview
@Composable
private fun TagDetailTabRowPreview() {
    DiaryTheme {
        TagDetailTabRow()
    }
}
