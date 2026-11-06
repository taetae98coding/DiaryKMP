@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.qr.ui.add

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.domain.qr.exception.QrTitleBlankException
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class QrAddViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-f8cb4a3f-8b52-40bb-b8cc-3a7badbc7706 추가 처리 중 전달된 추가 요청은 처리하지 않고 처리가 끝난 뒤의 요청은 다시 처리한다") {
            runTest(mainDispatcher) {
                val firstDetail = Arb.qrDetail().next()
                val secondDetail = Arb.qrDetail().next()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = AddQrUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) } coAnswers { completion.await() }
                coEvery { useCase(parameter = AddQrUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = viewModel(addQrUseCase = useCase)

                viewModel.add(detail = firstDetail, tagIdSet = emptySet())
                runCurrent()
                viewModel.add(detail = secondDetail, tagIdSet = emptySet())
                runCurrent()

                coVerify(exactly = 1) { useCase(parameter = AddQrUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) }
                coVerify(exactly = 0) { useCase(parameter = AddQrUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) }

                completion.complete(Result.success(Arb.kotlinUuid().next()))
                advanceUntilIdle()
                viewModel.add(detail = secondDetail, tagIdSet = emptySet())
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = AddQrUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) }
            }
        }

        test("TC-9da64d2c-eed0-4581-a0f8-fe353d09136e 추가에 성공하면 성공 Effect를 한 번 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = Arb.qrDetail().next()
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = AddQrUseCase.Parameter(detail = detail, tagIdSet = emptySet())) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = viewModel(addQrUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail, tagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe QrAddEffect.AddSucceeded
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-a6fde504-b12a-4648-9b7f-cad5ca5d3fd0 고른 태그를 새 QR과 함께 추가하도록 넘긴다") {
            runTest(mainDispatcher) {
                val detail = Arb.qrDetail().next()
                val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = viewModel(addQrUseCase = useCase)

                viewModel.add(detail = detail, tagIdSet = tagIdSet)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = AddQrUseCase.Parameter(detail = detail, tagIdSet = tagIdSet)) }
            }
        }

        test("TC-1a5e7fb5-df16-4fd9-8bef-df01b624f7e9 제목 조건이 성립하지 않으면 제목 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = Arb.qrDetail().next()
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = AddQrUseCase.Parameter(detail = detail, tagIdSet = emptySet())) } returns Result.failure(QrTitleBlankException())
                val viewModel = viewModel(addQrUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail, tagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe QrAddEffect.TitleBlank
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-b707d875-5ed9-49ab-ad91-bd0647f2bba5 QR 값 조건이 성립하지 않으면 QR 값 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = Arb.qrDetail().next()
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = AddQrUseCase.Parameter(detail = detail, tagIdSet = emptySet())) } returns Result.failure(QrValueEmptyException())
                val viewModel = viewModel(addQrUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail, tagIdSet = emptySet())
                    advanceUntilIdle()

                    awaitItem() shouldBe QrAddEffect.ValueEmpty
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-018763b3-6a0e-4132-b201-64d8ed5b8b6d 기기 저장에 실패하면 Effect 없이 진행 상태만 해제하고 같은 내용으로 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val detail = Arb.qrDetail().next()
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = AddQrUseCase.Parameter(detail = detail, tagIdSet = emptySet())) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = viewModel(addQrUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail, tagIdSet = emptySet())
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()

                    viewModel.add(detail = detail, tagIdSet = emptySet())
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 2) { useCase(parameter = AddQrUseCase.Parameter(detail = detail, tagIdSet = emptySet())) }
            }
        }

        test("TC-1e6c2fde-1356-4fe2-a374-f4ba7f54761a 추가를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val detail = Arb.qrDetail().next()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = AddQrUseCase.Parameter(detail = detail, tagIdSet = emptySet())) } coAnswers { completion.await() }
                val viewModel = viewModel(addQrUseCase = useCase)

                viewModel.add(detail = detail, tagIdSet = emptySet())
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                completion.complete(Result.success(Arb.kotlinUuid().next()))
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-edafbb3b-c8fe-4b77-bf3e-5f61dfd84093 저장된 기본 지도를 위치 포맷의 지도 제공자로 알린다") {
            runTest(mainDispatcher) {
                listOf(MapProvider.NAVER, MapProvider.GOOGLE).forEach { provider ->
                    val viewModel = viewModel(addQrUseCase = mockk(), defaultMapProviderUseCase = defaultMapProviderUseCase(Result.success(provider)))
                    advanceUntilIdle()

                    viewModel.uiState.value.defaultProvider shouldBe provider
                    viewModel.uiState.value.isMapDisplayed
                        .shouldBeTrue()
                }
            }
        }

        test("TC-35105c5b-bd39-4bf3-a8ec-364973e7e4d9 표시 중에 기본 지도가 바뀌면 바뀐 기본 지도를 알린다") {
            runTest(mainDispatcher) {
                val providerFlow = MutableStateFlow(Result.success(MapProvider.NAVER))
                val useCase = mockk<GetDefaultMapProviderUseCase>()
                every { useCase(Unit) } returns providerFlow
                val viewModel = viewModel(addQrUseCase = mockk(), defaultMapProviderUseCase = useCase)
                advanceUntilIdle()
                viewModel.uiState.value.defaultProvider shouldBe MapProvider.NAVER

                providerFlow.value = Result.success(MapProvider.GOOGLE)
                advanceUntilIdle()

                viewModel.uiState.value.defaultProvider shouldBe MapProvider.GOOGLE
                viewModel.uiState.value.isMapDisplayed
                    .shouldBeTrue()
            }
        }

        test("TC-7148148e-ecdc-40ac-a941-7b1607d78fbe 기본 지도를 읽지 못하면 지도를 표시하지 않는다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        addQrUseCase = mockk(),
                        defaultMapProviderUseCase = defaultMapProviderUseCase(Result.failure(IllegalStateException(Arb.string().next()))),
                    )
                advanceUntilIdle()

                viewModel.uiState.value.defaultProvider
                    .shouldBeNull()
                viewModel.uiState.value.isMapDisplayed
                    .shouldBeFalse()
            }
        }

        test("추가가 취소되면 진행 상태를 해제하고 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val firstDetail = Arb.qrDetail().next()
                val secondDetail = Arb.qrDetail().next()
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = AddQrUseCase.Parameter(detail = firstDetail, tagIdSet = emptySet())) } throws CancellationException()
                coEvery { useCase(parameter = AddQrUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = viewModel(addQrUseCase = useCase)

                viewModel.add(detail = firstDetail, tagIdSet = emptySet())
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()

                viewModel.add(detail = secondDetail, tagIdSet = emptySet())
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = AddQrUseCase.Parameter(detail = secondDetail, tagIdSet = emptySet())) }
            }
        }
    }

    private fun TestScope.viewModel(
        addQrUseCase: AddQrUseCase,
        defaultMapProviderUseCase: GetDefaultMapProviderUseCase = defaultMapProviderUseCase(Result.success(MapProvider.NAVER)),
    ): QrAddViewModel {
        val viewModel =
            QrAddViewModel(
                addQrUseCase = addQrUseCase,
                getDefaultMapProviderUseCase = defaultMapProviderUseCase,
            )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel
    }

    private fun defaultMapProviderUseCase(result: Result<MapProvider>): GetDefaultMapProviderUseCase =
        mockk<GetDefaultMapProviderUseCase>().also { useCase ->
            every { useCase(Unit) } returns flowOf(result)
        }
}
