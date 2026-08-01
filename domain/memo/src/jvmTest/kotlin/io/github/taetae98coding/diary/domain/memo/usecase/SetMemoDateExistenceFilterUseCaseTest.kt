package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.domain.memo.repository.MemoExistenceFilterRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class SetMemoDateExistenceFilterUseCaseTest :
    BehaviorSpec({
        Given("유무 필터 저장소가 준비되어 있다") {
            val memoExistenceFilterRepository = mockk<MemoExistenceFilterRepository>()
            coEvery { memoExistenceFilterRepository.updateDate(existence = any()) } just Runs
            val useCase = SetMemoDateExistenceFilterUseCase(memoExistenceFilterRepository = memoExistenceFilterRepository)

            When("TC-MEMO-HOME-DATA-010 날짜 축을 바꾼다") {
                Then("날짜 축만 기기에 저장한다") {
                    MemoFilterExistence.entries.forEach { existence ->
                        useCase(parameter = existence).shouldBeSuccess()

                        coVerify(exactly = 1) {
                            memoExistenceFilterRepository.updateDate(existence = existence)
                        }
                    }
                }
            }
        }

        Given("유무 필터 저장이 실패하도록 준비되어 있다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val memoExistenceFilterRepository = mockk<MemoExistenceFilterRepository>()
            coEvery { memoExistenceFilterRepository.updateDate(existence = any()) } throws failure
            val useCase = SetMemoDateExistenceFilterUseCase(memoExistenceFilterRepository = memoExistenceFilterRepository)

            When("날짜 축을 바꾼다") {
                Then("저장 실패를 그대로 전달한다") {
                    useCase(parameter = MemoFilterExistence.EXIST)
                        .shouldBeFailure { throwable -> throwable shouldBe failure }
                }
            }
        }
    })
