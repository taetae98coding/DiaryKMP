package io.github.taetae98coding.diary.core.database.impl.calendarfilter.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.calendarfilter.entity.CalendarFilterTagLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
internal interface CalendarFilterTagDao : RoomDao<CalendarFilterTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN calendar_filter_tag
            ON calendar_filter_tag.tag_id = tag.id AND calendar_filter_tag.account_id = :accountId
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE tag.is_finished = 0 AND tag.is_deleted = 0
        ORDER BY tag.title ASC
        """,
    )
    fun getTagList(accountId: Uuid): Flow<List<TagLocalEntity>>

    @Query(
        """
        DELETE FROM calendar_filter_tag
        WHERE account_id = :accountId AND tag_id = :tagId
        """,
    )
    suspend fun delete(
        accountId: Uuid,
        tagId: Uuid,
    )

    @Query(
        """
        DELETE FROM calendar_filter_tag
        WHERE account_id = :accountId
        """,
    )
    suspend fun deleteAll(accountId: Uuid)
}
