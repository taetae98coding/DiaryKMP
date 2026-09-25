package io.github.taetae98coding.diary.domain.place.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.location.CoordinateCircle
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.domain.place.repository.GooglePlaceSearchRepository
import io.github.taetae98coding.diary.domain.place.repository.NaverPlaceSearchRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class FetchSearchedPlaceUseCaseTest :
    BehaviorSpec({
        Given("네이버로 검색하고 저장소가 장소 목록을 제공한다") {
            val query = query()
            val placeList = List(PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
            val naverPlaceSearchRepository = mockk<NaverPlaceSearchRepository>()
            val googlePlaceSearchRepository = mockk<GooglePlaceSearchRepository>()
            val useCase =
                FetchSearchedPlaceUseCase(
                    naverPlaceSearchRepository = naverPlaceSearchRepository,
                    googlePlaceSearchRepository = googlePlaceSearchRepository,
                )
            coEvery { naverPlaceSearchRepository.fetch(query = query) } returns placeList

            When("보고 있는 영역과 함께 검색한다") {
                Then("TC-PLACE-SEARCH-DIALOG-DOMAIN-009 기준 위치 없이 검색어만으로 조회하고 받은 목록을 전달한다") {
                    useCase(
                        parameter =
                            FetchSearchedPlaceUseCase.Parameter(
                                query = query,
                                provider = MapProvider.NAVER,
                                bounds = fixtureMonkey.giveMeOne<CoordinateBounds>(),
                            ),
                    ).shouldBeSuccess() shouldBe placeList

                    coVerify(exactly = 1) { naverPlaceSearchRepository.fetch(query = query) }
                    coVerify(exactly = 0) { googlePlaceSearchRepository.fetch(query = any(), bias = any()) }
                }
            }
        }

        Given("Google로 검색하고 저장소가 장소 목록을 제공한다") {
            val query = query()
            val placeList = List(PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
            val naverPlaceSearchRepository = mockk<NaverPlaceSearchRepository>()
            val googlePlaceSearchRepository = mockk<GooglePlaceSearchRepository>()
            val useCase =
                FetchSearchedPlaceUseCase(
                    naverPlaceSearchRepository = naverPlaceSearchRepository,
                    googlePlaceSearchRepository = googlePlaceSearchRepository,
                )
            val biasList = mutableListOf<CoordinateCircle?>()
            coEvery { googlePlaceSearchRepository.fetch(query = query, bias = any()) } answers {
                biasList += arg<CoordinateCircle?>(1)
                placeList
            }

            When("남북이 더 좁은 영역을 보고 있는 상태로 검색한다") {
                val bounds =
                    CoordinateBounds(
                        south = 37.0,
                        north = 37.2,
                        west = 127.0,
                        east = 127.4,
                    )

                Then("TC-PLACE-SEARCH-DIALOG-DOMAIN-014 영역의 가운데를 중심으로 남북 절반을 반경으로 삼는다") {
                    useCase(parameter = googleParameter(query = query, bounds = bounds)).shouldBeSuccess() shouldBe placeList

                    val bias = biasList.last().shouldNotBeNull()
                    bias.center.latitude shouldBe (37.1 plusOrMinus COORDINATE_TOLERANCE)
                    bias.center.longitude shouldBe (127.2 plusOrMinus COORDINATE_TOLERANCE)
                    bias.radiusMeters shouldBe (NORTH_SOUTH_HALF_RADIUS_METERS plusOrMinus RADIUS_TOLERANCE_METERS)
                }
            }

            When("동서가 더 좁은 영역을 보고 있는 상태로 검색한다") {
                val bounds =
                    CoordinateBounds(
                        south = 37.0,
                        north = 37.4,
                        west = 127.0,
                        east = 127.1,
                    )

                Then("TC-PLACE-SEARCH-DIALOG-DOMAIN-014 영역의 가운데를 중심으로 동서 절반을 반경으로 삼는다") {
                    useCase(parameter = googleParameter(query = query, bounds = bounds)).shouldBeSuccess() shouldBe placeList

                    val bias = biasList.last().shouldNotBeNull()
                    bias.center.latitude shouldBe (37.2 plusOrMinus COORDINATE_TOLERANCE)
                    bias.center.longitude shouldBe (127.05 plusOrMinus COORDINATE_TOLERANCE)
                    bias.radiusMeters shouldBe (WEST_EAST_HALF_RADIUS_METERS plusOrMinus RADIUS_TOLERANCE_METERS)
                }
            }

            When("보고 있는 영역을 확인할 수 없는 상태로 검색한다") {
                Then("TC-PLACE-SEARCH-DIALOG-DOMAIN-016 기준 위치 없이 검색한다") {
                    useCase(parameter = googleParameter(query = query, bounds = null)).shouldBeSuccess() shouldBe placeList

                    biasList.last().shouldBeNull()
                }
            }

            When("남북과 동서 폭이 없는 영역을 보고 있는 상태로 검색한다") {
                val bounds =
                    CoordinateBounds(
                        south = 37.0,
                        north = 37.0,
                        west = 127.0,
                        east = 127.0,
                    )

                Then("TC-PLACE-SEARCH-DIALOG-DOMAIN-016 크기가 없는 영역이면 기준 위치 없이 검색한다") {
                    useCase(parameter = googleParameter(query = query, bounds = bounds)).shouldBeSuccess() shouldBe placeList

                    biasList.last().shouldBeNull()
                }
            }

            When("아주 넓은 영역을 보고 있는 상태로 검색한다") {
                val bounds =
                    CoordinateBounds(
                        south = 30.0,
                        north = 40.0,
                        west = 120.0,
                        east = 130.0,
                    )

                Then("TC-PLACE-SEARCH-DIALOG-DOMAIN-019 반경을 줄이지 않고 가장 큰 원을 그대로 기준 위치로 삼는다") {
                    useCase(parameter = googleParameter(query = query, bounds = bounds)).shouldBeSuccess() shouldBe placeList

                    biasList.last().shouldNotBeNull().radiusMeters shouldBe
                        (WIDE_AREA_RADIUS_METERS plusOrMinus RADIUS_TOLERANCE_METERS)
                }
            }
        }

        Given("앞뒤에 공백이 붙은 검색어로 검색한다") {
            val trimmed = query()
            val placeList = List(PLACE_COUNT) { fixtureMonkey.giveMeOne<SearchedPlace>() }
            val naverPlaceSearchRepository = mockk<NaverPlaceSearchRepository>()
            val googlePlaceSearchRepository = mockk<GooglePlaceSearchRepository>()
            val useCase =
                FetchSearchedPlaceUseCase(
                    naverPlaceSearchRepository = naverPlaceSearchRepository,
                    googlePlaceSearchRepository = googlePlaceSearchRepository,
                )
            coEvery { naverPlaceSearchRepository.fetch(query = any()) } returns placeList
            coEvery { googlePlaceSearchRepository.fetch(query = any(), bias = any()) } returns placeList

            When("네이버로 검색한다") {
                Then("TC-NAVER-PLACE-SEARCH-DOMAIN-002 앞뒤 공백을 뺀 검색어로 조회한다") {
                    useCase(
                        parameter =
                            FetchSearchedPlaceUseCase.Parameter(
                                query = "  $trimmed\t\n ",
                                provider = MapProvider.NAVER,
                            ),
                    ).shouldBeSuccess() shouldBe placeList

                    coVerify(exactly = 1) { naverPlaceSearchRepository.fetch(query = trimmed) }
                }
            }

            When("Google로 검색한다") {
                Then("TC-GOOGLE-PLACE-SEARCH-DOMAIN-006 앞뒤 공백을 뺀 검색어로 조회한다") {
                    useCase(
                        parameter =
                            FetchSearchedPlaceUseCase.Parameter(
                                query = "  $trimmed\t\n ",
                                provider = MapProvider.GOOGLE,
                            ),
                    ).shouldBeSuccess() shouldBe placeList

                    coVerify(exactly = 1) { googlePlaceSearchRepository.fetch(query = trimmed, bias = any()) }
                }
            }
        }

        Given("검색 저장소가 실패한다") {
            val query = query()
            val throwable = IllegalStateException("검색에 실패함")
            val naverPlaceSearchRepository = mockk<NaverPlaceSearchRepository>()
            val googlePlaceSearchRepository = mockk<GooglePlaceSearchRepository>()
            val useCase =
                FetchSearchedPlaceUseCase(
                    naverPlaceSearchRepository = naverPlaceSearchRepository,
                    googlePlaceSearchRepository = googlePlaceSearchRepository,
                )
            coEvery { naverPlaceSearchRepository.fetch(query = any()) } throws throwable
            coEvery { googlePlaceSearchRepository.fetch(query = any(), bias = any()) } throws throwable

            MapProvider.entries.forEach { provider ->
                When("$provider 로 검색한다") {
                    Then("실패를 그대로 전달한다") {
                        useCase(
                            parameter =
                                FetchSearchedPlaceUseCase.Parameter(
                                    query = query,
                                    provider = provider,
                                ),
                        ).shouldBeFailure() shouldBe throwable
                    }
                }
            }
        }
    }) {
    private companion object {
        private const val PLACE_COUNT = 3
        private const val COORDINATE_TOLERANCE = 1e-9
        private const val RADIUS_TOLERANCE_METERS = 1.0

        // 남북 0.2도 영역의 절반
        private const val NORTH_SOUTH_HALF_RADIUS_METERS = 11_132.0

        // 위도 37.2도에서 동서 0.1도 영역의 절반
        private const val WEST_EAST_HALF_RADIUS_METERS = 4_433.5

        // 위도 35도에서 동서 10도 영역의 절반
        private const val WIDE_AREA_RADIUS_METERS = 455_940.0

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun query(): String = "검색어-${fixtureMonkey.giveMeOne<String>()}"

        private fun googleParameter(
            query: String,
            bounds: CoordinateBounds?,
        ): FetchSearchedPlaceUseCase.Parameter =
            FetchSearchedPlaceUseCase.Parameter(
                query = query,
                provider = MapProvider.GOOGLE,
                bounds = bounds,
            )
    }
}
