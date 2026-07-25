package io.github.taetae98coding.diary.core.naver.network.api.datasource

import io.github.taetae98coding.diary.core.naver.network.api.entity.NaverPlaceRemoteEntity

public interface NaverPlaceRemoteDataSource {
    public suspend fun search(query: String): List<NaverPlaceRemoteEntity>
}
