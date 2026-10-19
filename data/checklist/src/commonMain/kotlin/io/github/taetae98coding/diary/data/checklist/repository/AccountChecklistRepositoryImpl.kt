package io.github.taetae98coding.diary.data.checklist.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.checklist.datasource.AccountChecklistLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklist.transaction.AccountChecklistTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.data.checklist.mapper.toDomain
import io.github.taetae98coding.diary.data.checklist.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountChecklistRepositoryImpl(
    private val accountChecklistLocalDataSource: AccountChecklistLocalDataSource,
    private val accountChecklistTransaction: AccountChecklistTransaction,
) : AccountChecklistRepository {
    override fun page(account: Account): Flow<PagingData<ChecklistSummary>> =
        pagingFlow(
            pagingSourceFactory = { accountChecklistLocalDataSource.page(accountId = account.id) },
            transform = { local -> local.toDomain() },
        )

    override fun find(
        account: Account,
        checklistId: Uuid,
    ): Flow<Checklist?> =
        accountChecklistLocalDataSource
            .find(
                accountId = account.id,
                checklistId = checklistId,
            ).map { local -> local?.toDomain() }

    override suspend fun upsert(
        account: Account,
        checklist: Checklist,
        itemList: List<ChecklistItem>,
    ) {
        accountChecklistTransaction.upsert(
            accountId = account.id,
            checklist = checklist.toLocal(),
            itemList = itemList.map { item -> item.toLocal() },
        )
    }

    override suspend fun updateDetail(
        account: Account,
        checklistId: Uuid,
        detail: ChecklistDetail,
        updatedAt: Instant,
    ): Int =
        accountChecklistTransaction.updateDetail(
            accountId = account.id,
            checklistId = checklistId,
            detail = detail.toLocal(),
            updatedAt = updatedAt,
        )

    override suspend fun updateDeleted(
        account: Account,
        checklistId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        accountChecklistTransaction.updateDeleted(
            accountId = account.id,
            checklistId = checklistId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
}
