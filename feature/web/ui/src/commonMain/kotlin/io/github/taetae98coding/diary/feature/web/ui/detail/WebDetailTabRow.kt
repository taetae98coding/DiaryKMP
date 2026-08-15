package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.EditIcon
import io.github.taetae98coding.diary.compose.core.icon.WebIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.web_detail_form_tab_content_description
import io.github.taetae98coding.diary.feature.web.ui.web_detail_page_tab_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WebDetailTabRow(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: WebDetailScaffoldState = rememberWebDetailScaffoldState(),
) {
    PrimaryTabRow(
        selectedTabIndex = webDetailTabList.indexOf(state.tab),
        modifier = modifier,
    ) {
        webDetailTabList.forEach { tab ->
            Tab(
                selected = tab == state.tab,
                onClick = { onEvent(WebDetailScaffoldEvent.SelectTab(tab = tab)) },
                icon = { TabIcon(tab = tab) },
            )
        }
    }
}

@Composable
private fun TabIcon(tab: WebDetailTab) {
    when (tab) {
        WebDetailTab.FORM ->
            EditIcon(contentDescription = stringResource(Res.string.web_detail_form_tab_content_description))

        WebDetailTab.PAGE ->
            WebIcon(contentDescription = stringResource(Res.string.web_detail_page_tab_content_description))
    }
}

@ComponentPreview
@Composable
private fun WebDetailTabRowPreview() {
    DiaryTheme {
        WebDetailTabRow(onEvent = {})
    }
}
