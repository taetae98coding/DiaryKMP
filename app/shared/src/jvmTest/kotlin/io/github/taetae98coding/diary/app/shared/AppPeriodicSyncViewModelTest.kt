@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.app.shared

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.SchedulePeriodicSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class AppPeriodicSyncViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-DATA-SYNC-DOMAIN-059 TC-DATA-SYNC-DOMAIN-083 TC-DATA-SYNC-DOMAIN-088 인증 여부와 관계없이 바뀐 계정을 주기 동기화 예약 계기로 전달한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true, isSessionPending = false)
                val pendingAccount = account.copy(isSessionValid = false, isSessionPending = true)
                val invalidAccount = account.copy(isSessionValid = false, isSessionPending = false)
                val accountFlow = MutableStateFlow<Account>(account)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                viewModel.uiState.confirmedAccount().test {
                    awaitItem() shouldBe account
                    accountFlow.value = pendingAccount
                    awaitItem() shouldBe pendingAccount
                    accountFlow.value = invalidAccount
                    awaitItem() shouldBe invalidAccount
                    accountFlow.value = Account.Guest
                    awaitItem() shouldBe Account.Guest
                    expectNoEvents()
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-057 같은 계정이 다시 확인되기만 하면 주기 동기화 예약 계기가 발생하지 않는다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val viewModel = viewModel(accountFlow = flowOf(Result.success(account), Result.success(account)))

                viewModel.uiState.confirmedAccount().test {
                    awaitItem() shouldBe account
                    expectNoEvents()
                }
            }
        }

        test("계정 확인에 실패하면 주기 동기화 예약 계기가 발생하지 않는다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(accountFlow = flowOf(Result.failure(IllegalStateException("account error"))))

                viewModel.uiState.confirmedAccount().test {
                    expectNoEvents()
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-056 주기 동기화 예약을 요청하면 예약 UseCase를 실행한다") {
            runTest(mainDispatcher) {
                val schedulePeriodicSyncUseCase = mockk<SchedulePeriodicSyncUseCase>()
                coEvery { schedulePeriodicSyncUseCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel = viewModel(schedulePeriodicSyncUseCase = schedulePeriodicSyncUseCase)

                viewModel.schedulePeriodicSync()
                advanceUntilIdle()

                coVerify(exactly = 1) { schedulePeriodicSyncUseCase(parameter = Unit) }
            }
        }

        test("주기 동기화 예약이 진행 중일 때 다시 요청하면 예약 UseCase를 한 번만 실행하고, 끝난 뒤 다시 요청하면 다시 실행한다") {
            runTest(mainDispatcher) {
                val schedulePeriodicSyncUseCase = mockk<SchedulePeriodicSyncUseCase>()
                coEvery { schedulePeriodicSyncUseCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel = viewModel(schedulePeriodicSyncUseCase = schedulePeriodicSyncUseCase)

                viewModel.schedulePeriodicSync()
                viewModel.schedulePeriodicSync()
                advanceUntilIdle()

                coVerify(exactly = 1) { schedulePeriodicSyncUseCase(parameter = Unit) }

                viewModel.schedulePeriodicSync()
                advanceUntilIdle()

                coVerify(exactly = 2) { schedulePeriodicSyncUseCase(parameter = Unit) }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            accountFlow: Flow<Result<Account>> = flowOf(Result.success(Account.Guest)),
            schedulePeriodicSyncUseCase: SchedulePeriodicSyncUseCase = mockk(relaxed = true),
        ): AppPeriodicSyncViewModel {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow

            return AppPeriodicSyncViewModel(
                getAccountUseCase = getAccountUseCase,
                schedulePeriodicSyncUseCase = schedulePeriodicSyncUseCase,
            )
        }

        private fun Flow<Account>.toResultFlow(): Flow<Result<Account>> = map { account -> Result.success(account) }

        private fun Flow<AppPeriodicSyncUiState>.confirmedAccount(): Flow<Account> = filterIsInstance<AppPeriodicSyncUiState.Confirmed>().map { uiState -> uiState.account }
    }
}
