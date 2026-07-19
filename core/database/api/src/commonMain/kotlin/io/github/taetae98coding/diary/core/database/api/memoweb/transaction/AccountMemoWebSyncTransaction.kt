package io.github.taetae98coding.diary.core.database.api.memoweb.transaction

import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import kotlin.uuid.Uuid

public interface AccountMemoWebSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        memoWebList: List<MemoWebLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        memoWebList: List<MemoWebLocalEntity>,
        cursor: Long,
    )
}
