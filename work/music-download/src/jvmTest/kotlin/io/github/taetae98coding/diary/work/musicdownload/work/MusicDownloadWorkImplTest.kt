package io.github.taetae98coding.diary.work.musicdownload.work

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.file.api.datasource.AppFileLocalDataSource
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadEvent
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicDownloadTargetUseCase
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadEventHolder
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadStateHolder
import io.github.taetae98coding.diary.work.musicdownload.tool.DownloadToolPrepareResult
import io.github.taetae98coding.diary.work.musicdownload.tool.DownloadToolPreparer
import io.github.taetae98coding.diary.work.musicdownload.tool.MusicDownloader
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlin.uuid.Uuid

private const val YT_DLP_PATH = "/opt/homebrew/bin/yt-dlp"
private const val PROGRESS_ARGUMENT_INDEX = 3

class MusicDownloadWorkImplTest :
    BehaviorSpec({
        Given("두 도구가 모두 준비되어 있다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-011 도구를 설치하지 않고 곧바로 받는다") {
                    val target = testDownloadTarget()
                    val eventHolder = MusicDownloadEventHolder()
                    val downloader = succeedingDownloader()
                    val work = work(targetList = listOf(target), musicDownloader = downloader, musicDownloadEventHolder = eventHolder)

                    work.doWork(sort = ListSort.TITLE)

                    coVerify(exactly = 1) { downloader.download(ytDlpPath = YT_DLP_PATH, link = target.link, path = any(), onProgress = any()) }
                    eventHolder.event.test { expectNoEvents() }
                }

                Then("TC-MUSIC-DOWNLOAD-DOMAIN-010 실행할 때마다 도구가 있는지 다시 확인한다") {
                    val preparer = mockk<DownloadToolPreparer>()
                    coEvery { preparer.prepare() } returns DownloadToolPrepareResult.Prepared(ytDlpPath = YT_DLP_PATH)
                    val work = work(downloadToolPreparer = preparer)

                    work.doWork(sort = ListSort.TITLE)
                    work.doWork(sort = ListSort.TITLE)

                    coVerify(exactly = 2) { preparer.prepare() }
                }
            }
        }

        Given("도구가 없고 Homebrew도 없다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-013 직접 설치할 것을 알리고 어떤 곡도 받지 않는다") {
                    val eventHolder = MusicDownloadEventHolder()
                    val holder = MusicDownloadStateHolder()
                    val downloader = succeedingDownloader()
                    val work =
                        work(
                            downloadToolPreparer = preparer(result = DownloadToolPrepareResult.NotInstalled),
                            musicDownloader = downloader,
                            musicDownloadStateHolder = holder,
                            musicDownloadEventHolder = eventHolder,
                        )

                    eventHolder.event.test {
                        work.doWork(sort = ListSort.TITLE)

                        awaitItem() shouldBe MusicDownloadEvent.TOOL_NOT_INSTALLED
                    }

                    holder.stateMap.value shouldBe emptyMap()
                    coVerify(exactly = 0) { downloader.download(ytDlpPath = any(), link = any(), path = any(), onProgress = any()) }
                }

                Then("TC-MUSIC-DOWNLOAD-DOMAIN-011 대상 곡을 조회하지 않는다") {
                    val findMusicDownloadTargetUseCase = mockk<FindMusicDownloadTargetUseCase>()
                    val work =
                        work(
                            downloadToolPreparer = preparer(result = DownloadToolPrepareResult.NotInstalled),
                            findMusicDownloadTargetUseCase = findMusicDownloadTargetUseCase,
                        )

                    work.doWork(sort = ListSort.TITLE)

                    coVerify(exactly = 0) { findMusicDownloadTargetUseCase(parameter = any()) }
                }
            }
        }

        Given("도구를 설치하지 못한다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-014 준비 실패를 알리고 어떤 곡도 받지 않는다") {
                    val eventHolder = MusicDownloadEventHolder()
                    val holder = MusicDownloadStateHolder()
                    val downloader = succeedingDownloader()
                    val work =
                        work(
                            downloadToolPreparer = preparer(result = DownloadToolPrepareResult.Failed),
                            musicDownloader = downloader,
                            musicDownloadStateHolder = holder,
                            musicDownloadEventHolder = eventHolder,
                        )

                    eventHolder.event.test {
                        work.doWork(sort = ListSort.TITLE)

                        awaitItem() shouldBe MusicDownloadEvent.TOOL_PREPARE_FAILED
                    }

                    holder.stateMap.value shouldBe emptyMap()
                    coVerify(exactly = 0) { downloader.download(ytDlpPath = any(), link = any(), path = any(), onProgress = any()) }
                }
            }
        }

        Given("받을 수 있는 곡이 목록 순서대로 여러 개 있다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-002 목록 순서대로 한 곡씩 받는다") {
                    val first = testDownloadTarget()
                    val second = testDownloadTarget()
                    val third = testDownloadTarget()
                    val linkList = mutableListOf<String>()
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(ytDlpPath = any(), link = any(), path = any(), onProgress = any()) } coAnswers
                        {
                            linkList += secondArg<String>()
                            true
                        }

                    val work = work(targetList = listOf(first, second, third), musicDownloader = downloader)

                    work.doWork(sort = ListSort.TITLE)

                    linkList shouldContainExactly listOf(first.link, second.link, third.link)
                }

                Then("TC-MUSIC-DOWNLOAD-FEATURE-001 아직 차례가 오지 않은 곡은 대기 상태가 된다") {
                    val first = testDownloadTarget()
                    val second = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    var pendingSnapshot: Map<Uuid, MusicDownloadState> = emptyMap()
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(ytDlpPath = any(), link = first.link, path = any(), onProgress = any()) } coAnswers
                        {
                            pendingSnapshot = holder.stateMap.value
                            true
                        }
                    coEvery { downloader.download(ytDlpPath = any(), link = second.link, path = any(), onProgress = any()) } returns true

                    val work = work(targetList = listOf(first, second), musicDownloader = downloader, musicDownloadStateHolder = holder)

                    work.doWork(sort = ListSort.TITLE)

                    pendingSnapshot[second.id] shouldBe MusicDownloadState.Pending
                }
            }
        }

        Given("받는 도중인 곡이 있다") {
            When("받은 만큼이 알려지면") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-002 그 곡이 받은 만큼을 진행 중 상태로 갖는다") {
                    val target = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    var runningSnapshot: MusicDownloadState? = null
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(ytDlpPath = any(), link = any(), path = any(), onProgress = any()) } coAnswers
                        {
                            arg<suspend (Float) -> Unit>(PROGRESS_ARGUMENT_INDEX).invoke(0.62F)
                            runningSnapshot = holder.stateMap.value[target.id]
                            true
                        }

                    val work = work(targetList = listOf(target), musicDownloader = downloader, musicDownloadStateHolder = holder)

                    work.doWork(sort = ListSort.TITLE)

                    runningSnapshot shouldBe MusicDownloadState.Running(progress = 0.62F)
                }
            }
        }

        Given("받을 수 있는 곡이 있다") {
            When("받기를 마치면") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-003 그 곡이 완료 상태가 된다") {
                    val target = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    val work = work(targetList = listOf(target), musicDownloadStateHolder = holder)

                    work.doWork(sort = ListSort.TITLE)

                    holder.stateMap.value[target.id] shouldBe MusicDownloadState.Done
                }

                Then("TC-MUSIC-DOWNLOAD-DATA-001 TC-MUSIC-DOWNLOAD-DATA-003 그 곡의 파일 자리로 내려받기를 맡긴다") {
                    val target = testDownloadTarget()
                    val downloader = succeedingDownloader()
                    val fileDataSource = fileDataSource(exists = false)
                    val work = work(targetList = listOf(target), musicDownloader = downloader, appFileLocalDataSource = fileDataSource)

                    work.doWork(sort = ListSort.TITLE)

                    coVerify(exactly = 1) {
                        downloader.download(
                            ytDlpPath = YT_DLP_PATH,
                            link = target.link,
                            path = "/tmp/music/${target.id}.mp4",
                            onProgress = any(),
                        )
                    }
                }
            }
        }

        Given("제목과 가수가 같고 링크가 다른 곡이 두 개 있다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DATA-005 곡마다 다른 이름의 파일에 저장한다") {
                    val first = testDownloadTarget()
                    val second = testDownloadTarget()
                    val nameList = mutableListOf<String>()
                    val fileDataSource = mockk<AppFileLocalDataSource>()
                    coEvery { fileDataSource.exists(directory = any(), name = any()) } returns false
                    coEvery { fileDataSource.resolve(directory = any(), name = any()) } coAnswers
                        {
                            nameList += secondArg<String>()
                            "/tmp/music/${secondArg<String>()}"
                        }
                    coEvery { fileDataSource.delete(directory = any(), name = any()) } returns Unit

                    val work = work(targetList = listOf(first, second), appFileLocalDataSource = fileDataSource)

                    work.doWork(sort = ListSort.TITLE)

                    nameList shouldContainExactly listOf("${first.id}.mp4", "${second.id}.mp4")
                }
            }
        }

        Given("이미 받아 둔 파일이 있는 곡이다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-003 받지 않고 곧바로 완료가 된다") {
                    val target = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    val downloader = succeedingDownloader()
                    val work =
                        work(
                            targetList = listOf(target),
                            musicDownloader = downloader,
                            appFileLocalDataSource = fileDataSource(exists = true),
                            musicDownloadStateHolder = holder,
                        )

                    work.doWork(sort = ListSort.TITLE)

                    holder.stateMap.value[target.id] shouldBe MusicDownloadState.Done
                    coVerify(exactly = 0) { downloader.download(ytDlpPath = any(), link = any(), path = any(), onProgress = any()) }
                }
            }
        }

        Given("받지 못하는 곡이다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-004 그 곡이 실패 상태가 된다") {
                    val target = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    val work = work(targetList = listOf(target), musicDownloader = failingDownloader(), musicDownloadStateHolder = holder)

                    work.doWork(sort = ListSort.TITLE)

                    holder.stateMap.value[target.id] shouldBe MusicDownloadState.Failed
                }

                Then("TC-MUSIC-DOWNLOAD-FEATURE-004 내려받기가 예외로 끝나도 실패 상태가 된다") {
                    val target = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(ytDlpPath = any(), link = any(), path = any(), onProgress = any()) } throws
                        IllegalStateException("process failed")

                    val work = work(targetList = listOf(target), musicDownloader = downloader, musicDownloadStateHolder = holder)

                    work.doWork(sort = ListSort.TITLE)

                    holder.stateMap.value[target.id] shouldBe MusicDownloadState.Failed
                }

                Then("TC-MUSIC-DOWNLOAD-DATA-006 받다 만 파일을 지운다") {
                    val target = testDownloadTarget()
                    val fileDataSource = fileDataSource(exists = false)
                    val work =
                        work(
                            targetList = listOf(target),
                            musicDownloader = failingDownloader(),
                            appFileLocalDataSource = fileDataSource,
                        )

                    work.doWork(sort = ListSort.TITLE)

                    coVerify(exactly = 1) { fileDataSource.delete(directory = MUSIC_FILE_DIRECTORY, name = "${target.id}.mp4") }
                }
            }
        }

        Given("첫 곡만 받지 못하는 곡 목록이다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-005 나머지 곡을 계속 받는다") {
                    val first = testDownloadTarget()
                    val second = testDownloadTarget()
                    val third = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(ytDlpPath = any(), link = first.link, path = any(), onProgress = any()) } returns false
                    coEvery { downloader.download(ytDlpPath = any(), link = second.link, path = any(), onProgress = any()) } returns true
                    coEvery { downloader.download(ytDlpPath = any(), link = third.link, path = any(), onProgress = any()) } returns true

                    val work =
                        work(
                            targetList = listOf(first, second, third),
                            musicDownloader = downloader,
                            musicDownloadStateHolder = holder,
                        )

                    work.doWork(sort = ListSort.TITLE)

                    holder.stateMap.value[first.id] shouldBe MusicDownloadState.Failed
                    holder.stateMap.value[second.id] shouldBe MusicDownloadState.Done
                    holder.stateMap.value[third.id] shouldBe MusicDownloadState.Done
                }
            }
        }

        Given("한 곡은 이미 받아 두었고 다른 곡은 아직 받지 못했다") {
            When("다운로드를 다시 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-005 TC-MUSIC-DOWNLOAD-DOMAIN-008 파일이 없는 곡만 다시 받는다") {
                    val downloaded = testDownloadTarget()
                    val notDownloaded = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    val downloader = succeedingDownloader()
                    val fileDataSource = mockk<AppFileLocalDataSource>()
                    coEvery { fileDataSource.exists(directory = any(), name = "${downloaded.id}.mp4") } returns true
                    coEvery { fileDataSource.exists(directory = any(), name = "${notDownloaded.id}.mp4") } returns false
                    coEvery { fileDataSource.resolve(directory = any(), name = any()) } returns "/tmp/music/file.mp4"
                    coEvery { fileDataSource.delete(directory = any(), name = any()) } returns Unit

                    val work =
                        work(
                            targetList = listOf(downloaded, notDownloaded),
                            musicDownloader = downloader,
                            appFileLocalDataSource = fileDataSource,
                            musicDownloadStateHolder = holder,
                        )

                    work.doWork(sort = ListSort.TITLE)

                    coVerify(exactly = 0) { downloader.download(ytDlpPath = any(), link = downloaded.link, path = any(), onProgress = any()) }
                    coVerify(exactly = 1) { downloader.download(ytDlpPath = any(), link = notDownloaded.link, path = any(), onProgress = any()) }
                    holder.stateMap.value[downloaded.id] shouldBe MusicDownloadState.Done
                    holder.stateMap.value[notDownloaded.id] shouldBe MusicDownloadState.Done
                }
            }
        }

        Given("다운로드를 실행한 뒤 새 곡이 저장된다") {
            When("진행 중인 다운로드가 이어지면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-006 새 곡은 진행 중인 다운로드에 끼어들지 않는다") {
                    val target = testDownloadTarget()
                    val added = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    val targetList = mutableListOf(target)
                    val findMusicDownloadTargetUseCase = mockk<FindMusicDownloadTargetUseCase>()
                    coEvery { findMusicDownloadTargetUseCase(parameter = any()) } returns Result.success(targetList.toList())
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(ytDlpPath = any(), link = any(), path = any(), onProgress = any()) } coAnswers
                        {
                            targetList += added
                            true
                        }

                    val work =
                        work(
                            findMusicDownloadTargetUseCase = findMusicDownloadTargetUseCase,
                            musicDownloader = downloader,
                            musicDownloadStateHolder = holder,
                        )

                    work.doWork(sort = ListSort.TITLE)

                    holder.stateMap.value.keys shouldContainExactly setOf(target.id)
                }
            }
        }

        Given("노출되는 곡이 하나도 없다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-007 아무것도 받지 않고 끝난다") {
                    val holder = MusicDownloadStateHolder()
                    val downloader = succeedingDownloader()
                    val work = work(targetList = emptyList(), musicDownloader = downloader, musicDownloadStateHolder = holder)

                    work.doWork(sort = ListSort.TITLE)

                    holder.stateMap.value shouldBe emptyMap()
                    coVerify(exactly = 0) { downloader.download(ytDlpPath = any(), link = any(), path = any(), onProgress = any()) }
                }
            }
        }

        Given("다운로드가 끝난 곡이 있다") {
            When("다운로드가 완료되면") {
                Then("TC-MUSIC-DOWNLOAD-DATA-007 곡을 업로드 대기 상태로 만들지 않는다") {
                    val findMusicDownloadTargetUseCase = mockk<FindMusicDownloadTargetUseCase>()
                    coEvery { findMusicDownloadTargetUseCase(parameter = any()) } returns Result.success(listOf(testDownloadTarget()))
                    val work = work(findMusicDownloadTargetUseCase = findMusicDownloadTargetUseCase)

                    work.doWork(sort = ListSort.TITLE)

                    // 곡을 읽는 것 말고는 곡에 어떤 조작도 하지 않는다.
                    coVerify(exactly = 1) { findMusicDownloadTargetUseCase(parameter = ListSort.TITLE) }
                    confirmVerified(findMusicDownloadTargetUseCase)
                }
            }
        }
    })

private fun preparer(result: DownloadToolPrepareResult = DownloadToolPrepareResult.Prepared(ytDlpPath = YT_DLP_PATH)): DownloadToolPreparer {
    val preparer = mockk<DownloadToolPreparer>()
    coEvery { preparer.prepare() } returns result

    return preparer
}

private fun succeedingDownloader(): MusicDownloader {
    val downloader = mockk<MusicDownloader>()
    coEvery { downloader.download(ytDlpPath = any(), link = any(), path = any(), onProgress = any()) } returns true

    return downloader
}

private fun failingDownloader(): MusicDownloader {
    val downloader = mockk<MusicDownloader>()
    coEvery { downloader.download(ytDlpPath = any(), link = any(), path = any(), onProgress = any()) } returns false

    return downloader
}

private fun fileDataSource(exists: Boolean): AppFileLocalDataSource {
    val fileDataSource = mockk<AppFileLocalDataSource>()
    coEvery { fileDataSource.exists(directory = any(), name = any()) } returns exists
    coEvery { fileDataSource.resolve(directory = any(), name = any()) } coAnswers { "/tmp/music/${secondArg<String>()}" }
    coEvery { fileDataSource.delete(directory = any(), name = any()) } returns Unit

    return fileDataSource
}

private fun work(
    targetList: List<MusicDownloadTarget> = listOf(testDownloadTarget()),
    downloadToolPreparer: DownloadToolPreparer = preparer(),
    findMusicDownloadTargetUseCase: FindMusicDownloadTargetUseCase =
        mockk<FindMusicDownloadTargetUseCase>().apply {
            coEvery { this@apply(parameter = any()) } returns Result.success(targetList)
        },
    musicDownloader: MusicDownloader = succeedingDownloader(),
    appFileLocalDataSource: AppFileLocalDataSource = fileDataSource(exists = false),
    musicDownloadStateHolder: MusicDownloadStateHolder = MusicDownloadStateHolder(),
    musicDownloadEventHolder: MusicDownloadEventHolder = MusicDownloadEventHolder(),
): MusicDownloadWorkImpl =
    MusicDownloadWorkImpl(
        downloadToolPreparer = downloadToolPreparer,
        musicDownloader = musicDownloader,
        findMusicDownloadTargetUseCase = findMusicDownloadTargetUseCase,
        appFileLocalDataSource = appFileLocalDataSource,
        musicDownloadStateHolder = musicDownloadStateHolder,
        musicDownloadEventHolder = musicDownloadEventHolder,
    )
