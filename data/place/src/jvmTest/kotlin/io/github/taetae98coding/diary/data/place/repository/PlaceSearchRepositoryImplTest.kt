package io.github.taetae98coding.diary.data.place.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.google.network.api.datasource.GooglePlaceRemoteDataSource
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationBias
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationRemoteEntity
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceRemoteEntity
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.location.CoordinateCircle
import io.github.taetae98coding.diary.core.naver.network.api.datasource.NaverPlaceRemoteDataSource
import io.github.taetae98coding.diary.core.naver.network.api.entity.NaverPlaceRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class PlaceSearchRepositoryImplTest :
    FunSpec({
        test("네이버 검색은 응답한 장소 개수만큼 서로 다른 식별자를 가진 결과를 전달한다") {
            val query = query()
            val remoteList = fixtureMonkey.giveMe<NaverPlaceRemoteEntity>(PLACE_COUNT)
            val dataSource = mockk<NaverPlaceRemoteDataSource>()
            coEvery { dataSource.search(query = query) } returns remoteList
            val repository = NaverPlaceSearchRepositoryImpl(naverPlaceRemoteDataSource = dataSource)

            val actual = repository.fetch(query = query)

            actual shouldHaveSize PLACE_COUNT
            actual
                .map { place -> place.id }
                .toSet() shouldHaveSize PLACE_COUNT
            coVerify(exactly = 1) { dataSource.search(query = query) }
        }

        test("네이버 검색 결과가 없으면 빈 목록을 전달한다") {
            val query = query()
            val dataSource = mockk<NaverPlaceRemoteDataSource>()
            coEvery { dataSource.search(query = query) } returns emptyList()
            val repository = NaverPlaceSearchRepositoryImpl(naverPlaceRemoteDataSource = dataSource)

            repository.fetch(query = query).shouldBeEmpty()
        }

        test("네이버 검색 실패를 그대로 전파한다") {
            val query = query()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val dataSource = mockk<NaverPlaceRemoteDataSource>()
            coEvery { dataSource.search(query = query) } throws throwable
            val repository = NaverPlaceSearchRepositoryImpl(naverPlaceRemoteDataSource = dataSource)

            shouldThrow<IllegalStateException> {
                repository.fetch(query = query)
            } shouldBe throwable
        }

        test("TC-NAVER-PLACE-SEARCH-DATA-005 네이버 검색은 같은 검색어라도 요청할 때마다 새로 조회한다") {
            val query = query()
            val remoteList = fixtureMonkey.giveMe<NaverPlaceRemoteEntity>(PLACE_COUNT)
            val dataSource = mockk<NaverPlaceRemoteDataSource>()
            coEvery { dataSource.search(query = query) } returns remoteList
            val repository = NaverPlaceSearchRepositoryImpl(naverPlaceRemoteDataSource = dataSource)

            repository.fetch(query = query)
            repository.fetch(query = query)

            coVerify(exactly = 2) { dataSource.search(query = query) }
        }

        test("Google 검색은 기준 위치를 좌표와 반경으로 바꿔 전달한다") {
            val query = query()
            val bias =
                CoordinateCircle(
                    center = Coordinate(latitude = 37.5665, longitude = 126.9780),
                    radiusMeters = 1_000.0,
                )
            val dataSource = mockk<GooglePlaceRemoteDataSource>()
            coEvery { dataSource.search(query = query, locationBias = any()) } returns emptyList()
            val repository = GooglePlaceSearchRepositoryImpl(googlePlaceRemoteDataSource = dataSource)

            repository.fetch(query = query, bias = bias)

            coVerify(exactly = 1) {
                dataSource.search(
                    query = query,
                    locationBias =
                        GooglePlaceLocationBias(
                            latitude = bias.center.latitude,
                            longitude = bias.center.longitude,
                            radiusMeters = bias.radiusMeters,
                        ),
                )
            }
        }

        test("Google 검색은 기준 위치가 없으면 위치 조건 없이 조회한다") {
            val query = query()
            val remoteList = fixtureMonkey.giveMe<GooglePlaceRemoteEntity>(PLACE_COUNT)
            val dataSource = mockk<GooglePlaceRemoteDataSource>()
            coEvery { dataSource.search(query = query, locationBias = null) } returns remoteList
            val repository = GooglePlaceSearchRepositoryImpl(googlePlaceRemoteDataSource = dataSource)

            val actual = repository.fetch(query = query, bias = null)

            actual shouldHaveSize PLACE_COUNT
            coVerify(exactly = 1) { dataSource.search(query = query, locationBias = null) }
        }

        test("TC-GOOGLE-PLACE-SEARCH-DATA-006 Google 검색은 같은 검색어라도 요청할 때마다 새로 조회한다") {
            val query = query()
            val remoteList = fixtureMonkey.giveMe<GooglePlaceRemoteEntity>(PLACE_COUNT)
            val dataSource = mockk<GooglePlaceRemoteDataSource>()
            coEvery { dataSource.search(query = query, locationBias = null) } returns remoteList
            val repository = GooglePlaceSearchRepositoryImpl(googlePlaceRemoteDataSource = dataSource)

            repository.fetch(query = query, bias = null)
            repository.fetch(query = query, bias = null)

            coVerify(exactly = 2) { dataSource.search(query = query, locationBias = null) }
        }

        test("TC-NAVER-PLACE-SEARCH-DATA-006 네이버 응답에서 좌표가 빠진 장소는 좌표를 확인할 수 없는 장소로 받은 순서대로 전달한다") {
            val query = query()
            val remoteList =
                fixtureMonkey
                    .giveMe<NaverPlaceRemoteEntity>(PLACE_COUNT)
                    .mapIndexed { index, remote -> remote.copy(title = "장소-$index") }
            val withoutCoordinate = remoteList[1].copy(mapx = "", mapy = "")
            val dataSource = mockk<NaverPlaceRemoteDataSource>()
            coEvery { dataSource.search(query = query) } returns listOf(remoteList[0], withoutCoordinate, remoteList[2])
            val repository = NaverPlaceSearchRepositoryImpl(naverPlaceRemoteDataSource = dataSource)

            val actual = repository.fetch(query = query)

            actual.map { place -> place.name } shouldBe remoteList.map { remote -> remote.title }
            actual[1].coordinate.isRepresentable shouldBe false
        }

        test("TC-GOOGLE-PLACE-SEARCH-DATA-007 Google 응답에서 좌표가 빠진 장소는 좌표를 확인할 수 없는 장소로 받은 순서대로 전달한다") {
            val query = query()
            val remoteList = fixtureMonkey.giveMe<GooglePlaceRemoteEntity>(PLACE_COUNT)
            val withoutCoordinate = remoteList[1].copy(location = GooglePlaceLocationRemoteEntity())
            val dataSource = mockk<GooglePlaceRemoteDataSource>()
            coEvery { dataSource.search(query = query, locationBias = null) } returns listOf(remoteList[0], withoutCoordinate, remoteList[2])
            val repository = GooglePlaceSearchRepositoryImpl(googlePlaceRemoteDataSource = dataSource)

            val actual = repository.fetch(query = query, bias = null)

            actual.map { place -> place.name } shouldBe listOf(remoteList[0], withoutCoordinate, remoteList[2]).map { remote -> remote.displayName.text }
            actual[1].coordinate.isRepresentable shouldBe false
        }

        test("Google 검색 실패를 그대로 전파한다") {
            val query = query()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val dataSource = mockk<GooglePlaceRemoteDataSource>()
            coEvery { dataSource.search(query = query, locationBias = null) } throws throwable
            val repository = GooglePlaceSearchRepositoryImpl(googlePlaceRemoteDataSource = dataSource)

            shouldThrow<IllegalStateException> {
                repository.fetch(query = query, bias = null)
            } shouldBe throwable
        }
    }) {
    private companion object {
        private const val PLACE_COUNT = 3

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun query(): String = "검색어-${fixtureMonkey.giveMeOne<String>()}"
    }
}
