package io.github.taetae98coding.diary.core.network.api.entitytag.datasource

import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagKindRemoteEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagRemoteEntity

public interface EntityTagRemoteDataSource {
    public suspend fun push(
        kind: EntityTagKindRemoteEntity,
        entityTagList: List<EntityTagRemoteEntity>,
    )

    public suspend fun pull(
        kind: EntityTagKindRemoteEntity,
        usn: Long,
    ): List<EntityTagPullRemoteEntity>
}
