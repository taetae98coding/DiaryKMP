package io.github.taetae98coding.diary.feature.dday.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_detail_update_succeeded_message
import io.github.taetae98coding.diary.feature.dday.ui.dday_repeat_blank_message
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormState
import io.github.taetae98coding.diary.feature.dday.ui.form.rememberDDayDetailFormState
import io.github.taetae98coding.diary.feature.dday.ui.form.showRepeatBlank
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DDayDetailScreenEffect(
    navigateUp: () -> Unit,
    effect: Flow<DDayDetailEffect> = emptyFlow(),
    state: DDayFormState = rememberDDayDetailFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.dday_detail_update_succeeded_message)
    val repeatBlankMessage = stringResource(Res.string.dday_repeat_blank_message)

    CollectEffect(effect) { value ->
        when (value) {
            is DDayDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { state.snackbarHostState.showImmediate(message = updateSucceededMessage) }
            }

            is DDayDetailEffect.RepeatBlank -> {
                coroutineScope.showRepeatBlank(state = state, message = repeatBlankMessage)
            }

            is DDayDetailEffect.DeleteSucceeded -> {
                navigateUp()
            }
        }
    }
}
