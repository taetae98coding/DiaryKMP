package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_succeeded_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_title_blank_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_value_empty_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrAddScreenEffect(
    effect: Flow<QrAddEffect> = emptyFlow(),
    state: QrAddFormState = rememberQrAddFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.qr_add_succeeded_message)
    val titleBlankMessage = stringResource(Res.string.qr_add_title_blank_message)
    val valueEmptyMessage = stringResource(Res.string.qr_add_value_empty_message)

    CollectEffect(effect) { value ->
        when (value) {
            is QrAddEffect.AddSucceeded -> {
                state.clearText()
                state.titleState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = addSucceededMessage) }
            }

            is QrAddEffect.TitleBlank -> {
                state.titleState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = titleBlankMessage) }
            }

            is QrAddEffect.ValueEmpty -> {
                state.valueState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = valueEmptyMessage) }
            }
        }
    }
}
