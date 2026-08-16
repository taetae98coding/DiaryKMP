package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_add_phone_number_blank_message
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_update_succeeded_message
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactDetailScreenEffect(
    navigateUp: () -> Unit,
    effect: Flow<ContactDetailEffect> = emptyFlow(),
    state: ContactFormState = rememberContactDetailFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.contact_detail_update_succeeded_message)
    val phoneNumberBlankMessage = stringResource(Res.string.contact_add_phone_number_blank_message)

    CollectEffect(effect) { value ->
        when (value) {
            is ContactDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { state.hostState.showImmediate(message = updateSucceededMessage) }
            }

            is ContactDetailEffect.PhoneNumberBlank -> {
                coroutineScope.launch { state.hostState.showImmediate(message = phoneNumberBlankMessage) }
            }

            is ContactDetailEffect.DeleteSucceeded -> navigateUp()
        }
    }
}
