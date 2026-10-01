package io.github.taetae98coding.diary.feature.setting.ui.download.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.transparentIndicator
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_download_proxy_address_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_download_proxy_address_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_download_proxy_address_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingDownloadAddressInput(
    modifier: Modifier = Modifier,
    state: SettingDownloadFormState = rememberSettingDownloadFormState(),
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val description = stringResource(Res.string.setting_download_proxy_address_description)

    Column(modifier = modifier) {
        Card(modifier = Modifier.fillMaxWidth()) {
            TextField(
                state = state.addressState,
                // 입력은 자식 시맨틱을 자기 노드로 합치므로 카드 밖 보조 문구는 입력 노드에 옮겨 달아 라벨과 함께 읽히게 한다.
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = description },
                label = { Text(text = stringResource(Res.string.setting_download_proxy_address_label)) },
                placeholder = { Text(text = stringResource(Res.string.setting_download_proxy_address_placeholder)) },
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done,
                    ),
                onKeyboardAction = { keyboardController?.hide() },
                lineLimits = TextFieldLineLimits.SingleLine,
                colors = TextFieldDefaults.colors().transparentIndicator(),
            )
        }
        Text(
            text = description,
            modifier = Modifier.clearAndSetSemantics {},
            color = DiaryTheme.colorScheme.onSurfaceVariant,
            style = DiaryTheme.typography.bodySmall,
        )
    }
}

@ComponentPreview
@Composable
private fun SettingDownloadAddressInputPreview() {
    DiaryTheme {
        Surface {
            SettingDownloadAddressInput()
        }
    }
}
