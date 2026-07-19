package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.transaction.AccountMemoTransaction
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoWebLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoTransactionImpl(
    private val database: DiaryDatabase,
) : AccountMemoTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        memoList: List<MemoLocalEntity>,
        memoTagList: List<MemoTagLocalEntity>,
        memoPlaceList: List<MemoPlaceLocalEntity>,
        memoWebList: List<MemoWebLocalEntity>,
    ) {
        database.withWriteTransaction {
            database.memoDao().upsert(memoList)
            database.accountMemoDao().upsert(
                memoList.map { memo ->
                    AccountMemoLocalEntity(
                        accountId = accountId,
                        memoId = memo.id,
                        isDirty = true,
                    )
                },
            )
            database.memoTagDao().upsert(memoTagList)
            database.accountMemoTagDao().upsert(
                memoTagList.map { memoTag ->
                    AccountMemoTagLocalEntity(
                        accountId = accountId,
                        memoId = memoTag.memoId,
                        tagId = memoTag.tagId,
                        isDirty = true,
                    )
                },
            )
            database.memoPlaceDao().upsert(memoPlaceList)
            database.accountMemoPlaceDao().upsert(
                memoPlaceList.map { memoPlace ->
                    AccountMemoPlaceLocalEntity(
                        accountId = accountId,
                        memoId = memoPlace.memoId,
                        placeId = memoPlace.placeId,
                        isDirty = true,
                    )
                },
            )
            database.memoWebDao().upsert(memoWebList)
            database.accountMemoWebDao().upsert(
                memoWebList.map { memoWeb ->
                    AccountMemoWebLocalEntity(
                        accountId = accountId,
                        memoId = memoWeb.memoId,
                        webId = memoWeb.webId,
                        isDirty = true,
                    )
                },
            )
        }
    }

    override suspend fun updateFinished(
        accountId: Uuid,
        memoId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            memoId = memoId,
        ) {
            database.accountMemoDao().updateFinished(
                accountId = accountId,
                memoId = memoId,
                isFinished = isFinished,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateDeleted(
        accountId: Uuid,
        memoId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            memoId = memoId,
        ) {
            database.accountMemoDao().updateDeleted(
                accountId = accountId,
                memoId = memoId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateDetail(
        accountId: Uuid,
        memoId: Uuid,
        detail: MemoDetailLocalEntity,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            memoId = memoId,
        ) {
            database.accountMemoDao().updateDetail(
                accountId = accountId,
                memoId = memoId,
                title = detail.title,
                description = detail.description,
                color = detail.color,
                isAllDay = detail.isAllDay,
                start = detail.start,
                endInclusive = detail.endInclusive,
                updatedAt = updatedAt,
            )
        }

    private suspend fun updateAndMarkPending(
        accountId: Uuid,
        memoId: Uuid,
        update: suspend () -> Int,
    ): Int =
        database.withWriteTransaction {
            val updatedCount = update()
            if (updatedCount > 0) {
                database.accountMemoDao().markPending(
                    accountId = accountId,
                    memoId = memoId,
                )
            }
            updatedCount
        }
}
