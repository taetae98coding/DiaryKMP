package io.github.taetae98coding.diary.domain.place.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FindPlaceUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 저장된 장소가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val place = place()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            every { accountPlaceRepository.find(account = account, placeId = place.id) } returns flowOf(place)
            val useCase =
                FindPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("대상 장소를 조회한다") {
                Then("TC-PLACE-DETAIL-DATA-001 계정과 대상 식별자로 조회한 장소를 전달한다") {
                    useCase(parameter = place.id).first().shouldBeSuccess(place)
                }
            }
        }

        Given("대상 식별자의 장소가 없다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val id = Uuid.random()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            every { accountPlaceRepository.find(account = account, placeId = id) } returns flowOf(null)
            val useCase =
                FindPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("대상 장소를 조회한다") {
                Then("TC-PLACE-DETAIL-DOMAIN-001 조회할 수 없는 것으로 전달한다") {
                    useCase(parameter = id).first().shouldBeSuccess(null)
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
            val useCase =
                FindPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("대상 장소를 조회한다") {
                Then("TC-PLACE-DETAIL-DOMAIN-002 실패를 그대로 전달한다") {
                    useCase(parameter = Uuid.random())
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
                }
            }
        }

        Given("저장된 장소가 바뀌도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val place = place()
            val updated = place.copy(detail = place.detail.copy(title = "updated-${fixtureMonkey.giveMeOne<String>()}"))
            val placeFlow = MutableStateFlow(place)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            every { accountPlaceRepository.find(account = account, placeId = place.id) } returns placeFlow
            val useCase =
                FindPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("저장된 제목이 바뀐다") {
                Then("TC-PLACE-DETAIL-DATA-002 조회 결과가 갱신된다") {
                    runTest {
                        useCase(parameter = place.id).first().shouldBeSuccess(place)

                        placeFlow.value = updated

                        useCase(parameter = place.id).first().shouldBeSuccess(updated)
                    }
                }
            }
        }
    }) {
    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        // FixtureMonkey가 Instant를 생성하지 못하므로 장소는 직접 만든다.
        private fun place(): Place =
            Place(
                id = Uuid.random(),
                detail =
                    PlaceDetail(
                        title = "title-${fixtureMonkey.giveMeOne<String>()}",
                        description = fixtureMonkey.giveMeOne<String>(),
                        color = fixtureMonkey.giveMeOne<Long>(),
                        coordinate = Coordinate(latitude = 37.5, longitude = 127.0),
                        address = fixtureMonkey.giveMeOne<String>(),
                    ),
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            )
    }
}
