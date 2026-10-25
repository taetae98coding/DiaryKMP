package io.github.taetae98coding.diary.domain.sync.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncManager
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration.Companion.hours

class SchedulePeriodicSyncUseCaseTest :
    BehaviorSpec({
        Given("로그인 세션이 인증된 사용자 계정이 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))

            When("앱이 인증된 사용자 계정을 확인한다") {
                Then("TC-d271b7b4-fb16-488a-a26d-a93e0462cc67 확인된 사용자 계정으로 주기 동기화가 예약된다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        SchedulePeriodicSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = syncManager,
                        )

                    val result = useCase(parameter = Unit)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.schedulePeriodicSync(period = 4.hours) }
                    verify(exactly = 0) { syncManager.cancelPeriodicSync() }
                }
            }
        }

        Given("확인된 계정이 다른 인증된 계정으로 바뀌었다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))

            When("앱이 바뀐 계정을 확인한다") {
                Then("TC-c52f665c-e8d8-45a8-a3aa-28ed08b2e5cf 바뀐 계정으로 주기 동기화가 예약된다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        SchedulePeriodicSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = syncManager,
                        )

                    val result = useCase(parameter = Unit)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.schedulePeriodicSync(period = 4.hours) }
                }
            }
        }

        Given("로그아웃되어 인증된 계정이 없어졌다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(Account.Guest))

            When("앱이 계정 변화를 확인한다") {
                Then("TC-cc07f234-2c6a-4021-a650-4a0161718819 주기 동기화 예약이 해제된다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        SchedulePeriodicSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = syncManager,
                        )

                    val result = useCase(parameter = Unit)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.cancelPeriodicSync() }
                    verify(exactly = 0) { syncManager.schedulePeriodicSync(period = any()) }
                }
            }
        }

        Given("로그인 세션이 인증되지 않은 것으로 확인된 사용자 계정이 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = false, isSessionPending = false)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))

            When("앱이 계정을 확인한다") {
                Then("TC-b7fbbed2-9c57-498e-91dd-d578a86ea847 주기 동기화 예약이 해제된다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        SchedulePeriodicSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = syncManager,
                        )

                    val result = useCase(parameter = Unit)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.cancelPeriodicSync() }
                    verify(exactly = 0) { syncManager.schedulePeriodicSync(period = any()) }
                }
            }
        }

        Given("주기 동기화가 예약되어 있고 로그인 세션을 확인하는 중인 사용자 계정이 있다") {
            val pendingAccount = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = false, isSessionPending = true)
            val validAccount = pendingAccount.copy(isSessionValid = true, isSessionPending = false)
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(pendingAccount))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow

            When("세션을 확인하는 중인 계정이 확인된 뒤 세션이 인증된 계정이 확인된다") {
                Then("TC-a4e73b81-e32d-4898-8ad2-2d9a37112d55 세션을 확인하는 동안에는 예약을 해제하지도 새로 잡지도 않는다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        SchedulePeriodicSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = syncManager,
                        )

                    useCase(parameter = Unit).shouldBeSuccess(Unit)

                    verify(exactly = 0) { syncManager.cancelPeriodicSync() }
                    verify(exactly = 0) { syncManager.schedulePeriodicSync(period = any()) }

                    accountFlow.value = Result.success(validAccount)
                    useCase(parameter = Unit).shouldBeSuccess(Unit)

                    verify(exactly = 0) { syncManager.cancelPeriodicSync() }
                    verify(exactly = 1) { syncManager.schedulePeriodicSync(period = 4.hours) }
                }
            }
        }

        Given("주기 동기화가 예약되어 있지 않고 로그인 세션을 확인하는 중인 사용자 계정이 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = false, isSessionPending = true)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))

            When("세션을 확인하는 중인 계정이 확인된다") {
                Then("TC-dba098ee-22c5-49fd-bd8b-4a9a80cb3a7a 주기 동기화를 새로 예약하지 않는다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        SchedulePeriodicSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = syncManager,
                        )

                    useCase(parameter = Unit).shouldBeSuccess(Unit)

                    verify(exactly = 0) { syncManager.schedulePeriodicSync(period = any()) }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))

            When("앱이 계정을 확인한다") {
                Then("TC-1375db5a-df98-4c1e-853f-33b350e859cd 계정 조회 실패를 전달하고 예약을 바꾸지 않는다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        SchedulePeriodicSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = syncManager,
                        )

                    val result = useCase(parameter = Unit)

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    verify(exactly = 0) { syncManager.schedulePeriodicSync(period = any()) }
                    verify(exactly = 0) { syncManager.cancelPeriodicSync() }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
