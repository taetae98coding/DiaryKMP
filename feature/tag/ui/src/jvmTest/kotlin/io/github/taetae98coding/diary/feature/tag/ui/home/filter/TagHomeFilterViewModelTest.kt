@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.home.filter

import app.cash.turbine.test
import io.github.taetae98coding.diary.domain.tag.usecase.DisableTopLevelTagFilterUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.EnableTopLevelTagFilterUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetTopLevelTagFilterUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class TagHomeFilterViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-TAG-HOME-FEATURE-027 저장된 필터 선택이 꺼져 있으면 꺼진 상태를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(getTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase(flowOf(Result.success(false))))

                viewModel.uiState.test {
                    awaitItem() shouldBe TagHomeFilterUiState()
                    expectNoEvents()
                }
            }
        }

        test("TC-TAG-HOME-FEATURE-030 저장된 필터 선택이 켜져 있으면 켜진 상태를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(getTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase(flowOf(Result.success(true))))

                viewModel.uiState.test {
                    awaitItem() shouldBe TagHomeFilterUiState()
                    awaitItem() shouldBe TagHomeFilterUiState(isTopLevelOnly = true)
                }
            }
        }

        test("저장된 필터 선택이 바뀌면 바뀐 상태를 노출한다") {
            runTest(mainDispatcher) {
                val isTopLevelOnly = MutableStateFlow(false)
                val viewModel =
                    viewModel(
                        getTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase(isTopLevelOnly.map { value -> Result.success(value) }),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe TagHomeFilterUiState()

                    isTopLevelOnly.value = true

                    awaitItem() shouldBe TagHomeFilterUiState(isTopLevelOnly = true)
                }
            }
        }

        test("필터 선택 조회에 실패하면 꺼진 상태를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase(flowOf(Result.failure(IllegalStateException()))),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe TagHomeFilterUiState()
                    expectNoEvents()
                }
            }
        }

        test("TC-TAG-HOME-FEATURE-028 최상위 태그만 보기를 켜면 필터를 켜는 동작을 한 번 실행한다") {
            runTest(mainDispatcher) {
                val enableTopLevelTagFilterUseCase = enableTopLevelTagFilterUseCase()
                val viewModel = viewModel(enableTopLevelTagFilterUseCase = enableTopLevelTagFilterUseCase)

                viewModel.enableTopLevelOnly()
                runCurrent()

                coVerify(exactly = 1) { enableTopLevelTagFilterUseCase(parameter = Unit) }
            }
        }

        test("TC-TAG-HOME-FEATURE-035 필터 선택을 저장하지 못하면 바꾸기 전의 선택을 그대로 노출한다") {
            listOf(false, true).forEach { savedValue ->
                runTest(mainDispatcher) {
                    val enableTopLevelTagFilterUseCase = mockk<EnableTopLevelTagFilterUseCase>()
                    coEvery { enableTopLevelTagFilterUseCase(parameter = Unit) } returns Result.failure(IllegalStateException())
                    val disableTopLevelTagFilterUseCase = mockk<DisableTopLevelTagFilterUseCase>()
                    coEvery { disableTopLevelTagFilterUseCase(parameter = Unit) } returns Result.failure(IllegalStateException())
                    val viewModel =
                        viewModel(
                            getTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase(MutableStateFlow(savedValue).map { value -> Result.success(value) }),
                            enableTopLevelTagFilterUseCase = enableTopLevelTagFilterUseCase,
                            disableTopLevelTagFilterUseCase = disableTopLevelTagFilterUseCase,
                        )

                    viewModel.uiState.test {
                        runCurrent()
                        expectMostRecentItem() shouldBe TagHomeFilterUiState(isTopLevelOnly = savedValue)

                        if (savedValue) viewModel.disableTopLevelOnly() else viewModel.enableTopLevelOnly()
                        runCurrent()

                        expectNoEvents()
                        viewModel.uiState.value shouldBe TagHomeFilterUiState(isTopLevelOnly = savedValue)
                    }
                }
            }
        }

        test("TC-TAG-HOME-FEATURE-029 최상위 태그만 보기를 끄면 필터를 끄는 동작을 한 번 실행한다") {
            runTest(mainDispatcher) {
                val disableTopLevelTagFilterUseCase = disableTopLevelTagFilterUseCase()
                val viewModel = viewModel(disableTopLevelTagFilterUseCase = disableTopLevelTagFilterUseCase)

                viewModel.disableTopLevelOnly()
                runCurrent()

                coVerify(exactly = 1) { disableTopLevelTagFilterUseCase(parameter = Unit) }
            }
        }
    }

    public companion object {
        private fun viewModel(
            getTopLevelTagFilterUseCase: GetTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase(emptyFlow()),
            enableTopLevelTagFilterUseCase: EnableTopLevelTagFilterUseCase = enableTopLevelTagFilterUseCase(),
            disableTopLevelTagFilterUseCase: DisableTopLevelTagFilterUseCase = disableTopLevelTagFilterUseCase(),
        ): TagHomeFilterViewModel =
            TagHomeFilterViewModel(
                getTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase,
                enableTopLevelTagFilterUseCase = enableTopLevelTagFilterUseCase,
                disableTopLevelTagFilterUseCase = disableTopLevelTagFilterUseCase,
            )

        private fun getTopLevelTagFilterUseCase(flow: Flow<Result<Boolean>>): GetTopLevelTagFilterUseCase {
            val getTopLevelTagFilterUseCase = mockk<GetTopLevelTagFilterUseCase>()
            every { getTopLevelTagFilterUseCase(parameter = Unit) } returns flow

            return getTopLevelTagFilterUseCase
        }

        private fun enableTopLevelTagFilterUseCase(): EnableTopLevelTagFilterUseCase {
            val enableTopLevelTagFilterUseCase = mockk<EnableTopLevelTagFilterUseCase>()
            coEvery { enableTopLevelTagFilterUseCase(parameter = Unit) } returns Result.success(Unit)

            return enableTopLevelTagFilterUseCase
        }

        private fun disableTopLevelTagFilterUseCase(): DisableTopLevelTagFilterUseCase {
            val disableTopLevelTagFilterUseCase = mockk<DisableTopLevelTagFilterUseCase>()
            coEvery { disableTopLevelTagFilterUseCase(parameter = Unit) } returns Result.success(Unit)

            return disableTopLevelTagFilterUseCase
        }
    }
}
