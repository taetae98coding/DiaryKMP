package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.domain.contact.exception.ContactPhoneNumberBlankException
import io.github.taetae98coding.diary.domain.contact.usecase.DeleteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.UpdateContactUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class ContactDetailViewModel(
    @InjectedParam private val id: Uuid,
    private val updateContactUseCase: UpdateContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    findContactUseCase: FindContactUseCase,
) : ViewModel() {
    private val isUpdateInProgress = MutableStateFlow(false)
    private val isDeleteInProgress = MutableStateFlow(false)

    val uiState: StateFlow<ContactDetailUiState> =
        combine(
            findContactUseCase(parameter = id),
            isUpdateInProgress,
            isDeleteInProgress,
        ) { result, isUpdateInProgress, isDeleteInProgress ->
            result.getOrNull()?.let { contact ->
                ContactDetailUiState.Content(
                    id = contact.id,
                    detail = contact.detail,
                    isUpdateInProgress = isUpdateInProgress,
                    isDeleteInProgress = isDeleteInProgress,
                )
            } ?: ContactDetailUiState.Loading
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = ContactDetailUiState.Loading,
        )

    private val _effect = Channel<ContactDetailEffect>(Channel.BUFFERED)
    val effect: Flow<ContactDetailEffect> = _effect.receiveAsFlow()

    fun update(detail: ContactDetail) {
        if (isUpdateInProgress.value) return

        viewModelScope.launch {
            isUpdateInProgress.value = true
            try {
                updateContactUseCase(parameter = UpdateContactUseCase.Parameter(id = id, detail = detail))
                    .onSuccess { _effect.send(ContactDetailEffect.UpdateSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is ContactPhoneNumberBlankException -> _effect.send(ContactDetailEffect.PhoneNumberBlank)
                        }
                    }
            } finally {
                isUpdateInProgress.value = false
            }
        }
    }

    fun delete() {
        if (isDeleteInProgress.value) return

        viewModelScope.launch {
            isDeleteInProgress.value = true
            try {
                deleteContactUseCase(parameter = id)
                    .onSuccess { _effect.send(ContactDetailEffect.DeleteSucceeded) }
            } finally {
                isDeleteInProgress.value = false
            }
        }
    }
}
