package io.github.taetae98coding.diary.core.database.api.tag.transaction

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountTagTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        tagList: List<TagLocalEntity>,
        tagLinkList: List<TagLinkLocalEntity>,
    )

    public suspend fun updateFinished(
        accountId: Uuid,
        tagId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        accountId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDetail(
        accountId: Uuid,
        tagId: Uuid,
        detail: TagDetailLocalEntity,
        updatedAt: Instant,
    ): Int
}
