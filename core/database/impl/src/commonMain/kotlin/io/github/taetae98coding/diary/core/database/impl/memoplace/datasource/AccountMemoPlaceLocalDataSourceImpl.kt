package io.github.taetae98coding.diary.core.database.impl.memoplace.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.memoplace.datasource.AccountMemoPlaceLocalDataSource
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountMemoPlaceLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoPlaceLocalDataSource {
    override fun getPlaceList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<PlaceLocalEntity>> =
        database.accountMemoPlaceDao().getPlaceList(
            accountId = accountId,
            memoId = memoId,
        )

    override suspend fun readPlaceIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid> =
        database.accountMemoPlaceDao().findPlaceIdList(
            accountId = accountId,
            memoId = memoId,
        )
}
