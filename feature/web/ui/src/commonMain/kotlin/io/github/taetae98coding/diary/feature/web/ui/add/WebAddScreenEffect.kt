package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.web.api.WebAddedResult
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormState
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebAddFormState
import io.github.taetae98coding.diary.feature.web.ui.web_add_succeeded_message
import io.github.taetae98coding.diary.feature.web.ui.web_add_title_blank_message
import io.github.taetae98coding.diary.feature.web.ui.web_add_url_blank_message
import io.github.taetae98coding.diary.feature.web.ui.web_header_name_blank_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WebAddScreenEffect(
    effect: Flow<WebAddEffect> = emptyFlow(),
    state: WebFormState = rememberWebAddFormState(),
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.web_add_succeeded_message)
    val titleBlankMessage = stringResource(Res.string.web_add_title_blank_message)
    val urlBlankMessage = stringResource(Res.string.web_add_url_blank_message)
    val headerNameBlankMessage = stringResource(Res.string.web_header_name_blank_message)

    CollectEffect(effect) { value ->
        when (value) {
            is WebAddEffect.AddSucceeded -> {
                resultEventBus.sendResult<WebAddedResult>(result = WebAddedResult(id = value.id))
                state.titleState.clearText()
                state.descriptionState.clearText()
                state.urlState.clearText()
                state.headerState.clear()
                state.titleState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = addSucceededMessage) }
            }

            is WebAddEffect.TitleBlank -> {
                state.titleState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = titleBlankMessage) }
            }

            is WebAddEffect.UrlBlank -> {
                state.urlState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = urlBlankMessage) }
            }

            is WebAddEffect.HeaderNameBlank -> {
                coroutineScope.launch { state.hostState.showImmediate(message = headerNameBlankMessage) }
            }
        }
    }
}
