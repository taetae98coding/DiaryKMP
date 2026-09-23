package io.github.taetae98coding.diary.feature.more.ui.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.more.ui.Res
import io.github.taetae98coding.diary.feature.more.ui.more_profile_image_edit_failed_message
import io.github.taetae98coding.diary.feature.more.ui.photo.PhotoPicker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfileImageEditScreen(
    navigateUp: () -> Unit,
    photoPicker: PhotoPicker,
    viewModel: ProfileImageEditViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val state = rememberProfileImageEditState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChangeProfileImageEffect(
        navigateUp = navigateUp,
        effect = viewModel.effect,
        hostState = hostState,
    )

    ProfileImageEditScaffold(
        onEvent = { event ->
            when (event) {
                is ProfileImageEditScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is ProfileImageEditScaffoldEvent.ClickChoosePhoto -> {
                    coroutineScope.launch {
                        photoPicker.open()?.let(state::changePhoto)
                    }
                }

                is ProfileImageEditScaffoldEvent.ClickApply -> {
                    val uri = state.uri
                    val cropRegion = state.cropRegion()

                    if (uri != null && cropRegion != null) {
                        viewModel.changeProfileImage(uri = uri, cropRegion = cropRegion)
                    }
                }
            }
        },
        modifier = modifier,
        state = state,
        uiStateProvider = { uiState },
        hostState = hostState,
    )
}

@Composable
private fun ChangeProfileImageEffect(
    navigateUp: () -> Unit,
    effect: Flow<ProfileImageEditEffect> = emptyFlow(),
    hostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val failedMessage = stringResource(Res.string.more_profile_image_edit_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is ProfileImageEditEffect.ChangeSucceeded -> {
                navigateUp()
            }

            is ProfileImageEditEffect.ChangeFailed -> {
                hostState.showImmediate(message = failedMessage)
            }
        }
    }
}
