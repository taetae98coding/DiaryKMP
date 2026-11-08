package io.github.taetae98coding.diary.data.dday.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.dday.datasource.AccountTagDDayLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.dday.repository.AccountTagDDayRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagDDayRepositoryImpl(
    private val accountTagDDayLocalDataSource: AccountTagDDayLocalDataSource,
) : AccountTagDDayRepository {
    override fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
    ): Flow<PagingData<DDay>> =
        pagingFlow(
            pagingSourceFactory = {
                accountTagDDayLocalDataSource.page(
                    accountId = account.id,
                    tagId = tagId,
                    scope = scope.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
