@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.home

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.domain.file.usecase.RefreshFileUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileHomeRefreshViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-FILE-HOME-FEATURE-028 당겨서 다시 불러오는 동안 새로고침 중이고 끝나면 새로고침 중이 아니다") {
            runTest(mainDispatcher) {
                val response = CompletableDeferred<Result<Unit>>()
                val useCase = mockk<RefreshFileUseCase>()
                coEvery { useCase(parameter = Unit) } coAnswers { response.await() }
                val viewModel = FileHomeRefreshViewModel(refreshFileUseCase = useCase)

                viewModel.refresh()
                runCurrent()
                viewModel.uiState.value.isRefreshing
                    .shouldBeTrue()

                response.complete(Result.success(Unit))
                runCurrent()
                viewModel.uiState.value.isRefreshing
                    .shouldBeFalse()
            }
        }

        test("TC-FILE-HOME-FEATURE-031 다시 불러오는 동안 다시 요청하면 다시 불러오지 않는다") {
            runTest(mainDispatcher) {
                val useCase = mockk<RefreshFileUseCase>()
                coEvery { useCase(parameter = Unit) } coAnswers { awaitCancellation() }
                val viewModel = FileHomeRefreshViewModel(refreshFileUseCase = useCase)

                viewModel.refresh()
                runCurrent()
                viewModel.refresh()
                runCurrent()

                coVerify(exactly = 1) { useCase(parameter = Unit) }
            }
        }

        test("TC-FILE-HOME-FEATURE-023 다시 불러오기에 실패하면 실패를 한 번 알리고 새로고침 중이 아니다") {
            runTest(mainDispatcher) {
                val useCase = mockk<RefreshFileUseCase>()
                coEvery { useCase(parameter = Unit) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = FileHomeRefreshViewModel(refreshFileUseCase = useCase)

                viewModel.effect.test {
                    viewModel.refresh()
                    runCurrent()

                    awaitItem() shouldBe FileHomeRefreshEffect.RefreshFailed
                    expectNoEvents()
                }
                viewModel.uiState.value.isRefreshing
                    .shouldBeFalse()
            }
        }

        test("TC-FILE-HOME-FEATURE-039 다시 불러오는 동안 올리기에 성공하면 진행 중이던 불러오기를 멈추고 진행 표시 없이 다시 불러와 처음으로 돌아갈 준비를 한다") {
            runTest(mainDispatcher) {
                val firstFileIdBefore = fixtureMonkey.giveMeOne<Uuid>()
                val useCase = mockk<RefreshFileUseCase>()
                coEvery { useCase(parameter = Unit) } coAnswers { awaitCancellation() } andThen Result.success(Unit)
                val viewModel = FileHomeRefreshViewModel(refreshFileUseCase = useCase)

                viewModel.effect.test {
                    viewModel.refresh()
                    runCurrent()
                    viewModel.refreshAfterUpload(firstFileIdBefore = firstFileIdBefore)
                    runCurrent()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { useCase(parameter = Unit) }
                viewModel.uiState.value shouldBe
                    FileHomeRefreshUiState(
                        isRefreshing = false,
                        scrollToTop = FileHomeScrollToTop.AfterFirstFileChanges(firstFileIdBefore = firstFileIdBefore),
                    )
            }
        }

        test("처음으로 돌아간 뒤에는 처음으로 돌아갈 준비를 지운다") {
            runTest(mainDispatcher) {
                val useCase = mockk<RefreshFileUseCase>()
                coEvery { useCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel = FileHomeRefreshViewModel(refreshFileUseCase = useCase)

                viewModel.refreshAfterUpload(firstFileIdBefore = fixtureMonkey.giveMeOne<Uuid>())
                runCurrent()
                viewModel.onScrolledToTop()

                viewModel.uiState.value.scrollToTop shouldBe FileHomeScrollToTop.None
            }
        }

        test("TC-FILE-HOME-FEATURE-043 계정이 바뀌어 다시 불러오기를 그만두면 실패를 알리지 않고 새로고침 중이 아니다") {
            runTest(mainDispatcher) {
                val useCase = mockk<RefreshFileUseCase>()
                coEvery { useCase(parameter = Unit) } coAnswers { awaitCancellation() }
                val viewModel = FileHomeRefreshViewModel(refreshFileUseCase = useCase)

                viewModel.effect.test {
                    viewModel.refresh()
                    runCurrent()
                    viewModel.cancelRefresh()
                    runCurrent()

                    expectNoEvents()
                }
                viewModel.uiState.value shouldBe FileHomeRefreshUiState()
            }
        }

        test("TC-FILE-HOME-FEATURE-044 올리기 성공으로 다시 불러오는 동안 당겨도 다시 불러오지 않는다") {
            runTest(mainDispatcher) {
                val useCase = mockk<RefreshFileUseCase>()
                coEvery { useCase(parameter = Unit) } coAnswers { awaitCancellation() }
                val viewModel = FileHomeRefreshViewModel(refreshFileUseCase = useCase)

                viewModel.refreshAfterUpload(firstFileIdBefore = fixtureMonkey.giveMeOne<Uuid>())
                runCurrent()
                viewModel.refresh()
                runCurrent()

                coVerify(exactly = 1) { useCase(parameter = Unit) }
                viewModel.uiState.value.isRefreshing
                    .shouldBeFalse()
            }
        }

        test("TC-FILE-HOME-FEATURE-018 올리기 뒤 다시 불러오는 동안에는 새로고침 진행을 표시하지 않는다") {
            runTest(mainDispatcher) {
                val response = CompletableDeferred<Result<Unit>>()
                val useCase = mockk<RefreshFileUseCase>()
                coEvery { useCase(parameter = Unit) } coAnswers { response.await() }
                val viewModel = FileHomeRefreshViewModel(refreshFileUseCase = useCase)

                viewModel.refreshAfterUpload(firstFileIdBefore = fixtureMonkey.giveMeOne<Uuid>())
                runCurrent()

                viewModel.uiState.value.isRefreshing
                    .shouldBeFalse()
                response.complete(Result.success(Unit))
            }
        }

        test("TC-FILE-HOME-FEATURE-049 FileAdd를 보는 동안 올리기에 성공하면 진행 표시 없이 다시 불러와 처음으로 돌아갈 준비를 한다") {
            runTest(mainDispatcher) {
                val firstFileIdBefore = fixtureMonkey.giveMeOne<Uuid>()
                val useCase = mockk<RefreshFileUseCase>()
                coEvery { useCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel = FileHomeRefreshViewModel(refreshFileUseCase = useCase)

                viewModel.refreshAfterUploadOnFileAdd(firstFileIdBefore = firstFileIdBefore)
                runCurrent()

                coVerify(exactly = 1) { useCase(parameter = Unit) }
                viewModel.uiState.value shouldBe
                    FileHomeRefreshUiState(
                        isRefreshing = false,
                        scrollToTop = FileHomeScrollToTop.AfterFirstFileChanges(firstFileIdBefore = firstFileIdBefore),
                    )
            }
        }

        test("TC-FILE-HOME-FEATURE-050 FileAdd를 보는 동안 성공한 올리기 뒤 다시 불러오기에 실패해도 알리지 않는다") {
            runTest(mainDispatcher) {
                val useCase = mockk<RefreshFileUseCase>()
                coEvery { useCase(parameter = Unit) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = FileHomeRefreshViewModel(refreshFileUseCase = useCase)

                viewModel.effect.test {
                    viewModel.refreshAfterUploadOnFileAdd(firstFileIdBefore = fixtureMonkey.giveMeOne<Uuid>())
                    runCurrent()

                    expectNoEvents()
                }
                viewModel.uiState.value.scrollToTop shouldBe FileHomeScrollToTop.None
            }
        }
    }
}
