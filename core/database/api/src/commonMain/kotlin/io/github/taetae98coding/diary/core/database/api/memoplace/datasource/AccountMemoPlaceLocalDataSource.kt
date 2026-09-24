package io.github.taetae98coding.diary.core.database.api.memoplace.datasource

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountMemoPlaceLocalDataSource {
    public fun getPlaceList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<PlaceLocalEntity>>

    public suspend fun findPlaceIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid>
}
