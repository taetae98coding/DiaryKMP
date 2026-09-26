package io.github.taetae98coding.diary.domain.qr.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.qr.repository.AccountQrRepository
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

class DeleteQrUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 삭제되지 않은 QR이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val isDeletedSlot = slot<Boolean>()
            val updatedAtSlot = slot<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountQrRepository = mockk<AccountQrRepository>()
            coEvery {
                accountQrRepository.updateDeleted(
                    account = account,
                    qrId = id,
                    isDeleted = capture(isDeletedSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = fixtureMonkey.giveMeOne<Instant>()
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                DeleteQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountQrRepository = accountQrRepository,
                    clock = clock,
                )

            When("QR을 삭제한다") {
                Then("TC-QR-HOME-DOMAIN-009 삭제 여부를 삭제로 바꾸고 수정 시각을 갱신한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    isDeletedSlot.captured shouldBe true
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-SYNC-REFRESH-FEATURE-004 TC-QR-HOME-DATA-004 로컬 저장 결과로 성공을 판단하고 동기화를 요청한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("QR을 삭제할 수 있는 현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountQrRepository = mockk<AccountQrRepository>()
            coEvery {
                accountQrRepository.updateDeleted(account = account, qrId = id, isDeleted = any(), updatedAt = any())
            } returns 1
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                DeleteQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountQrRepository = accountQrRepository,
                    clock = Clock.System,
                )

            When("QR을 삭제한다") {
                Then("TC-SYNC-REFRESH-FEATURE-004 TC-QR-HOME-DATA-004 기기에 저장한 뒤 서버와 맞추기 위한 동기화를 한 번 요청한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("현재 계정과 연결된 QR이 없도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountQrRepository = mockk<AccountQrRepository>()
            coEvery {
                accountQrRepository.updateDeleted(account = account, qrId = any(), isDeleted = any(), updatedAt = any())
            } returns 0
            val useCase =
                DeleteQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountQrRepository = accountQrRepository,
                    clock = Clock.System,
                )

            When("QR을 삭제한다") {
                Then("바뀐 QR이 없음을 전달한다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>()).shouldBeSuccess(0)
                }
            }
        }

        Given("게스트 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.Guest>()
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountQrRepository = mockk<AccountQrRepository>()
            coEvery {
                accountQrRepository.updateDeleted(account = account, qrId = id, isDeleted = any(), updatedAt = any())
            } returns 1
            val useCase =
                DeleteQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountQrRepository = accountQrRepository,
                    clock = Clock.System,
                )

            When("QR을 삭제한다") {
                Then("TC-QR-HOME-DATA-005 게스트 계정 기준으로 기기에 반영한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    coVerify(exactly = 1) {
                        accountQrRepository.updateDeleted(account = account, qrId = id, isDeleted = true, updatedAt = any())
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountQrRepository = mockk<AccountQrRepository>(relaxed = true)
            val useCase =
                DeleteQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountQrRepository = accountQrRepository,
                    clock = Clock.System,
                )

            When("QR을 삭제한다") {
                Then("성공으로 다뤄지지 않고 QR의 삭제 여부를 바꾸지 않는다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) {
                        accountQrRepository.updateDeleted(account = any(), qrId = any(), isDeleted = any(), updatedAt = any())
                    }
                }
            }
        }

        Given("삭제 여부 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountQrRepository = mockk<AccountQrRepository>()
            coEvery {
                accountQrRepository.updateDeleted(account = account, qrId = any(), isDeleted = any(), updatedAt = any())
            } throws throwable
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                DeleteQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountQrRepository = accountQrRepository,
                    clock = Clock.System,
                )

            When("QR을 삭제한다") {
                Then("실패를 그대로 전달하고 동기화를 요청하지 않는다") {
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
