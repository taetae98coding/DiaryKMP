package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.search.repository.SearchTagRepository
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

class SearchTagUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 질의를 만족하는 태그가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagList = List(2) { tag() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val searchTagRepository = mockk<SearchTagRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every {
                searchTagRepository.page(account = account, query = QUERY, sort = ListSort.TITLE)
            } returns flowOf(PagingData.from(tagList))
            val useCase =
                SearchTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    searchTagRepository = searchTagRepository,
                )

            When("질의로 태그를 검색한다") {
                Then("TC-SEARCH-HOME-DOMAIN-002 현재 계정의 검색 결과를 페이지로 전달한다") {
                    val pagingData = useCase(parameter = SearchTagUseCase.Parameter(query = QUERY, sort = ListSort.TITLE)).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe tagList
                }

                Then("앞뒤 공백을 뺀 질의로 조회한다") {
                    val pagingData = useCase(parameter = SearchTagUseCase.Parameter(query = "  $QUERY  ", sort = ListSort.TITLE)).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe tagList
                }
            }
        }

        Given("빈 질의가 준비되어 있다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val searchTagRepository = mockk<SearchTagRepository>()
            val useCase =
                SearchTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    searchTagRepository = searchTagRepository,
                )

            When("빈 질의로 태그를 검색한다") {
                Then("TC-SEARCH-HOME-DOMAIN-001 결과가 비어 있고 검색을 요청하지 않는다") {
                    listOf("", " ", "   ").forEach { query ->
                        val pagingData = useCase(parameter = SearchTagUseCase.Parameter(query = query, sort = ListSort.TITLE)).first().shouldBeSuccess()

                        flowOf(pagingData).asSnapshot().shouldBeEmpty()
                    }

                    verify(exactly = 0) { searchTagRepository.page(account = any(), query = any(), sort = any()) }
                }
            }
        }

        Given("계정 조회에 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val searchTagRepository = mockk<SearchTagRepository>(relaxed = true)
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val useCase =
                SearchTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    searchTagRepository = searchTagRepository,
                )

            When("질의로 태그를 검색한다") {
                Then("TC-SEARCH-HOME-DOMAIN-009 계정 조회 실패를 전달하고 검색을 요청하지 않는다") {
                    useCase(parameter = SearchTagUseCase.Parameter(query = QUERY, sort = ListSort.TITLE))
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    verify(exactly = 0) { searchTagRepository.page(account = any(), query = any(), sort = any()) }
                }
            }
        }
    }) {
    public companion object {
        private const val QUERY: String = "여행"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
