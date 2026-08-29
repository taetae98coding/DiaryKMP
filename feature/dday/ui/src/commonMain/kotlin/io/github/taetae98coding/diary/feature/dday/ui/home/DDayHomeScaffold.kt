package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun DDayHomeScaffold(
    onEvent: (DDayHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 본문에 둘 내용은 아직 정하지 않았다. 상단 바만 두고 본문은 비워 둔다.
    Scaffold(
        modifier = modifier,
        topBar = { DDayHomeTopBar(onEvent = onEvent) },
    ) { _ -> }
}

@ScreenPreview
@Composable
private fun DDayHomeScaffoldPreview() {
    DiaryTheme {
        DDayHomeScaffold(onEvent = {})
    }
}
