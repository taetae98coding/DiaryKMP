package io.github.taetae98coding.diary.feature.core.list

import io.github.taetae98coding.diary.domain.core.UseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.uuid.Uuid

internal class ListItemActionRunner<Effect : Any> {
    private val effectChannel = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = effectChannel.receiveAsFlow()

    private val inProgressActionSet = mutableSetOf<Pair<ListItemAction, Uuid>>()

    suspend fun run(
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
}

internal enum class ListItemAction {
    FINISH,
    RESTART,
    DELETE,
    RESTORE,
}
