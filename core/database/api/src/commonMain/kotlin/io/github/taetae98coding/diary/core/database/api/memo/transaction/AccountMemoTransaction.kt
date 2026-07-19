package io.github.taetae98coding.diary.core.database.api.memo.transaction

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountMemoTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        memoList: List<MemoLocalEntity>,
        memoTagList: List<MemoTagLocalEntity>,
        memoPlaceList: List<MemoPlaceLocalEntity> = emptyList(),
        memoWebList: List<MemoWebLocalEntity> = emptyList(),
    )

    public suspend fun updateFinished(
        accountId: Uuid,
        memoId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        accountId: Uuid,
        memoId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDetail(
        accountId: Uuid,
        memoId: Uuid,
        detail: MemoDetailLocalEntity,
        updatedAt: Instant,
    ): Int
}
