package io.github.taetae98coding.diary.core.database.api.memoplace.transaction

import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import kotlin.uuid.Uuid

public interface AccountMemoPlaceSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        memoPlaceList: List<MemoPlaceLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        memoPlaceList: List<MemoPlaceLocalEntity>,
        cursor: Long,
    )
}
