package io.github.taetae98coding.diary.core.database.impl.routinefilter.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.routinefilter.entity.RoutineViewScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinefilter.entity.RoutineFilterLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
internal interface RoutineFilterDao : RoomDao<RoutineFilterLocalEntity> {
    @Query(
        """
        SELECT view_scope
        FROM routine_filter
        WHERE account_id = :accountId
        """,
    )
    fun findViewScope(accountId: Uuid): Flow<RoutineViewScopeLocalEntity?>
}
