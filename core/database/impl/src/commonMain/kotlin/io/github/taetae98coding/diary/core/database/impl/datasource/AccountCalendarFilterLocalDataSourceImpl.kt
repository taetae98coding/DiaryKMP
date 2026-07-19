package io.github.taetae98coding.diary.core.database.impl.datasource

import io.github.taetae98coding.diary.core.database.api.calendarfilter.datasource.AccountCalendarFilterLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.CalendarFilterTagLocalEntity
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountCalendarFilterLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountCalendarFilterLocalDataSource {
    override fun getTagList(accountId: Uuid): Flow<List<TagLocalEntity>> = database.calendarFilterTagDao().getTagList(accountId = accountId)

    override suspend fun upsert(
        accountId: Uuid,
        tagId: Uuid,
    ) {
        database.calendarFilterTagDao().upsert(
            entity =
                CalendarFilterTagLocalEntity(
                    accountId = accountId,
                    tagId = tagId,
                ),
        )
    }

    override suspend fun delete(
        accountId: Uuid,
        tagId: Uuid,
    ) {
        database.calendarFilterTagDao().delete(
            accountId = accountId,
            tagId = tagId,
        )
    }

    override suspend fun deleteAll(accountId: Uuid) {
        database.calendarFilterTagDao().deleteAll(accountId = accountId)
    }
}
