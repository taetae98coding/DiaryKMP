package io.github.taetae98coding.diary.data.contact.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountContactLocalDataSource
import io.github.taetae98coding.diary.core.database.api.contact.transaction.AccountContactTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.data.contact.mapper.toDomain
import io.github.taetae98coding.diary.data.contact.mapper.toLocal
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.PAGE_SIZE
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountContactRepositoryImpl(
    private val accountContactLocalDataSource: AccountContactLocalDataSource,
    private val accountContactTransaction: AccountContactTransaction,
) : AccountContactRepository {
    override fun page(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Contact>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountContactLocalDataSource.page(
                    accountId = account.id,
                    sort = sort.toLocal(),
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override fun get(
        account: Account,
        contactIdSet: Set<Uuid>,
    ): Flow<List<Contact>> =
        accountContactLocalDataSource
            .get(
                accountId = account.id,
                contactIdSet = contactIdSet,
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override fun find(
        account: Account,
        contactId: Uuid,
    ): Flow<Contact?> =
        accountContactLocalDataSource
            .find(
                accountId = account.id,
                contactId = contactId,
            ).map { local -> local?.toDomain() }

    override suspend fun upsert(
        account: Account,
        contact: Contact,
    ) {
        accountContactTransaction.upsert(
            accountId = account.id,
            contactList = listOf(contact.toLocal()),
        )
    }

    override suspend fun updateDetail(
        account: Account,
        contactId: Uuid,
        detail: ContactDetail,
        updatedAt: Instant,
    ): Int =
        accountContactTransaction.updateDetail(
            accountId = account.id,
            contactId = contactId,
            detail = detail.toLocal(),
            updatedAt = updatedAt,
        )

    override suspend fun updateFavorite(
        account: Account,
        contactId: Uuid,
        isFavorite: Boolean,
        updatedAt: Instant,
    ): Int =
        accountContactTransaction.updateFavorite(
            accountId = account.id,
            contactId = contactId,
            isFavorite = isFavorite,
            updatedAt = updatedAt,
        )

    override suspend fun updateDeleted(
        account: Account,
        contactId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        accountContactTransaction.updateDeleted(
            accountId = account.id,
            contactId = contactId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
}
