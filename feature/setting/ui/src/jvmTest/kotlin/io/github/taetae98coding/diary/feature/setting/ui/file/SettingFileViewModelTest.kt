@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.file

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadSetting
import io.github.taetae98coding.diary.domain.file.usecase.GetFileDownloadSettingUseCase
import io.github.taetae98coding.diary.domain.file.usecase.SetFileDownloadDirectoryUseCase
import io.github.taetae98coding.diary.domain.file.usecase.UnsetFileDownloadDirectoryUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
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

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class SettingFileViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-SETTING-FILE-FEATURE-002 저장된 설정을 확인하기 전이거나 읽지 못하면 확인 중 상태를 유지한다") {
            listOf(emptyFlow(), flowOf(Result.failure<FileDownloadSetting>(IllegalStateException("read error")))).forEach { setting ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(setting = setting)

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingFileUiState.Loading
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-SETTING-FILE-FEATURE-003 TC-SETTING-FILE-FEATURE-004 저장된 설정의 고른 폴더를 그대로 제공한다") {
            listOf(null, fixtureMonkey.giveMeOne<FileDownloadDirectory>()).forEach { directory ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(setting = flowOf(Result.success(FileDownloadSetting(directory = directory))))

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingFileUiState.Loading
                        awaitItem() shouldBe SettingFileUiState.Content(directory = directory)
                    }
                }
            }
        }

        test("TC-SETTING-FILE-FEATURE-006 폴더를 고르면 곧바로 저장되고 저장된 폴더가 화면의 위치가 된다") {
            runTest(mainDispatcher) {
                val uri = fixtureMonkey.giveMeOne<String>()
                val directory = fixtureMonkey.giveMeOne<FileDownloadDirectory>().copy(uri = uri)
                val stored = MutableStateFlow(Result.success(FileDownloadSetting(directory = null)))
                val setUseCase = mockk<SetFileDownloadDirectoryUseCase>()
                coEvery { setUseCase(uri) } coAnswers {
                    stored.value = Result.success(FileDownloadSetting(directory = directory))
                    Result.success(Unit)
                }
                val viewModel = viewModel(setting = stored, setUseCase = setUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingFileUiState.Loading
                    awaitItem() shouldBe SettingFileUiState.Content(directory = null)

                    viewModel.setDirectory(uri = uri)

                    awaitItem() shouldBe SettingFileUiState.Content(directory = directory)
                }
                viewModel.effect.test {
                    advanceUntilIdle()
                    expectNoEvents()
                }
                coVerify(exactly = 1) { setUseCase(uri) }
            }
        }

        test("TC-SETTING-FILE-FEATURE-008 폴더를 쓰도록 허락받지 못하거나 저장에 실패하면 실패를 알리고 설정은 그대로 둔다") {
            runTest(mainDispatcher) {
                val directory = fixtureMonkey.giveMeOne<FileDownloadDirectory>()
                val setUseCase = mockk<SetFileDownloadDirectoryUseCase>()
                coEvery { setUseCase(any()) } returns Result.failure(SecurityException("permission denied"))
                val viewModel = viewModel(setting = flowOf(Result.success(FileDownloadSetting(directory = directory))), setUseCase = setUseCase)

                viewModel.effect.test {
                    viewModel.setDirectory(uri = fixtureMonkey.giveMeOne<String>())

                    awaitItem() shouldBe SettingFileEffect.ChangeFailed
                }
                viewModel.uiState.test {
                    awaitItem() shouldBe SettingFileUiState.Loading
                    awaitItem() shouldBe SettingFileUiState.Content(directory = directory)
                }
            }
        }

        test("TC-SETTING-FILE-FEATURE-009 기본 위치로 되돌리면 고른 폴더가 없는 설정이 화면의 위치가 된다") {
            runTest(mainDispatcher) {
                val stored = MutableStateFlow(Result.success(FileDownloadSetting(directory = fixtureMonkey.giveMeOne<FileDownloadDirectory>())))
                val unsetUseCase = mockk<UnsetFileDownloadDirectoryUseCase>()
                coEvery { unsetUseCase(Unit) } coAnswers {
                    stored.value = Result.success(FileDownloadSetting(directory = null))
                    Result.success(Unit)
                }
                val viewModel = viewModel(setting = stored, unsetUseCase = unsetUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingFileUiState.Loading
                    awaitItem()

                    viewModel.unsetDirectory()

                    awaitItem() shouldBe SettingFileUiState.Content(directory = null)
                }
                coVerify(exactly = 1) { unsetUseCase(Unit) }
            }
        }

        test("TC-SETTING-FILE-FEATURE-010 되돌리기에 실패하면 실패를 알린다") {
            runTest(mainDispatcher) {
                val unsetUseCase = mockk<UnsetFileDownloadDirectoryUseCase>()
                coEvery { unsetUseCase(Unit) } returns Result.failure(IllegalStateException("write error"))
                val viewModel = viewModel(setting = flowOf(Result.success(FileDownloadSetting(directory = fixtureMonkey.giveMeOne<FileDownloadDirectory>()))), unsetUseCase = unsetUseCase)

                viewModel.effect.test {
                    viewModel.unsetDirectory()

                    awaitItem() shouldBe SettingFileEffect.ChangeFailed
                }
            }
        }

        test("TC-SETTING-FILE-DOMAIN-001 저장하는 동안에는 폴더 고르기와 되돌리기를 다시 처리하지 않는다") {
            runTest(mainDispatcher) {
                val pending = CompletableDeferred<Result<Unit>>()
                val setUseCase = mockk<SetFileDownloadDirectoryUseCase>()
                coEvery { setUseCase(any()) } coAnswers { pending.await() }
                val unsetUseCase = mockk<UnsetFileDownloadDirectoryUseCase>()
                coEvery { unsetUseCase(Unit) } coAnswers { pending.await() }
                val viewModel = viewModel(setting = flowOf(Result.success(FileDownloadSetting(directory = null))), setUseCase = setUseCase, unsetUseCase = unsetUseCase)

                viewModel.unsetDirectory()
                advanceUntilIdle()
                viewModel.unsetDirectory()
                viewModel.setDirectory(uri = fixtureMonkey.giveMeOne<String>())
                advanceUntilIdle()

                coVerify(exactly = 1) { unsetUseCase(Unit) }
                coVerify(exactly = 0) { setUseCase(any()) }

                pending.complete(Result.success(Unit))
                advanceUntilIdle()
                viewModel.unsetDirectory()
                advanceUntilIdle()

                coVerify(exactly = 2) { unsetUseCase(Unit) }
            }
        }
    }

    private fun viewModel(
        setting: Flow<Result<FileDownloadSetting>>,
        setUseCase: SetFileDownloadDirectoryUseCase = mockk(),
        unsetUseCase: UnsetFileDownloadDirectoryUseCase = mockk(),
    ): SettingFileViewModel {
        val getUseCase = mockk<GetFileDownloadSettingUseCase>()
        every { getUseCase(Unit) } returns setting

        return SettingFileViewModel(
            setFileDownloadDirectoryUseCase = setUseCase,
            unsetFileDownloadDirectoryUseCase = unsetUseCase,
            getFileDownloadSettingUseCase = getUseCase,
        )
    }
}
