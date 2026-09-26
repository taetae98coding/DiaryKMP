package io.github.taetae98coding.diary.work.musicdownload.manager

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadEvent
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.work.musicdownload.scheduler.MusicDownloadWorkScheduler
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadEventHolder
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadStateHolder
import io.github.taetae98coding.diary.work.musicdownload.work.testDownloadTarget
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class MusicDownloadManagerImplTest :
    BehaviorSpec({
        Given("다운로드 예약기가 준비되어 있다") {
            When("다운로드를 요청한다") {
                Then("TC-PLAYLIST-HOME-FEATURE-019 지금 정렬로 목록의 곡 내려받기를 예약한다") {
                    val scheduler = mockk<MusicDownloadWorkScheduler>()
                    every { scheduler.download(sort = any()) } returns Unit
                    val manager = manager(scheduler = scheduler)

                    manager.requestDownload(sort = ListSort.RECENTLY_UPDATED)

                    verify(exactly = 1) { scheduler.download(sort = ListSort.RECENTLY_UPDATED) }
                }
            }
        }

        Given("다운로드를 요청하지 않았다") {
            When("상태와 이벤트를 관찰만 한다") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-020 내려받기를 예약하지 않아 준비와 도구 설치가 일어나지 않는다") {
                    val scheduler = mockk<MusicDownloadWorkScheduler>(relaxed = true)
                    val manager = manager(scheduler = scheduler)

                    manager.stateMap.test {
                        awaitItem()
                        cancelAndIgnoreRemainingEvents()
                    }
                    manager.event.test {
                        expectNoEvents()
                        cancelAndIgnoreRemainingEvents()
                    }

                    verify(exactly = 0) { scheduler.download(sort = any()) }
                }
            }
        }

        Given("곡의 다운로드 상태가 바뀐다") {
            When("상태를 관찰한다") {
                Then("작업이 남긴 상태를 그대로 전달한다") {
                    val target = testDownloadTarget()
                    val stateHolder = MusicDownloadStateHolder()
                    val manager = manager(stateHolder = stateHolder)

                    manager.stateMap.test {
                        awaitItem() shouldBe emptyMap()

                        stateHolder.update(target = target, state = MusicDownloadState.Running(progress = 0.62F))

                        awaitItem() shouldBe mapOf(target to MusicDownloadState.Running(progress = 0.62F))
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        Given("도구 준비 결과를 알려야 한다") {
            When("이벤트를 관찰한다") {
                Then("작업이 보낸 이벤트를 그대로 전달한다") {
                    val eventHolder = MusicDownloadEventHolder()
                    val manager = manager(eventHolder = eventHolder)

                    manager.event.test {
                        eventHolder.send(event = MusicDownloadEvent.TOOL_NOT_INSTALLED)

                        awaitItem() shouldBe MusicDownloadEvent.TOOL_NOT_INSTALLED
                        expectNoEvents()
                    }
                }
            }
        }
    }) {
    public companion object {
        private fun manager(
            scheduler: MusicDownloadWorkScheduler = mockk(relaxed = true),
            stateHolder: MusicDownloadStateHolder = MusicDownloadStateHolder(),
            eventHolder: MusicDownloadEventHolder = MusicDownloadEventHolder(),
        ): MusicDownloadManagerImpl =
            MusicDownloadManagerImpl(
                musicDownloadWorkScheduler = scheduler.apply { every { isSupported } returns true },
                musicDownloadStateHolder = stateHolder,
                musicDownloadEventHolder = eventHolder,
            )
    }
}
