package io.github.taetae98coding.diary.feature.dday.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.input.showDiaryFormAdded
import io.github.taetae98coding.diary.compose.core.input.showDiaryFormTitleBlank
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_add_succeeded_message
import io.github.taetae98coding.diary.feature.dday.ui.dday_add_title_blank_message
import io.github.taetae98coding.diary.feature.dday.ui.dday_repeat_blank_message
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormState
import io.github.taetae98coding.diary.feature.dday.ui.form.rememberDDayAddFormState
import io.github.taetae98coding.diary.feature.dday.ui.form.showRepeatBlank
import io.github.taetae98coding.diary.feature.dday.ui.form.today
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DDayAddScreenEffect(
    effect: Flow<DDayAddEffect> = emptyFlow(),
    state: DDayFormState = rememberDDayAddFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.dday_add_succeeded_message)
    val titleBlankMessage = stringResource(Res.string.dday_add_title_blank_message)
    val repeatBlankMessage = stringResource(Res.string.dday_repeat_blank_message)

    CollectEffect(effect) { value ->
        when (value) {
            is DDayAddEffect.AddSucceeded -> {
                state.titleState.clearText()
                state.ruleState.reset(baseDate = today())
                coroutineScope.launch { state.descriptionState.reset() }
                state.scrollState.scrollTo(0)
                coroutineScope.showDiaryFormAdded(
                    titleState = state.titleState,
                    colorState = state.colorState,
                    snackbarHostState = state.snackbarHostState,
                    message = addSucceededMessage,
                )
            }

            is DDayAddEffect.TitleBlank -> {
                state.scrollState.scrollTo(0)
                coroutineScope.showDiaryFormTitleBlank(
                    titleState = state.titleState,
                    snackbarHostState = state.snackbarHostState,
                    message = titleBlankMessage,
                )
            }

            is DDayAddEffect.RepeatBlank -> {
                coroutineScope.showRepeatBlank(state = state, message = repeatBlankMessage)
            }
        }
    }
}
