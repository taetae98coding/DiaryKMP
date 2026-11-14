package io.github.taetae98coding.diary.core.database.impl.routinefilter.datasource

import io.github.taetae98coding.diary.core.database.api.routinefilter.datasource.AccountRoutineFilterLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routinefilter.entity.RoutineViewScopeLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routinefilter.entity.RoutineFilterLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinefilter.entity.RoutineFilterTagLocalEntity
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountRoutineFilterLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountRoutineFilterLocalDataSource {
    override fun getTagList(accountId: Uuid): Flow<List<TagLocalEntity>> = database.routineFilterTagDao().getTagList(accountId = accountId)

    override fun getTagIdList(accountId: Uuid): Flow<List<Uuid>> = database.routineFilterTagDao().getTagIdList(accountId = accountId)

    override suspend fun upsert(
        accountId: Uuid,
        tagId: Uuid,
    ) {
        database.routineFilterTagDao().upsert(
            entity =
                RoutineFilterTagLocalEntity(
                    accountId = accountId,
                    tagId = tagId,
                ),
        )
    }

    override suspend fun delete(
        accountId: Uuid,
        tagId: Uuid,
    ) {
        database.routineFilterTagDao().delete(
            accountId = accountId,
            tagId = tagId,
        )
    }

    override suspend fun deleteAll(accountId: Uuid) {
        database.routineFilterTagDao().deleteAll(accountId = accountId)
    }

    override fun findViewScope(accountId: Uuid): Flow<RoutineViewScopeLocalEntity?> = database.routineFilterDao().findViewScope(accountId = accountId)

    override suspend fun upsertViewScope(
        accountId: Uuid,
        viewScope: RoutineViewScopeLocalEntity,
    ) {
        database.routineFilterDao().upsert(
            entity =
                RoutineFilterLocalEntity(
                    accountId = accountId,
                    viewScope = viewScope,
                ),
        )
    }
}
