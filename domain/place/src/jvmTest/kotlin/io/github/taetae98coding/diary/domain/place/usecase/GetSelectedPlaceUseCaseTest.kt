package io.github.taetae98coding.diary.domain.place.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
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
import kotlin.uuid.Uuid

class GetSelectedPlaceUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 선택한 장소가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val placeList = List(2) { place() }
            val placeIdSet = placeList.mapTo(mutableSetOf()) { place -> place.id }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            every { accountPlaceRepository.get(account = account, placeIdSet = placeIdSet) } returns flowOf(placeList)
            val useCase =
                GetSelectedPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("선택한 장소를 조회한다") {
                Then("선택한 식별자에 해당하는 장소를 전달한다") {
                    useCase(parameter = placeIdSet).first().shouldBeSuccess(placeList)
                }
            }
        }

        Given("선택한 장소가 없다") {
            val getAccountUseCase = mockk<GetAccountUseCase>(relaxed = true)
            val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
            val useCase =
                GetSelectedPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("선택한 장소를 조회한다") {
                Then("저장소를 조회하지 않고 빈 목록을 전달한다") {
                    useCase(parameter = emptySet<Uuid>()).first().shouldBeSuccess(emptyList())

                    verify(exactly = 0) { accountPlaceRepository.get(account = any(), placeIdSet = any()) }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(throwable))
            val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
            val useCase =
                GetSelectedPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                )

            When("선택한 장소를 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = setOf(Arb.kotlinUuid().next()))
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
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
