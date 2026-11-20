package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.input.showDiaryFormAdded
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.tag.api.TagAddedResult
import io.github.taetae98coding.diary.feature.tag.api.tagAddedResultKey
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.form.TagFormState
import io.github.taetae98coding.diary.feature.tag.ui.tag_add_succeeded_message
import io.github.taetae98coding.diary.feature.tag.ui.tag_add_title_blank_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun TagAddScreenEffect(
    clearLink: () -> Unit,
    addedResultRequestKey: Uuid?,
    effect: Flow<TagAddEffect>,
    scaffoldState: TagFormState,
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.tag_add_succeeded_message)
    val titleBlankMessage = stringResource(Res.string.tag_add_title_blank_message)

    CollectEffect(effect) { value ->
        when (value) {
            is TagAddEffect.AddSucceeded -> {
                addedResultRequestKey?.let { requestKey ->
                    resultEventBus.sendResult(resultKey = tagAddedResultKey(requestKey = requestKey), result = TagAddedResult(id = value.id))
                }
                scaffoldState.emojiState.clearText()
                scaffoldState.titleState.clearText()
                scaffoldState.descriptionState.clearText()
                clearLink()
                scaffoldState.titleState.requestFocus()
                coroutineScope.launch {
                    showDiaryFormAdded(
                        colorState = scaffoldState.colorState,
                        snackbarHostState = scaffoldState.snackbarHostState,
                        message = addSucceededMessage,
                    )
                }
            }

            is TagAddEffect.TitleBlank -> {
                scaffoldState.titleState.requestFocus()
                coroutineScope.launch { scaffoldState.snackbarHostState.showImmediate(message = titleBlankMessage) }
            }
        }
    }
}
