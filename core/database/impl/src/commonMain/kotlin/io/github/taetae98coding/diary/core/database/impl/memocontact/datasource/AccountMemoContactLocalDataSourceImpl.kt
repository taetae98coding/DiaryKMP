package io.github.taetae98coding.diary.core.database.impl.memocontact.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactListItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.datasource.AccountMemoContactLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountMemoContactLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoContactLocalDataSource {
    override fun getContactList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<ContactListItemLocalEntity>> =
        database.accountMemoContactDao().getContactList(
            accountId = accountId,
            memoId = memoId,
        )

    override fun pageSelectableContact(
        accountId: Uuid,
        query: String,
    ): PagingSource<Int, ContactListItemLocalEntity> =
        database.accountMemoContactDao().pageSelectableContact(
            accountId = accountId,
            query = query,
        )

    override suspend fun readContactIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid> =
        database.accountMemoContactDao().findContactIdList(
            accountId = accountId,
            memoId = memoId,
        )
}
