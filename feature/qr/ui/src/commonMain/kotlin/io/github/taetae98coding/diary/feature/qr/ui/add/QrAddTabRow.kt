package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.InfoIcon
import io.github.taetae98coding.diary.compose.core.icon.QrIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_info_tab_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_qr_tab_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrAddTabRow(
    modifier: Modifier = Modifier,
    state: QrAddFormState = rememberQrAddFormState(),
) {
    val selectedTab = state.tab

    PrimaryTabRow(
        selectedTabIndex = qrAddTabList.indexOf(selectedTab),
        modifier = modifier,
    ) {
        qrAddTabList.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { state.tab = tab },
                icon = { QrAddTabIcon(tab = tab) },
            )
        }
    }
}

@Composable
private fun QrAddTabIcon(tab: QrAddTab) {
    when (tab) {
        QrAddTab.INFO -> InfoIcon(contentDescription = stringResource(Res.string.qr_add_info_tab_content_description))
        QrAddTab.QR -> QrIcon(contentDescription = stringResource(Res.string.qr_add_qr_tab_content_description))
    }
}

@ComponentPreview
@Composable
private fun QrAddTabRowPreview() {
    DiaryTheme {
        Surface {
            QrAddTabRow()
        }
    }
}
