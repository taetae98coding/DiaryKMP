package io.github.taetae98coding.diary.data.checklist.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.checklist.datasource.AccountTagChecklistLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.data.checklist.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.checklist.repository.AccountTagChecklistRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagChecklistRepositoryImpl(
    private val accountTagChecklistLocalDataSource: AccountTagChecklistLocalDataSource,
) : AccountTagChecklistRepository {
    override fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
    ): Flow<PagingData<ChecklistSummary>> =
        pagingFlow(
            pagingSourceFactory = {
                accountTagChecklistLocalDataSource.page(
                    accountId = account.id,
                    tagId = tagId,
                    scope = scope.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
