package io.github.taetae98coding.diary.work.fileupload.manager

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.testing.file.fileUploadContent
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.fileupload.scheduler.FileUploadWorkScheduler
import io.github.taetae98coding.diary.work.fileupload.state.FileScreenViewingHolder
import io.github.taetae98coding.diary.work.fileupload.state.FileUploadEventHolder
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
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
                Then("TC-FILE-STORAGE-DOMAIN-016 고른 파일을 읽을 권한을 붙든 뒤 제목과 설명을 담아 올리기를 맡긴다") {
                    runTest {
                        val fixture = ManagerFixture(isUploading = false)
                        val content = fixtureMonkey.fileUploadContent()
                        val accountId = fixtureMonkey.giveMeOne<Uuid>()

                        fixture.manager.requestUpload(content = content, accountId = accountId)

                        coVerifyOrder {
                            fixture.fileRepository.addUploadSource(uri = content.uri)
                            fixture.scheduler.upload(request = FileUploadRequest(content = content, accountId = accountId))
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
                        val content = fixtureMonkey.fileUploadContent()
                        coEvery { fixture.fileRepository.addUploadSource(uri = content.uri) } throws SecurityException(fixtureMonkey.giveMeOne<String>())

                        shouldThrow<SecurityException> { fixture.manager.requestUpload(content = content, accountId = fixtureMonkey.giveMeOne<Uuid>()) }

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

                        fixture.manager.requestUpload(content = fixtureMonkey.fileUploadContent(), accountId = fixtureMonkey.giveMeOne<Uuid>())

                        coVerify(exactly = 0) { fixture.fileRepository.addUploadSource(uri = any()) }
                        coVerify(exactly = 0) { fixture.scheduler.upload(request = any()) }
                    }
                }
            }
        }

        Given("파일 화면을 보는지 알린다") {
            When("화면마다 보기 시작과 멈춤을 차례로 알린다") {
                Then("보고 있는 화면이 그대로 바뀐다") {
                    val fixture = ManagerFixture(isUploading = false)

                    fixture.manager.startViewing(screen = FileScreen.HOME)
                    fixture.viewingHolder.viewingScreen shouldBe FileScreen.HOME

                    fixture.manager.startViewing(screen = FileScreen.ADD)
                    fixture.manager.stopViewing(screen = FileScreen.HOME)
                    fixture.viewingHolder.viewingScreen shouldBe FileScreen.ADD

                    fixture.manager.stopViewing(screen = FileScreen.ADD)
                    fixture.viewingHolder.viewingScreen.shouldBeNull()
                }
            }
        }

        Given("올리는 중이거나 다시 올릴 예정인 파일이 있다") {
            When("확인된 계정이 아닌 다른 계정의 올리기를 취소한다") {
                Then("TC-FILE-STORAGE-DOMAIN-017 취소한 올리기가 붙들고 있던 파일을 놓고 시스템이 이어 올리던 다른 계정의 올리기도 취소한다") {
                    runTest {
                        val fixture = ManagerFixture(isUploading = true)
                        val uri = fixtureMonkey.fileUri()
                        val exceptAccountId = fixtureMonkey.giveMeOne<Uuid>()
                        coEvery { fixture.scheduler.cancel(exceptAccountId = exceptAccountId) } returns listOf(uri)

                        fixture.manager.cancelUpload(exceptAccountId = exceptAccountId)

                        coVerify(exactly = 1) { fixture.scheduler.cancel(exceptAccountId = exceptAccountId) }
                        coVerify(exactly = 1) { fixture.fileRepository.removeUploadSource(uri = uri) }
                        coVerify(exactly = 1) { fixture.fileRepository.deleteContinuedUpload(exceptAccountId = exceptAccountId) }
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
                        FileScreen.entries.forEach { screen ->
                            fixture.manager.getEvent(screen = screen).test {
                                fixture.eventHolder.send(screen = screen, event = event)

                                awaitItem() shouldBe event
                            }
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
            coEvery { deleteContinuedUpload(exceptAccountId = any()) } returns Unit
        }
    val eventHolder = FileUploadEventHolder()
    val viewingHolder = FileScreenViewingHolder()
    val manager =
        FileUploadManagerImpl(
            fileUploadWorkScheduler = scheduler,
            fileRepository = fileRepository,
            fileUploadEventHolder = eventHolder,
            fileScreenViewingHolder = viewingHolder,
        )
}
