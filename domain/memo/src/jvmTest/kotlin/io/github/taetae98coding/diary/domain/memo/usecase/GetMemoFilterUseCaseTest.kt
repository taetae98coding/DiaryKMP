package io.github.taetae98coding.diary.domain.memo.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoFilterRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GetMemoFilterUseCaseTest :
    BehaviorSpec({
        Given("현재 계정의 필터 선택 태그가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagList = listOf(tag(), tag())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountMemoFilterRepository = mockk<AccountMemoFilterRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every { accountMemoFilterRepository.getTagList(account = account) } returns flowOf(tagList)
            val useCase =
                GetMemoFilterUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoFilterRepository = accountMemoFilterRepository,
                )

            When("필터 선택 태그를 조회한다") {
                Then("현재 계정의 필터 선택 태그를 반환한다") {
                    useCase(parameter = Unit).test {
                        awaitItem().getOrThrow() shouldBe tagList
                        awaitComplete()
                    }

                    verify(exactly = 1) {
                        accountMemoFilterRepository.getTagList(account = account)
                    }
                }
            }
        }

        Given("현재 계정 조회에 실패하도록 준비되어 있다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountMemoFilterRepository = mockk<AccountMemoFilterRepository>(relaxed = true)
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(failure))
            val useCase =
                GetMemoFilterUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoFilterRepository = accountMemoFilterRepository,
                )

            When("필터 선택 태그를 조회한다") {
                Then("계정 조회 실패를 전달하고 저장소에 선택 태그를 요청하지 않는다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }

                    verify(exactly = 0) {
                        accountMemoFilterRepository.getTagList(account = any())
                    }
                }
            }
        }
    }) {
    public companion object {
        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
