package io.github.taetae98coding.diary.data.contact.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountTagContactLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.contact.repository.AccountTagContactRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagContactRepositoryImpl(
    private val accountTagContactLocalDataSource: AccountTagContactLocalDataSource,
) : AccountTagContactRepository {
    override fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
        sort: ListSort,
    ): Flow<PagingData<ContactListItem>> =
        pagingFlow(
            pagingSourceFactory = {
                accountTagContactLocalDataSource.page(
                    accountId = account.id,
                    tagId = tagId,
                    scope = scope.toLocal(),
                    sort = sort.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
