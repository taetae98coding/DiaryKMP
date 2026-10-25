package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
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
    val title = stringResource(Res.string.more_home_title)
    val settingContentDescription = stringResource(Res.string.more_setting_button_content_description)

    DiaryTopBar(
        title = { Title(text = title) },
        modifier = modifier,
        actions = {
            IconAction(
                icon = DiaryTopBarIcon.SETTING,
                contentDescription = settingContentDescription,
                onClick = { onEvent(MoreHomeScaffoldEvent.ClickSetting) },
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
