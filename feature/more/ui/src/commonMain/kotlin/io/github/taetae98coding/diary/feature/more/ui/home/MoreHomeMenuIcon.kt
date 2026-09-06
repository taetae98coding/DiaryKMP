package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.ChecklistIcon
import io.github.taetae98coding.diary.compose.core.icon.ContactIcon
import io.github.taetae98coding.diary.compose.core.icon.DDayIcon
import io.github.taetae98coding.diary.compose.core.icon.FileIcon
import io.github.taetae98coding.diary.compose.core.icon.HolidayIcon
import io.github.taetae98coding.diary.compose.core.icon.MapIcon
import io.github.taetae98coding.diary.compose.core.icon.PlaylistIcon
import io.github.taetae98coding.diary.compose.core.icon.QrIcon
import io.github.taetae98coding.diary.compose.core.icon.SearchIcon
import io.github.taetae98coding.diary.compose.core.icon.WebIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun MoreHomeMenuIcon(
    menu: MoreHomeMenu,
    modifier: Modifier = Modifier,
) {
    when (menu) {
        MoreHomeMenu.SEARCH -> SearchIcon(modifier = modifier)
        MoreHomeMenu.WEB -> WebIcon(modifier = modifier)
        MoreHomeMenu.PLACE -> MapIcon(modifier = modifier)
        MoreHomeMenu.HOLIDAY -> HolidayIcon(modifier = modifier)
        MoreHomeMenu.QR -> QrIcon(modifier = modifier)
        MoreHomeMenu.D_DAY -> DDayIcon(modifier = modifier)
        MoreHomeMenu.CONTACT -> ContactIcon(modifier = modifier)
        MoreHomeMenu.CHECKLIST -> ChecklistIcon(modifier = modifier)
        MoreHomeMenu.FILE -> FileIcon(modifier = modifier)
        MoreHomeMenu.PLAYLIST -> PlaylistIcon(modifier = modifier)
    }
}

@ComponentPreview
@Composable
private fun MoreHomeMenuIconPreview() {
    DiaryTheme {
        Surface {
            MoreHomeMenuIcon(menu = MoreHomeMenu.HOLIDAY)
        }
    }
}
