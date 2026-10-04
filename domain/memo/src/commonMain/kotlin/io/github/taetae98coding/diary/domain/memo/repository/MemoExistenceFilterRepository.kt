package io.github.taetae98coding.diary.domain.memo.repository

import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import kotlinx.coroutines.flow.Flow

public interface MemoExistenceFilterRepository {
    public fun get(): Flow<MemoExistenceFilter>

    public suspend fun upsertDate(existence: MemoFilterExistence)

    public suspend fun upsertTag(existence: MemoFilterExistence)

    public suspend fun upsertPlace(existence: MemoFilterExistence)
}
