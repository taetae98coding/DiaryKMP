package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_home_title
import io.github.taetae98coding.diary.feature.qr.ui.qr_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrHomeScaffold(
    onEvent: (QrHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.qr_home_title),
                onNavigateUp = { onEvent(QrHomeScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.qr_navigate_up_button_content_description),
            )
        },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(QrHomeScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.qr_add_button_content_description),
            )
        },
    ) { _ -> }
}

@ScreenPreview
@Composable
private fun QrHomeScaffoldPreview() {
    DiaryTheme {
        QrHomeScaffold(onEvent = {})
    }
}
