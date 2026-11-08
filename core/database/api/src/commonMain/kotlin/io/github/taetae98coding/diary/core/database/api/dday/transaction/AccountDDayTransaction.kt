package io.github.taetae98coding.diary.core.database.api.dday.transaction

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountDDayTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        dday: DDayLocalEntity,
        ddayTagList: List<EntityTagLocalEntity>,
    )

    public suspend fun updateDetail(
        accountId: Uuid,
        ddayId: Uuid,
        detail: DDayDetailLocalEntity,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        accountId: Uuid,
        ddayId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
