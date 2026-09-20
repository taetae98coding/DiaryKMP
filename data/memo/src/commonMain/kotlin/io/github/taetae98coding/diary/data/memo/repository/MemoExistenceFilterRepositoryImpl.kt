package io.github.taetae98coding.diary.data.memo.repository

import io.github.taetae98coding.diary.core.database.api.memofilter.datasource.MemoExistenceFilterLocalDataSource
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.data.memo.mapper.toDomain
import io.github.taetae98coding.diary.data.memo.mapper.toLocal
import io.github.taetae98coding.diary.domain.memo.repository.MemoExistenceFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class MemoExistenceFilterRepositoryImpl(
    private val memoExistenceFilterLocalDataSource: MemoExistenceFilterLocalDataSource,
) : MemoExistenceFilterRepository {
    override fun get(): Flow<MemoExistenceFilter> =
        memoExistenceFilterLocalDataSource
            .find()
            .map { entity -> entity?.toDomain() ?: MemoExistenceFilter() }

    override suspend fun updateDate(existence: MemoFilterExistence) {
        memoExistenceFilterLocalDataSource.upsertHasDate(hasDate = existence.toLocal())
    }

    override suspend fun updateTag(existence: MemoFilterExistence) {
        memoExistenceFilterLocalDataSource.upsertHasTag(hasTag = existence.toLocal())
    }

    override suspend fun updatePlace(existence: MemoFilterExistence) {
        memoExistenceFilterLocalDataSource.upsertHasPlace(hasPlace = existence.toLocal())
    }
}
