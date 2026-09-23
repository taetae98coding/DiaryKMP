package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_home_title
import io.github.taetae98coding.diary.feature.dday.ui.dday_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DDayHomeScaffold(
    onEvent: (DDayHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.dday_home_title),
                onNavigateUp = { onEvent(DDayHomeScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.dday_navigate_up_button_content_description),
            )
        },
    ) { _ -> }
}

@ScreenPreview
@Composable
private fun DDayHomeScaffoldPreview() {
    DiaryTheme {
        DDayHomeScaffold(onEvent = {})
    }
}
