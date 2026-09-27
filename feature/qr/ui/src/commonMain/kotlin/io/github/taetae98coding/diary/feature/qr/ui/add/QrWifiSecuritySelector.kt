package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.qr.content.QrWifiSecurity
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_wifi_security_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_wifi_security_none
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrWifiSecuritySelector(
    modifier: Modifier = Modifier,
    state: QrContentFormState = rememberQrContentFormState(),
) {
    val selectorContentDescription = stringResource(Res.string.qr_wifi_security_label)
    val selectedSecurity = state.wifiSecurity

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.semantics { contentDescription = selectorContentDescription },
    ) {
        qrWifiSecurityList.forEachIndexed { index, security ->
            SegmentedButton(
                selected = security == selectedSecurity,
                onClick = { state.wifiSecurity = security },
                shape =
                    SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = qrWifiSecurityList.size,
                    ),
                label = { Text(text = security.label()) },
            )
        }
    }
}

@Composable
private fun QrWifiSecurity.label(): String =
    when (this) {
        QrWifiSecurity.WPA -> "WPA/WPA2/WPA3"
        QrWifiSecurity.WEP -> "WEP"
        QrWifiSecurity.NONE -> stringResource(Res.string.qr_wifi_security_none)
    }

@ComponentPreview
@Composable
private fun QrWifiSecuritySelectorPreview() {
    DiaryTheme {
        Surface {
            QrWifiSecuritySelector()
        }
    }
}
