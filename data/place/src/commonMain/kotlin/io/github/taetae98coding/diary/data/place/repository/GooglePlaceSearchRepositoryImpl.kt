package io.github.taetae98coding.diary.data.place.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.google.network.api.datasource.GooglePlaceRemoteDataSource
import io.github.taetae98coding.diary.core.model.location.CoordinateCircle
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.data.place.mapper.toDomain
import io.github.taetae98coding.diary.data.place.mapper.toRemote
import io.github.taetae98coding.diary.domain.place.repository.GooglePlaceSearchRepository
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class GooglePlaceSearchRepositoryImpl(
    private val googlePlaceRemoteDataSource: GooglePlaceRemoteDataSource,
) : GooglePlaceSearchRepository {
    override suspend fun fetch(
        query: String,
        bias: CoordinateCircle?,
    ): List<SearchedPlace> {
        val remoteList =
            googlePlaceRemoteDataSource.search(
                query = query,
                locationBias = bias?.toRemote(),
            )

        // 결과 항목을 구분하는 식별자는 제공자와 무관하게 검색할 때마다 새로 만든다.
        return remoteList.map { remote -> remote.toDomain(id = Uuid.random()) }
    }
}
