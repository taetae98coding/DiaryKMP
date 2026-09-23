package io.github.taetae98coding.diary.core.database.impl.memoplace.datasource

import io.github.taetae98coding.diary.core.database.api.memoplace.datasource.AccountMemoPlaceLocalDataSource
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
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
}
