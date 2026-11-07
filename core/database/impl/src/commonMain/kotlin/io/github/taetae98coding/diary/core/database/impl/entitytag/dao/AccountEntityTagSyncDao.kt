package io.github.taetae98coding.diary.core.database.impl.entitytag.dao

import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal interface AccountEntityTagSyncDao<L, A> : RoomDao<A> {
    suspend fun findPending(accountId: Uuid): List<L>

    suspend fun clearPending(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ): Int
}
