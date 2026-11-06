package io.github.taetae98coding.diary.feature.core.list

import io.github.taetae98coding.diary.domain.core.UseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.uuid.Uuid

public class ListItemActions<Effect : Any>(
    private val finishUseCase: UseCase<Uuid, *>,
    private val restartUseCase: UseCase<Uuid, *>,
    private val deleteUseCase: UseCase<Uuid, *>,
    private val restoreUseCase: UseCase<Uuid, *>,
    private val finishedEffect: ((Uuid) -> Effect)? = null,
    private val restartedEffect: ((Uuid) -> Effect)? = null,
    private val deletedEffect: ((Uuid) -> Effect)? = null,
) {
    private val effectChannel = Channel<Effect>(Channel.BUFFERED)
    public val effect: Flow<Effect> = effectChannel.receiveAsFlow()

    private val inProgressActionSet = mutableSetOf<Pair<ListItemAction, Uuid>>()

    public suspend fun finish(id: Uuid) {
        runAction(action = ListItemAction.FINISH, id = id, useCase = finishUseCase, successEffect = finishedEffect)
    }

    public suspend fun restart(id: Uuid) {
        runAction(action = ListItemAction.RESTART, id = id, useCase = restartUseCase, successEffect = restartedEffect)
    }

    public suspend fun delete(id: Uuid) {
        runAction(action = ListItemAction.DELETE, id = id, useCase = deleteUseCase, successEffect = deletedEffect)
    }

    public suspend fun restore(id: Uuid) {
        runAction(action = ListItemAction.RESTORE, id = id, useCase = restoreUseCase, successEffect = null)
    }

    private suspend fun runAction(
        action: ListItemAction,
        id: Uuid,
        useCase: UseCase<Uuid, *>,
        successEffect: ((Uuid) -> Effect)?,
    ) {
        val key = action to id
        if (!inProgressActionSet.add(key)) return

        try {
            useCase(parameter = id).onSuccess {
                if (successEffect != null) effectChannel.send(successEffect(id))
            }
        } finally {
            inProgressActionSet.remove(key)
        }
    }

    private enum class ListItemAction {
        FINISH,
        RESTART,
        DELETE,
        RESTORE,
    }
}
