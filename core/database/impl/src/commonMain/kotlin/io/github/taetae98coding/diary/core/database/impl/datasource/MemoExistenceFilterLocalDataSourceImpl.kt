package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.memofilter.datasource.MemoExistenceFilterLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dao.MemoExistenceFilterDao
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
internal class MemoExistenceFilterLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : MemoExistenceFilterLocalDataSource {
    override fun find(): Flow<MemoExistenceFilterLocalEntity?> = database.memoExistenceFilterDao().find(id = MemoExistenceFilterLocalEntity.SINGLETON_ID)

    override suspend fun upsertHasDate(hasDate: Boolean?) {
        updateInTransaction { dao ->
            dao.updateHasDate(id = MemoExistenceFilterLocalEntity.SINGLETON_ID, hasDate = hasDate)
        }
    }

    override suspend fun upsertHasTag(hasTag: Boolean?) {
        updateInTransaction { dao ->
            dao.updateHasTag(id = MemoExistenceFilterLocalEntity.SINGLETON_ID, hasTag = hasTag)
        }
    }

    override suspend fun upsertHasPlace(hasPlace: Boolean?) {
        updateInTransaction { dao ->
            dao.updateHasPlace(id = MemoExistenceFilterLocalEntity.SINGLETON_ID, hasPlace = hasPlace)
        }
    }

    private suspend fun updateInTransaction(update: suspend (MemoExistenceFilterDao) -> Unit) {
        database.withWriteTransaction {
            val dao = database.memoExistenceFilterDao()

            dao.insertIgnore(
                entity =
                    MemoExistenceFilterLocalEntity(
                        hasDate = null,
                        hasTag = null,
                        hasPlace = null,
                    ),
            )
            update(dao)
        }
    }
}
