package io.github.taetae98coding.diary.domain.routine.repository

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountRoutineFilterRepository {
    public fun getTagList(account: Account): Flow<List<Tag>>

    public fun getTagIdSet(account: Account): Flow<Set<Uuid>>

    public suspend fun upsert(
        account: Account,
        tagId: Uuid,
    )

    public suspend fun delete(
        account: Account,
        tagId: Uuid,
    )

    public suspend fun deleteAll(account: Account)

    public fun getViewScope(account: Account): Flow<RoutineViewScope>

    public suspend fun upsertViewScope(
        account: Account,
        viewScope: RoutineViewScope,
    )
}
