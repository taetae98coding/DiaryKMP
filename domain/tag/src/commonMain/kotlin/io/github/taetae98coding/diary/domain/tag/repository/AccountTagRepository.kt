package io.github.taetae98coding.diary.domain.tag.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountTagRepository {
    public fun get(
        account: Account,
        tagIdSet: Set<Uuid>,
    ): Flow<List<Tag>>

    public fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<Tag>>

    public fun pageTopLevel(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Tag>>

    public fun pageFinished(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Tag>>

    public fun find(
        account: Account,
        tagId: Uuid,
    ): Flow<Tag?>

    public suspend fun upsert(
        account: Account,
        tag: Tag,
        linkedTagIdSet: Set<Uuid>,
    )

    public suspend fun updateFinished(
        account: Account,
        tagId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        account: Account,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDetail(
        account: Account,
        tagId: Uuid,
        detail: TagDetail,
        updatedAt: Instant,
    ): Int
}
