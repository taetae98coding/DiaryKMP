package io.github.taetae98coding.diary.feature.qr.ui.detail.action

import androidx.compose.ui.graphics.vector.ImageVector
import io.github.taetae98coding.diary.compose.icon.CalendarAddOn
import io.github.taetae98coding.diary.compose.icon.Call
import io.github.taetae98coding.diary.compose.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.icon.Mail
import io.github.taetae98coding.diary.compose.icon.Map
import io.github.taetae98coding.diary.compose.icon.OpenInNew
import io.github.taetae98coding.diary.compose.icon.PersonAdd
import io.github.taetae98coding.diary.compose.icon.Sms
import io.github.taetae98coding.diary.compose.icon.Wifi
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_add_contact_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_add_event_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_call_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_join_wifi_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_open_url_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_send_email_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_send_sms_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_show_location_button_content_description
import org.jetbrains.compose.resources.StringResource

internal val QrAction.topBarIcon: ImageVector
    get() =
        when (this) {
            is QrAction.OpenUrl -> DiaryIcons.OpenInNew
            is QrAction.Call -> DiaryIcons.Call
            is QrAction.SendEmail -> DiaryIcons.Mail
            is QrAction.SendSms -> DiaryIcons.Sms
            is QrAction.ShowLocation -> DiaryIcons.Map
            is QrAction.AddContact -> DiaryIcons.PersonAdd
            is QrAction.AddEvent -> DiaryIcons.CalendarAddOn
            is QrAction.JoinWifi -> DiaryIcons.Wifi
        }

internal val QrAction.contentDescription: StringResource
    get() =
        when (this) {
            is QrAction.OpenUrl -> Res.string.qr_action_open_url_button_content_description
            is QrAction.Call -> Res.string.qr_action_call_button_content_description
            is QrAction.SendEmail -> Res.string.qr_action_send_email_button_content_description
            is QrAction.SendSms -> Res.string.qr_action_send_sms_button_content_description
            is QrAction.ShowLocation -> Res.string.qr_action_show_location_button_content_description
            is QrAction.AddContact -> Res.string.qr_action_add_contact_button_content_description
            is QrAction.AddEvent -> Res.string.qr_action_add_event_button_content_description
            is QrAction.JoinWifi -> Res.string.qr_action_join_wifi_button_content_description
        }
