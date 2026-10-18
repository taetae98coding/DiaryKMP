package io.github.taetae98coding.diary.feature.more.ui.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.more.ui.Res
import io.github.taetae98coding.diary.feature.more.ui.more_profile_image_edit_failed_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfileImageEditScreenEffect(
    navigateUp: () -> Unit,
    snackbarHostState: SnackbarHostState,
    effect: Flow<ProfileImageEditEffect> = emptyFlow(),
) {
    val failedMessage = stringResource(Res.string.more_profile_image_edit_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is ProfileImageEditEffect.ChangeSucceeded -> {
                navigateUp()
            }

            is ProfileImageEditEffect.ChangeFailed -> {
                snackbarHostState.showImmediate(message = failedMessage)
            }
        }
    }
}
