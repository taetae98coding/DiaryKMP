package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun QrScanScreen(
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QrScanScaffold(
        onEvent = { event ->
            when (event) {
                is QrScanScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }
            }
        },
        modifier = modifier,
    )
}
