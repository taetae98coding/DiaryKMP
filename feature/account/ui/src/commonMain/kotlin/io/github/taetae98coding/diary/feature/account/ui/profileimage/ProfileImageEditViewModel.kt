package io.github.taetae98coding.diary.feature.account.ui.profileimage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.domain.account.usecase.ChangeProfileImageUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class ProfileImageEditViewModel(
    private val changeProfileImageUseCase: ChangeProfileImageUseCase,
) : ViewModel() {
    val uiState: StateFlow<ProfileImageEditUiState>
        field = MutableStateFlow(ProfileImageEditUiState())

    private val _effect = Channel<ProfileImageEditEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileImageEditEffect> = _effect.receiveAsFlow()

    fun changeProfileImage(
        uri: FileUri,
        cropRegion: ImageCropRegion,
    ) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { state -> state.copy(isInProgress = true) }
            try {
                changeProfileImageUseCase(parameter = ChangeProfileImageUseCase.Parameter(uri = uri, cropRegion = cropRegion))
                    .onSuccess { _effect.send(ProfileImageEditEffect.ChangeSucceeded) }
                    .onFailure { _effect.send(ProfileImageEditEffect.ChangeFailed) }
            } finally {
                uiState.update { state -> state.copy(isInProgress = false) }
            }
        }
    }
}
