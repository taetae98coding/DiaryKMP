package io.github.taetae98coding.diary.feature.core.list

import io.github.taetae98coding.diary.domain.core.UseCase
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public class ListItemDeleteActions<Effect : Any>(
    private val deleteUseCase: UseCase<Uuid, *>,
    private val restoreUseCase: UseCase<Uuid, *>,
    private val deletedEffect: ((Uuid) -> Effect)? = null,
) {
    private val runner = ListItemActionRunner<Effect>()

    public val effect: Flow<Effect> = runner.effect

    public suspend fun delete(id: Uuid) {
        runner.run(action = ListItemAction.DELETE, id = id, useCase = deleteUseCase, successEffect = deletedEffect)
    }

    public suspend fun restore(id: Uuid) {
        runner.run(action = ListItemAction.RESTORE, id = id, useCase = restoreUseCase, successEffect = null)
    }
}
