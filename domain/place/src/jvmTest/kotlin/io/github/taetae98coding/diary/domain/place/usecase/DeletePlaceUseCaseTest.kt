package io.github.taetae98coding.diary.domain.place.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class DeletePlaceUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 저장된 장소가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val id = Uuid.random()
            val isDeletedSlot = slot<Boolean>()
            val updatedAtSlot = slot<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            coEvery {
                accountPlaceRepository.updateDeleted(
                    account = account,
                    placeId = id,
                    isDeleted = capture(isDeletedSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                DeletePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                    clock = clock,
                )

            When("장소를 삭제한다") {
                Then("TC-PLACE-DETAIL-DOMAIN-010 삭제 여부와 수정 시각만 바꾼다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    isDeletedSlot.captured shouldBe true
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-SYNC-REFRESH-FEATURE-004 TC-PLACE-DETAIL-DATA-008 삭제를 서버와 맞추기 위한 동기화를 요청한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("삭제는 성공하지만 서버 반영이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val id = Uuid.random()
            val isDeletedSlot = slot<Boolean>()
            val updatedAtSlot = slot<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            coEvery {
                accountPlaceRepository.updateDeleted(
                    account = account,
                    placeId = id,
                    isDeleted = capture(isDeletedSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns
                Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
            val useCase =
                DeletePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                    clock = clock,
                )

            When("장소를 삭제한다") {
                Then("TC-PLACE-DETAIL-DATA-010 삭제는 성공으로 전달되고 기기에 반영한 삭제를 되돌리지 않는다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    isDeletedSlot.captured shouldBe true
                    updatedAtSlot.captured shouldBe now
                    coVerify(exactly = 1) {
                        accountPlaceRepository.updateDeleted(
                            account = account,
                            placeId = id,
                            isDeleted = any(),
                            updatedAt = any(),
                        )
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
            val useCase =
                DeletePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountPlaceRepository = accountPlaceRepository,
                    clock = Clock.System,
                )

            When("장소를 삭제한다") {
                Then("계정 조회 실패를 전달하고 삭제를 저장하지 않는다") {
                    val result = useCase(parameter = Uuid.random())

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) {
                        accountPlaceRepository.updateDeleted(
                            account = any(),
                            placeId = any(),
                            isDeleted = any(),
                            updatedAt = any(),
                        )
                    }
                }
            }
        }

        Given("삭제 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            coEvery {
                accountPlaceRepository.updateDeleted(
                    account = account,
                    placeId = any(),
                    isDeleted = any(),
                    updatedAt = any(),
                )
            } throws throwable
            val useCase =
                DeletePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountPlaceRepository = accountPlaceRepository,
                    clock = Clock.System,
                )

            When("장소를 삭제한다") {
                Then("TC-PLACE-DETAIL-DATA-005 삭제를 성공으로 다루지 않고 저장 실패를 전달한다") {
                    val result = useCase(parameter = Uuid.random())

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                }
            }
        }
    }) {
    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun requestSyncUseCase(): RequestSyncUseCase = mockk(relaxed = true)
    }
}
