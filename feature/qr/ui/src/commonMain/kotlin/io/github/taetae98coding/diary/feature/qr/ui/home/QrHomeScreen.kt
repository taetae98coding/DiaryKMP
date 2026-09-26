package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun QrHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QrHomeScaffold(
        onEvent = { event ->
            when (event) {
                is QrHomeScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is QrHomeScaffoldEvent.ClickAdd -> {
                    navigateToAdd()
                }
            }
        },
        modifier = modifier,
    )
}
