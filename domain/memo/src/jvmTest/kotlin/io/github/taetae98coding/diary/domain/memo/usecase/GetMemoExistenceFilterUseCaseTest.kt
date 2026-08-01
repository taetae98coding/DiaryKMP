package io.github.taetae98coding.diary.domain.memo.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.domain.memo.repository.MemoExistenceFilterRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GetMemoExistenceFilterUseCaseTest :
    BehaviorSpec({
        Given("기기에 유지된 유무 필터가 준비되어 있다") {
            val existence =
                MemoExistenceFilter(
                    date = MemoFilterExistence.EXIST,
                    tag = MemoFilterExistence.NOT_EXIST,
                    place = MemoFilterExistence.ALL,
                )
            val memoExistenceFilterRepository = mockk<MemoExistenceFilterRepository>()
            every { memoExistenceFilterRepository.get() } returns flowOf(existence)
            val useCase = GetMemoExistenceFilterUseCase(memoExistenceFilterRepository = memoExistenceFilterRepository)

            When("유무 필터를 조회한다") {
                Then("기기에 유지된 유무 필터를 반환한다") {
                    useCase(parameter = Unit).test {
                        awaitItem().getOrThrow() shouldBe existence
                        awaitComplete()
                    }

                    verify(exactly = 1) {
                        memoExistenceFilterRepository.get()
                    }
                }
            }
        }

        Given("유무 필터 조회가 실패하도록 준비되어 있다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val memoExistenceFilterRepository = mockk<MemoExistenceFilterRepository>()
            every { memoExistenceFilterRepository.get() } returns flow { throw failure }
            val useCase = GetMemoExistenceFilterUseCase(memoExistenceFilterRepository = memoExistenceFilterRepository)

            When("유무 필터를 조회한다") {
                Then("조회 실패를 그대로 전달한다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }
                }
            }
        }
    })
