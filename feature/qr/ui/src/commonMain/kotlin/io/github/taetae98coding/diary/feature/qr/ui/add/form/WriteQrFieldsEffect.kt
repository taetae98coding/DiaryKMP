package io.github.taetae98coding.diary.feature.qr.ui.add.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow

@Composable
internal fun WriteQrFieldsEffect(state: QrContentFormState) {
    LaunchedEffect(state) {
        snapshotFlow { state.content }
            .collect { state.writeFields() }
    }
}
