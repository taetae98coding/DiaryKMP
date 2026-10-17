package io.github.taetae98coding.diary.feature.qr.ui.detail.action

import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.icon.CalendarAddOnIcon
import io.github.taetae98coding.diary.compose.core.icon.CallIcon
import io.github.taetae98coding.diary.compose.core.icon.MailIcon
import io.github.taetae98coding.diary.compose.core.icon.MapIcon
import io.github.taetae98coding.diary.compose.core.icon.OpenInNewIcon
import io.github.taetae98coding.diary.compose.core.icon.PersonAddIcon
import io.github.taetae98coding.diary.compose.core.icon.SmsIcon
import io.github.taetae98coding.diary.compose.core.icon.WifiIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.previewQrActionList
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_add_contact_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_add_event_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_call_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_join_wifi_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_open_url_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_send_email_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_send_sms_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_show_location_button_content_description
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrActionButton(
    action: QrAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription = stringResource(action.contentDescription)

    DiaryTooltipBox(text = contentDescription) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            when (action) {
                is QrAction.OpenUrl -> OpenInNewIcon(contentDescription = contentDescription)
                is QrAction.Call -> CallIcon(contentDescription = contentDescription)
                is QrAction.SendEmail -> MailIcon(contentDescription = contentDescription)
                is QrAction.SendSms -> SmsIcon(contentDescription = contentDescription)
                is QrAction.ShowLocation -> MapIcon(contentDescription = contentDescription)
                is QrAction.AddContact -> PersonAddIcon(contentDescription = contentDescription)
                is QrAction.AddEvent -> CalendarAddOnIcon(contentDescription = contentDescription)
                is QrAction.JoinWifi -> WifiIcon(contentDescription = contentDescription)
            }
        }
    }
}

private val QrAction.contentDescription: StringResource
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

private class QrActionPreviewParameter : PreviewParameterProvider<QrAction> {
    override val values: Sequence<QrAction> =
        previewQrActionList().asSequence()
}

@ComponentPreview
@Composable
private fun QrActionButtonPreview(
    @PreviewParameter(QrActionPreviewParameter::class) action: QrAction,
) {
    DiaryTheme {
        Surface {
            QrActionButton(action = action, onClick = {})
        }
    }
}
