@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.qr.ui.detail

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.usecase.DeleteQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.FindQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.UpdateQrUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

class QrDetailViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-QR-DETAIL-FEATURE-001 조회에 성공하면 저장된 내용과 기본 지도로 내용 상태를 만든다") {
            runTest(mainDispatcher) {
                val qr = fixtureMonkey.qr(isDeleted = false)
                val provider = fixtureMonkey.giveMeOne<MapProvider>()
                val viewModel = viewModel(qr = qr, findResult = flowOf(Result.success(qr)), provider = provider)
                collectUiState(viewModel)

                viewModel.uiState.value shouldBe QrDetailUiState.Content(id = qr.id, detail = qr.detail, defaultProvider = provider)
            }
        }

        test("TC-QR-DETAIL-FEATURE-004 조회할 수 없거나 조회에 실패하면 조회 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val qr = fixtureMonkey.qr(isDeleted = false)

                listOf<Result<Qr?>>(Result.success(null), Result.failure(IllegalStateException())).forEach { result ->
                    val viewModel = viewModel(qr = qr, findResult = flowOf(result))
                    collectUiState(viewModel)

                    viewModel.uiState.value shouldBe QrDetailUiState.Loading
                }
            }
        }

        test("TC-QR-DETAIL-FEATURE-018 저장 내용이 바뀌면 최신 저장 내용으로 내용 상태를 갱신한다") {
            runTest(mainDispatcher) {
                val qr = fixtureMonkey.qr(isDeleted = false)
                val stored = MutableStateFlow(Result.success<Qr?>(qr))
                val viewModel = viewModel(qr = qr, findResult = stored)
                collectUiState(viewModel)
                val changed = qr.copy(detail = fixtureMonkey.qrDetail())

                stored.value = Result.success(changed)
                runCurrent()

                viewModel.content().detail shouldBe changed.detail
            }
        }

        test("TC-QR-DETAIL-FEATURE-019 대상 QR이 삭제 상태가 되어도 내용 상태를 유지한다") {
            runTest(mainDispatcher) {
                val qr = fixtureMonkey.qr(isDeleted = false)
                val stored = MutableStateFlow(Result.success<Qr?>(qr))
                val viewModel = viewModel(qr = qr, findResult = stored)
                collectUiState(viewModel)

                stored.value = Result.success(qr.copy(isDeleted = true))
                runCurrent()

                viewModel.content() shouldBe QrDetailUiState.Content(id = qr.id, detail = qr.detail)
            }
        }

        test("TC-QR-DETAIL-FEATURE-007 수정에 성공하면 성공 Effect를 한 번 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val qr = fixtureMonkey.qr(isDeleted = false)
                val detail = fixtureMonkey.qrDetail()
                val updateQrUseCase = mockk<UpdateQrUseCase>()
                coEvery { updateQrUseCase(parameter = UpdateQrUseCase.Parameter(id = qr.id, detail = detail)) } returns Result.success(1)
                val viewModel = viewModel(qr = qr, updateQrUseCase = updateQrUseCase)
                collectUiState(viewModel)

                viewModel.effect.test {
                    viewModel.update(detail = detail)
                    advanceUntilIdle()

                    awaitItem() shouldBe QrDetailEffect.UpdateSucceeded
                    expectNoEvents()
                }

                viewModel.content().isUpdateInProgress.shouldBeFalse()
            }
        }

        test("TC-QR-DETAIL-FEATURE-011 QR 값이 비어 있으면 QR 내용 미입력 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val qr = fixtureMonkey.qr(isDeleted = false)
                val detail = fixtureMonkey.qrDetail(value = "")
                val updateQrUseCase = mockk<UpdateQrUseCase>()
                coEvery { updateQrUseCase(parameter = any()) } returns Result.failure(QrValueEmptyException())
                val viewModel = viewModel(qr = qr, updateQrUseCase = updateQrUseCase)

                viewModel.effect.test {
                    viewModel.update(detail = detail)
                    advanceUntilIdle()

                    awaitItem() shouldBe QrDetailEffect.ValueEmpty
                    expectNoEvents()
                }
            }
        }

        test("TC-QR-DETAIL-FEATURE-009 수정에 실패하면 Effect 없이 진행 상태만 해제한다") {
            runTest(mainDispatcher) {
                val qr = fixtureMonkey.qr(isDeleted = false)
                val updateQrUseCase = mockk<UpdateQrUseCase>()
                coEvery { updateQrUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(qr = qr, updateQrUseCase = updateQrUseCase)
                collectUiState(viewModel)

                viewModel.effect.test {
                    viewModel.update(detail = fixtureMonkey.qrDetail())
                    advanceUntilIdle()

                    expectNoEvents()
                }

                viewModel.content().isUpdateInProgress.shouldBeFalse()
            }
        }

        test("TC-QR-DETAIL-FEATURE-008 TC-QR-DETAIL-FEATURE-016 수정과 삭제를 처리하는 동안 각각의 진행 상태를 표시한다") {
            runTest(mainDispatcher) {
                val qr = fixtureMonkey.qr(isDeleted = false)
                val updateQrUseCase = mockk<UpdateQrUseCase>()
                coEvery { updateQrUseCase(parameter = any()) } coAnswers { CompletableDeferred<Result<Int>>().await() }
                val deleteQrUseCase = mockk<DeleteQrUseCase>()
                coEvery { deleteQrUseCase(parameter = qr.id) } coAnswers { CompletableDeferred<Result<Int>>().await() }
                val viewModel = viewModel(qr = qr, updateQrUseCase = updateQrUseCase, deleteQrUseCase = deleteQrUseCase)
                collectUiState(viewModel)

                viewModel.update(detail = fixtureMonkey.qrDetail())
                runCurrent()
                viewModel.content().isUpdateInProgress.shouldBeTrue()
                viewModel.content().isDeleteInProgress.shouldBeFalse()

                viewModel.delete()
                runCurrent()
                viewModel.content().isDeleteInProgress.shouldBeTrue()
            }
        }

        test("TC-QR-DETAIL-FEATURE-015 삭제에 성공하면 삭제 Effect를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val qr = fixtureMonkey.qr(isDeleted = false)
                val deleteQrUseCase = mockk<DeleteQrUseCase>()
                coEvery { deleteQrUseCase(parameter = qr.id) } returns Result.success(1)
                val viewModel = viewModel(qr = qr, deleteQrUseCase = deleteQrUseCase)

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()

                    awaitItem() shouldBe QrDetailEffect.DeleteSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-QR-DETAIL-FEATURE-017 삭제에 실패하면 Effect 없이 진행 상태만 해제하고 다시 삭제할 수 있다") {
            runTest(mainDispatcher) {
                val qr = fixtureMonkey.qr(isDeleted = false)
                val deleteQrUseCase = mockk<DeleteQrUseCase>()
                coEvery { deleteQrUseCase(parameter = qr.id) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(qr = qr, deleteQrUseCase = deleteQrUseCase)
                collectUiState(viewModel)

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()
                    viewModel.delete()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                viewModel.content().isDeleteInProgress.shouldBeFalse()
                coVerify(exactly = 2) { deleteQrUseCase(parameter = qr.id) }
            }
        }

        test("TC-QR-DETAIL-DOMAIN-006 같은 종류의 요청은 처리 중에 다시 처리하지 않고 다른 종류는 제한하지 않는다") {
            runTest(mainDispatcher) {
                val qr = fixtureMonkey.qr(isDeleted = false)
                val updateQrUseCase = mockk<UpdateQrUseCase>()
                coEvery { updateQrUseCase(parameter = any()) } coAnswers { CompletableDeferred<Result<Int>>().await() }
                val deleteQrUseCase = mockk<DeleteQrUseCase>()
                coEvery { deleteQrUseCase(parameter = qr.id) } coAnswers { CompletableDeferred<Result<Int>>().await() }
                val viewModel = viewModel(qr = qr, updateQrUseCase = updateQrUseCase, deleteQrUseCase = deleteQrUseCase)

                viewModel.update(detail = fixtureMonkey.qrDetail())
                runCurrent()
                viewModel.delete()
                runCurrent()
                viewModel.update(detail = fixtureMonkey.qrDetail())
                viewModel.delete()
                runCurrent()

                coVerify(exactly = 1) { updateQrUseCase(parameter = any()) }
                coVerify(exactly = 1) { deleteQrUseCase(parameter = qr.id) }
            }
        }

        test("TC-QR-DETAIL-DOMAIN-006 삭제를 처리하는 동안 실행한 수정은 요청된다") {
            runTest(mainDispatcher) {
                val qr = fixtureMonkey.qr(isDeleted = false)
                val updateQrUseCase = mockk<UpdateQrUseCase>()
                coEvery { updateQrUseCase(parameter = any()) } returns Result.success(1)
                val deleteQrUseCase = mockk<DeleteQrUseCase>()
                coEvery { deleteQrUseCase(parameter = qr.id) } coAnswers { CompletableDeferred<Result<Int>>().await() }
                val viewModel = viewModel(qr = qr, updateQrUseCase = updateQrUseCase, deleteQrUseCase = deleteQrUseCase)

                viewModel.delete()
                runCurrent()
                viewModel.update(detail = fixtureMonkey.qrDetail())
                runCurrent()

                coVerify(exactly = 1) { updateQrUseCase(parameter = any()) }
            }
        }
    }

    private fun TestScope.collectUiState(viewModel: QrDetailViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        runCurrent()
    }

    private fun QrDetailViewModel.content(): QrDetailUiState.Content = uiState.value.shouldBeInstanceOf<QrDetailUiState.Content>()

    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun viewModel(
            qr: Qr,
            findResult: Flow<Result<Qr?>> = flowOf(Result.success(qr)),
            provider: MapProvider? = null,
            updateQrUseCase: UpdateQrUseCase = mockk(),
            deleteQrUseCase: DeleteQrUseCase = mockk(),
        ): QrDetailViewModel {
            val findQrUseCase = mockk<FindQrUseCase>()
            every { findQrUseCase(parameter = qr.id) } returns findResult
            val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
            every { getDefaultMapProviderUseCase(parameter = Unit) } returns flowOf(if (provider == null) Result.failure(IllegalStateException()) else Result.success(provider))

            return QrDetailViewModel(
                id = qr.id,
                updateQrUseCase = updateQrUseCase,
                deleteQrUseCase = deleteQrUseCase,
                findQrUseCase = findQrUseCase,
                getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
            )
        }
    }
}
