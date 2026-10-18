package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.form.TagFormState
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_update_succeeded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailScreenEffect(
    navigateUp: () -> Unit,
    effect: Flow<TagDetailEffect>,
    scaffoldState: TagFormState,
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.tag_detail_update_succeeded_message)

    CollectEffect(effect) { value ->
        when (value) {
            is TagDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { scaffoldState.snackbarHostState.showImmediate(message = updateSucceededMessage) }
            }

            is TagDetailEffect.DeleteSucceeded -> navigateUp()
        }
    }
}
