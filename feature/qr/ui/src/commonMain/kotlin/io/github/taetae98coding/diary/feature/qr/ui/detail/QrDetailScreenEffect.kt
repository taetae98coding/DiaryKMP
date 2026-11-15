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
import kotlinx.coroutines.CoroutineScope
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
    val emptyMessageMap = qrValueEmptyMessageMap()

    CollectEffect(effect) { value ->
        when (value) {
            is QrDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = updateSucceededMessage) }
            }

            is QrDetailEffect.ValueEmpty -> {
                state.showValueEmpty(emptyMessageMap = emptyMessageMap, coroutineScope = coroutineScope)
            }

            is QrDetailEffect.DeleteSucceeded -> navigateUp()
        }
    }
}

@Composable
internal fun qrValueEmptyMessageMap(): Map<QrFormat, String> = QrFormat.entries.associateWith { format -> stringResource(format.emptyMessage) }

internal suspend fun QrAddFormState.showValueEmpty(
    emptyMessageMap: Map<QrFormat, String>,
    coroutineScope: CoroutineScope,
) {
    val message = emptyMessageMap.getValue(contentState.format)

    tab = QrAddTab.QR
    awaitTabComposed()
    contentState.requestFocusFirstField()
    coroutineScope.launch { snackbarHostState.showImmediate(message = message) }
}
