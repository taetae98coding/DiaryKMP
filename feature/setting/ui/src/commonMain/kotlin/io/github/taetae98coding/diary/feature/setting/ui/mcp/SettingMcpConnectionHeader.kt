package io.github.taetae98coding.diary.feature.setting.ui.mcp

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_mcp_connection_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_mcp_connection_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingMcpConnectionHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.setting_mcp_connection_label),
            style = DiaryTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(Res.string.setting_mcp_connection_description),
            color = DiaryTheme.colorScheme.onSurfaceVariant,
            style = DiaryTheme.typography.bodySmall,
        )
    }
}

@ComponentPreview
@Composable
private fun SettingMcpConnectionHeaderPreview() {
    DiaryTheme {
        SettingMcpConnectionHeader()
    }
}
