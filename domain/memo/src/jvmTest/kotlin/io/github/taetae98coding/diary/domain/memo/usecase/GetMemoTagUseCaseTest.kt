package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoTagRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

class GetMemoTagUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 메모에 연결된 태그가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val tagList = List(2) { tag() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>()
            every { accountMemoTagRepository.getTagList(account = account, memoId = memoId) } returns flowOf(tagList)
            val useCase =
                GetMemoTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoTagRepository = accountMemoTagRepository,
                )

            When("메모의 태그를 조회한다") {
                Then("TC-MEMO-DETAIL-FEATURE-033 현재 계정의 메모에 연결된 태그를 전달한다") {
                    useCase(parameter = memoId).first().shouldBeSuccess(tagList)
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>(relaxed = true)
            val useCase =
                GetMemoTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoTagRepository = accountMemoTagRepository,
                )

            When("메모의 태그를 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
