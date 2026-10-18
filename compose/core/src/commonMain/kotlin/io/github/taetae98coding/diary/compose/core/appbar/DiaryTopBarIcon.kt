package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.ui.graphics.vector.ImageVector
import io.github.taetae98coding.diary.compose.core.icon.AutoAwesome
import io.github.taetae98coding.diary.compose.core.icon.CalendarAddOn
import io.github.taetae98coding.diary.compose.core.icon.Call
import io.github.taetae98coding.diary.compose.core.icon.ContentCopy
import io.github.taetae98coding.diary.compose.core.icon.Delete
import io.github.taetae98coding.diary.compose.core.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.core.icon.Download
import io.github.taetae98coding.diary.compose.core.icon.FilterList
import io.github.taetae98coding.diary.compose.core.icon.List
import io.github.taetae98coding.diary.compose.core.icon.Mail
import io.github.taetae98coding.diary.compose.core.icon.Map
import io.github.taetae98coding.diary.compose.core.icon.OpenInNew
import io.github.taetae98coding.diary.compose.core.icon.PersonAdd
import io.github.taetae98coding.diary.compose.core.icon.QrCodeScanner
import io.github.taetae98coding.diary.compose.core.icon.Search
import io.github.taetae98coding.diary.compose.core.icon.Settings
import io.github.taetae98coding.diary.compose.core.icon.Sms
import io.github.taetae98coding.diary.compose.core.icon.Star
import io.github.taetae98coding.diary.compose.core.icon.StarFill
import io.github.taetae98coding.diary.compose.core.icon.Undo
import io.github.taetae98coding.diary.compose.core.icon.Verified
import io.github.taetae98coding.diary.compose.core.icon.Wifi

public enum class DiaryTopBarIcon {
    AUTO_AWESOME,
    CALENDAR_ADD_ON,
    CALL,
    COPY,
    DELETE,
    DOWNLOAD,
    FILTER,
    FINISH,
    LIST,
    MAIL,
    MAP,
    OPEN_IN_NEW,
    PERSON_ADD,
    QR_SCAN,
    SEARCH,
    SETTING,
    SMS,
    STAR,
    STAR_FILL,
    UNDO,
    WIFI,
}

internal val DiaryTopBarIcon.imageVector: ImageVector
    get() =
        when (this) {
            DiaryTopBarIcon.AUTO_AWESOME -> DiaryIcons.AutoAwesome
            DiaryTopBarIcon.CALENDAR_ADD_ON -> DiaryIcons.CalendarAddOn
            DiaryTopBarIcon.CALL -> DiaryIcons.Call
            DiaryTopBarIcon.COPY -> DiaryIcons.ContentCopy
            DiaryTopBarIcon.DELETE -> DiaryIcons.Delete
            DiaryTopBarIcon.DOWNLOAD -> DiaryIcons.Download
            DiaryTopBarIcon.FILTER -> DiaryIcons.FilterList
            DiaryTopBarIcon.FINISH -> DiaryIcons.Verified
            DiaryTopBarIcon.LIST -> DiaryIcons.List
            DiaryTopBarIcon.MAIL -> DiaryIcons.Mail
            DiaryTopBarIcon.MAP -> DiaryIcons.Map
            DiaryTopBarIcon.OPEN_IN_NEW -> DiaryIcons.OpenInNew
            DiaryTopBarIcon.PERSON_ADD -> DiaryIcons.PersonAdd
            DiaryTopBarIcon.QR_SCAN -> DiaryIcons.QrCodeScanner
            DiaryTopBarIcon.SEARCH -> DiaryIcons.Search
            DiaryTopBarIcon.SETTING -> DiaryIcons.Settings
            DiaryTopBarIcon.SMS -> DiaryIcons.Sms
            DiaryTopBarIcon.STAR -> DiaryIcons.Star
            DiaryTopBarIcon.STAR_FILL -> DiaryIcons.StarFill
            DiaryTopBarIcon.UNDO -> DiaryIcons.Undo
            DiaryTopBarIcon.WIFI -> DiaryIcons.Wifi
        }

// 한 버튼이 상태에 따라 바꿔 쓰는 아이콘은 같은 버튼으로 이어 교차 전환하고, 그 밖의 아이콘으로 바뀌면 다른 버튼으로 보아 바로 바꾼다.
internal val DiaryTopBarIcon.buttonKey: String
    get() =
        when (this) {
            DiaryTopBarIcon.STAR, DiaryTopBarIcon.STAR_FILL -> "favorite"
            DiaryTopBarIcon.LIST, DiaryTopBarIcon.MAP -> "viewMode"
            else -> name
        }
