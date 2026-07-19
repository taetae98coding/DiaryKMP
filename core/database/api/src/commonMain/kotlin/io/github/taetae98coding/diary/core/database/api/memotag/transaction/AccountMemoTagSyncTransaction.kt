package io.github.taetae98coding.diary.core.database.api.memotag.transaction

import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import kotlin.uuid.Uuid

public interface AccountMemoTagSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        memoTagList: List<MemoTagLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        memoTagList: List<MemoTagLocalEntity>,
        cursor: Long,
    )
}
