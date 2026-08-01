package io.github.taetae98coding.diary.data.memo.repository

import io.github.taetae98coding.diary.core.database.api.memoplace.datasource.AccountMemoPlaceLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memoplace.transaction.AccountMemoPlaceTransaction
import io.github.taetae98coding.diary.core.mapper.place.toDomain
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoPlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoPlaceRepositoryImpl(
    private val accountMemoPlaceLocalDataSource: AccountMemoPlaceLocalDataSource,
    private val accountMemoPlaceTransaction: AccountMemoPlaceTransaction,
) : AccountMemoPlaceRepository {
    override fun getPlaceList(
        account: Account,
        memoId: Uuid,
    ): Flow<List<Place>> =
        accountMemoPlaceLocalDataSource
            .getPlaceList(
                accountId = account.id,
                memoId = memoId,
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override suspend fun upsert(
        account: Account,
        memoId: Uuid,
        placeId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ) {
        accountMemoPlaceTransaction.upsert(
            accountId = account.id,
            memoId = memoId,
            placeId = placeId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
    }
}
