package io.github.taetae98coding.diary.data.memo.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.memocontact.datasource.AccountMemoContactLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memocontact.transaction.AccountMemoContactTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.data.contact.mapper.toDomain
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoContactRepositoryImpl(
    private val accountMemoContactLocalDataSource: AccountMemoContactLocalDataSource,
    private val accountMemoContactTransaction: AccountMemoContactTransaction,
) : AccountMemoContactRepository {
    override fun getContactList(
        account: Account,
        memoId: Uuid,
    ): Flow<List<Contact>> =
        accountMemoContactLocalDataSource
            .getContactList(
                accountId = account.id,
                memoId = memoId,
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override fun pageSelectableContact(
        account: Account,
        query: String,
    ): Flow<PagingData<Contact>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountMemoContactLocalDataSource.pageSelectableContact(
                    accountId = account.id,
                    query = query,
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override suspend fun findContactIdSet(
        account: Account,
        memoId: Uuid,
    ): Set<Uuid> =
        accountMemoContactLocalDataSource
            .findContactIdList(
                accountId = account.id,
                memoId = memoId,
            ).toSet()

    override suspend fun upsert(
        account: Account,
        memoId: Uuid,
        contactId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        accountMemoContactTransaction.upsert(
            accountId = account.id,
            memoId = memoId,
            contactId = contactId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
    }

    private companion object {
        const val PAGE_SIZE: Int = 20
    }
}
