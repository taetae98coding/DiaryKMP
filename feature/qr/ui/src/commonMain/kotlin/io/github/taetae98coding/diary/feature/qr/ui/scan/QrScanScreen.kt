package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier

@Composable
internal fun QrScanScreen(
    navigateUp: () -> Unit,
    navigateUpWithValue: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentNavigateUpWithValue by rememberUpdatedState(navigateUpWithValue)
    val reader = remember { QrScanReader(onRead = { value -> currentNavigateUpWithValue(value) }) }

    QrScanScaffold(
        onEvent = { event ->
            when (event) {
                is QrScanScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is QrScanScaffoldEvent.DetectQr -> {
                    reader.read(value = event.value)
                }
            }
        },
        modifier = modifier,
    )
}
