package io.github.taetae98coding.diary.domain.web.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
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

class RestoreWebUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 삭제된 웹 항목이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val isDeletedSlot = slot<Boolean>()
            val updatedAtSlot = slot<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>()
            coEvery {
                accountWebRepository.updateDeleted(
                    account = account,
                    webId = id,
                    isDeleted = capture(isDeletedSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = fixtureMonkey.giveMeOne<Instant>()
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                RestoreWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountWebRepository = accountWebRepository,
                    clock = clock,
                )

            When("웹 항목의 삭제를 실행 취소한다") {
                Then("TC-WEB-HOME-DOMAIN-011 삭제 여부를 미삭제로 되돌리고 수정 시각을 갱신한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    isDeletedSlot.captured shouldBe false
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-WEB-HOME-DATA-007 로컬 저장 결과로 성공을 판단하고 동기화를 요청한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("게스트 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.Guest>()
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>()
            coEvery {
                accountWebRepository.updateDeleted(account = account, webId = id, isDeleted = any(), updatedAt = any())
            } returns 1
            val useCase =
                RestoreWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountWebRepository = accountWebRepository,
                    clock = Clock.System,
                )

            When("웹 항목의 삭제를 실행 취소한다") {
                Then("TC-WEB-HOME-DATA-008 게스트 계정 기준으로 기기에만 실행 취소를 반영한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    coVerify(exactly = 1) {
                        accountWebRepository.updateDeleted(account = account, webId = id, isDeleted = false, updatedAt = any())
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
            val useCase =
                RestoreWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountWebRepository = accountWebRepository,
                    clock = Clock.System,
                )

            When("웹 항목의 삭제를 실행 취소한다") {
                Then("TC-WEB-HOME-DOMAIN-012 실행 취소가 성공으로 다뤄지지 않고 웹 항목의 삭제 여부를 바꾸지 않는다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) {
                        accountWebRepository.updateDeleted(account = any(), webId = any(), isDeleted = any(), updatedAt = any())
                    }
                }
            }
        }

        Given("실행 취소 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>()
            coEvery {
                accountWebRepository.updateDeleted(account = account, webId = any(), isDeleted = any(), updatedAt = any())
            } throws throwable
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                RestoreWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountWebRepository = accountWebRepository,
                    clock = Clock.System,
                )

            When("웹 항목의 삭제를 실행 취소한다") {
                Then("TC-WEB-HOME-DATA-009 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun requestSyncUseCase(): RequestSyncUseCase {
            val useCase = mockk<RequestSyncUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)

            return useCase
        }
    }
}
