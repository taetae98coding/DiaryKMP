package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_home_title
import io.github.taetae98coding.diary.feature.file.ui.file_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileHomeTopBar(
    onEvent: (FileHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(text = stringResource(Res.string.file_home_title)) },
        modifier = modifier,
        navigationIcon = {
            NavigateUpButton(
                onClick = { onEvent(FileHomeScaffoldEvent.ClickNavigateUp) },
                contentDescription = stringResource(Res.string.file_navigate_up_button_content_description),
            )
        },
    )
}

@ComponentPreview
@Composable
private fun FileHomeTopBarPreview() {
    DiaryTheme {
        FileHomeTopBar(onEvent = {})
    }
}
