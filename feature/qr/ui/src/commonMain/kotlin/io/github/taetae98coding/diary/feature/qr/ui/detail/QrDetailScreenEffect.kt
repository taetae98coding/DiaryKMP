package io.github.taetae98coding.diary.feature.qr.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.domain.qr.content.QrFormat
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.add.QrAddTab
import io.github.taetae98coding.diary.feature.qr.ui.add.awaitTabComposed
import io.github.taetae98coding.diary.feature.qr.ui.add.emptyMessage
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.form.rememberQrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.form.requestFocusFirstField
import io.github.taetae98coding.diary.feature.qr.ui.qr_detail_update_succeeded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrDetailScreenEffect(
    navigateUp: () -> Unit,
    effect: Flow<QrDetailEffect> = emptyFlow(),
    state: QrAddFormState = rememberQrAddFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.qr_detail_update_succeeded_message)
    val emptyMessageMap = QrFormat.entries.associateWith { format -> stringResource(format.emptyMessage) }

    CollectEffect(effect) { value ->
        when (value) {
            is QrDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = updateSucceededMessage) }
            }

            is QrDetailEffect.ValueEmpty -> {
                val message = emptyMessageMap.getValue(state.contentState.format)

                state.tab = QrAddTab.QR
                awaitTabComposed()
                state.contentState.requestFocusFirstField()
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = message) }
            }

            is QrDetailEffect.DeleteSucceeded -> navigateUp()
        }
    }
}
