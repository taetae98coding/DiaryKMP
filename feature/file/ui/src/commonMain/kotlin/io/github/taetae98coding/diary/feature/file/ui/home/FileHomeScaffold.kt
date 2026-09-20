package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_home_title
import io.github.taetae98coding.diary.feature.file.ui.file_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileHomeScaffold(
    onEvent: (FileHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 본문에 둘 내용은 아직 정하지 않았다. 상단 바만 두고 본문은 비워 둔다.
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.file_home_title),
                onNavigateUp = { onEvent(FileHomeScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.file_navigate_up_button_content_description),
            )
        },
    ) { _ -> }
}

@ScreenPreview
@Composable
private fun FileHomeScaffoldPreview() {
    DiaryTheme {
        FileHomeScaffold(onEvent = {})
    }
}
