package io.github.taetae98coding.diary.feature.setting.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholder
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.icon.SettingIcon
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ScreenPreview
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingDetailPlaceholder(modifier: Modifier = Modifier) {
    DiaryPlaceholder(
        icon = {
            SettingIcon(
                modifier =
                    Modifier
                        .size(DiaryPlaceholderDefaults.IconSize)
                        .testTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG),
            )
        },
        message = { Text(text = stringResource(Res.string.setting_detail_placeholder_message)) },
        modifier = modifier,
    )
}

@ScreenPreview
@Composable
private fun SettingDetailPlaceholderPreview() {
    DiaryTheme {
        SettingDetailPlaceholder()
    }
}

internal const val SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG: String = "settingDetailPlaceholderIcon"
