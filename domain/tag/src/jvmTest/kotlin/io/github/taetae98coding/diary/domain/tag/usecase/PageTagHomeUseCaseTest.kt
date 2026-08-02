package io.github.taetae98coding.diary.domain.tag.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagFilterRepository
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

class PageTagHomeUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 최상위 태그, 그 외 태그가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val topLevelTagList = List(2) { tag() }
            val tagList = topLevelTagList + tag()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountTagRepository = mockk<AccountTagRepository>()
            every { accountTagRepository.page(account = account, query = "", sort = ListSort.TITLE) } returns flowOf(PagingData.from(tagList))
            every { accountTagRepository.pageTopLevel(account = account, sort = ListSort.TITLE) } returns flowOf(PagingData.from(topLevelTagList))

            When("필터가 꺼져 있다") {
                val accountTagFilterRepository = mockk<AccountTagFilterRepository>()
                every { accountTagFilterRepository.getTopLevelOnly(account = account) } returns flowOf(false)
                val useCase =
                    PageTagHomeUseCase(
                        getAccountUseCase = getAccountUseCase,
                        accountTagRepository = accountTagRepository,
                        accountTagFilterRepository = accountTagFilterRepository,
                    )

                Then("TC-TAG-HOME-FEATURE-029 노출 대상 태그 전체를 페이지로 전달한다") {
                    val pagingData = useCase(parameter = ListSort.TITLE).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe tagList
                }
            }

            When("필터가 켜져 있다") {
                val accountTagFilterRepository = mockk<AccountTagFilterRepository>()
                every { accountTagFilterRepository.getTopLevelOnly(account = account) } returns flowOf(true)
                val useCase =
                    PageTagHomeUseCase(
                        getAccountUseCase = getAccountUseCase,
                        accountTagRepository = accountTagRepository,
                        accountTagFilterRepository = accountTagFilterRepository,
                    )

                Then("TC-TAG-HOME-FEATURE-028 최상위 태그만 페이지로 전달한다") {
                    val pagingData = useCase(parameter = ListSort.TITLE).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe topLevelTagList
                }
            }

            When("필터 선택이 켜짐으로 바뀐다") {
                val isTopLevelOnly = MutableStateFlow(false)
                val accountTagFilterRepository = mockk<AccountTagFilterRepository>()
                every { accountTagFilterRepository.getTopLevelOnly(account = account) } returns isTopLevelOnly
                val useCase =
                    PageTagHomeUseCase(
                        getAccountUseCase = getAccountUseCase,
                        accountTagRepository = accountTagRepository,
                        accountTagFilterRepository = accountTagFilterRepository,
                    )

                Then("TC-TAG-HOME-DATA-007 바뀐 선택으로 목록을 다시 조회한다") {
                    flowOf(useCase(parameter = ListSort.TITLE).first().shouldBeSuccess()).asSnapshot() shouldBe tagList

                    isTopLevelOnly.value = true

                    flowOf(useCase(parameter = ListSort.TITLE).first().shouldBeSuccess()).asSnapshot() shouldBe topLevelTagList
                }
            }
        }

        Given("계정 조회에 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountTagRepository = mockk<AccountTagRepository>(relaxed = true)
            val accountTagFilterRepository = mockk<AccountTagFilterRepository>(relaxed = true)
            val useCase =
                PageTagHomeUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountTagRepository = accountTagRepository,
                    accountTagFilterRepository = accountTagFilterRepository,
                )

            When("태그 목록을 페이지로 조회한다") {
                Then("계정 조회 실패를 전달하고 태그 페이지를 요청하지 않는다") {
                    useCase(parameter = ListSort.TITLE)
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    verify(exactly = 0) { accountTagRepository.page(account = any(), query = any(), sort = any()) }
                    verify(exactly = 0) { accountTagRepository.pageTopLevel(account = any(), sort = any()) }
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
                .setExp(Tag::isFinished, false)
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
