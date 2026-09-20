package io.github.taetae98coding.diary.data.place.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.place.datasource.AccountPlaceLocalDataSource
import io.github.taetae98coding.diary.core.database.api.place.transaction.AccountPlaceTransaction
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.place.mapper.toDomain
import io.github.taetae98coding.diary.data.place.mapper.toLocal
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountPlaceRepositoryImpl(
    private val accountPlaceLocalDataSource: AccountPlaceLocalDataSource,
    private val accountPlaceTransaction: AccountPlaceTransaction,
) : AccountPlaceRepository {
    override fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<Place>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountPlaceLocalDataSource.page(
                    accountId = account.id,
                    query = query,
                    sort = sort.toLocal(),
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override fun get(
        account: Account,
        placeIdSet: Set<Uuid>,
    ): Flow<List<Place>> =
        accountPlaceLocalDataSource
            .get(accountId = account.id, placeIdSet = placeIdSet)
            .map { localList -> localList.map { local -> local.toDomain() } }

    override fun get(
        account: Account,
        bounds: CoordinateBounds,
        sort: ListSort,
    ): Flow<List<Place>> =
        accountPlaceLocalDataSource
            .get(
                accountId = account.id,
                south = bounds.south,
                north = bounds.north,
                west = bounds.west,
                east = bounds.east,
                sort = sort.toLocal(),
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override fun find(
        account: Account,
        placeId: Uuid,
    ): Flow<Place?> =
        accountPlaceLocalDataSource
            .find(accountId = account.id, placeId = placeId)
            .map { local -> local?.toDomain() }

    override suspend fun upsert(
        account: Account,
        place: Place,
        tagIdSet: Set<Uuid>,
    ) {
        accountPlaceTransaction.upsert(
            accountId = account.id,
            placeList = listOf(place.toLocal()),
            placeTagList =
                tagIdSet.map { tagId ->
                    PlaceTagLocalEntity(
                        placeId = place.id,
                        tagId = tagId,
                        isDeleted = false,
                        updatedAt = place.updatedAt,
                        createdAt = place.createdAt,
                    )
                },
        )
    }

    override suspend fun updateDetail(
        account: Account,
        placeId: Uuid,
        detail: PlaceDetail,
        updatedAt: Instant,
    ): Int =
        accountPlaceTransaction.updateDetail(
            accountId = account.id,
            placeId = placeId,
            detail = detail.toLocal(),
            updatedAt = updatedAt,
        )

    override suspend fun updateDeleted(
        account: Account,
        placeId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        accountPlaceTransaction.updateDeleted(
            accountId = account.id,
            placeId = placeId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )

    private companion object {
        const val PAGE_SIZE: Int = 20
    }
}
