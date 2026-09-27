@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.detail

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.place.exception.PlaceCoordinateInvalidException
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.UpdatePlaceUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
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
import kotlin.time.Instant
import kotlin.uuid.Uuid

class PlaceDetailViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-PLACE-DETAIL-FEATURE-001 조회가 완료되지 않으면 조회 중 상태로 둔다") {
            runTest(mainDispatcher) {
                val findPlaceUseCase = mockk<FindPlaceUseCase>()
                every { findPlaceUseCase(any<Uuid>()) } returns emptyFlow()
                val viewModel = viewModel(findPlaceUseCase = findPlaceUseCase)
                collectUiState(viewModel)
                advanceUntilIdle()

                viewModel.uiState.value shouldBe PlaceDetailUiState.Loading
            }
        }

        test("TC-PLACE-DETAIL-FEATURE-002 조회에 성공하면 저장된 내용을 표시 상태로 둔다") {
            runTest(mainDispatcher) {
                val place = place()
                val viewModel = viewModel(id = place.id, place = place)
                collectUiState(viewModel)
                advanceUntilIdle()

                val content = viewModel.uiState.value.shouldBeInstanceOf<PlaceDetailUiState.Content>()
                content.id shouldBe place.id
                content.detail shouldBe place.detail
            }
        }

        test("TC-PLACE-DETAIL-FEATURE-003 장소를 조회할 수 없으면 조회 중 상태를 유지한다") {
            listOf(
                Result.failure<Place?>(IllegalStateException("조회에 실패함")),
                Result.success<Place?>(null),
            ).forEach { result ->
                runTest(mainDispatcher) {
                    val findPlaceUseCase = mockk<FindPlaceUseCase>()
                    every { findPlaceUseCase(any<Uuid>()) } returns flowOf(result)
                    val viewModel = viewModel(findPlaceUseCase = findPlaceUseCase)
                    collectUiState(viewModel)
                    advanceUntilIdle()

                    viewModel.uiState.value shouldBe PlaceDetailUiState.Loading
                }
            }
        }

        test("TC-PLACE-DETAIL-FEATURE-006 수정에 성공하면 성공 Effect를 한 번 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val place = place()
                val updatePlaceUseCase = mockk<UpdatePlaceUseCase>()
                coEvery { updatePlaceUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(id = place.id, place = place, updatePlaceUseCase = updatePlaceUseCase)
                collectUiState(viewModel)

                viewModel.effect.test {
                    viewModel.update(detail = detail())
                    advanceUntilIdle()

                    awaitItem() shouldBe PlaceDetailEffect.UpdateSucceeded
                    expectNoEvents()
                }

                viewModel.isUpdateInProgress().shouldBeFalse()
            }
        }

        test("TC-PLACE-DETAIL-FEATURE-006 수정은 대상 장소의 식별자와 입력한 내용으로 요청한다") {
            runTest(mainDispatcher) {
                val place = place()
                val detail = detail()
                val updatePlaceUseCase = mockk<UpdatePlaceUseCase>()
                coEvery { updatePlaceUseCase(any()) } returns Result.success(1)
                val viewModel = viewModel(id = place.id, place = place, updatePlaceUseCase = updatePlaceUseCase)
                collectUiState(viewModel)

                viewModel.update(detail = detail)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    updatePlaceUseCase(UpdatePlaceUseCase.Parameter(id = place.id, detail = detail))
                }
            }
        }

        test("TC-PLACE-DETAIL-FEATURE-008 수정을 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Int>>()
                val updatePlaceUseCase = mockk<UpdatePlaceUseCase>()
                coEvery { updatePlaceUseCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(updatePlaceUseCase = updatePlaceUseCase)
                collectUiState(viewModel)
                advanceUntilIdle()

                viewModel.update(detail = detail())
                runCurrent()

                viewModel.isUpdateInProgress().shouldBeTrue()

                completion.complete(Result.success(1))
                advanceUntilIdle()

                viewModel.isUpdateInProgress().shouldBeFalse()
            }
        }

        test("TC-PLACE-DETAIL-FEATURE-010 좌표가 성립하지 않으면 좌표 오류 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val updatePlaceUseCase = mockk<UpdatePlaceUseCase>()
                coEvery { updatePlaceUseCase(any()) } returns Result.failure(PlaceCoordinateInvalidException())
                val viewModel = viewModel(updatePlaceUseCase = updatePlaceUseCase)
                collectUiState(viewModel)

                viewModel.effect.test {
                    viewModel.update(detail = detail(coordinate = Coordinate(latitude = Double.NaN, longitude = Double.NaN)))
                    advanceUntilIdle()

                    awaitItem() shouldBe PlaceDetailEffect.CoordinateInvalid
                    expectNoEvents()
                }

                viewModel.isUpdateInProgress().shouldBeFalse()
            }
        }

        test("TC-PLACE-DETAIL-FEATURE-012 삭제에 성공하면 삭제 성공 Effect를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val place = place()
                val deletePlaceUseCase = mockk<DeletePlaceUseCase>()
                coEvery { deletePlaceUseCase(place.id) } returns Result.success(1)
                val viewModel = viewModel(id = place.id, place = place, deletePlaceUseCase = deletePlaceUseCase)
                collectUiState(viewModel)

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()

                    awaitItem() shouldBe PlaceDetailEffect.DeleteSucceeded
                    expectNoEvents()
                }

                coVerify(exactly = 1) { deletePlaceUseCase(place.id) }
            }
        }

        test("TC-PLACE-DETAIL-FEATURE-013 삭제를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Int>>()
                val deletePlaceUseCase = mockk<DeletePlaceUseCase>()
                coEvery { deletePlaceUseCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(deletePlaceUseCase = deletePlaceUseCase)
                collectUiState(viewModel)
                advanceUntilIdle()

                viewModel.delete()
                runCurrent()

                viewModel.isDeleteInProgress().shouldBeTrue()

                completion.complete(Result.success(1))
                advanceUntilIdle()

                viewModel.isDeleteInProgress().shouldBeFalse()
            }
        }

        test("TC-PLACE-DETAIL-DOMAIN-011 처리 중에는 같은 종류의 요청만 제한한다") {
            runTest(mainDispatcher) {
                val updateCompletion = CompletableDeferred<Result<Int>>()
                val deleteCompletion = CompletableDeferred<Result<Int>>()
                val updatePlaceUseCase = mockk<UpdatePlaceUseCase>()
                coEvery { updatePlaceUseCase(any()) } coAnswers { updateCompletion.await() }
                val deletePlaceUseCase = mockk<DeletePlaceUseCase>()
                coEvery { deletePlaceUseCase(any()) } coAnswers { deleteCompletion.await() }
                val viewModel =
                    viewModel(
                        updatePlaceUseCase = updatePlaceUseCase,
                        deletePlaceUseCase = deletePlaceUseCase,
                    )
                collectUiState(viewModel)

                viewModel.update(detail = detail())
                runCurrent()
                viewModel.update(detail = detail())
                runCurrent()
                viewModel.delete()
                runCurrent()
                viewModel.delete()
                runCurrent()

                coVerify(exactly = 1) { updatePlaceUseCase(any()) }
                coVerify(exactly = 1) { deletePlaceUseCase(any()) }

                updateCompletion.complete(Result.success(1))
                deleteCompletion.complete(Result.success(1))
                advanceUntilIdle()
            }
        }

        test("TC-PLACE-DETAIL-DOMAIN-012 저장된 기본 지도를 초기 지도 제공자로 지정한다") {
            MapProvider.entries.forEach { provider ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(defaultProvider = provider)
                    collectUiState(viewModel)
                    advanceUntilIdle()

                    viewModel.uiState.value
                        .shouldBeInstanceOf<PlaceDetailUiState.Content>()
                        .defaultProvider shouldBe provider
                }
            }
        }

        test("TC-PLACE-DETAIL-FEATURE-020 기본 지도를 읽지 못하면 지도 제공자를 확인하지 않은 상태로 둔다") {
            runTest(mainDispatcher) {
                val defaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { defaultMapProviderUseCase(Unit) } returns flowOf(Result.failure(IllegalStateException()))
                val viewModel = viewModel(defaultMapProviderUseCase = defaultMapProviderUseCase)
                collectUiState(viewModel)
                advanceUntilIdle()

                viewModel.uiState.value
                    .shouldBeInstanceOf<PlaceDetailUiState.Content>()
                    .defaultProvider
                    .shouldBeNull()
            }
        }

        test("TC-PLACE-DETAIL-DOMAIN-015 기본 지도가 바뀌면 바뀐 기본 지도를 따라간다") {
            runTest(mainDispatcher) {
                val providerFlow = MutableStateFlow(Result.success(MapProvider.NAVER))
                val defaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
                every { defaultMapProviderUseCase(Unit) } returns providerFlow
                val viewModel = viewModel(defaultMapProviderUseCase = defaultMapProviderUseCase)
                collectUiState(viewModel)
                advanceUntilIdle()

                viewModel.uiState.value
                    .shouldBeInstanceOf<PlaceDetailUiState.Content>()
                    .defaultProvider shouldBe MapProvider.NAVER

                providerFlow.value = Result.success(MapProvider.GOOGLE)
                advanceUntilIdle()

                viewModel.uiState.value
                    .shouldBeInstanceOf<PlaceDetailUiState.Content>()
                    .defaultProvider shouldBe MapProvider.GOOGLE
            }
        }

        test("TC-PLACE-DETAIL-DATA-002 저장된 내용이 바뀌면 표시 상태가 갱신된다") {
            runTest(mainDispatcher) {
                val place = place()
                val updated = place.copy(detail = place.detail.copy(title = "updated-${fixtureMonkey.giveMeOne<String>()}"))
                val placeFlow = MutableStateFlow(Result.success<Place?>(place))
                val findPlaceUseCase = mockk<FindPlaceUseCase>()
                every { findPlaceUseCase(place.id) } returns placeFlow
                val viewModel = viewModel(id = place.id, findPlaceUseCase = findPlaceUseCase)
                collectUiState(viewModel)
                advanceUntilIdle()

                viewModel.uiState.value
                    .shouldBeInstanceOf<PlaceDetailUiState.Content>()
                    .detail shouldBe place.detail

                placeFlow.value = Result.success(updated)
                advanceUntilIdle()

                viewModel.uiState.value
                    .shouldBeInstanceOf<PlaceDetailUiState.Content>()
                    .detail shouldBe updated.detail
            }
        }

        test("TC-PLACE-DETAIL-FEATURE-023 대상 장소가 삭제 상태로 바뀌어도 내용 표시 상태를 유지한다") {
            runTest(mainDispatcher) {
                val place = place()
                val placeFlow = MutableStateFlow(Result.success<Place?>(place))
                val findPlaceUseCase = mockk<FindPlaceUseCase>()
                every { findPlaceUseCase(place.id) } returns placeFlow
                val viewModel = viewModel(id = place.id, findPlaceUseCase = findPlaceUseCase)
                collectUiState(viewModel)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<PlaceDetailUiState.Content>()

                // 조회는 삭제 여부를 가리지 않으므로 삭제 상태가 되어도 같은 장소가 계속 조회된다.
                placeFlow.value = Result.success(place.copy(isDeleted = true))
                advanceUntilIdle()

                viewModel.uiState.value
                    .shouldBeInstanceOf<PlaceDetailUiState.Content>()
                    .detail shouldBe place.detail
            }
        }

        test("TC-PLACE-DETAIL-DOMAIN-020 삭제 상태인 장소로 진입해도 내용 표시 상태가 된다") {
            runTest(mainDispatcher) {
                val place = place().copy(isDeleted = true)
                val viewModel = viewModel(id = place.id, place = place)
                collectUiState(viewModel)
                advanceUntilIdle()

                viewModel.uiState.value
                    .shouldBeInstanceOf<PlaceDetailUiState.Content>()
                    .detail shouldBe place.detail
            }
        }

        test("TC-PLACE-DETAIL-FEATURE-054 수정 저장에 실패하면 Effect를 보내지 않고 진행 상태만 해제해 수정 반영을 다시 실행할 수 있다") {
            runTest(mainDispatcher) {
                val id = Uuid.random()
                val detail = detail()
                val parameter = UpdatePlaceUseCase.Parameter(id = id, detail = detail)
                val updatePlaceUseCase = mockk<UpdatePlaceUseCase>()
                coEvery { updatePlaceUseCase(any()) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(id = id, updatePlaceUseCase = updatePlaceUseCase)
                collectUiState(viewModel)

                viewModel.effect.test {
                    viewModel.update(detail = detail)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.isUpdateInProgress().shouldBeFalse()

                    viewModel.update(detail = detail)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 2) { updatePlaceUseCase(parameter) }
                viewModel.isUpdateInProgress().shouldBeFalse()
            }
        }

        test("삭제가 실패하면 삭제 성공 Effect를 보내지 않고 진행 상태만 해제한다") {
            runTest(mainDispatcher) {
                val deletePlaceUseCase = mockk<DeletePlaceUseCase>()
                coEvery { deletePlaceUseCase(any()) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(deletePlaceUseCase = deletePlaceUseCase)
                collectUiState(viewModel)

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                viewModel.isDeleteInProgress().shouldBeFalse()
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        // uiState는 구독자가 있을 때만 갱신되므로 검증 동안 수집을 유지한다.
        private fun TestScope.collectUiState(viewModel: PlaceDetailViewModel) {
            backgroundScope.launch { viewModel.uiState.collect() }
        }

        private fun PlaceDetailViewModel.isUpdateInProgress(): Boolean = (uiState.value as? PlaceDetailUiState.Content)?.isUpdateInProgress == true

        private fun PlaceDetailViewModel.isDeleteInProgress(): Boolean = (uiState.value as? PlaceDetailUiState.Content)?.isDeleteInProgress == true

        private fun viewModel(
            id: Uuid = Uuid.random(),
            place: Place = place(id = id),
            updatePlaceUseCase: UpdatePlaceUseCase = mockk(),
            deletePlaceUseCase: DeletePlaceUseCase = mockk(),
            findPlaceUseCase: FindPlaceUseCase = findPlaceUseCase(id = id, place = place),
            defaultProvider: MapProvider = MapProvider.NAVER,
            defaultMapProviderUseCase: GetDefaultMapProviderUseCase = defaultMapProviderUseCase(provider = defaultProvider),
        ): PlaceDetailViewModel =
            PlaceDetailViewModel(
                id = id,
                updatePlaceUseCase = updatePlaceUseCase,
                deletePlaceUseCase = deletePlaceUseCase,
                findPlaceUseCase = findPlaceUseCase,
                getDefaultMapProviderUseCase = defaultMapProviderUseCase,
            )

        private fun defaultMapProviderUseCase(provider: MapProvider = MapProvider.NAVER): GetDefaultMapProviderUseCase =
            mockk<GetDefaultMapProviderUseCase>().also { useCase ->
                every { useCase(Unit) } returns flowOf(Result.success(provider))
            }

        private fun findPlaceUseCase(
            id: Uuid,
            place: Place,
        ): FindPlaceUseCase =
            mockk<FindPlaceUseCase>().also { useCase ->
                every { useCase(id) } returns flowOf(Result.success(place))
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

        private fun place(id: Uuid = Uuid.random()): Place =
            Place(
                id = id,
                detail = detail(),
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                createdAt = fixtureMonkey.giveMeOne<Instant>(),
            )
    }
}
