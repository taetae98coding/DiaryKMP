package io.github.taetae98coding.diary.core.network.api.place.datasource

import io.github.taetae98coding.diary.core.network.api.place.entity.PlacePullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity

public interface PlaceRemoteDataSource {
    public suspend fun push(placeList: List<PlaceRemoteEntity>)

    public suspend fun pull(usn: Long): List<PlacePullRemoteEntity>
}
