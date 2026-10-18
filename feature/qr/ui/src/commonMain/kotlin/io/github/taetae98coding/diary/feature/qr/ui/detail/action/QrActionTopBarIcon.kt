package io.github.taetae98coding.diary.feature.qr.ui.detail.action

import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
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

internal val QrAction.topBarIcon: DiaryTopBarIcon
    get() =
        when (this) {
            is QrAction.OpenUrl -> DiaryTopBarIcon.OPEN_IN_NEW
            is QrAction.Call -> DiaryTopBarIcon.CALL
            is QrAction.SendEmail -> DiaryTopBarIcon.MAIL
            is QrAction.SendSms -> DiaryTopBarIcon.SMS
            is QrAction.ShowLocation -> DiaryTopBarIcon.MAP
            is QrAction.AddContact -> DiaryTopBarIcon.PERSON_ADD
            is QrAction.AddEvent -> DiaryTopBarIcon.CALENDAR_ADD_ON
            is QrAction.JoinWifi -> DiaryTopBarIcon.WIFI
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
