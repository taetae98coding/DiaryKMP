package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.search.repository.SearchWebRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
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

class SearchWebUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 질의를 만족하는 웹 항목이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val webList = List(2) { web() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val searchWebRepository = mockk<SearchWebRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every {
                searchWebRepository.page(account = account, query = QUERY, sort = ListSort.TITLE)
            } returns flowOf(PagingData.from(webList))
            val useCase =
                SearchWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    searchWebRepository = searchWebRepository,
                )

            When("질의로 웹 항목을 검색한다") {
                Then("TC-SEARCH-HOME-DOMAIN-002 현재 계정의 검색 결과를 페이지로 전달한다") {
                    val pagingData = useCase(parameter = SearchWebUseCase.Parameter(query = QUERY, sort = ListSort.TITLE)).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe webList
                }

                Then("앞뒤 공백을 뺀 질의로 조회한다") {
                    val pagingData = useCase(parameter = SearchWebUseCase.Parameter(query = "  $QUERY  ", sort = ListSort.TITLE)).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe webList
                }
            }
        }

        Given("로그인하지 않은 게스트 상태이고 질의를 만족하는 웹 항목이 준비되어 있다") {
            val webList = List(2) { web() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val searchWebRepository = mockk<SearchWebRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(Account.Guest))
            every {
                searchWebRepository.page(account = Account.Guest, query = QUERY, sort = ListSort.TITLE)
            } returns flowOf(PagingData.from(webList))
            val useCase =
                SearchWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    searchWebRepository = searchWebRepository,
                )

            When("질의로 웹 항목을 검색한다") {
                Then("TC-SEARCH-HOME-DOMAIN-015 게스트 계정의 검색 결과를 같은 기준으로 전달한다") {
                    val pagingData = useCase(parameter = SearchWebUseCase.Parameter(query = QUERY, sort = ListSort.TITLE)).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe webList
                }
            }
        }

        Given("빈 질의가 준비되어 있다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val searchWebRepository = mockk<SearchWebRepository>()
            val useCase =
                SearchWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    searchWebRepository = searchWebRepository,
                )

            When("빈 질의로 웹 항목을 검색한다") {
                Then("TC-SEARCH-HOME-DOMAIN-001 결과가 비어 있고 검색을 요청하지 않는다") {
                    listOf("", " ", "   ").forEach { query ->
                        val pagingData = useCase(parameter = SearchWebUseCase.Parameter(query = query, sort = ListSort.TITLE)).first().shouldBeSuccess()

                        flowOf(pagingData).asSnapshot().shouldBeEmpty()
                    }

                    verify(exactly = 0) { searchWebRepository.page(account = any(), query = any(), sort = any()) }
                }
            }
        }

        Given("계정 조회에 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val searchWebRepository = mockk<SearchWebRepository>(relaxed = true)
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val useCase =
                SearchWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    searchWebRepository = searchWebRepository,
                )

            When("질의로 웹 항목을 검색한다") {
                Then("TC-SEARCH-HOME-DOMAIN-009 계정 조회 실패를 전달하고 검색을 요청하지 않는다") {
                    useCase(parameter = SearchWebUseCase.Parameter(query = QUERY, sort = ListSort.TITLE))
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    verify(exactly = 0) { searchWebRepository.page(account = any(), query = any(), sort = any()) }
                }
            }
        }
    }) {
    public companion object {
        private const val QUERY: String = "여행"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun web(): Web =
            fixtureMonkey
                .giveMeKotlinBuilder<Web>()
                .setExp(Web::isDeleted, false)
                .setExp(Web::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Web::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
