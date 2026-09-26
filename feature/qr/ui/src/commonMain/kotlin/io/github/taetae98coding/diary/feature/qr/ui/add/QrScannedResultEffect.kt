package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScannedResult

@Composable
internal fun QrScannedResultEffect(
    state: QrAddScaffoldState,
    resultEventBus: ResultEventBus,
) {
    ResultEffect<QrScannedResult>(resultEventBus = resultEventBus) { result ->
        state.valueState.setTextAndPlaceCursorAtEnd(result.value)
    }
}
