package io.github.taetae98coding.diary.domain.memo.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

class PageMemoSelectableTagUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 메모가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val tagList = List(2) { tag() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>()
            every {
                accountMemoTagRepository.pageSelectableTag(account = account, memoId = memoId, query = any())
            } returns flowOf(PagingData.from(tagList))
            val useCase =
                PageMemoSelectableTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoTagRepository = accountMemoTagRepository,
                )

            When("검색어로 선택 목록을 조회한다") {
                val query = "  Travel  "

                Then("앞뒤 공백을 뺀 검색어로 조회하고 그 결과를 페이지로 전달한다") {
                    val pagingData =
                        useCase(parameter = PageMemoSelectableTagUseCase.Parameter(memoId = memoId, query = query))
                            .first()
                            .shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe tagList
                    verify(exactly = 1) {
                        accountMemoTagRepository.pageSelectableTag(account = account, memoId = memoId, query = "Travel")
                    }
                }
            }

            When("TC-MEMO-TAG-INPUT-DOMAIN-016 공백만 있는 검색어로 선택 목록을 조회한다") {
                Then("검색어가 없는 것과 같게 조회한다") {
                    useCase(parameter = PageMemoSelectableTagUseCase.Parameter(memoId = memoId, query = "   "))
                        .first()
                        .shouldBeSuccess()

                    verify(exactly = 1) {
                        accountMemoTagRepository.pageSelectableTag(account = account, memoId = memoId, query = "")
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>(relaxed = true)
            val useCase =
                PageMemoSelectableTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoTagRepository = accountMemoTagRepository,
                )

            When("선택 목록을 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = PageMemoSelectableTagUseCase.Parameter(memoId = fixtureMonkey.giveMeOne<Uuid>(), query = ""))
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
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
