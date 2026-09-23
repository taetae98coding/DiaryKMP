package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.contact.api.ContactAddedResult
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_add_name_blank_message
import io.github.taetae98coding.diary.feature.contact.ui.contact_add_phone_number_blank_message
import io.github.taetae98coding.diary.feature.contact.ui.contact_add_succeeded_message
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactAddFormState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactAddScreenEffect(
    effect: Flow<ContactAddEffect> = emptyFlow(),
    state: ContactFormState = rememberContactAddFormState(),
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.contact_add_succeeded_message)
    val nameBlankMessage = stringResource(Res.string.contact_add_name_blank_message)
    val phoneNumberBlankMessage = stringResource(Res.string.contact_add_phone_number_blank_message)

    CollectEffect(effect) { value ->
        when (value) {
            is ContactAddEffect.AddSucceeded -> {
                resultEventBus.sendResult<ContactAddedResult>(result = ContactAddedResult(id = value.id))
                state.nameState.clearText()
                state.descriptionState.clearText()
                state.heightState.clearText()
                state.footSizeState.clearText()
                state.birthdayState.clear()
                state.hometownState.clearText()
                state.phoneNumberState.clear()
                state.nameState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = addSucceededMessage) }
            }

            is ContactAddEffect.NameBlank -> {
                state.nameState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = nameBlankMessage) }
            }

            is ContactAddEffect.PhoneNumberBlank -> {
                coroutineScope.launch { state.hostState.showImmediate(message = phoneNumberBlankMessage) }
            }
        }
    }
}
