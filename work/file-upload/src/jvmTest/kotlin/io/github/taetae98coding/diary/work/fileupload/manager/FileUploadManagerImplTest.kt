package io.github.taetae98coding.diary.work.fileupload.manager

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.fileupload.scheduler.FileUploadWorkScheduler
import io.github.taetae98coding.diary.work.fileupload.state.FileHomeViewingHolder
import io.github.taetae98coding.diary.work.fileupload.state.FileUploadEventHolder
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class FileUploadManagerImplTest :
    BehaviorSpec({
        Given("올리는 중인 파일이 없다") {
            When("파일 올리기를 요청한다") {
                Then("고른 파일을 읽을 권한을 붙든 뒤 올리기를 맡긴다") {
                    runTest {
                        val fixture = ManagerFixture(isUploading = false)
                        val uri = fixtureMonkey.fileUri()
                        val accountId = fixtureMonkey.giveMeOne<Uuid>()

                        fixture.manager.requestUpload(uri = uri, accountId = accountId)

                        coVerifyOrder {
                            fixture.fileRepository.addUploadSource(uri = uri)
                            fixture.scheduler.upload(request = FileUploadRequest(uri = uri, accountId = accountId))
                        }
                    }
                }
            }
        }

        Given("Android에서 고른 파일을 제공하는 앱이 그 파일을 붙들어 두는 것을 허용하지 않는다") {
            When("그 파일로 올리기를 요청한다") {
                Then("TC-FILE-STORAGE-DATA-022 올리기를 맡기지 않고 실패를 전달한다") {
                    runTest {
                        val fixture = ManagerFixture(isUploading = false)
                        val uri = fixtureMonkey.fileUri()
                        coEvery { fixture.fileRepository.addUploadSource(uri = uri) } throws SecurityException(fixtureMonkey.giveMeOne<String>())

                        shouldThrow<SecurityException> { fixture.manager.requestUpload(uri = uri, accountId = fixtureMonkey.giveMeOne<Uuid>()) }

                        coVerify(exactly = 0) { fixture.scheduler.upload(request = any()) }
                    }
                }
            }
        }

        Given("올리는 중인 파일이 있다") {
            When("파일 올리기를 요청한다") {
                Then("TC-FILE-STORAGE-DOMAIN-009 권한을 붙들지 않고 올리기도 맡기지 않는다") {
                    runTest {
                        val fixture = ManagerFixture(isUploading = true)

                        fixture.manager.requestUpload(uri = fixtureMonkey.fileUri(), accountId = fixtureMonkey.giveMeOne<Uuid>())

                        coVerify(exactly = 0) { fixture.fileRepository.addUploadSource(uri = any()) }
                        coVerify(exactly = 0) { fixture.scheduler.upload(request = any()) }
                    }
                }
            }
        }

        Given("FileHome을 보는지 알린다") {
            When("보고 있음과 보고 있지 않음을 차례로 알린다") {
                Then("보고 있는 상태가 그대로 바뀐다") {
                    val fixture = ManagerFixture(isUploading = false)

                    fixture.manager.setFileHomeViewing(isViewing = true)
                    fixture.viewingHolder.isViewing shouldBe true

                    fixture.manager.setFileHomeViewing(isViewing = false)
                    fixture.viewingHolder.isViewing shouldBe false
                }
            }
        }

        Given("올리는 중이거나 다시 올릴 예정인 파일이 있다") {
            When("로그아웃해 올리기를 취소한다") {
                Then("TC-FILE-STORAGE-DOMAIN-014 취소한 올리기가 붙들고 있던 파일을 놓고 시스템이 이어 올리던 올리기도 취소한다") {
                    runTest {
                        val fixture = ManagerFixture(isUploading = true)
                        val uri = fixtureMonkey.fileUri()
                        coEvery { fixture.scheduler.cancel() } returns listOf(uri)

                        fixture.manager.cancelUpload()

                        coVerify(exactly = 1) { fixture.scheduler.cancel() }
                        coVerify(exactly = 1) { fixture.fileRepository.removeUploadSource(uri = uri) }
                        coVerify(exactly = 1) { fixture.fileRepository.deleteContinuedUpload() }
                    }
                }
            }
        }

        Given("올리기 상태와 결과가 있다") {
            When("상태와 결과를 받는다") {
                Then("올리기 수단의 상태와 화면에 전달할 결과를 그대로 전달한다") {
                    runTest {
                        val state = FileUploadState.Uploading(percent = fixtureMonkey.giveMeOne<Int>())
                        val fixture = ManagerFixture(isUploading = false, state = state)
                        val event = FileUploadEvent.Succeeded(fileId = fixtureMonkey.giveMeOne<Uuid>())

                        fixture.manager.state.first() shouldBe state
                        fixture.manager.event.test {
                            fixture.eventHolder.send(event = event)

                            awaitItem() shouldBe event
                        }
                    }
                }
            }
        }
    })

private class ManagerFixture(
    isUploading: Boolean,
    state: FileUploadState = FileUploadState.Idle,
) {
    val scheduler =
        mockk<FileUploadWorkScheduler> {
            every { this@mockk.state } returns flowOf(state)
            coEvery { isUploading() } returns isUploading
            coEvery { upload(request = any()) } returns Unit
        }
    val fileRepository =
        mockk<FileRepository> {
            coEvery { addUploadSource(uri = any()) } returns Unit
            coEvery { removeUploadSource(uri = any()) } returns Unit
            coEvery { deleteContinuedUpload() } returns Unit
        }
    val eventHolder = FileUploadEventHolder()
    val viewingHolder = FileHomeViewingHolder()
    val manager =
        FileUploadManagerImpl(
            fileUploadWorkScheduler = scheduler,
            fileRepository = fileRepository,
            fileUploadEventHolder = eventHolder,
            fileHomeViewingHolder = viewingHolder,
        )
}
