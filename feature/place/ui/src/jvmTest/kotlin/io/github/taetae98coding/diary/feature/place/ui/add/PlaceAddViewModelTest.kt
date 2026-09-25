@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.add

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.place.exception.PlaceCoordinateInvalidException
import io.github.taetae98coding.diary.domain.place.exception.PlaceTitleBlankException
import io.github.taetae98coding.diary.domain.place.usecase.AddPlaceUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.feature.place.ui.form.mapCoordinateInFormPrecision
import io.github.taetae98coding.diary.feature.place.ui.form.placeAddFormState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class PlaceAddViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-PLACE-ADD-DOMAIN-007 추가 처리 중 전달된 추가 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val firstDetail = detail()
                val secondDetail = detail()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddPlaceUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(addPlaceUseCase = useCase)
                collectUiState(viewModel)

                viewModel.add(firstDetail, tagIdSet = emptySet())
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                viewModel.add(secondDetail, tagIdSet = emptySet())
                runCurrent()

                coVerify(exactly = 1) { useCase(AddPlaceUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) }
                coVerify(exactly = 0) { useCase(AddPlaceUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) }

                completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                advanceUntilIdle()
            }
        }

        test("TC-PLACE-ADD-FEATURE-005 추가에 성공하면 성공 Effect를 한 번 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val useCase = mockk<AddPlaceUseCase>()
                coEvery { useCase(any()) } returns Result.success(id)
                val viewModel = viewModel(addPlaceUseCase = useCase)
                collectUiState(viewModel)

                viewModel.effect.test {
                    viewModel.add(detail(), tagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe PlaceAddEffect.AddSucceeded(id = id)
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-PLACE-ADD-FEATURE-008 제목이 공백이면 제목 미입력 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<AddPlaceUseCase>()
                coEvery { useCase(any()) } returns Result.failure(PlaceTitleBlankException())
                val viewModel = viewModel(addPlaceUseCase = useCase)
                collectUiState(viewModel)

                viewModel.effect.test {
                    viewModel.add(detail(title = "  "), tagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe PlaceAddEffect.TitleBlank
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-PLACE-ADD-FEATURE-009 좌표가 성립하지 않으면 좌표 오류 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<AddPlaceUseCase>()
                coEvery { useCase(any()) } returns Result.failure(PlaceCoordinateInvalidException())
                val viewModel = viewModel(addPlaceUseCase = useCase)
                collectUiState(viewModel)

                viewModel.effect.test {
                    viewModel.add(detail(coordinate = Coordinate(latitude = Double.NaN, longitude = Double.NaN)), tagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe PlaceAddEffect.CoordinateInvalid
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-PLACE-ADD-FEATURE-019 지도에서 고른 좌표로 장소를 추가한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val selectedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
                val useCase = mockk<AddPlaceUseCase>()
                coEvery { useCase(any()) } returns Result.success(id)
                val viewModel = viewModel(addPlaceUseCase = useCase)
                collectUiState(viewModel)
                val state = placeAddFormState(initialMapCoordinate = fixtureMonkey.mapCoordinateInFormPrecision())
                state.titleState.setText("title-${fixtureMonkey.giveMeOne<String>()}")

                state.selectSpotOnMap(selectedCoordinate)
                viewModel.effect.test {
                    viewModel.add(detail = state.detail, tagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe PlaceAddEffect.AddSucceeded(id = id)
                    expectNoEvents()
                }

                coVerify(exactly = 1) {
                    useCase(
                        match { parameter ->
                            parameter.detail.coordinate == Coordinate(latitude = selectedCoordinate.latitude, longitude = selectedCoordinate.longitude)
                        },
                    )
                }
            }
        }

        test("TC-PLACE-ADD-FEATURE-006 추가를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddPlaceUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(addPlaceUseCase = useCase)
                collectUiState(viewModel)

                viewModel.add(detail(), tagIdSet = emptySet())
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-PLACE-ADD-DOMAIN-014 저장된 기본 지도를 초기 지도 제공자로 지정한다") {
            MapProvider.entries.forEach { provider ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(defaultProvider = provider)
                    collectUiState(viewModel)
                    advanceUntilIdle()

                    viewModel.uiState.value.defaultProvider shouldBe provider
                }
            }
        }

        test("TC-PLACE-ADD-FEATURE-024 기본 지도를 읽지 못하면 지도 제공자를 확인하지 않은 상태로 둔다") {
            runTest(mainDispatcher) {
                val defaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { defaultMapProviderUseCase(Unit) } returns flowOf(Result.failure(IllegalStateException()))
                val viewModel = viewModel(defaultMapProviderUseCase = defaultMapProviderUseCase)
                collectUiState(viewModel)
                advanceUntilIdle()

                viewModel.uiState.value.defaultProvider
                    .shouldBeNull()
            }
        }

        test("기본 지도가 바뀌면 바뀐 기본 지도를 따라간다") {
            runTest(mainDispatcher) {
                val providerFlow = MutableStateFlow(Result.success(MapProvider.NAVER))
                val defaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { defaultMapProviderUseCase(Unit) } returns providerFlow
                val viewModel = viewModel(defaultMapProviderUseCase = defaultMapProviderUseCase)
                collectUiState(viewModel)
                advanceUntilIdle()

                viewModel.uiState.value.defaultProvider shouldBe MapProvider.NAVER

                providerFlow.value = Result.success(MapProvider.GOOGLE)
                advanceUntilIdle()

                viewModel.uiState.value.defaultProvider shouldBe MapProvider.GOOGLE
            }
        }

        test("추가가 취소되면 진행 상태를 해제하고 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val firstDetail = detail()
                val secondDetail = detail()
                val useCase = mockk<AddPlaceUseCase>()
                coEvery { useCase(AddPlaceUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) } throws CancellationException()
                coEvery { useCase(AddPlaceUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = viewModel(addPlaceUseCase = useCase)
                collectUiState(viewModel)

                viewModel.add(firstDetail, tagIdSet = emptySet())
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()

                viewModel.add(secondDetail, tagIdSet = emptySet())
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(AddPlaceUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) }
                coVerify(exactly = 1) { useCase(AddPlaceUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) }
            }
        }

        test("구독자가 없어도 추가 요청 제한이 동작한다") {
            runTest(mainDispatcher) {
                val firstDetail = detail()
                val secondDetail = detail()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddPlaceUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(addPlaceUseCase = useCase)

                viewModel.add(firstDetail, tagIdSet = emptySet())
                runCurrent()
                viewModel.add(secondDetail, tagIdSet = emptySet())
                runCurrent()

                coVerify(exactly = 1) { useCase(AddPlaceUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) }
                coVerify(exactly = 0) { useCase(AddPlaceUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) }

                completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                advanceUntilIdle()
            }
        }

        test("TC-PLACE-ADD-FEATURE-039 저장에 실패하면 Effect를 보내지 않고 진행 상태만 해제해 같은 내용으로 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val detail = detail()
                val tagIdSet = setOf(fixtureMonkey.giveMeOne<Uuid>())
                val parameter = AddPlaceUseCase.Parameter(detail = detail, tagIdSet = tagIdSet)
                val useCase = mockk<AddPlaceUseCase>()
                coEvery { useCase(any()) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(addPlaceUseCase = useCase)
                collectUiState(viewModel)

                viewModel.effect.test {
                    viewModel.add(detail, tagIdSet = tagIdSet)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()

                    viewModel.add(detail, tagIdSet = tagIdSet)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 2) { useCase(parameter) }
                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        // uiState는 구독자가 있을 때만 갱신되므로 검증 동안 수집을 유지한다.
        private fun TestScope.collectUiState(viewModel: PlaceAddViewModel) {
            backgroundScope.launch { viewModel.uiState.collect() }
        }

        private fun viewModel(
            addPlaceUseCase: AddPlaceUseCase = mockk(),
            defaultProvider: MapProvider = MapProvider.NAVER,
            defaultMapProviderUseCase: GetDefaultMapProviderUseCase = defaultMapProviderUseCase(provider = defaultProvider),
        ): PlaceAddViewModel =
            PlaceAddViewModel(
                addPlaceUseCase = addPlaceUseCase,
                getDefaultMapProviderUseCase = defaultMapProviderUseCase,
            )

        private fun defaultMapProviderUseCase(provider: MapProvider = MapProvider.NAVER): GetDefaultMapProviderUseCase =
            mockk<GetDefaultMapProviderUseCase>().also { useCase ->
                every { useCase(Unit) } returns flowOf(Result.success(provider))
            }

        private fun detail(
            title: String = "title-${fixtureMonkey.giveMeOne<String>()}",
            description: String = fixtureMonkey.giveMeOne<String>(),
            color: Long = fixtureMonkey.giveMeOne<Long>(),
            coordinate: Coordinate = Coordinate(latitude = 37.5, longitude = 127.0),
            address: String = "address-${fixtureMonkey.giveMeOne<String>()}",
        ): PlaceDetail =
            PlaceDetail(
                title = title,
                description = description,
                color = color,
                coordinate = coordinate,
                address = address,
            )
    }
}
