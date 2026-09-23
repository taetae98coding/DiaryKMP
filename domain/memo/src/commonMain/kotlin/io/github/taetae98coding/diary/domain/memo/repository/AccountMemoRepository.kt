package io.github.taetae98coding.diary.domain.memo.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountMemoRepository {
    public fun page(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Memo>>

    public fun pageFinished(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Memo>>

    public fun find(
        account: Account,
        memoId: Uuid,
    ): Flow<Memo?>

    public suspend fun upsert(
        account: Account,
        memo: Memo,
        tagIdSet: Set<Uuid>,
        placeIdSet: Set<Uuid> = emptySet(),
        webIdSet: Set<Uuid> = emptySet(),
        contactIdSet: Set<Uuid> = emptySet(),
    )

    public suspend fun updateFinished(
        account: Account,
        memoId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        account: Account,
        memoId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDetail(
        account: Account,
        memoId: Uuid,
        detail: MemoDetail,
        updatedAt: Instant,
    ): Int
}
