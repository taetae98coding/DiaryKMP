@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.qr.ui.add

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.domain.qr.exception.QrTitleBlankException
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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

        test("TC-QR-ADD-DOMAIN-015 추가 처리 중 전달된 추가 요청은 처리하지 않고 처리가 끝난 뒤의 요청은 다시 처리한다") {
            runTest(mainDispatcher) {
                val firstDetail = fixtureMonkey.giveMeOne<QrDetail>()
                val secondDetail = fixtureMonkey.giveMeOne<QrDetail>()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = firstDetail) } coAnswers { completion.await() }
                coEvery { useCase(parameter = secondDetail) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = QrAddViewModel(addQrUseCase = useCase)

                viewModel.add(detail = firstDetail)
                runCurrent()
                viewModel.add(detail = secondDetail)
                runCurrent()

                coVerify(exactly = 1) { useCase(parameter = firstDetail) }
                coVerify(exactly = 0) { useCase(parameter = secondDetail) }

                completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                advanceUntilIdle()
                viewModel.add(detail = secondDetail)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = secondDetail) }
            }
        }

        test("TC-QR-ADD-FEATURE-022 추가에 성공하면 성공 Effect를 한 번 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<QrDetail>()
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = detail) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = QrAddViewModel(addQrUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail)
                    advanceUntilIdle()

                    awaitItem() shouldBe QrAddEffect.AddSucceeded
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-QR-ADD-FEATURE-025 성립하지 않은 조건에 맞는 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val caseMap =
                    mapOf<Throwable, QrAddEffect>(
                        QrTitleBlankException() to QrAddEffect.TitleBlank,
                        QrValueEmptyException() to QrAddEffect.ValueEmpty,
                    )

                caseMap.forEach { (throwable, expected) ->
                    val detail = fixtureMonkey.giveMeOne<QrDetail>()
                    val useCase = mockk<AddQrUseCase>()
                    coEvery { useCase(parameter = detail) } returns Result.failure(throwable)
                    val viewModel = QrAddViewModel(addQrUseCase = useCase)

                    viewModel.effect.test {
                        viewModel.add(detail = detail)
                        advanceUntilIdle()

                        awaitItem() shouldBe expected
                        expectNoEvents()
                    }

                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()
                }
            }
        }

        test("TC-QR-ADD-FEATURE-024 기기 저장에 실패하면 Effect 없이 진행 상태만 해제하고 같은 내용으로 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<QrDetail>()
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = detail) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = QrAddViewModel(addQrUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()

                    viewModel.add(detail = detail)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 2) { useCase(parameter = detail) }
            }
        }

        test("TC-QR-ADD-FEATURE-023 추가를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val detail = fixtureMonkey.giveMeOne<QrDetail>()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = detail) } coAnswers { completion.await() }
                val viewModel = QrAddViewModel(addQrUseCase = useCase)

                viewModel.add(detail = detail)
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("추가가 취소되면 진행 상태를 해제하고 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val firstDetail = fixtureMonkey.giveMeOne<QrDetail>()
                val secondDetail = fixtureMonkey.giveMeOne<QrDetail>()
                val useCase = mockk<AddQrUseCase>()
                coEvery { useCase(parameter = firstDetail) } throws CancellationException()
                coEvery { useCase(parameter = secondDetail) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = QrAddViewModel(addQrUseCase = useCase)

                viewModel.add(detail = firstDetail)
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()

                viewModel.add(detail = secondDetail)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = secondDetail) }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
