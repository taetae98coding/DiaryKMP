package io.github.taetae98coding.diary.data.place.repository

import io.github.taetae98coding.diary.core.mapper.place.toDomain
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.core.naver.network.api.datasource.NaverPlaceRemoteDataSource
import io.github.taetae98coding.diary.domain.place.repository.NaverPlaceSearchRepository
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class NaverPlaceSearchRepositoryImpl(
    private val naverPlaceRemoteDataSource: NaverPlaceRemoteDataSource,
) : NaverPlaceSearchRepository {
    override suspend fun fetch(query: String): List<SearchedPlace> {
        val remoteList = naverPlaceRemoteDataSource.search(query = query)

        // 네이버 응답에는 장소 식별자가 없어 결과 항목을 구분할 식별자를 만들어 준다.
        return remoteList.map { remote -> remote.toDomain(id = Uuid.random()) }
    }
}
