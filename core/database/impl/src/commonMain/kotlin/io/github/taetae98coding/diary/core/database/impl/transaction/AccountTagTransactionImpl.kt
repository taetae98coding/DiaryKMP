package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.transaction.AccountTagTransaction
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountTagTransactionImpl(
    private val database: DiaryDatabase,
) : AccountTagTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        tagList: List<TagLocalEntity>,
        tagLinkList: List<TagLinkLocalEntity>,
    ) {
        database.withWriteTransaction {
            database.tagDao().upsert(tagList)
            database.accountTagDao().upsert(
                tagList.map { tag ->
                    AccountTagLocalEntity(
                        accountId = accountId,
                        tagId = tag.id,
                        isDirty = true,
                    )
                },
            )
            database.tagLinkDao().upsert(tagLinkList)
            database.accountTagLinkDao().upsert(
                tagLinkList.map { tagLink ->
                    AccountTagLinkLocalEntity(
                        accountId = accountId,
                        fromTagId = tagLink.fromTagId,
                        toTagId = tagLink.toTagId,
                        isDirty = true,
                    )
                },
            )
        }
    }

    override suspend fun updateFinished(
        accountId: Uuid,
        tagId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            tagId = tagId,
        ) {
            database.accountTagDao().updateFinished(
                accountId = accountId,
                tagId = tagId,
                isFinished = isFinished,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateDeleted(
        accountId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            tagId = tagId,
        ) {
            database.accountTagDao().updateDeleted(
                accountId = accountId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateDetail(
        accountId: Uuid,
        tagId: Uuid,
        detail: TagDetailLocalEntity,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            tagId = tagId,
        ) {
            database.accountTagDao().updateDetail(
                accountId = accountId,
                tagId = tagId,
                emoji = detail.emoji,
                title = detail.title,
                description = detail.description,
                color = detail.color,
                updatedAt = updatedAt,
            )
        }

    private suspend fun updateAndMarkPending(
        accountId: Uuid,
        tagId: Uuid,
        update: suspend () -> Int,
    ): Int =
        database.withWriteTransaction {
            val updatedCount = update()
            if (updatedCount > 0) {
                database.accountTagDao().markPending(
                    accountId = accountId,
                    tagId = tagId,
                )
            }
            updatedCount
        }
}
