package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.input.showDiaryFormAdded
import io.github.taetae98coding.diary.compose.core.input.showDiaryFormTitleBlank
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoAddFormState
import io.github.taetae98coding.diary.feature.memo.ui.memo_add_succeeded_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_add_title_blank_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoAddScreenEffect(
    effect: Flow<MemoAddEffect> = emptyFlow(),
    scaffoldState: MemoFormState = rememberMemoAddFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.memo_add_succeeded_message)
    val titleBlankMessage = stringResource(Res.string.memo_add_title_blank_message)

    CollectEffect(effect) { value ->
        when (value) {
            is MemoAddEffect.AddSucceeded -> {
                scaffoldState.titleState.clearText()
                coroutineScope.launch { scaffoldState.descriptionState.reset() }
                coroutineScope.showDiaryFormAdded(
                    titleState = scaffoldState.titleState,
                    colorState = scaffoldState.colorState,
                    snackbarHostState = scaffoldState.snackbarHostState,
                    message = addSucceededMessage,
                )
            }

            is MemoAddEffect.TitleBlank -> {
                coroutineScope.showDiaryFormTitleBlank(
                    titleState = scaffoldState.titleState,
                    snackbarHostState = scaffoldState.snackbarHostState,
                    message = titleBlankMessage,
                )
            }
        }
    }
}
