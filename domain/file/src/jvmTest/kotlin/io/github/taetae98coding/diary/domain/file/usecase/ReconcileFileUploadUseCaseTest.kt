package io.github.taetae98coding.diary.domain.file.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ReconcileFileUploadUseCaseTest :
    BehaviorSpec({
        Given("계정 상태가 게스트가 되었다") {
            val fileUploadManager = mockk<FileUploadManager>()
            coEvery { fileUploadManager.cancelUpload() } returns Unit
            val useCase = ReconcileFileUploadUseCase(fileUploadManager = fileUploadManager)

            When("그 계정 상태를 반영한다") {
                val result = useCase(parameter = Account.Guest)

                Then("TC-FILE-STORAGE-DOMAIN-014 올리는 중이거나 다시 올릴 예정인 올리기를 모두 취소한다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { fileUploadManager.cancelUpload() }
                }
            }
        }

        Given("계정 상태가 사용자다") {
            val fileUploadManager = mockk<FileUploadManager>()
            val useCase = ReconcileFileUploadUseCase(fileUploadManager = fileUploadManager)

            When("그 계정 상태를 반영한다") {
                useCase(parameter = fixtureMonkey.giveMeOne<Account.User>())

                Then("TC-FILE-STORAGE-DOMAIN-014 올리기를 취소하지 않는다") {
                    coVerify(exactly = 0) { fileUploadManager.cancelUpload() }
                }
            }
        }
    })
