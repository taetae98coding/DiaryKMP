package io.github.taetae98coding.diary.core.database.api.memofilter.datasource

import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import kotlinx.coroutines.flow.Flow

public interface MemoExistenceFilterLocalDataSource {
    public fun find(): Flow<MemoExistenceFilterLocalEntity?>

    public suspend fun upsertHasDate(hasDate: Boolean?)

    public suspend fun upsertHasTag(hasTag: Boolean?)

    public suspend fun upsertHasPlace(hasPlace: Boolean?)
}
