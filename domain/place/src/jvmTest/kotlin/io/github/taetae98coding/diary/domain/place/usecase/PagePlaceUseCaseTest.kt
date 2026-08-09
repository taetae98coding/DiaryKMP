package io.github.taetae98coding.diary.domain.place.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
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

class PagePlaceUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 계정의 장소가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val placeList = List(2) { place() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            val query = fixtureMonkey.giveMeOne<String>()
            every { accountPlaceRepository.page(account = account, query = query.trim(), sort = ListSort.DEFAULT) } returns flowOf(PagingData.from(placeList))
            val useCase =
                PagePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("검색어로 장소 목록을 페이지로 조회한다") {
                Then("현재 계정의 장소를 페이지로 전달한다") {
                    val pagingData = useCase(parameter = query).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe placeList
                }
            }
        }

        Given("로그인한 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            every { accountPlaceRepository.page(account = account, query = any(), sort = any()) } returns flowOf(PagingData.empty())
            val useCase =
                PagePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("TC-MEMO-PLACE-CARD-DOMAIN-021 공백만 있는 검색어로 장소 목록을 페이지로 조회한다") {
                Then("검색어가 없는 것과 같게 조회한다") {
                    useCase(parameter = "   ").first().shouldBeSuccess()

                    verify(exactly = 1) { accountPlaceRepository.page(account = account, query = "", sort = ListSort.DEFAULT) }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
            val useCase =
                PagePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("장소 목록을 페이지로 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = "")
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

        private fun place(): Place =
            fixtureMonkey
                .giveMeKotlinBuilder<Place>()
                .setExp(Place::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Place::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
