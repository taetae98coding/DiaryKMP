package io.github.taetae98coding.diary.domain.place.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

class PagePlaceUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 계정의 장소가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val placeList = List(2) { place() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            val query = Arb.string().next()
            every { accountPlaceRepository.page(account = account, query = query.trim(), sort = ListSort.DEFAULT) } returns flowOf(PagingData.from(placeList))
            val useCase =
                PagePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("검색어로 장소 목록을 페이지로 조회한다") {
                Then("현재 계정의 장소를 페이지로 전달한다") {
                    val pagingData = useCase(parameter = PagePlaceUseCase.Parameter(query = query, sort = ListSort.DEFAULT)).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe placeList
                }
            }
        }

        Given("로그인한 계정과 정렬 기준으로 조회되는 장소가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val placeList = List(2) { place() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            every { accountPlaceRepository.page(account = account, query = "", sort = ListSort.RECENTLY_UPDATED) } returns flowOf(PagingData.from(placeList))
            val useCase =
                PagePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("정렬 기준으로 장소 목록을 페이지로 조회한다") {
                Then("TC-edda8644-9fe2-4b74-b810-abb7c9a12a7a 현재 계정의 장소를 고른 정렬 기준으로 검색어 없이 조회해 페이지로 전달한다") {
                    val pagingData = useCase(parameter = PagePlaceUseCase.Parameter(query = "", sort = ListSort.RECENTLY_UPDATED)).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe placeList
                    verify(exactly = 1) { accountPlaceRepository.page(account = account, query = "", sort = ListSort.RECENTLY_UPDATED) }
                }
            }
        }

        Given("로그인한 계정이 준비되어 있다") {
            val account = Arb.accountUser().next()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            every { accountPlaceRepository.page(account = account, query = any(), sort = any()) } returns flowOf(PagingData.empty())
            val useCase =
                PagePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("TC-cda116a9-739a-4c75-8747-49ee08e64212 공백만 있는 검색어로 장소 목록을 페이지로 조회한다") {
                Then("검색어가 없는 것과 같게 조회한다") {
                    useCase(parameter = PagePlaceUseCase.Parameter(query = "   ", sort = ListSort.DEFAULT)).first().shouldBeSuccess()

                    verify(exactly = 1) { accountPlaceRepository.page(account = account, query = "", sort = ListSort.DEFAULT) }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(throwable))
            val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
            val useCase =
                PagePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("장소 목록을 페이지로 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = PagePlaceUseCase.Parameter(query = "", sort = ListSort.DEFAULT))
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
                }
            }
        }

        Given("현재 계정을 확인할 수 없도록 준비되어 있다") {
            val throwable = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(throwable))
            val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
            val useCase =
                PagePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("정렬 기준으로 장소 목록을 페이지로 조회한다") {
                Then("TC-8b86ec99-2778-490f-bd5f-772bac789153 실패를 그대로 전달하고 장소를 조회하지 않는다") {
                    useCase(parameter = PagePlaceUseCase.Parameter(query = "", sort = ListSort.TITLE))
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    verify(exactly = 0) { accountPlaceRepository.page(account = any(), query = any(), sort = any()) }
                }
            }
        }
    }) {
    public companion object {
        private fun place(): Place =
            Arb
                .place()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )
    }
}
