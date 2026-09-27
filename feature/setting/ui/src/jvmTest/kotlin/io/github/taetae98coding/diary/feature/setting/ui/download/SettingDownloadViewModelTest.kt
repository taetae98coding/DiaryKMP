@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.download

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxyStatus
import io.github.taetae98coding.diary.domain.playlist.usecase.GetMusicDownloadProxyStatusUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetMusicDownloadProxySettingUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.SetMusicDownloadProxySettingUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
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

private const val ADDRESS = "http://192.168.0.10:27180"
private const val OTHER_ADDRESS = "http://10.0.0.5:27180"
private val ADDRESS_LIST = listOf(ADDRESS, OTHER_ADDRESS)

class SettingDownloadViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-SETTING-DOWNLOAD-FEATURE-001 프록시가 제공 중이면 주소 후보를 모두 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(statusFlow = flowOf(Result.success(MusicDownloadProxyStatus.Serving(addressList = ADDRESS_LIST))))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingDownloadUiState.Loading
                    awaitItem() shouldBe SettingDownloadUiState.Serving(addressList = ADDRESS_LIST)
                }
            }
        }

        test("TC-SETTING-DOWNLOAD-FEATURE-020 데스크톱 앱에서는 화면에 다시 들어와도 앞서 확인한 주소 후보를 그대로 제공한다") {
            runTest(mainDispatcher) {
                val statusFlow = MutableStateFlow(Result.success<MusicDownloadProxyStatus>(MusicDownloadProxyStatus.Serving(addressList = ADDRESS_LIST)))

                viewModel(statusFlow = statusFlow).uiState.test {
                    awaitItem() shouldBe SettingDownloadUiState.Loading
                    awaitItem() shouldBe SettingDownloadUiState.Serving(addressList = ADDRESS_LIST)
                }

                viewModel(statusFlow = statusFlow).uiState.test {
                    awaitItem() shouldBe SettingDownloadUiState.Loading
                    awaitItem() shouldBe SettingDownloadUiState.Serving(addressList = ADDRESS_LIST)
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-SETTING-DOWNLOAD-FEATURE-002 제공 중이지만 주소 후보가 없으면 빈 후보를 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(statusFlow = flowOf(Result.success(MusicDownloadProxyStatus.Serving(addressList = emptyList()))))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingDownloadUiState.Loading
                    awaitItem() shouldBe SettingDownloadUiState.Serving(addressList = emptyList())
                }
            }
        }

        test("TC-SETTING-DOWNLOAD-FEATURE-003 프록시를 제공하지 못하면 그 상태를 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(statusFlow = flowOf(Result.success(MusicDownloadProxyStatus.Unavailable)))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingDownloadUiState.Loading
                    awaitItem() shouldBe SettingDownloadUiState.Unavailable
                }
            }
        }

        test("TC-SETTING-DOWNLOAD-FEATURE-004 TC-SETTING-DOWNLOAD-FEATURE-018 프록시 상태나 저장된 주소를 확인하지 못하면 확인 중 상태를 유지한다") {
            val caseList =
                listOf(
                    emptyFlow<Result<MusicDownloadProxyStatus>>() to notProvidedSettingFlow(MusicDownloadProxySetting.EMPTY),
                    flowOf(Result.failure<MusicDownloadProxyStatus>(IllegalStateException("status error"))) to notProvidedSettingFlow(MusicDownloadProxySetting.EMPTY),
                    notProvidedStatusFlow() to emptyFlow<Result<MusicDownloadProxySetting>>(),
                    notProvidedStatusFlow() to flowOf(Result.failure<MusicDownloadProxySetting>(IllegalStateException("setting error"))),
                )

            caseList.forEach { (statusFlow, settingFlow) ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(statusFlow = statusFlow, settingFlow = settingFlow)

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingDownloadUiState.Loading
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-SETTING-DOWNLOAD-FEATURE-005 프록시를 제공하지 않는 환경에서는 저장된 주소를 그대로 제공한다") {
            runTest(mainDispatcher) {
                val setting = MusicDownloadProxySetting(address = ADDRESS)
                val viewModel = viewModel(settingFlow = notProvidedSettingFlow(setting))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingDownloadUiState.Loading
                    awaitItem() shouldBe SettingDownloadUiState.Consumer(setting = setting)
                }
            }
        }

        test("TC-SETTING-DOWNLOAD-FEATURE-006 저장된 주소가 없으면 비어 있는 설정을 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel()

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingDownloadUiState.Loading
                    awaitItem() shouldBe SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY)
                }
            }
        }

        test("TC-SETTING-DOWNLOAD-FEATURE-009 저장하면 입력한 주소가 그대로 저장된다") {
            runTest(mainDispatcher) {
                val stored = MutableStateFlow(Result.success(MusicDownloadProxySetting.EMPTY))
                val setUseCase = mockk<SetMusicDownloadProxySettingUseCase>()
                val target = MusicDownloadProxySetting(address = ADDRESS)
                coEvery { setUseCase(target) } coAnswers {
                    stored.value = Result.success(target)
                    Result.success(Unit)
                }
                val viewModel = viewModel(settingFlow = stored, setUseCase = setUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingDownloadUiState.Loading
                    awaitItem() shouldBe SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY)

                    viewModel.save(setting = target)
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { setUseCase(target) }
            }
        }

        test("TC-SETTING-DOWNLOAD-DOMAIN-002 TC-SETTING-DOWNLOAD-DOMAIN-003 비어 있거나 형태가 아닌 주소도 그대로 저장한다") {
            listOf("", "not an address").forEach { address ->
                runTest(mainDispatcher) {
                    val setting = MusicDownloadProxySetting(address = address)
                    val setUseCase = mockk<SetMusicDownloadProxySettingUseCase>()
                    coEvery { setUseCase(setting) } returns Result.success(Unit)
                    val viewModel = viewModel(setUseCase = setUseCase)

                    viewModel.effect.test {
                        viewModel.save(setting = setting)
                        advanceUntilIdle()

                        awaitItem() shouldBe SettingDownloadEffect.SaveSucceeded
                    }

                    coVerify(exactly = 1) { setUseCase(setting) }
                }
            }
        }

        test("TC-SETTING-DOWNLOAD-FEATURE-010 저장하는 동안 진행 상태를 제공한다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Unit>>()
                val setUseCase = mockk<SetMusicDownloadProxySettingUseCase>()
                coEvery { setUseCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(setUseCase = setUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingDownloadUiState.Loading
                    awaitItem() shouldBe SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY)

                    viewModel.save(setting = MusicDownloadProxySetting(address = ADDRESS))
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY, isInProgress = true)

                    completion.complete(Result.success(Unit))
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY, isInProgress = false)
                }
            }
        }

        test("TC-SETTING-DOWNLOAD-FEATURE-011 저장 중에는 같은 저장 요청을 처리하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Unit>>()
                val setUseCase = mockk<SetMusicDownloadProxySettingUseCase>()
                coEvery { setUseCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(setUseCase = setUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingDownloadUiState.Loading
                    awaitItem() shouldBe SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY)

                    viewModel.save(setting = MusicDownloadProxySetting(address = ADDRESS))
                    advanceUntilIdle()
                    viewModel.save(setting = MusicDownloadProxySetting(address = ADDRESS))
                    advanceUntilIdle()

                    completion.complete(Result.success(Unit))
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { setUseCase(any()) }
            }
        }

        test("TC-SETTING-DOWNLOAD-FEATURE-012 저장에 성공하면 성공을 알린다") {
            runTest(mainDispatcher) {
                val setting = MusicDownloadProxySetting(address = ADDRESS)
                val setUseCase = mockk<SetMusicDownloadProxySettingUseCase>()
                coEvery { setUseCase(setting) } returns Result.success(Unit)
                val viewModel = viewModel(setUseCase = setUseCase)

                viewModel.effect.test {
                    viewModel.save(setting = setting)
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingDownloadEffect.SaveSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-SETTING-DOWNLOAD-FEATURE-013 저장에 실패하면 실패를 알리고 다시 저장할 수 있다") {
            runTest(mainDispatcher) {
                val setting = MusicDownloadProxySetting(address = ADDRESS)
                val setUseCase = mockk<SetMusicDownloadProxySettingUseCase>()
                coEvery { setUseCase(setting) } returns Result.failure(IllegalStateException("save error"))
                val viewModel = viewModel(setUseCase = setUseCase)

                viewModel.effect.test {
                    viewModel.save(setting = setting)
                    advanceUntilIdle()
                    awaitItem() shouldBe SettingDownloadEffect.SaveFailed

                    viewModel.save(setting = setting)
                    advanceUntilIdle()
                    awaitItem() shouldBe SettingDownloadEffect.SaveFailed
                }

                coVerify(exactly = 2) { setUseCase(setting) }
            }
        }
    }

    private companion object {
        private fun notProvidedStatusFlow(): Flow<Result<MusicDownloadProxyStatus>> = flowOf(Result.success(MusicDownloadProxyStatus.NotProvided))

        private fun notProvidedSettingFlow(setting: MusicDownloadProxySetting): Flow<Result<MusicDownloadProxySetting>> = flowOf(Result.success(setting))

        private fun viewModel(
            statusFlow: Flow<Result<MusicDownloadProxyStatus>> = notProvidedStatusFlow(),
            settingFlow: Flow<Result<MusicDownloadProxySetting>> = notProvidedSettingFlow(MusicDownloadProxySetting.EMPTY),
            setUseCase: SetMusicDownloadProxySettingUseCase = mockk(),
        ): SettingDownloadViewModel {
            val getStatusUseCase = mockk<GetMusicDownloadProxyStatusUseCase>()
            every { getStatusUseCase(Unit) } returns statusFlow
            val getSettingUseCase = mockk<GetMusicDownloadProxySettingUseCase>()
            every { getSettingUseCase(Unit) } returns settingFlow

            return SettingDownloadViewModel(
                getMusicDownloadProxyStatusUseCase = getStatusUseCase,
                getMusicDownloadProxySettingUseCase = getSettingUseCase,
                setMusicDownloadProxySettingUseCase = setUseCase,
            )
        }
    }
}
