package io.github.taetae98coding.diary.core.network.api.tag.datasource

import io.github.taetae98coding.diary.core.network.api.tag.entity.TagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity

public interface TagRemoteDataSource {
    public suspend fun push(tagList: List<TagRemoteEntity>)

    public suspend fun pull(usn: Long): List<TagPullRemoteEntity>
}
