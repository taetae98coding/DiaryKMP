package io.github.taetae98coding.diary.domain.place.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

class GetPlaceListUseCaseTest :
    BehaviorSpec({
        Given("계정이 조회되고 저장소가 영역 안의 장소를 제공한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
            val placeList = List(PLACE_COUNT) { place() }
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            val useCase =
                GetPlaceListUseCase(
                    getAccountUseCase = getAccountUseCase(Result.success(account)),
                    accountPlaceRepository = accountPlaceRepository,
                )
            every { accountPlaceRepository.get(account = account, bounds = bounds, sort = ListSort.TITLE) } returns flowOf(placeList)

            When("보이는 영역의 장소 목록을 조회한다") {
                Then("저장소가 제공한 장소 목록을 성공으로 전달한다") {
                    useCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)).test {
                        awaitItem().shouldBeSuccess() shouldBe placeList
                        awaitComplete()
                    }

                    verify(exactly = 1) { accountPlaceRepository.get(account = account, bounds = bounds, sort = ListSort.TITLE) }
                }
            }
        }

        Given("현재 계정을 확인할 수 없다") {
            val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
            val throwable = IllegalStateException("account unavailable")
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            val useCase =
                GetPlaceListUseCase(
                    getAccountUseCase = getAccountUseCase(Result.failure(throwable)),
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("보이는 영역의 장소 목록을 조회한다") {
                Then("TC-PLACE-HOME-DOMAIN-014 실패를 전달하고 저장소를 조회하지 않는다") {
                    useCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)).test {
                        awaitItem().shouldBeFailure() shouldBe throwable
                        awaitComplete()
                    }

                    verify(exactly = 0) { accountPlaceRepository.get(account = any(), bounds = any(), sort = any()) }
                }
            }
        }

        Given("저장소 조회가 실패한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
            val throwable = IllegalStateException("query error")
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            val useCase =
                GetPlaceListUseCase(
                    getAccountUseCase = getAccountUseCase(Result.success(account)),
                    accountPlaceRepository = accountPlaceRepository,
                )
            every { accountPlaceRepository.get(account = account, bounds = bounds, sort = ListSort.TITLE) } returns flow { throw throwable }

            When("보이는 영역의 장소 목록을 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE)).test {
                        awaitItem().shouldBeFailure() shouldBe throwable
                        awaitComplete()
                    }
                }
            }
        }
    }) {
    private companion object {
        private const val PLACE_COUNT = 3

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        // FixtureMonkey가 Instant를 생성하지 못하므로 장소는 직접 만든다.
        private fun place(): Place =
            Place(
                id = Uuid.random(),
                detail = fixtureMonkey.giveMeOne<PlaceDetail>(),
                isDeleted = false,
                updatedAt = instant(),
                createdAt = instant(),
            )

        private fun getAccountUseCase(account: Result<Account>): GetAccountUseCase {
            val useCase = mockk<GetAccountUseCase>()
            every { useCase(parameter = Unit) } returns flowOf(account)

            return useCase
        }
    }
}
