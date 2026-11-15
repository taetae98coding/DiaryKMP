package io.github.taetae98coding.diary.domain.memo.usecase

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.memo.repository.AccountCalendarFilterRepository
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

class GetCalendarFilterUseCaseTest :
    BehaviorSpec({
        Given("현재 계정의 캘린더 필터 선택 태그가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val tagList = listOf(tag(), tag())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountCalendarFilterRepository = mockk<AccountCalendarFilterRepository>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            every { accountCalendarFilterRepository.getTagList(account = account) } returns flowOf(tagList)
            val useCase =
                GetCalendarFilterUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarFilterRepository = accountCalendarFilterRepository,
                )

            When("캘린더 필터 선택 태그를 조회한다") {
                Then("현재 계정의 캘린더 필터 선택 태그를 반환한다") {
                    useCase().test {
                        awaitItem().getOrThrow() shouldBe tagList
                        awaitComplete()
                    }

                    verify(exactly = 1) {
                        accountCalendarFilterRepository.getTagList(account = account)
                    }
                }
            }
        }

        Given("현재 계정 조회에 실패하도록 준비되어 있다") {
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountCalendarFilterRepository = mockk<AccountCalendarFilterRepository>(relaxed = true)
            every { getAccountUseCase() } returns flowOf(Result.failure(failure))
            val useCase =
                GetCalendarFilterUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarFilterRepository = accountCalendarFilterRepository,
                )

            When("캘린더 필터 선택 태그를 조회한다") {
                Then("계정 조회 실패를 전달하고 저장소에 선택 태그를 요청하지 않는다") {
                    useCase().test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }

                    verify(exactly = 0) {
                        accountCalendarFilterRepository.getTagList(account = any())
                    }
                }
            }
        }
    }) {
    public companion object {
        private fun tag(): Tag =
            Arb
                .tag()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )
    }
}
