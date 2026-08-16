package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.domain.contact.exception.ContactNameBlankException
import io.github.taetae98coding.diary.domain.contact.exception.ContactPhoneNumberBlankException
import io.github.taetae98coding.diary.domain.contact.usecase.AddContactUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ContactAddViewModel(
    private val addContactUseCase: AddContactUseCase,
) : ViewModel() {
    val uiState: StateFlow<ContactAddUiState>
        field = MutableStateFlow(ContactAddUiState())

    private val _effect = Channel<ContactAddEffect>(Channel.BUFFERED)
    val effect: Flow<ContactAddEffect> = _effect.receiveAsFlow()

    fun add(detail: ContactDetail) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { value -> value.copy(isInProgress = true) }
            try {
                addContactUseCase(parameter = AddContactUseCase.Parameter(detail = detail))
                    .onSuccess { _effect.send(ContactAddEffect.AddSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is ContactNameBlankException -> _effect.send(ContactAddEffect.NameBlank)
                            is ContactPhoneNumberBlankException -> _effect.send(ContactAddEffect.PhoneNumberBlank)
                        }
                    }
            } finally {
                uiState.update { value -> value.copy(isInProgress = false) }
            }
        }
    }
}
