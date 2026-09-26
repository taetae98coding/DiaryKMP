package io.github.taetae98coding.diary.domain.tag.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagLinkRepository
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

class PageTagLinkSelectableTagUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 출발 태그가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val fromTagId = fixtureMonkey.giveMeOne<Uuid>()
            val tagList = List(2) { tag() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagLinkRepository = mockk<AccountTagLinkRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every {
                accountTagLinkRepository.pageSelectableTag(account = account, fromTagId = fromTagId, query = any())
            } returns flowOf(PagingData.from(tagList))
            val useCase =
                PageTagLinkSelectableTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountTagLinkRepository = accountTagLinkRepository,
                )

            When("연결할 수 있는 태그를 페이지로 조회한다") {
                Then("TC-TAG-LINK-INPUT-DATA-001 출발 태그를 기준으로 조회한 태그를 페이지로 전달한다") {
                    val pagingData =
                        useCase(parameter = PageTagLinkSelectableTagUseCase.Parameter(fromTagId = fromTagId, query = ""))
                            .first()
                            .shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe tagList
                }
            }
        }

        Given("검색어를 다루는 현재 계정과 출발 태그가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val fromTagId = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagLinkRepository = mockk<AccountTagLinkRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every {
                accountTagLinkRepository.pageSelectableTag(account = account, fromTagId = fromTagId, query = any())
            } returns flowOf(PagingData.empty())
            val useCase =
                PageTagLinkSelectableTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountTagLinkRepository = accountTagLinkRepository,
                )

            When("앞뒤에 공백이 있는 검색어로 연결할 수 있는 태그를 페이지로 조회한다") {
                Then("앞뒤 공백을 뺀 검색어로 조회한다") {
                    useCase(parameter = PageTagLinkSelectableTagUseCase.Parameter(fromTagId = fromTagId, query = "  Travel  "))
                        .first()
                        .shouldBeSuccess()

                    verify(exactly = 1) {
                        accountTagLinkRepository.pageSelectableTag(account = account, fromTagId = fromTagId, query = "Travel")
                    }
                }
            }

            When("TC-TAG-LINK-INPUT-DOMAIN-008 공백만 있는 검색어로 연결할 수 있는 태그를 페이지로 조회한다") {
                Then("검색어가 없는 것과 같게 조회한다") {
                    useCase(parameter = PageTagLinkSelectableTagUseCase.Parameter(fromTagId = fromTagId, query = "   "))
                        .first()
                        .shouldBeSuccess()

                    verify(exactly = 1) {
                        accountTagLinkRepository.pageSelectableTag(account = account, fromTagId = fromTagId, query = "")
                    }
                }
            }
        }

        Given("계정 조회에 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val fromTagId = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagLinkRepository = mockk<AccountTagLinkRepository>(relaxed = true)
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val useCase =
                PageTagLinkSelectableTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountTagLinkRepository = accountTagLinkRepository,
                )

            When("연결할 수 있는 태그를 페이지로 조회한다") {
                Then("계정 조회 실패를 전달하고 태그 페이지를 요청하지 않는다") {
                    useCase(parameter = PageTagLinkSelectableTagUseCase.Parameter(fromTagId = fromTagId, query = ""))
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    verify(exactly = 0) {
                        accountTagLinkRepository.pageSelectableTag(account = any(), fromTagId = any(), query = any())
                    }
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
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
