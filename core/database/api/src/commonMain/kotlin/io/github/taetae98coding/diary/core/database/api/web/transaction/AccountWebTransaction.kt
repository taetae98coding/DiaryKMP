package io.github.taetae98coding.diary.core.database.api.web.transaction

import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountWebTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        webList: List<WebLocalEntity>,
        webTagList: List<WebTagLocalEntity>,
    )

    public suspend fun updateDetail(
        accountId: Uuid,
        webId: Uuid,
        detail: WebDetailLocalEntity,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        accountId: Uuid,
        webId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
