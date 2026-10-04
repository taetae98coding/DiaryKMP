package io.github.taetae98coding.diary.feature.core.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.core.UseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

public abstract class ListItemActionViewModel<Effect : Any>(
    private val finishUseCase: UseCase<Uuid, *>,
    private val restartUseCase: UseCase<Uuid, *>,
    private val deleteUseCase: UseCase<Uuid, *>,
    private val restoreUseCase: UseCase<Uuid, *>,
    private val finishedEffect: ((Uuid) -> Effect)? = null,
    private val restartedEffect: ((Uuid) -> Effect)? = null,
    private val deletedEffect: ((Uuid) -> Effect)? = null,
) : ViewModel() {
    private val effectChannel = Channel<Effect>(Channel.BUFFERED)
    public val effect: Flow<Effect> = effectChannel.receiveAsFlow()

    private val inProgressActionSet = mutableSetOf<Pair<ListItemAction, Uuid>>()

    public fun finish(id: Uuid) {
        launchAction(action = ListItemAction.FINISH, id = id, useCase = finishUseCase, successEffect = finishedEffect)
    }

    public fun restart(id: Uuid) {
        launchAction(action = ListItemAction.RESTART, id = id, useCase = restartUseCase, successEffect = restartedEffect)
    }

    public fun delete(id: Uuid) {
        launchAction(action = ListItemAction.DELETE, id = id, useCase = deleteUseCase, successEffect = deletedEffect)
    }

    public fun restore(id: Uuid) {
        launchAction(action = ListItemAction.RESTORE, id = id, useCase = restoreUseCase, successEffect = null)
    }

    private fun launchAction(
        action: ListItemAction,
        id: Uuid,
        useCase: UseCase<Uuid, *>,
        successEffect: ((Uuid) -> Effect)?,
    ) {
        val key = action to id
        if (!inProgressActionSet.add(key)) return

        viewModelScope.launch {
            try {
                useCase(parameter = id).onSuccess {
                    if (successEffect != null) effectChannel.send(successEffect(id))
                }
            } finally {
                inProgressActionSet.remove(key)
            }
        }
    }

    private enum class ListItemAction {
        FINISH,
        RESTART,
        DELETE,
        RESTORE,
    }
}
