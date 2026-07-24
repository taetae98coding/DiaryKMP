package io.github.taetae98coding.diary.core.network.api.taglink.datasource

import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkRemoteEntity

public interface TagLinkRemoteDataSource {
    public suspend fun push(tagLinkList: List<TagLinkRemoteEntity>)

    public suspend fun pull(usn: Long): List<TagLinkPullRemoteEntity>
}
