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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

class PageTagUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 해당 계정의 미완료·미삭제 태그가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagList = List(2) { tag() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagRepository = mockk<AccountTagRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val query = fixtureMonkey.giveMeOne<String>()
            every { accountTagRepository.page(account = account, query = query.trim(), sort = ListSort.DEFAULT) } returns flowOf(PagingData.from(tagList))
            val useCase =
                PageTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountTagRepository = accountTagRepository,
                )

            When("검색어로 태그 목록을 페이지로 조회한다") {
                Then("TC-TAG-HOME-DATA-001 현재 계정의 태그를 페이지로 전달한다") {
                    val pagingData = useCase(parameter = query).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe tagList
                }

                Then("완료된 태그 페이지 조회를 요청하지 않는다") {
                    useCase(parameter = query).first()

                    verify(exactly = 0) { accountTagRepository.pageFinished(account = any(), sort = any()) }
                }

                Then("TC-TAG-HOME-DATA-012 최상위 태그로 좁힌 목록을 요청하지 않는다") {
                    useCase(parameter = query).first()

                    verify(exactly = 0) { accountTagRepository.pageTopLevel(account = any(), sort = any()) }
                }
            }
        }

        Given("계정 조회에 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagRepository = mockk<AccountTagRepository>(relaxed = true)
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val useCase =
                PageTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountTagRepository = accountTagRepository,
                )

            When("태그 목록을 페이지로 조회한다") {
                Then("계정 조회 실패를 전달하고 태그 페이지를 요청하지 않는다") {
                    useCase(parameter = "")
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    verify(exactly = 0) { accountTagRepository.page(account = any(), query = any(), sort = any()) }
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
