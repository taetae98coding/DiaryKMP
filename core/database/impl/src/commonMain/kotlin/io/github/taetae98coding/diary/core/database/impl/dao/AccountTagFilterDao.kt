package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.tagfilter.entity.TagFilterLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
internal interface AccountTagFilterDao : RoomDao<TagFilterLocalEntity> {
    @Query(
        """
        SELECT *
        FROM tag_filter
        WHERE account_id = :accountId
        """,
    )
    fun find(accountId: Uuid): Flow<TagFilterLocalEntity?>
}
