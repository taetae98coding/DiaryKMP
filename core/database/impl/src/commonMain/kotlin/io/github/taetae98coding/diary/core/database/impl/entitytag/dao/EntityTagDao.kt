package io.github.taetae98coding.diary.core.database.impl.entitytag.dao

import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

// 종류마다 테이블이 달라 SQL은 종류별 DAO가 갖고, 공통 구현은 이 계약으로만 다룬다.
internal interface EntityTagDao<T> : RoomDao<T> {
    suspend fun findByEntityIdList(entityIdList: List<Uuid>): List<T>

    suspend fun updateDeleted(
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
