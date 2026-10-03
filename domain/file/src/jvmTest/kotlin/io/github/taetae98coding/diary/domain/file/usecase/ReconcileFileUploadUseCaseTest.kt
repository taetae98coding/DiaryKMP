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
        Given("게스트가 확인되었다") {
            val fileUploadManager = mockk<FileUploadManager>()
            coEvery { fileUploadManager.cancelUpload(exceptAccountId = any()) } returns Unit
            val useCase = ReconcileFileUploadUseCase(fileUploadManager = fileUploadManager)

            When("그 계정 상태를 반영한다") {
                val result = useCase(parameter = Account.Guest)

                Then("TC-FILE-STORAGE-DOMAIN-017 어느 계정의 올리기도 남기지 않고 모두 취소한다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { fileUploadManager.cancelUpload(exceptAccountId = null) }
                }
            }
        }

        Given("사용자가 확인되었다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val fileUploadManager = mockk<FileUploadManager>()
            coEvery { fileUploadManager.cancelUpload(exceptAccountId = any()) } returns Unit
            val useCase = ReconcileFileUploadUseCase(fileUploadManager = fileUploadManager)

            When("그 계정 상태를 반영한다") {
                val result = useCase(parameter = account)

                Then("TC-FILE-STORAGE-DOMAIN-017 그 계정이 시작한 올리기만 남기고 다른 계정의 올리기를 취소한다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { fileUploadManager.cancelUpload(exceptAccountId = account.id) }
                }
            }
        }
    })
