package io.github.taetae98coding.diary.domain.file.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileUploadManagerUseCaseTest :
    BehaviorSpec({
        Given("올리기를 맡는 쪽이 올리기 상태와 결과를 알린다") {
            val fileId = fixtureMonkey.giveMeOne<Uuid>()
            val uploading = fixtureMonkey.giveMeOne<FileUploadState.Uploading>()
            val fileUploadManager = mockk<FileUploadManager>()
            every { fileUploadManager.state } returns flowOf(FileUploadState.Idle, uploading)
            every { fileUploadManager.event } returns flowOf(FileUploadEvent.Succeeded(fileId = fileId), FileUploadEvent.TooLarge, FileUploadEvent.Failed)
            every { fileUploadManager.succeededEvent } returns flowOf(FileUploadEvent.Succeeded(fileId = fileId))

            When("올리기 상태를 조회한다") {
                Then("알린 상태를 그대로 전달한다") {
                    GetFileUploadStateUseCase(fileUploadManager = fileUploadManager)(parameter = Unit).test {
                        awaitItem().getOrThrow() shouldBe FileUploadState.Idle
                        awaitItem().getOrThrow() shouldBe uploading
                        awaitComplete()
                    }
                }
            }

            When("올리기 결과를 조회한다") {
                Then("알린 결과를 그대로 전달한다") {
                    GetFileUploadEventUseCase(fileUploadManager = fileUploadManager)(parameter = Unit).test {
                        awaitItem().getOrThrow() shouldBe FileUploadEvent.Succeeded(fileId = fileId)
                        awaitItem().getOrThrow() shouldBe FileUploadEvent.TooLarge
                        awaitItem().getOrThrow() shouldBe FileUploadEvent.Failed
                        awaitComplete()
                    }
                }
            }

            When("쌓아 둔 성공을 조회한다") {
                Then("알린 성공을 그대로 전달한다") {
                    GetFileUploadSucceededEventUseCase(fileUploadManager = fileUploadManager)(parameter = Unit).test {
                        awaitItem().getOrThrow() shouldBe FileUploadEvent.Succeeded(fileId = fileId)
                        awaitComplete()
                    }
                }
            }
        }
    })
