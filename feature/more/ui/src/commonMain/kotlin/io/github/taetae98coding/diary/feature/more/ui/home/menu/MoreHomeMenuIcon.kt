package io.github.taetae98coding.diary.feature.more.ui.home.menu

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.icon.ChecklistIcon
import io.github.taetae98coding.diary.compose.icon.ContactIcon
import io.github.taetae98coding.diary.compose.icon.DDayIcon
import io.github.taetae98coding.diary.compose.icon.FileIcon
import io.github.taetae98coding.diary.compose.icon.HolidayIcon
import io.github.taetae98coding.diary.compose.icon.MapIcon
import io.github.taetae98coding.diary.compose.icon.PlaylistIcon
import io.github.taetae98coding.diary.compose.icon.QrIcon
import io.github.taetae98coding.diary.compose.icon.SearchIcon
import io.github.taetae98coding.diary.compose.icon.WebIcon
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview

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
