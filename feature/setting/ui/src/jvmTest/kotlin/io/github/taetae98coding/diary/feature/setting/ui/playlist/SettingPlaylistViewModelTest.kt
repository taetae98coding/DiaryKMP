@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.playlist

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

class SettingPlaylistViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-a025dfac-509e-46c4-9dff-cdbfbcf8f10a 프록시가 제공 중이면 주소 후보를 모두 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(statusFlow = flowOf(Result.success(MusicDownloadProxyStatus.Serving(addressList = ADDRESS_LIST))))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingPlaylistUiState.Loading
                    awaitItem() shouldBe SettingPlaylistUiState.Serving(addressList = ADDRESS_LIST)
                }
            }
        }

        test("TC-2a3a91a7-024d-4cdd-9639-bfd0a8216bb8 데스크톱 앱에서는 화면에 다시 들어와도 앞서 확인한 주소 후보를 그대로 제공한다") {
            runTest(mainDispatcher) {
                val statusFlow = MutableStateFlow(Result.success<MusicDownloadProxyStatus>(MusicDownloadProxyStatus.Serving(addressList = ADDRESS_LIST)))

                viewModel(statusFlow = statusFlow).uiState.test {
                    awaitItem() shouldBe SettingPlaylistUiState.Loading
                    awaitItem() shouldBe SettingPlaylistUiState.Serving(addressList = ADDRESS_LIST)
                }

                viewModel(statusFlow = statusFlow).uiState.test {
                    awaitItem() shouldBe SettingPlaylistUiState.Loading
                    awaitItem() shouldBe SettingPlaylistUiState.Serving(addressList = ADDRESS_LIST)
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-fd314021-f83b-40a0-bc92-a9ecb2624307 제공 중이지만 주소 후보가 없으면 빈 후보를 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(statusFlow = flowOf(Result.success(MusicDownloadProxyStatus.Serving(addressList = emptyList()))))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingPlaylistUiState.Loading
                    awaitItem() shouldBe SettingPlaylistUiState.Serving(addressList = emptyList())
                }
            }
        }

        test("TC-76eecc48-b73e-477c-a51a-f11b29636ad1 프록시를 제공하지 못하면 그 상태를 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(statusFlow = flowOf(Result.success(MusicDownloadProxyStatus.Unavailable)))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingPlaylistUiState.Loading
                    awaitItem() shouldBe SettingPlaylistUiState.Unavailable
                }
            }
        }

        test("TC-961290bb-1f2f-46e5-a6c6-85b97fb23f7a TC-2d8858ab-8ffb-4838-b988-31d6a5fd4285 프록시 상태나 저장된 주소를 확인하지 못하면 확인 중 상태를 유지한다") {
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
                        awaitItem() shouldBe SettingPlaylistUiState.Loading
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-e1a76015-6bf0-4a0b-ab8a-fdb645335c90 프록시를 제공하지 않는 환경에서는 저장된 주소를 그대로 제공한다") {
            runTest(mainDispatcher) {
                val setting = MusicDownloadProxySetting(address = ADDRESS)
                val viewModel = viewModel(settingFlow = notProvidedSettingFlow(setting))

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingPlaylistUiState.Loading
                    awaitItem() shouldBe SettingPlaylistUiState.Consumer(setting = setting)
                }
            }
        }

        test("TC-42fdff61-1a02-4b02-9e0f-62a2cba2e174 저장된 주소가 없으면 비어 있는 설정을 제공한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel()

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingPlaylistUiState.Loading
                    awaitItem() shouldBe SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY)
                }
            }
        }

        test("TC-add84e31-1f7d-4282-8e59-4f2c9483eb3c 저장하면 입력한 주소가 그대로 저장된다") {
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
                    awaitItem() shouldBe SettingPlaylistUiState.Loading
                    awaitItem() shouldBe SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY)

                    viewModel.save(setting = target)
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { setUseCase(target) }
            }
        }

        test("TC-8da5ce4f-99f1-4997-a608-7cfb24d0c8f5 TC-e50e41c3-2fb9-452d-ac54-08ca1e4f20d1 비어 있거나 형태가 아닌 주소도 그대로 저장한다") {
            listOf("", "not an address").forEach { address ->
                runTest(mainDispatcher) {
                    val setting = MusicDownloadProxySetting(address = address)
                    val setUseCase = mockk<SetMusicDownloadProxySettingUseCase>()
                    coEvery { setUseCase(setting) } returns Result.success(Unit)
                    val viewModel = viewModel(setUseCase = setUseCase)

                    viewModel.effect.test {
                        viewModel.save(setting = setting)
                        advanceUntilIdle()

                        awaitItem() shouldBe SettingPlaylistEffect.SaveSucceeded
                    }

                    coVerify(exactly = 1) { setUseCase(setting) }
                }
            }
        }

        test("TC-e44d6284-841a-4d2e-89b5-a4329b0df2ee 저장하는 동안 진행 상태를 제공한다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Unit>>()
                val setUseCase = mockk<SetMusicDownloadProxySettingUseCase>()
                coEvery { setUseCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(setUseCase = setUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingPlaylistUiState.Loading
                    awaitItem() shouldBe SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY)

                    viewModel.save(setting = MusicDownloadProxySetting(address = ADDRESS))
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY, isInProgress = true)

                    completion.complete(Result.success(Unit))
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY, isInProgress = false)
                }
            }
        }

        test("TC-469b2e1f-170a-4844-9a4e-f69af633c9dd 저장 중에는 같은 저장 요청을 처리하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Unit>>()
                val setUseCase = mockk<SetMusicDownloadProxySettingUseCase>()
                coEvery { setUseCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(setUseCase = setUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingPlaylistUiState.Loading
                    awaitItem() shouldBe SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY)

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

        test("TC-7fa11d70-a289-4ad6-9e49-6755b5eac1e5 저장에 성공하면 성공을 알린다") {
            runTest(mainDispatcher) {
                val setting = MusicDownloadProxySetting(address = ADDRESS)
                val setUseCase = mockk<SetMusicDownloadProxySettingUseCase>()
                coEvery { setUseCase(setting) } returns Result.success(Unit)
                val viewModel = viewModel(setUseCase = setUseCase)

                viewModel.effect.test {
                    viewModel.save(setting = setting)
                    advanceUntilIdle()

                    awaitItem() shouldBe SettingPlaylistEffect.SaveSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-2817b1b7-db7b-4300-85ee-fb49c9b2f54c 저장에 실패하면 실패를 알리고 다시 저장할 수 있다") {
            runTest(mainDispatcher) {
                val setting = MusicDownloadProxySetting(address = ADDRESS)
                val setUseCase = mockk<SetMusicDownloadProxySettingUseCase>()
                coEvery { setUseCase(setting) } returns Result.failure(IllegalStateException("save error"))
                val viewModel = viewModel(setUseCase = setUseCase)

                viewModel.effect.test {
                    viewModel.save(setting = setting)
                    advanceUntilIdle()
                    awaitItem() shouldBe SettingPlaylistEffect.SaveFailed

                    viewModel.save(setting = setting)
                    advanceUntilIdle()
                    awaitItem() shouldBe SettingPlaylistEffect.SaveFailed
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
        ): SettingPlaylistViewModel {
            val getStatusUseCase = mockk<GetMusicDownloadProxyStatusUseCase>()
            every { getStatusUseCase(Unit) } returns statusFlow
            val getSettingUseCase = mockk<GetMusicDownloadProxySettingUseCase>()
            every { getSettingUseCase(Unit) } returns settingFlow

            return SettingPlaylistViewModel(
                getMusicDownloadProxyStatusUseCase = getStatusUseCase,
                getMusicDownloadProxySettingUseCase = getSettingUseCase,
                setMusicDownloadProxySettingUseCase = setUseCase,
            )
        }
    }
}
