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
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk

private const val PROGRESS_ARGUMENT_INDEX = 2

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

                    coVerify(exactly = 1) { downloader.download(target = target, path = any(), onProgress = any()) }
                    eventHolder.event.test { expectNoEvents() }
                }

                Then("TC-MUSIC-DOWNLOAD-DOMAIN-010 실행할 때마다 내려받을 수단이 갖춰져 있는지 다시 확인한다") {
                    val preparer = mockk<DownloadToolPreparer>()
                    coEvery { preparer.prepare() } returns DownloadToolPrepareResult.Prepared
                    val work = work(downloadToolPreparer = preparer)

                    work.doWork(sort = ListSort.TITLE)
                    work.doWork(sort = ListSort.TITLE)

                    coVerify(exactly = 2) { preparer.prepare() }
                }
            }
        }

        Given("프록시에 연결된다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-017 알리지 않고 곧바로 그 곡의 영상을 요청하기 시작한다") {
                    val target = testDownloadTarget()
                    val eventHolder = MusicDownloadEventHolder()
                    val downloader = succeedingDownloader()
                    val work =
                        work(
                            targetList = listOf(target),
                            downloadToolPreparer = preparer(result = DownloadToolPrepareResult.Prepared),
                            musicDownloader = downloader,
                            musicDownloadEventHolder = eventHolder,
                        )

                    work.doWork(sort = ListSort.TITLE)

                    coVerify(exactly = 1) { downloader.download(target = target, path = any(), onProgress = any()) }
                    eventHolder.event.test { expectNoEvents() }
                }
            }
        }

        Given("내려받기 준비에 실패하는 조건이다") {
            When("다운로드를 실행하면") {
                val caseList =
                    listOf(
                        DownloadToolPrepareResult.NotInstalled to MusicDownloadEvent.TOOL_NOT_INSTALLED,
                        DownloadToolPrepareResult.Failed to MusicDownloadEvent.TOOL_PREPARE_FAILED,
                        DownloadToolPrepareResult.ProxyNotConfigured to MusicDownloadEvent.PROXY_NOT_CONFIGURED,
                        DownloadToolPrepareResult.ProxyUnreachable to MusicDownloadEvent.PROXY_UNREACHABLE,
                    )

                Then(
                    "TC-MUSIC-DOWNLOAD-FEATURE-013 TC-MUSIC-DOWNLOAD-FEATURE-014 TC-MUSIC-DOWNLOAD-FEATURE-015 TC-MUSIC-DOWNLOAD-FEATURE-016 " +
                        "그 조건을 알리고 어떤 곡도 받지 않는다",
                ) {
                    caseList.forEach { (result, event) ->
                        val eventHolder = MusicDownloadEventHolder()
                        val holder = MusicDownloadStateHolder()
                        val downloader = succeedingDownloader()
                        val work =
                            work(
                                downloadToolPreparer = preparer(result = result),
                                musicDownloader = downloader,
                                musicDownloadStateHolder = holder,
                                musicDownloadEventHolder = eventHolder,
                            )

                        eventHolder.event.test {
                            work.doWork(sort = ListSort.TITLE)

                            awaitItem() shouldBe event
                        }

                        holder.stateMap.value shouldBe emptyMap()
                        coVerify(exactly = 0) { downloader.download(target = any(), path = any(), onProgress = any()) }
                    }
                }

                Then("TC-MUSIC-DOWNLOAD-DOMAIN-011 대상 곡을 조회하지 않는다") {
                    caseList.forEach { (result, _) ->
                        val findMusicDownloadTargetUseCase = mockk<FindMusicDownloadTargetUseCase>()
                        val work =
                            work(
                                downloadToolPreparer = preparer(result = result),
                                findMusicDownloadTargetUseCase = findMusicDownloadTargetUseCase,
                            )

                        work.doWork(sort = ListSort.TITLE)

                        coVerify(exactly = 0) { findMusicDownloadTargetUseCase(parameter = any()) }
                    }
                }
            }
        }

        Given("받을 수 있는 곡이 목록 순서대로 여러 개 있다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-002 목록 순서대로 한 곡씩 받는다") {
                    val first = testDownloadTarget()
                    val second = testDownloadTarget()
                    val third = testDownloadTarget()
                    val targetList = mutableListOf<MusicDownloadTarget>()
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } coAnswers
                        {
                            targetList += firstArg<MusicDownloadTarget>()
                            true
                        }

                    val work = work(targetList = listOf(first, second, third), musicDownloader = downloader)

                    work.doWork(sort = ListSort.TITLE)

                    targetList shouldContainExactly listOf(first, second, third)
                }

                Then("TC-MUSIC-DOWNLOAD-FEATURE-001 아직 차례가 오지 않은 곡은 대기 상태가 된다") {
                    val first = testDownloadTarget()
                    val second = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    var pendingSnapshot: Map<MusicDownloadTarget, MusicDownloadState> = emptyMap()
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(target = first, path = any(), onProgress = any()) } coAnswers
                        {
                            pendingSnapshot = holder.stateMap.value
                            true
                        }
                    coEvery { downloader.download(target = second, path = any(), onProgress = any()) } returns true

                    val work = work(targetList = listOf(first, second), musicDownloader = downloader, musicDownloadStateHolder = holder)

                    work.doWork(sort = ListSort.TITLE)

                    pendingSnapshot[second] shouldBe MusicDownloadState.Pending
                }
            }
        }

        Given("받는 도중인 곡이 있다") {
            When("받은 만큼이 아직 알려지지 않았으면") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-018 그 곡은 백분율 없는 진행 중 상태를 갖는다") {
                    val target = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    var runningSnapshot: MusicDownloadState? = null
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } coAnswers
                        {
                            runningSnapshot = holder.stateMap.value[target]
                            true
                        }

                    val work = work(targetList = listOf(target), musicDownloader = downloader, musicDownloadStateHolder = holder)

                    work.doWork(sort = ListSort.TITLE)

                    runningSnapshot shouldBe MusicDownloadState.Running(progress = null)
                }
            }

            When("받은 만큼이 차례로 알려진 뒤 끝나면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-021 백분율을 가진 뒤로는 완료될 때까지 백분율 없는 진행 중이 되지 않는다") {
                    val target = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    val snapshotList = mutableListOf<MusicDownloadState?>()
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } coAnswers
                        {
                            val onProgress = arg<suspend (Float) -> Unit>(PROGRESS_ARGUMENT_INDEX)
                            listOf(0.25F, 0.5F, 1F).forEach { progress ->
                                onProgress(progress)
                                snapshotList += holder.stateMap.value[target]
                            }
                            true
                        }

                    val work = work(targetList = listOf(target), musicDownloader = downloader, musicDownloadStateHolder = holder)

                    work.doWork(sort = ListSort.TITLE)
                    snapshotList += holder.stateMap.value[target]

                    snapshotList shouldBe
                        listOf(
                            MusicDownloadState.Running(progress = 0.25F),
                            MusicDownloadState.Running(progress = 0.5F),
                            MusicDownloadState.Running(progress = 1F),
                            MusicDownloadState.Done,
                        )
                }
            }

            When("받은 만큼이 알려지면") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-002 TC-MUSIC-DOWNLOAD-FEATURE-019 그 곡이 받은 만큼을 진행 중 상태로 갖는다") {
                    val target = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    var runningSnapshot: MusicDownloadState? = null
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } coAnswers
                        {
                            arg<suspend (Float) -> Unit>(PROGRESS_ARGUMENT_INDEX).invoke(0.45F)
                            runningSnapshot = holder.stateMap.value[target]
                            true
                        }

                    val work = work(targetList = listOf(target), musicDownloader = downloader, musicDownloadStateHolder = holder)

                    work.doWork(sort = ListSort.TITLE)

                    runningSnapshot shouldBe MusicDownloadState.Running(progress = 0.45F)
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

                    holder.stateMap.value[target] shouldBe MusicDownloadState.Done
                }

                Then("TC-MUSIC-DOWNLOAD-DATA-001 TC-MUSIC-DOWNLOAD-DATA-008 그 곡의 영상 ID로 구분되는 파일 자리로 내려받기를 맡긴다") {
                    val target = testDownloadTarget()
                    val downloader = succeedingDownloader()
                    val fileDataSource = fileDataSource(exists = false)
                    val work = work(targetList = listOf(target), musicDownloader = downloader, appFileLocalDataSource = fileDataSource)

                    work.doWork(sort = ListSort.TITLE)

                    coVerify(exactly = 1) {
                        downloader.download(
                            target = target,
                            path = testMusicFilePath(videoId = target.videoId),
                            onProgress = any(),
                        )
                    }
                }
            }
        }

        Given("제목과 가수가 같고 영상이 다른 곡이 두 개 있다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DATA-005 TC-MUSIC-DOWNLOAD-DATA-009 영상마다 다른 이름의 파일에 저장한다") {
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

                    nameList.filter { name -> name.endsWith(".mp4") && !name.contains(".downloading.") } shouldContainExactly
                        listOf("${first.videoId}.mp4", "${second.videoId}.mp4")
                }
            }
        }

        Given("받아 둔 파일이 있는 곡이 삭제되거나 링크가 바뀐다") {
            When("그 뒤 같은 영상을 가리키는 곡으로 다운로드를 다시 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DATA-012 받아 둔 파일을 지우지 않아 다시 받지 않고 완료가 된다") {
                    val videoId = testVideoId()
                    val downloaded = testDownloadTarget(videoId = videoId)
                    val relinked = testDownloadTarget()
                    val sameVideo = testDownloadTarget(videoId = videoId)
                    val holder = MusicDownloadStateHolder()
                    val existingNameSet = mutableSetOf<String>()
                    val fileDataSource = mockk<AppFileLocalDataSource>()
                    coEvery { fileDataSource.exists(directory = any(), name = any()) } coAnswers { secondArg<String>() in existingNameSet }
                    coEvery { fileDataSource.resolve(directory = any(), name = any()) } coAnswers { "/tmp/music/${secondArg<String>()}" }
                    coEvery { fileDataSource.delete(directory = any(), name = any()) } coAnswers { existingNameSet -= secondArg<String>() }
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } coAnswers
                        {
                            existingNameSet += firstArg<MusicDownloadTarget>().videoId.toMusicFileName()
                            true
                        }
                    val downloadedTargetList = listOf(downloaded)
                    val deletedTargetList = emptyList<MusicDownloadTarget>()
                    val relinkedTargetList = listOf(relinked)
                    val sameVideoTargetList = listOf(sameVideo)
                    val targetListInRunOrder = listOf(downloadedTargetList, deletedTargetList, relinkedTargetList, sameVideoTargetList)
                    val findMusicDownloadTargetUseCase = mockk<FindMusicDownloadTargetUseCase>()
                    coEvery { findMusicDownloadTargetUseCase(parameter = any()) } returnsMany
                        targetListInRunOrder.map { targetList -> Result.success(targetList) }
                    val work =
                        work(
                            findMusicDownloadTargetUseCase = findMusicDownloadTargetUseCase,
                            musicDownloader = downloader,
                            appFileLocalDataSource = fileDataSource,
                            musicDownloadStateHolder = holder,
                        )

                    repeat(times = targetListInRunOrder.size) { work.doWork(sort = ListSort.TITLE) }

                    videoId.toMusicFileName() shouldBeIn existingNameSet
                    coVerify(exactly = 0) { fileDataSource.delete(directory = any(), name = videoId.toMusicFileName()) }
                    coVerify(exactly = 1) { downloader.download(target = downloaded, path = any(), onProgress = any()) }
                    coVerify(exactly = 0) { downloader.download(target = sameVideo, path = any(), onProgress = any()) }
                    holder.stateMap.value[sameVideo] shouldBe MusicDownloadState.Done
                }
            }
        }

        Given("같은 영상을 가리키는 곡이 두 개 있다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DATA-010 내려받기는 한 번만 요청되고 두 곡 모두 완료가 된다") {
                    val videoId = testVideoId()
                    val first = testDownloadTarget(videoId = videoId)
                    val second = testDownloadTarget(videoId = videoId)
                    val holder = MusicDownloadStateHolder()
                    val existingNameSet = mutableSetOf<String>()
                    val fileDataSource = mockk<AppFileLocalDataSource>()
                    coEvery { fileDataSource.exists(directory = any(), name = any()) } coAnswers { secondArg<String>() in existingNameSet }
                    coEvery { fileDataSource.resolve(directory = any(), name = any()) } coAnswers { "/tmp/music/${secondArg<String>()}" }
                    coEvery { fileDataSource.delete(directory = any(), name = any()) } returns Unit
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } coAnswers
                        {
                            existingNameSet += firstArg<MusicDownloadTarget>().videoId.toMusicFileName()
                            true
                        }

                    val work =
                        work(
                            targetList = listOf(first, second),
                            musicDownloader = downloader,
                            appFileLocalDataSource = fileDataSource,
                            musicDownloadStateHolder = holder,
                        )

                    work.doWork(sort = ListSort.TITLE)

                    coVerify(exactly = 1) { downloader.download(target = any(), path = any(), onProgress = any()) }
                    holder.stateMap.value[first] shouldBe MusicDownloadState.Done
                    holder.stateMap.value[second] shouldBe MusicDownloadState.Done
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

                    holder.stateMap.value[target] shouldBe MusicDownloadState.Done
                    coVerify(exactly = 0) { downloader.download(target = any(), path = any(), onProgress = any()) }
                }
            }
        }

        Given("받지 못하는 곡이다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-004 TC-MUSIC-DOWNLOAD-DATA-011 그 곡이 실패 상태가 된다") {
                    val target = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    val work = work(targetList = listOf(target), musicDownloader = failingDownloader(), musicDownloadStateHolder = holder)

                    work.doWork(sort = ListSort.TITLE)

                    holder.stateMap.value[target] shouldBe MusicDownloadState.Failed
                }

                Then("TC-MUSIC-DOWNLOAD-FEATURE-004 내려받기가 예외로 끝나도 실패 상태가 된다") {
                    val target = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } throws
                        IllegalStateException("process failed")

                    val work = work(targetList = listOf(target), musicDownloader = downloader, musicDownloadStateHolder = holder)

                    work.doWork(sort = ListSort.TITLE)

                    holder.stateMap.value[target] shouldBe MusicDownloadState.Failed
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

                    coVerify(exactly = 1) { fileDataSource.delete(directory = MUSIC_FILE_DIRECTORY, name = "${target.videoId}.mp4") }
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
                    coEvery { downloader.download(target = first, path = any(), onProgress = any()) } returns false
                    coEvery { downloader.download(target = second, path = any(), onProgress = any()) } returns true
                    coEvery { downloader.download(target = third, path = any(), onProgress = any()) } returns true

                    val work =
                        work(
                            targetList = listOf(first, second, third),
                            musicDownloader = downloader,
                            musicDownloadStateHolder = holder,
                        )

                    work.doWork(sort = ListSort.TITLE)

                    holder.stateMap.value[first] shouldBe MusicDownloadState.Failed
                    holder.stateMap.value[second] shouldBe MusicDownloadState.Done
                    holder.stateMap.value[third] shouldBe MusicDownloadState.Done
                }
            }
        }

        Given("다운로드를 한 번 실행해 한 곡은 완료되고 다른 한 곡은 실패했다") {
            When("두 곡 모두 성공하도록 설정하고 다운로드를 한 번 더 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-005 이전에 실패한 곡만 다시 받아 완료가 되고 완료된 곡은 내려받지 않은 채 완료로 남는다") {
                    val done = testDownloadTarget()
                    val failed = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    val existingNameSet = mutableSetOf<String>()
                    val fileDataSource = mockk<AppFileLocalDataSource>()
                    coEvery { fileDataSource.exists(directory = any(), name = any()) } coAnswers { secondArg<String>() in existingNameSet }
                    coEvery { fileDataSource.resolve(directory = any(), name = any()) } coAnswers { "/tmp/music/${secondArg<String>()}" }
                    coEvery { fileDataSource.delete(directory = any(), name = any()) } returns Unit
                    var isFailedTargetSucceeding = false
                    var doneStateWhileRetrying: MusicDownloadState? = null
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(target = done, path = any(), onProgress = any()) } coAnswers
                        {
                            existingNameSet += done.videoId.toMusicFileName()
                            true
                        }
                    coEvery { downloader.download(target = failed, path = any(), onProgress = any()) } coAnswers
                        {
                            if (isFailedTargetSucceeding) {
                                doneStateWhileRetrying = holder.stateMap.value[done]
                                existingNameSet += failed.videoId.toMusicFileName()
                            }
                            isFailedTargetSucceeding
                        }
                    val work =
                        work(
                            targetList = listOf(done, failed),
                            musicDownloader = downloader,
                            appFileLocalDataSource = fileDataSource,
                            musicDownloadStateHolder = holder,
                        )
                    work.doWork(sort = ListSort.TITLE)
                    holder.stateMap.value[done] shouldBe MusicDownloadState.Done
                    holder.stateMap.value[failed] shouldBe MusicDownloadState.Failed

                    isFailedTargetSucceeding = true
                    work.doWork(sort = ListSort.TITLE)

                    coVerify(exactly = 1) { downloader.download(target = done, path = any(), onProgress = any()) }
                    coVerify(exactly = 2) { downloader.download(target = failed, path = any(), onProgress = any()) }
                    doneStateWhileRetrying shouldBe MusicDownloadState.Done
                    holder.stateMap.value[done] shouldBe MusicDownloadState.Done
                    holder.stateMap.value[failed] shouldBe MusicDownloadState.Done
                }
            }
        }

        Given("다운로드를 한 번 실행해 목록 순서대로 있는 두 곡이 모두 실패했다") {
            When("첫째 곡이 진행 중인 채로 다운로드를 한 번 더 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-013 첫째 곡은 진행 중이 되고 둘째 곡은 대기로 바뀌지 않고 실패로 남는다") {
                    val first = testDownloadTarget()
                    val second = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    var isRetrying = false
                    var retrySnapshot: Map<MusicDownloadTarget, MusicDownloadState> = emptyMap()
                    val downloader = mockk<MusicDownloader>()
                    coEvery { downloader.download(target = first, path = any(), onProgress = any()) } coAnswers
                        {
                            if (isRetrying) retrySnapshot = holder.stateMap.value
                            false
                        }
                    coEvery { downloader.download(target = second, path = any(), onProgress = any()) } returns false
                    val work = work(targetList = listOf(first, second), musicDownloader = downloader, musicDownloadStateHolder = holder)
                    work.doWork(sort = ListSort.TITLE)
                    holder.stateMap.value shouldBe mapOf(first to MusicDownloadState.Failed, second to MusicDownloadState.Failed)

                    isRetrying = true
                    work.doWork(sort = ListSort.TITLE)

                    retrySnapshot[first] shouldBe MusicDownloadState.Running(progress = null)
                    retrySnapshot[second] shouldBe MusicDownloadState.Failed
                }
            }
        }

        Given("다운로드를 한 번 실행해 실패한 곡의 파일이 그 뒤 앱 전용 보관 공간에 생겼다") {
            When("다운로드를 한 번 더 실행해 그 곡의 차례가 되면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-019 내려받기를 다시 시작하지 않고 곧바로 완료가 된다") {
                    val target = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    var isFileExisting = false
                    val fileDataSource = mockk<AppFileLocalDataSource>()
                    coEvery { fileDataSource.exists(directory = any(), name = any()) } coAnswers { isFileExisting }
                    coEvery { fileDataSource.resolve(directory = any(), name = any()) } coAnswers { "/tmp/music/${secondArg<String>()}" }
                    coEvery { fileDataSource.delete(directory = any(), name = any()) } returns Unit
                    val downloader = failingDownloader()
                    val work =
                        work(
                            targetList = listOf(target),
                            musicDownloader = downloader,
                            appFileLocalDataSource = fileDataSource,
                            musicDownloadStateHolder = holder,
                        )
                    work.doWork(sort = ListSort.TITLE)
                    holder.stateMap.value[target] shouldBe MusicDownloadState.Failed

                    isFileExisting = true
                    work.doWork(sort = ListSort.TITLE)

                    holder.stateMap.value[target] shouldBe MusicDownloadState.Done
                    coVerify(exactly = 1) { downloader.download(target = target, path = any(), onProgress = any()) }
                }
            }
        }

        Given("앱을 새로 시작했고 두 곡 중 한 곡의 파일만 앱 전용 보관 공간에 있다") {
            When("다운로드를 실행하기 전에 곡의 다운로드 상태를 확인하면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-008 파일이 있는 곡을 포함해 두 곡 모두 어떤 상태도 갖지 않는다") {
                    val downloaded = testDownloadTarget()
                    val notDownloaded = testDownloadTarget()
                    val holder = MusicDownloadStateHolder()
                    val fileDataSource = mockk<AppFileLocalDataSource>()
                    coEvery { fileDataSource.exists(directory = any(), name = downloaded.videoId.toMusicFileName()) } returns true
                    coEvery { fileDataSource.exists(directory = any(), name = notDownloaded.videoId.toMusicFileName()) } returns false
                    work(
                        targetList = listOf(downloaded, notDownloaded),
                        appFileLocalDataSource = fileDataSource,
                        musicDownloadStateHolder = holder,
                    )

                    holder.stateMap.value[downloaded] shouldBe null
                    holder.stateMap.value[notDownloaded] shouldBe null
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
                    coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } coAnswers
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

                    holder.stateMap.value.keys shouldContainExactly setOf(target)
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
                    coVerify(exactly = 0) { downloader.download(target = any(), path = any(), onProgress = any()) }
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

private fun preparer(result: DownloadToolPrepareResult = DownloadToolPrepareResult.Prepared): DownloadToolPreparer {
    val preparer = mockk<DownloadToolPreparer>()
    coEvery { preparer.prepare() } returns result

    return preparer
}

private fun succeedingDownloader(): MusicDownloader {
    val downloader = mockk<MusicDownloader>()
    coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } returns true

    return downloader
}

private fun failingDownloader(): MusicDownloader {
    val downloader = mockk<MusicDownloader>()
    coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } returns false

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
