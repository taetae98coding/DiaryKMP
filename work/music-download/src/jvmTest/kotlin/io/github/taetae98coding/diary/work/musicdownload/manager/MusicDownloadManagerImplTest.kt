package io.github.taetae98coding.diary.work.musicdownload.manager

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.work.musicdownload.scheduler.MusicDownloadWorkScheduler
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadEventHolder
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadStateHolder
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.uuid.Uuid

class MusicDownloadManagerImplTest :
    BehaviorSpec({
        Given("진행 중인 다운로드가 없다") {
            When("다운로드를 요청한다") {
                Then("TC-PLAYLIST-HOME-FEATURE-019 목록의 곡 내려받기를 시작한다") {
                    val scheduler = mockk<MusicDownloadWorkScheduler>()
                    every { scheduler.download(sort = any()) } returns Unit
                    val manager = manager(scheduler = scheduler)

                    manager.requestDownload(sort = ListSort.RECENTLY_UPDATED)

                    verify(exactly = 1) { scheduler.download(sort = ListSort.RECENTLY_UPDATED) }
                }
            }
        }

        Given("받고 있는 곡이 남아 있다") {
            When("다운로드를 다시 요청한다") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-004 새 요청을 처리하지 않는다") {
                    val holder = MusicDownloadStateHolder()
                    holder.update(id = Uuid.random(), state = MusicDownloadState.Running(progress = 0.62F))
                    val scheduler = mockk<MusicDownloadWorkScheduler>()
                    val manager = manager(scheduler = scheduler, holder = holder)

                    manager.requestDownload(sort = ListSort.TITLE)

                    verify(exactly = 0) { scheduler.download(sort = any()) }
                }

                Then("TC-MUSIC-DOWNLOAD-FEATURE-006 진행 중인 곡의 상태가 그대로 남는다") {
                    val id = Uuid.random()
                    val holder = MusicDownloadStateHolder()
                    holder.update(id = id, state = MusicDownloadState.Running(progress = 0.62F))
                    val manager = manager(holder = holder)

                    manager.requestDownload(sort = ListSort.TITLE)

                    manager.stateMap.test {
                        awaitItem()[id] shouldBe MusicDownloadState.Running(progress = 0.62F)
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        Given("대기 중인 곡이 남아 있다") {
            When("다운로드를 다시 요청한다") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-004 새 요청을 처리하지 않는다") {
                    val holder = MusicDownloadStateHolder()
                    holder.submitPending(idList = listOf(Uuid.random()))
                    val scheduler = mockk<MusicDownloadWorkScheduler>()
                    val manager = manager(scheduler = scheduler, holder = holder)

                    manager.requestDownload(sort = ListSort.TITLE)

                    verify(exactly = 0) { scheduler.download(sort = any()) }
                }
            }
        }

        Given("모든 곡이 완료나 실패로 끝났다") {
            When("다운로드를 다시 요청한다") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-005 다시 시작한다") {
                    val holder = MusicDownloadStateHolder()
                    holder.update(id = Uuid.random(), state = MusicDownloadState.Done)
                    holder.update(id = Uuid.random(), state = MusicDownloadState.Failed)
                    val scheduler = mockk<MusicDownloadWorkScheduler>()
                    every { scheduler.download(sort = any()) } returns Unit
                    val manager = manager(scheduler = scheduler, holder = holder)

                    manager.requestDownload(sort = ListSort.TITLE)

                    verify(exactly = 1) { scheduler.download(sort = ListSort.TITLE) }
                }
            }
        }
    }) {
    public companion object {
        private fun manager(
            scheduler: MusicDownloadWorkScheduler = mockk(relaxed = true),
            holder: MusicDownloadStateHolder = MusicDownloadStateHolder(),
        ): MusicDownloadManagerImpl =
            MusicDownloadManagerImpl(
                musicDownloadWorkScheduler = scheduler,
                musicDownloadStateHolder = holder,
                musicDownloadEventHolder = MusicDownloadEventHolder(),
            )
    }
}
