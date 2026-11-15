package io.github.taetae98coding.diary.core.database.impl.routinefilter.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinefilter.entity.RoutineFilterTagLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
internal interface RoutineFilterTagDao : RoomDao<RoutineFilterTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN routine_filter_tag
            ON routine_filter_tag.tag_id = tag.id AND routine_filter_tag.account_id = :accountId
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE tag.is_finished = 0 AND tag.is_deleted = 0
        ORDER BY tag.title ASC
        """,
    )
    fun getTagList(accountId: Uuid): Flow<List<TagLocalEntity>>

    @Query(
        """
        SELECT tag_id
        FROM routine_filter_tag
        WHERE account_id = :accountId
        """,
    )
    fun getTagIdList(accountId: Uuid): Flow<List<Uuid>>

    @Query(
        """
        DELETE FROM routine_filter_tag
        WHERE account_id = :accountId AND tag_id = :tagId
        """,
    )
    suspend fun delete(
        accountId: Uuid,
        tagId: Uuid,
    )

    @Query(
        """
        DELETE FROM routine_filter_tag
        WHERE account_id = :accountId
        """,
    )
    suspend fun deleteAll(accountId: Uuid)
}
