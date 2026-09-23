package io.github.taetae98coding.diary.core.database.api.memocontact.transaction

import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import kotlin.uuid.Uuid

public interface AccountMemoContactSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        memoContactList: List<MemoContactLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        memoContactList: List<MemoContactLocalEntity>,
        cursor: Long,
    )
}
