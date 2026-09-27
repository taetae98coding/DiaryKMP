package io.github.taetae98coding.diary.domain.file.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class RefreshFileUseCaseTest :
    BehaviorSpec({
        Given("목록을 처음부터 다시 불러올 수 있다") {
            val fileRepository = mockk<FileRepository>()
            coEvery { fileRepository.refresh() } returns Unit
            val useCase = RefreshFileUseCase(fileRepository = fileRepository)

            When("목록을 다시 불러온다") {
                val result = useCase(parameter = Unit)

                Then("저장소의 목록을 한 번 다시 불러오고 성공한다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { fileRepository.refresh() }
                }
            }
        }

        Given("목록을 다시 불러오지 못한다") {
            val exception = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val fileRepository = mockk<FileRepository>()
            coEvery { fileRepository.refresh() } throws exception
            val useCase = RefreshFileUseCase(fileRepository = fileRepository)

            When("목록을 다시 불러온다") {
                val result = useCase(parameter = Unit)

                Then("실패를 그대로 전달한다") {
                    result.shouldBeFailure().message shouldBe exception.message
                }
            }
        }
    })
