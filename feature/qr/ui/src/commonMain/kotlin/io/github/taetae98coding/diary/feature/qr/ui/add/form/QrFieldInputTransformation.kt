package io.github.taetae98coding.diary.feature.qr.ui.add.form

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import io.github.taetae98coding.diary.domain.qr.content.QrContent
import io.github.taetae98coding.diary.domain.qr.content.QrFormat
import io.github.taetae98coding.diary.domain.qr.content.patch
import io.github.taetae98coding.diary.feature.qr.ui.code.fitsInQrCode

internal class QrFieldInputTransformation(
    private val field: QrTextField,
    private val rawProvider: () -> String,
    private val writtenContentProvider: () -> QrContent,
    private val contentOf: (format: QrFormat, editedField: QrTextField, editedText: String) -> QrContent,
) : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val format = field.format
        if (format == QrFormat.LOCATION) return

        val edited = asCharSequence().toString()
        val value =
            if (format == QrFormat.TEXT) {
                edited
            } else {
                contentOf(format, field, edited).patch(raw = rawProvider(), previous = writtenContentProvider())
            }

        if (value.isNotEmpty() && !value.fitsInQrCode()) {
            revertAllChanges()
        }
    }
}

internal val QrTextField.format: QrFormat
    get() =
        when (this) {
            QrTextField.TEXT -> QrFormat.TEXT

            QrTextField.URL -> QrFormat.URL

            QrTextField.CONTACT_NAME,
            QrTextField.CONTACT_PHONE_NUMBER,
            QrTextField.CONTACT_EMAIL,
            QrTextField.CONTACT_COMPANY,
            QrTextField.CONTACT_ADDRESS,
            QrTextField.CONTACT_WEBSITE,
            -> QrFormat.CONTACT

            QrTextField.WIFI_SSID,
            QrTextField.WIFI_PASSWORD,
            -> QrFormat.WIFI

            QrTextField.LATITUDE,
            QrTextField.LONGITUDE,
            -> QrFormat.LOCATION

            QrTextField.EMAIL_TO,
            QrTextField.EMAIL_SUBJECT,
            QrTextField.EMAIL_BODY,
            -> QrFormat.EMAIL

            QrTextField.PHONE_NUMBER -> QrFormat.PHONE

            QrTextField.SMS_PHONE_NUMBER,
            QrTextField.SMS_MESSAGE,
            -> QrFormat.SMS

            QrTextField.EVENT_TITLE,
            QrTextField.EVENT_LOCATION,
            QrTextField.EVENT_DESCRIPTION,
            -> QrFormat.EVENT
        }
