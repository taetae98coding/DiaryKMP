package io.github.taetae98coding.diary.core.network.api.placetag.datasource

import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagRemoteEntity

public interface PlaceTagRemoteDataSource {
    public suspend fun push(placeTagList: List<PlaceTagRemoteEntity>)

    public suspend fun pull(usn: Long): List<PlaceTagPullRemoteEntity>
}
