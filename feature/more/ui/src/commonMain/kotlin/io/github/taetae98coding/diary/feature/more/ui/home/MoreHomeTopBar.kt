package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.SettingButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.more.ui.Res
import io.github.taetae98coding.diary.feature.more.ui.more_home_title
import io.github.taetae98coding.diary.feature.more.ui.more_setting_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MoreHomeTopBar(
    onEvent: (MoreHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(text = stringResource(Res.string.more_home_title)) },
        modifier = modifier,
        actions = {
            SettingButton(
                onClick = { onEvent(MoreHomeScaffoldEvent.ClickSetting) },
                contentDescription = stringResource(Res.string.more_setting_button_content_description),
            )
        },
    )
}

@ComponentPreview
@Composable
private fun MoreHomeTopBarPreview() {
    DiaryTheme {
        MoreHomeTopBar(onEvent = {})
    }
}
