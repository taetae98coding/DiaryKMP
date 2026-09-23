package io.github.taetae98coding.diary.data.place.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.place.datasource.AccountTagPlaceLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.PAGE_SIZE
import io.github.taetae98coding.diary.data.place.mapper.toDomain
import io.github.taetae98coding.diary.data.tag.mapper.toLocal
import io.github.taetae98coding.diary.domain.place.repository.AccountTagPlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagPlaceRepositoryImpl(
    private val accountTagPlaceLocalDataSource: AccountTagPlaceLocalDataSource,
) : AccountTagPlaceRepository {
    override fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
        sort: ListSort,
    ): Flow<PagingData<Place>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountTagPlaceLocalDataSource.page(
                    accountId = account.id,
                    tagId = tagId,
                    scope = scope.toLocal(),
                    sort = sort.toLocal(),
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override fun get(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
        bounds: CoordinateBounds,
        sort: ListSort,
    ): Flow<List<Place>> =
        accountTagPlaceLocalDataSource
            .get(
                accountId = account.id,
                tagId = tagId,
                scope = scope.toLocal(),
                south = bounds.south,
                north = bounds.north,
                west = bounds.west,
                east = bounds.east,
                sort = sort.toLocal(),
            ).map { localList -> localList.map { local -> local.toDomain() } }
}
