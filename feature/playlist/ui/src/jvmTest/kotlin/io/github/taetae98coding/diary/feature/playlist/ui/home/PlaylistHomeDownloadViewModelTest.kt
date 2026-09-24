@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui.home

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadEvent
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.domain.playlist.usecase.GetMusicDownloadEventUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.GetMusicDownloadStateUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.RequestMusicDownloadUseCase
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
import kotlin.uuid.Uuid

class PlaylistHomeDownloadViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-PLAYLIST-HOME-FEATURE-019 다운로드를 선택하면 지금 정렬로 내려받기를 요청한다") {
            runTest(mainDispatcher) {
                val requestMusicDownloadUseCase = mockk<RequestMusicDownloadUseCase>()
                coEvery { requestMusicDownloadUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(requestMusicDownloadUseCase = requestMusicDownloadUseCase)

                viewModel.download(sort = ListSort.RECENTLY_UPDATED)
                advanceUntilIdle()

                coVerify(exactly = 1) { requestMusicDownloadUseCase(parameter = ListSort.RECENTLY_UPDATED) }
            }
        }

        test("TC-MUSIC-DOWNLOAD-FEATURE-008 다운로드를 실행하지 않으면 어떤 곡에도 상태가 없다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel()

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaylistHomeDownloadUiState()
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MUSIC-DOWNLOAD-FEATURE-013 Homebrew가 없으면 직접 설치할 것을 알린다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getMusicDownloadEventUseCase =
                            downloadEventUseCase(eventFlow = flowOf(Result.success(MusicDownloadEvent.TOOL_NOT_INSTALLED))),
                    )

                viewModel.effect.test {
                    awaitItem() shouldBe PlaylistHomeDownloadEffect.ToolNotInstalled
                    awaitComplete()
                }
            }
        }

        test("TC-MUSIC-DOWNLOAD-FEATURE-014 도구를 설치하지 못하면 준비 실패를 알린다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getMusicDownloadEventUseCase =
                            downloadEventUseCase(eventFlow = flowOf(Result.success(MusicDownloadEvent.TOOL_PREPARE_FAILED))),
                    )

                viewModel.effect.test {
                    awaitItem() shouldBe PlaylistHomeDownloadEffect.ToolPrepareFailed
                    awaitComplete()
                }
            }
        }

        test("TC-MUSIC-DOWNLOAD-FEATURE-011 알릴 것이 없으면 안내를 내보내지 않는다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel()

                viewModel.effect.test {
                    awaitComplete()
                }
            }
        }

        test("다운로드 이벤트 조회가 실패하면 안내를 내보내지 않는다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getMusicDownloadEventUseCase =
                            downloadEventUseCase(eventFlow = flowOf(Result.failure(IllegalStateException("event error")))),
                    )

                viewModel.effect.test {
                    awaitComplete()
                }
            }
        }

        test("TC-MUSIC-DOWNLOAD-DATA-002 목록을 표시하는 것만으로는 재생 정보를 조회하지 않는다") {
            runTest(mainDispatcher) {
                val requestMusicDownloadUseCase = mockk<RequestMusicDownloadUseCase>()
                val viewModel = viewModel(requestMusicDownloadUseCase = requestMusicDownloadUseCase)

                viewModel.uiState.test {
                    awaitItem()
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 0) { requestMusicDownloadUseCase(parameter = any()) }
            }
        }

        test("TC-PLAYLIST-HOME-FEATURE-020 화면을 떠났다 돌아와도 진행 중인 상태를 그대로 본다") {
            runTest(mainDispatcher) {
                val id = Uuid.random()
                val stateMapFlow = MutableStateFlow(Result.success(mapOf(id to MusicDownloadState.Running(progress = 0.62F))))
                val getMusicDownloadStateUseCase = downloadStateUseCase(stateMapFlow = stateMapFlow)

                // 화면을 떠나면 ViewModel이 사라지고, 돌아오면 새로 만들어진다.
                viewModel(getMusicDownloadStateUseCase = getMusicDownloadStateUseCase)
                    .uiState
                    .test {
                        awaitItem()
                        cancelAndIgnoreRemainingEvents()
                    }

                viewModel(getMusicDownloadStateUseCase = getMusicDownloadStateUseCase)
                    .uiState
                    .test {
                        awaitItem() shouldBe PlaylistHomeDownloadUiState()
                        awaitItem() shouldBe PlaylistHomeDownloadUiState(stateMap = mapOf(id to MusicDownloadState.Running(progress = 0.62F)))
                        cancelAndIgnoreRemainingEvents()
                    }
            }
        }

        test("다운로드 상태 조회가 실패하면 어떤 곡에도 상태를 두지 않는다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getMusicDownloadStateUseCase =
                            downloadStateUseCase(
                                stateMapFlow = flowOf(Result.failure(IllegalStateException("download state error"))),
                            ),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaylistHomeDownloadUiState()
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("대기나 진행 중인 곡이 있으면 다운로드가 진행 중이다") {
            runTest(mainDispatcher) {
                val id = Uuid.random()

                PlaylistHomeDownloadUiState().isDownloading shouldBe false
                PlaylistHomeDownloadUiState(stateMap = mapOf(id to MusicDownloadState.Pending)).isDownloading shouldBe true
                PlaylistHomeDownloadUiState(stateMap = mapOf(id to MusicDownloadState.Running(progress = 0F))).isDownloading shouldBe true
                PlaylistHomeDownloadUiState(stateMap = mapOf(id to MusicDownloadState.Done)).isDownloading shouldBe false
                PlaylistHomeDownloadUiState(stateMap = mapOf(id to MusicDownloadState.Failed)).isDownloading shouldBe false
            }
        }
    }

    private companion object {
        private fun viewModel(
            getMusicDownloadStateUseCase: GetMusicDownloadStateUseCase = downloadStateUseCase(),
            getMusicDownloadEventUseCase: GetMusicDownloadEventUseCase = downloadEventUseCase(),
            requestMusicDownloadUseCase: RequestMusicDownloadUseCase = mockk(),
        ): PlaylistHomeDownloadViewModel =
            PlaylistHomeDownloadViewModel(
                getMusicDownloadStateUseCase = getMusicDownloadStateUseCase,
                getMusicDownloadEventUseCase = getMusicDownloadEventUseCase,
                requestMusicDownloadUseCase = requestMusicDownloadUseCase,
            )

        private fun downloadEventUseCase(eventFlow: Flow<Result<MusicDownloadEvent>> = emptyFlow()): GetMusicDownloadEventUseCase {
            val getMusicDownloadEventUseCase = mockk<GetMusicDownloadEventUseCase>()
            every { getMusicDownloadEventUseCase(parameter = Unit) } returns eventFlow

            return getMusicDownloadEventUseCase
        }

        private fun downloadStateUseCase(stateMapFlow: Flow<Result<Map<Uuid, MusicDownloadState>>> = flowOf(Result.success(emptyMap()))): GetMusicDownloadStateUseCase {
            val getMusicDownloadStateUseCase = mockk<GetMusicDownloadStateUseCase>()
            every { getMusicDownloadStateUseCase(parameter = Unit) } returns stateMapFlow

            return getMusicDownloadStateUseCase
        }
    }
}
