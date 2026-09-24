@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.browser

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.domain.browser.usecase.FindChromeProfileListUseCase
import io.github.taetae98coding.diary.domain.browser.usecase.GetChromeSessionProfileDirectoryUseCase
import io.github.taetae98coding.diary.domain.browser.usecase.SelectChromeSessionProfileUseCase
import io.github.taetae98coding.diary.domain.browser.usecase.UnselectChromeSessionProfileUseCase
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private val profileA = ChromeProfile(directory = "Default", name = "TaeJong")
private val profileB = ChromeProfile(directory = "Profile 1", name = "Work")
private val profileList = listOf(profileA, profileB)

class SettingBrowserViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-SETTING-BROWSER-FEATURE-007 저장된 선택을 확인하기 전에는 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(directoryFlow = emptyFlow())

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingBrowserUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-SETTING-BROWSER-FEATURE-007 선택을 읽지 못하면 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(directoryFlow = flowOf(Result.failure(IllegalStateException("read error"))))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingBrowserUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-SETTING-BROWSER-FEATURE-002 TC-SETTING-BROWSER-FEATURE-003 프로필 목록과 저장된 선택을 제공한다") {
            listOf("", profileA.directory, profileB.directory).forEach { directory ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(directoryFlow = flowOf(Result.success(directory)))

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingBrowserUiState.Loading
                        awaitItem() shouldBe SettingBrowserUiState.Loaded(profileList = profileList, selectedProfileDirectory = directory)
                    }
                }
            }
        }

        test("TC-SETTING-BROWSER-FEATURE-008 Chrome 프로필이 없으면 빈 목록과 선택 안 함을 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(directoryFlow = flowOf(Result.success("")), profileList = emptyList())

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingBrowserUiState.Loading
                    awaitItem() shouldBe SettingBrowserUiState.Loaded(profileList = emptyList(), selectedProfileDirectory = "")
                }
            }
        }

        test("TC-SETTING-BROWSER-FEATURE-009 프로필 목록을 읽지 못하면 조회 실패와 선택 안 함을 제공한다") {
            listOf("", profileA.directory).forEach { directory ->
                runTest(mainDispatcher) {
                    val viewModel =
                        viewModel(
                            directoryFlow = flowOf(Result.success(directory)),
                            profileListResult = Result.failure(IllegalStateException("local state unreadable")),
                        )

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingBrowserUiState.Loading
                        awaitItem() shouldBe
                            SettingBrowserUiState.Loaded(profileList = emptyList(), selectedProfileDirectory = "", isProfileListUnavailable = true)
                    }
                }
            }
        }

        test("TC-SETTING-BROWSER-DOMAIN-001 목록에 없는 프로필이 저장되어 있으면 선택 안 함으로 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(directoryFlow = flowOf(Result.success("Profile 9")), profileList = listOf(profileA))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingBrowserUiState.Loading
                    awaitItem() shouldBe SettingBrowserUiState.Loaded(profileList = listOf(profileA), selectedProfileDirectory = "")
                }
            }
        }

        test("TC-SETTING-BROWSER-DOMAIN-002 목록에 없던 프로필이 다시 나타나면 이전 선택을 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(directoryFlow = flowOf(Result.success(profileB.directory)), profileList = profileList)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingBrowserUiState.Loading
                    awaitItem() shouldBe SettingBrowserUiState.Loaded(profileList = profileList, selectedProfileDirectory = profileB.directory)
                }
            }
        }

        test("TC-SETTING-BROWSER-FEATURE-004 선택하지 않은 프로필을 고르면 저장되고 선택 상태가 옮겨진다") {
            runTest(mainDispatcher) {
                val stored = MutableStateFlow<Result<String>>(Result.success(""))
                val selectUseCase = mockk<SelectChromeSessionProfileUseCase>()
                coEvery { selectUseCase(profileA.directory) } coAnswers {
                    stored.value = Result.success(profileA.directory)
                    Result.success(Unit)
                }
                val viewModel = viewModel(directoryFlow = stored, selectUseCase = selectUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingBrowserUiState.Loading
                    awaitItem() shouldBe SettingBrowserUiState.Loaded(profileList = profileList, selectedProfileDirectory = "")

                    viewModel.selectProfile(directory = profileA.directory)
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingBrowserUiState.Loaded(profileList = profileList, selectedProfileDirectory = profileA.directory)
                }

                coVerify(exactly = 1) { selectUseCase(profileA.directory) }
            }
        }

        test("TC-SETTING-BROWSER-FEATURE-004 선택 안 함을 고르면 없음이 저장되고 선택 상태가 옮겨진다") {
            runTest(mainDispatcher) {
                val stored = MutableStateFlow<Result<String>>(Result.success(profileB.directory))
                val unselectUseCase = mockk<UnselectChromeSessionProfileUseCase>()
                coEvery { unselectUseCase(Unit) } coAnswers {
                    stored.value = Result.success("")
                    Result.success(Unit)
                }
                val viewModel = viewModel(directoryFlow = stored, unselectUseCase = unselectUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingBrowserUiState.Loading
                    awaitItem() shouldBe SettingBrowserUiState.Loaded(profileList = profileList, selectedProfileDirectory = profileB.directory)

                    viewModel.unselectProfile()
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingBrowserUiState.Loaded(profileList = profileList, selectedProfileDirectory = "")
                }

                coVerify(exactly = 1) { unselectUseCase(Unit) }
            }
        }

        test("TC-SETTING-BROWSER-FEATURE-005 이미 선택된 항목을 다시 고르면 저장을 요청하지 않는다") {
            runTest(mainDispatcher) {
                val selectUseCase = mockk<SelectChromeSessionProfileUseCase>()
                val unselectUseCase = mockk<UnselectChromeSessionProfileUseCase>()
                val viewModel =
                    viewModel(
                        directoryFlow = MutableStateFlow(Result.success(profileA.directory)),
                        selectUseCase = selectUseCase,
                        unselectUseCase = unselectUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingBrowserUiState.Loading
                    awaitItem() shouldBe SettingBrowserUiState.Loaded(profileList = profileList, selectedProfileDirectory = profileA.directory)

                    viewModel.selectProfile(directory = profileA.directory)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 0) { selectUseCase(any()) }
                coVerify(exactly = 0) { unselectUseCase(Unit) }
            }
        }

        test("TC-SETTING-BROWSER-FEATURE-006 저장되지 않으면 선택 상태가 옮겨지지 않는다") {
            runTest(mainDispatcher) {
                val selectUseCase = mockk<SelectChromeSessionProfileUseCase>()
                coEvery { selectUseCase(profileA.directory) } returns Result.failure(IllegalStateException("save error"))
                val viewModel = viewModel(directoryFlow = MutableStateFlow(Result.success("")), selectUseCase = selectUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingBrowserUiState.Loading
                    awaitItem() shouldBe SettingBrowserUiState.Loaded(profileList = profileList, selectedProfileDirectory = "")

                    viewModel.selectProfile(directory = profileA.directory)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }
    }

    private fun viewModel(
        directoryFlow: Flow<Result<String>>,
        profileList: List<ChromeProfile> = io.github.taetae98coding.diary.feature.setting.ui.browser.profileList,
        profileListResult: Result<List<ChromeProfile>> = Result.success(profileList),
        selectUseCase: SelectChromeSessionProfileUseCase = mockk(),
        unselectUseCase: UnselectChromeSessionProfileUseCase = mockk(),
    ): SettingBrowserViewModel {
        val getDirectoryUseCase = mockk<GetChromeSessionProfileDirectoryUseCase>()
        every { getDirectoryUseCase(Unit) } returns directoryFlow
        val findProfileListUseCase = mockk<FindChromeProfileListUseCase>()
        coEvery { findProfileListUseCase(Unit) } returns profileListResult

        return SettingBrowserViewModel(
            getChromeSessionProfileDirectoryUseCase = getDirectoryUseCase,
            findChromeProfileListUseCase = findProfileListUseCase,
            selectChromeSessionProfileUseCase = selectUseCase,
            unselectChromeSessionProfileUseCase = unselectUseCase,
        )
    }
}
