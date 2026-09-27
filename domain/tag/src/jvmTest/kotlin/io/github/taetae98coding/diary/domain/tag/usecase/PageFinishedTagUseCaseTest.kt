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

class PageFinishedTagUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 해당 계정의 완료된 태그가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagList = List(2) { tag() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagRepository = mockk<AccountTagRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every { accountTagRepository.pageFinished(account = account, sort = ListSort.TITLE) } returns flowOf(PagingData.from(tagList))
            val useCase =
                PageFinishedTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountTagRepository = accountTagRepository,
                )

            When("완료된 태그 목록을 페이지로 조회한다") {
                Then("TC-TAG-FINISHED-LIST-DATA-001 현재 계정의 완료된 태그를 페이지로 전달한다") {
                    val pagingData = useCase(parameter = ListSort.TITLE).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe tagList
                }

                Then("미완료 태그 페이지 조회를 요청하지 않는다") {
                    useCase(parameter = ListSort.TITLE).first()

                    verify(exactly = 0) { accountTagRepository.page(account = any(), query = any(), sort = any()) }
                }
            }
        }

        Given("계정 조회에 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagRepository = mockk<AccountTagRepository>(relaxed = true)
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val useCase =
                PageFinishedTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountTagRepository = accountTagRepository,
                )

            When("완료된 태그 목록을 페이지로 조회한다") {
                Then("계정 조회 실패를 전달하고 태그 페이지를 요청하지 않는다") {
                    useCase(parameter = ListSort.TITLE)
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    verify(exactly = 0) { accountTagRepository.pageFinished(account = any(), sort = any()) }
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
                .setExp(Tag::isFinished, true)
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
