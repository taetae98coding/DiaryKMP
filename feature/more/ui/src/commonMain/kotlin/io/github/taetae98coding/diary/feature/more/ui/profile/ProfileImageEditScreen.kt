package io.github.taetae98coding.diary.feature.more.ui.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.feature.more.ui.photo.PhotoPicker
import kotlinx.coroutines.launch

@Composable
internal fun ProfileImageEditScreen(
    navigateUp: () -> Unit,
    photoPicker: PhotoPicker,
    viewModel: ProfileImageEditViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val state = rememberProfileImageEditState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileImageEditScreenEffect(
        navigateUp = navigateUp,
        effect = viewModel.effect,
        snackbarHostState = snackbarHostState,
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
        snackbarHostState = snackbarHostState,
    )
}
