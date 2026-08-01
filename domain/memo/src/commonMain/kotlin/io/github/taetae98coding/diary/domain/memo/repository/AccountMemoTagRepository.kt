package io.github.taetae98coding.diary.domain.memo.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountMemoTagRepository {
    public fun getTagList(
        account: Account,
        memoId: Uuid,
    ): Flow<List<Tag>>

    public fun pageSelectableTag(
        account: Account,
        memoId: Uuid,
        query: String,
    ): Flow<PagingData<Tag>>

    public suspend fun findTagIdSet(
        account: Account,
        memoId: Uuid,
    ): Set<Uuid>

    public suspend fun upsert(
        account: Account,
        memoId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )

    public suspend fun updatePrimaryTagId(
        account: Account,
        memoId: Uuid,
        primaryTagId: Uuid?,
        updatedAt: Instant,
    )
}
