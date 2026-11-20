package io.github.taetae98coding.diary.compose.dday

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.snackbar.DeleteUndoSnackbarEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
public fun DDayListUndoSnackbarEffect(
    onRestore: (Uuid) -> Unit,
    effect: Flow<DDayListEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    DeleteUndoSnackbarEffect(
        deletedMessage = stringResource(Res.string.dday_list_deleted_message),
        deletedId = { value ->
            when (value) {
                is DDayListEffect.Deleted -> value.id
            }
        },
        onRestore = onRestore,
        effect = effect,
        hostState = snackbarHostState,
    )
}
