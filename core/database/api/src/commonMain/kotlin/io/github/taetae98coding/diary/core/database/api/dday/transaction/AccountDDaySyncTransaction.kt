package io.github.taetae98coding.diary.core.database.api.dday.transaction

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import kotlin.uuid.Uuid

public interface AccountDDaySyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        ddayList: List<DDayLocalEntity>,
    )

    public suspend fun upsert(
        accountId: Uuid,
        ddayList: List<DDayLocalEntity>,
        cursor: Long,
    )
}
