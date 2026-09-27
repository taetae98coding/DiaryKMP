package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_wifi_hidden_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrWifiHiddenRow(
    modifier: Modifier = Modifier,
    state: QrContentFormState = rememberQrContentFormState(),
) {
    val isHidden = state.isWifiHidden

    Row(
        modifier =
            modifier
                .toggleable(
                    value = isHidden,
                    role = Role.Switch,
                    onValueChange = { value -> state.isWifiHidden = value },
                ).minimumInteractiveComponentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.qr_wifi_hidden_label),
            modifier = Modifier.weight(1F),
        )
        Switch(checked = isHidden, onCheckedChange = null)
    }
}

@ComponentPreview
@Composable
private fun QrWifiHiddenRowPreview() {
    DiaryTheme {
        Surface {
            QrWifiHiddenRow()
        }
    }
}
