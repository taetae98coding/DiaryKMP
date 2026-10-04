@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.qr.ui.add.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.qr.content.QrFormat
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_format_contact
import io.github.taetae98coding.diary.feature.qr.ui.qr_format_email
import io.github.taetae98coding.diary.feature.qr.ui.qr_format_event
import io.github.taetae98coding.diary.feature.qr.ui.qr_format_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_format_location
import io.github.taetae98coding.diary.feature.qr.ui.qr_format_phone
import io.github.taetae98coding.diary.feature.qr.ui.qr_format_sms
import io.github.taetae98coding.diary.feature.qr.ui.qr_format_text
import io.github.taetae98coding.diary.feature.qr.ui.qr_format_url
import io.github.taetae98coding.diary.feature.qr.ui.qr_format_wifi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrFormatSelector(
    modifier: Modifier = Modifier,
    state: QrContentFormState = rememberQrContentFormState(),
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { expanded -> isExpanded = expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = state.format.label(),
            onValueChange = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            label = { Text(text = stringResource(Res.string.qr_format_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
        ) {
            qrFormatList.forEach { format ->
                DropdownMenuItem(
                    text = { Text(text = format.label()) },
                    onClick = {
                        state.selectFormat(format)
                        isExpanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun QrFormat.label(): String =
    when (this) {
        QrFormat.TEXT -> stringResource(Res.string.qr_format_text)
        QrFormat.URL -> stringResource(Res.string.qr_format_url)
        QrFormat.CONTACT -> stringResource(Res.string.qr_format_contact)
        QrFormat.WIFI -> stringResource(Res.string.qr_format_wifi)
        QrFormat.LOCATION -> stringResource(Res.string.qr_format_location)
        QrFormat.EMAIL -> stringResource(Res.string.qr_format_email)
        QrFormat.PHONE -> stringResource(Res.string.qr_format_phone)
        QrFormat.SMS -> stringResource(Res.string.qr_format_sms)
        QrFormat.EVENT -> stringResource(Res.string.qr_format_event)
    }

@ComponentPreview
@Composable
private fun QrFormatSelectorPreview() {
    DiaryTheme {
        Surface {
            QrFormatSelector()
        }
    }
}
