package io.github.taetae98coding.diary.feature.more.ui.home

import io.github.taetae98coding.diary.feature.more.ui.Res
import io.github.taetae98coding.diary.feature.more.ui.more_menu_checklist_label
import io.github.taetae98coding.diary.feature.more.ui.more_menu_contact_label
import io.github.taetae98coding.diary.feature.more.ui.more_menu_d_day_label
import io.github.taetae98coding.diary.feature.more.ui.more_menu_file_label
import io.github.taetae98coding.diary.feature.more.ui.more_menu_holiday_label
import io.github.taetae98coding.diary.feature.more.ui.more_menu_place_label
import io.github.taetae98coding.diary.feature.more.ui.more_menu_playlist_label
import io.github.taetae98coding.diary.feature.more.ui.more_menu_qr_label
import io.github.taetae98coding.diary.feature.more.ui.more_menu_search_label
import io.github.taetae98coding.diary.feature.more.ui.more_menu_web_label
import org.jetbrains.compose.resources.StringResource

internal enum class MoreHomeMenu(
    val labelResource: StringResource,
) {
    SEARCH(Res.string.more_menu_search_label),
    WEB(Res.string.more_menu_web_label),
    PLACE(Res.string.more_menu_place_label),
    HOLIDAY(Res.string.more_menu_holiday_label),
    QR(Res.string.more_menu_qr_label),
    D_DAY(Res.string.more_menu_d_day_label),
    CONTACT(Res.string.more_menu_contact_label),
    CHECKLIST(Res.string.more_menu_checklist_label),
    FILE(Res.string.more_menu_file_label),
    PLAYLIST(Res.string.more_menu_playlist_label),
}
