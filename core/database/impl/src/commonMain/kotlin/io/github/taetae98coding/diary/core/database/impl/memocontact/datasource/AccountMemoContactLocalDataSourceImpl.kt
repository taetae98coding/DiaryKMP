package io.github.taetae98coding.diary.core.database.impl.memocontact.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.datasource.AccountMemoContactLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoContactLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoContactLocalDataSource {
    override fun getContactList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<ContactLocalEntity>> =
        database.accountMemoContactDao().getContactList(
            accountId = accountId,
            memoId = memoId,
        )

    override fun pageSelectableContact(
        accountId: Uuid,
        query: String,
    ): PagingSource<Int, ContactLocalEntity> =
        database.accountMemoContactDao().pageSelectableContact(
            accountId = accountId,
            query = query,
        )

    override suspend fun findContactIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid> =
        database.accountMemoContactDao().findContactIdList(
            accountId = accountId,
            memoId = memoId,
        )
}
