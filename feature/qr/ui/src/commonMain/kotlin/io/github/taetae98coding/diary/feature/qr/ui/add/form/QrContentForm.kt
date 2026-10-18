package io.github.taetae98coding.diary.feature.qr.ui.add.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInput
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.place.PlaceCoordinateInput
import io.github.taetae98coding.diary.domain.qr.content.QrFormat
import io.github.taetae98coding.diary.domain.qr.content.QrWifiSecurity
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_contact_address_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_contact_company_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_contact_email_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_contact_name_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_contact_website_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_email_body_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_email_subject_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_email_to_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_event_description_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_event_location_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_event_title_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_latitude_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_longitude_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_phone_number_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_sms_message_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_text_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_url_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_wifi_password_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_wifi_ssid_label
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrContentForm(
    modifier: Modifier = Modifier,
    state: QrContentFormState = rememberQrContentFormState(),
    isMapDisplayedProvider: () -> Boolean = { false },
) {
    WriteQrFieldsEffect(state = state)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
    ) {
        QrFormatSelector(
            state = state,
            modifier = Modifier.fillMaxWidth(),
        )
        when (state.format) {
            QrFormat.TEXT -> {
                QrContentFieldInput(state = state, field = QrTextField.TEXT, label = Res.string.qr_text_label, isMultiLine = true)
            }

            QrFormat.URL -> {
                QrContentFieldInput(state = state, field = QrTextField.URL, label = Res.string.qr_url_label, keyboardType = KeyboardType.Uri)
            }

            QrFormat.CONTACT -> QrContactInput(state = state)

            QrFormat.WIFI -> QrWifiInput(state = state)

            QrFormat.LOCATION -> QrLocationInput(state = state, isMapDisplayedProvider = isMapDisplayedProvider)

            QrFormat.EMAIL -> QrEmailInput(state = state)

            QrFormat.PHONE -> {
                QrContentFieldInput(
                    state = state,
                    field = QrTextField.PHONE_NUMBER,
                    label = Res.string.qr_phone_number_label,
                    keyboardType = KeyboardType.Phone,
                )
            }

            QrFormat.SMS -> QrSmsInput(state = state)

            QrFormat.EVENT -> QrEventInput(state = state)
        }
    }
}

@Composable
private fun QrContactInput(state: QrContentFormState) {
    QrContentFieldInput(state = state, field = QrTextField.CONTACT_NAME, label = Res.string.qr_contact_name_label)
    QrContentFieldInput(
        state = state,
        field = QrTextField.CONTACT_PHONE_NUMBER,
        label = Res.string.qr_phone_number_label,
        keyboardType = KeyboardType.Phone,
    )
    QrContentFieldInput(
        state = state,
        field = QrTextField.CONTACT_EMAIL,
        label = Res.string.qr_contact_email_label,
        keyboardType = KeyboardType.Email,
    )
    QrContentFieldInput(state = state, field = QrTextField.CONTACT_COMPANY, label = Res.string.qr_contact_company_label)
    QrContentFieldInput(
        state = state,
        field = QrTextField.CONTACT_ADDRESS,
        label = Res.string.qr_contact_address_label,
        isMultiLine = true,
    )
    QrContentFieldInput(
        state = state,
        field = QrTextField.CONTACT_WEBSITE,
        label = Res.string.qr_contact_website_label,
        keyboardType = KeyboardType.Uri,
    )
}

@Composable
private fun QrWifiInput(state: QrContentFormState) {
    QrContentFieldInput(state = state, field = QrTextField.WIFI_SSID, label = Res.string.qr_wifi_ssid_label)
    QrWifiSecuritySelector(
        state = state,
        modifier = Modifier.fillMaxWidth(),
    )
    if (state.wifiSecurity != QrWifiSecurity.NONE) {
        QrContentFieldInput(
            state = state,
            field = QrTextField.WIFI_PASSWORD,
            label = Res.string.qr_wifi_password_label,
            keyboardType = KeyboardType.Password,
        )
    }
    QrWifiHiddenRow(
        state = state,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun QrLocationInput(
    state: QrContentFormState,
    isMapDisplayedProvider: () -> Boolean,
) {
    if (isMapDisplayedProvider()) {
        QrLocationMap(
            state = state,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(DiaryTheme.dimens.fixedMapHeight),
        )
    }
    QrCoordinateInput(state = state, field = QrTextField.LATITUDE, label = Res.string.qr_latitude_label)
    QrCoordinateInput(state = state, field = QrTextField.LONGITUDE, label = Res.string.qr_longitude_label)
}

@Composable
private fun QrEmailInput(state: QrContentFormState) {
    QrContentFieldInput(state = state, field = QrTextField.EMAIL_TO, label = Res.string.qr_email_to_label, keyboardType = KeyboardType.Email)
    QrContentFieldInput(state = state, field = QrTextField.EMAIL_SUBJECT, label = Res.string.qr_email_subject_label)
    QrContentFieldInput(state = state, field = QrTextField.EMAIL_BODY, label = Res.string.qr_email_body_label, isMultiLine = true)
}

@Composable
private fun QrSmsInput(state: QrContentFormState) {
    QrContentFieldInput(
        state = state,
        field = QrTextField.SMS_PHONE_NUMBER,
        label = Res.string.qr_phone_number_label,
        keyboardType = KeyboardType.Phone,
    )
    QrContentFieldInput(state = state, field = QrTextField.SMS_MESSAGE, label = Res.string.qr_sms_message_label, isMultiLine = true)
}

@Composable
private fun QrEventInput(state: QrContentFormState) {
    QrContentFieldInput(state = state, field = QrTextField.EVENT_TITLE, label = Res.string.qr_event_title_label)
    DiaryDateTimeInput(
        state = state.eventPeriodState,
        modifier = Modifier.fillMaxWidth(),
    )
    QrContentFieldInput(state = state, field = QrTextField.EVENT_LOCATION, label = Res.string.qr_event_location_label)
    QrContentFieldInput(
        state = state,
        field = QrTextField.EVENT_DESCRIPTION,
        label = Res.string.qr_event_description_label,
        isMultiLine = true,
    )
}

@Composable
private fun QrContentFieldInput(
    state: QrContentFormState,
    field: QrTextField,
    label: StringResource,
    keyboardType: KeyboardType = KeyboardType.Text,
    isMultiLine: Boolean = false,
) {
    QrFieldInput(
        label = stringResource(label),
        modifier = Modifier.fillMaxWidth(),
        state = state.fieldState(field),
        inputTransformation = state.inputTransformation(field),
        keyboardType = keyboardType,
        isMultiLine = isMultiLine,
    )
}

@Composable
private fun QrCoordinateInput(
    state: QrContentFormState,
    field: QrTextField,
    label: StringResource,
) {
    val fieldState = state.fieldState(field)

    PlaceCoordinateInput(
        label = stringResource(label),
        modifier =
            Modifier
                .fillMaxWidth()
                .focusRequester(fieldState.focusRequester),
        state = fieldState.textFieldState,
    )
}

private class QrFormatPreviewParameter : PreviewParameterProvider<QrFormat> {
    override val values: Sequence<QrFormat> = qrFormatList.asSequence()
}

@ComponentPreview
@Composable
private fun QrContentFormPreview(
    @PreviewParameter(QrFormatPreviewParameter::class) format: QrFormat,
) {
    val state = rememberQrContentFormState()
    remember(format) { state.selectFormat(format) }

    DiaryTheme {
        Surface {
            QrContentForm(state = state)
        }
    }
}
