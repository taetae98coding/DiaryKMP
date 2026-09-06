@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.app

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class AppSyncViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-DATA-SYNC-DOMAIN-010 인증된 계정이 확인되면 동기화 계기가 한 번 발생한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val viewModel = viewModel(accountFlow = flowOf(Result.success(account)))

                viewModel.account.test {
                    awaitItem() shouldBe account
                    expectNoEvents()
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-011 게스트에서 인증된 계정으로 바뀌면 동기화 계기가 발생한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow<Account>(Account.Guest)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                viewModel.account.test {
                    awaitItem() shouldBe Account.Guest
                    accountFlow.value = account
                    awaitItem() shouldBe account
                    expectNoEvents()
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-031 로그인 세션의 인증 여부가 확인되면 동기화 계기가 발생한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = false)
                val refreshedAccount = account.copy(isSessionValid = true)
                val accountFlow = MutableStateFlow<Account>(account)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                viewModel.account.test {
                    awaitItem() shouldBe account
                    accountFlow.value = refreshedAccount
                    awaitItem() shouldBe refreshedAccount
                    expectNoEvents()
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-033 확인된 계정이 다른 계정으로 바뀌면 동기화 계기가 발생한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val otherAccount = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow<Account>(account)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                viewModel.account.test {
                    awaitItem() shouldBe account
                    accountFlow.value = otherAccount
                    awaitItem() shouldBe otherAccount
                    expectNoEvents()
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-034 같은 계정이 다시 확인되기만 하면 동기화 계기가 발생하지 않는다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableSharedFlow<Account>(replay = 1)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                viewModel.account.test {
                    accountFlow.emit(account)
                    awaitItem() shouldBe account
                    accountFlow.emit(account)
                    accountFlow.emit(account)
                    expectNoEvents()
                }
            }
        }

        test("계정 확인에 실패하면 동기화 계기가 발생하지 않는다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow(Result.failure<Account>(IllegalStateException("account error")))
                val viewModel = viewModel(accountFlow = accountFlow)

                viewModel.account.test {
                    expectNoEvents()
                    accountFlow.value = Result.success(account)
                    awaitItem() shouldBe account
                    expectNoEvents()
                }
            }
        }

        test("다시 관측을 시작하면 확인된 계정이 한 번만 다시 전달된다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow<Account>(account)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                viewModel.account.test {
                    awaitItem() shouldBe account
                    expectNoEvents()
                }

                viewModel.account.test {
                    awaitItem() shouldBe account
                    expectNoEvents()
                }
            }
        }

        test("관측이 끊긴 지 오래된 뒤 다시 관측해도 확인된 계정이 한 번만 다시 전달된다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow<Account>(account)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                val firstJob = launch { viewModel.account.collect { } }
                advanceUntilIdle()
                firstJob.cancelAndJoin()
                advanceTimeBy(STOP_TIMEOUT_ELAPSED_MILLIS)

                val accountList = mutableListOf<Account>()
                val secondJob = launch { viewModel.account.collect { value -> accountList.add(value) } }
                advanceUntilIdle()
                secondJob.cancelAndJoin()

                accountList shouldBe listOf(account)
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-003 동기화 계기가 발생하면 진행을 표시할 동기화를 요청한다") {
            runTest(mainDispatcher) {
                val requestSyncUseCase = mockk<RequestSyncUseCase>()
                coEvery { requestSyncUseCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED) } returns Result.success(Unit)
                val viewModel = viewModel(requestSyncUseCase = requestSyncUseCase)

                viewModel.requestSync()
                advanceUntilIdle()

                coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED) }
            }
        }
    }

    public companion object {
        private const val STOP_TIMEOUT_ELAPSED_MILLIS = 10_000L

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            accountFlow: Flow<Result<Account>> = flowOf(Result.success(Account.Guest)),
            requestSyncUseCase: RequestSyncUseCase = mockk(relaxed = true),
        ): AppSyncViewModel {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow

            return AppSyncViewModel(
                getAccountUseCase = getAccountUseCase,
                requestSyncUseCase = requestSyncUseCase,
            )
        }

        private fun Flow<Account>.toResultFlow(): Flow<Result<Account>> = map { account -> Result.success(account) }
    }
}
