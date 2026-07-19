package io.github.taetae98coding.diary.core.database.api.memo.transaction

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import kotlin.uuid.Uuid

public interface AccountMemoSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        memoList: List<MemoLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        memoList: List<MemoLocalEntity>,
        cursor: Long,
    )
}
