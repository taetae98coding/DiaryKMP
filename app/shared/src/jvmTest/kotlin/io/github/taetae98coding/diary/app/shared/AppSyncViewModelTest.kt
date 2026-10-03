@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.app.shared

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
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

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

                viewModel.uiState.authenticatedAccountId().test {
                    awaitItem() shouldBe account.id
                    expectNoEvents()
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-011 게스트에서 인증된 계정으로 바뀌면 동기화 계기가 발생한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow<Account>(Account.Guest)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                viewModel.uiState.authenticatedAccountId().test {
                    expectNoEvents()
                    accountFlow.value = account
                    awaitItem() shouldBe account.id
                    expectNoEvents()
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-011 TC-SYNC-REFRESH-FEATURE-003 로그아웃한 뒤 같은 계정으로 다시 로그인하면 동기화 계기가 다시 발생한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow<Account>(account)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                viewModel.uiState.authenticatedAccountId().test {
                    awaitItem() shouldBe account.id
                    accountFlow.value = Account.Guest
                    runCurrent()
                    expectNoEvents()
                    accountFlow.value = account
                    awaitItem() shouldBe account.id
                    expectNoEvents()
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-031 로그인 세션의 인증 여부가 확인되기 전에는 동기화 계기가 발생하지 않고 확인된 뒤 발생한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = false, isSessionPending = true)
                val refreshedAccount = account.copy(isSessionValid = true, isSessionPending = false)
                val accountFlow = MutableStateFlow<Account>(account)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                viewModel.uiState.authenticatedAccountId().test {
                    expectNoEvents()
                    accountFlow.value = refreshedAccount
                    awaitItem() shouldBe refreshedAccount.id
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

                viewModel.uiState.authenticatedAccountId().test {
                    awaitItem() shouldBe account.id
                    accountFlow.value = otherAccount
                    awaitItem() shouldBe otherAccount.id
                    expectNoEvents()
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-034 같은 계정이 다시 확인되기만 하면 동기화 계기가 발생하지 않는다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableSharedFlow<Account>(replay = 1)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                viewModel.uiState.authenticatedAccountId().test {
                    accountFlow.emit(account)
                    awaitItem() shouldBe account.id
                    accountFlow.emit(account)
                    accountFlow.emit(account)
                    expectNoEvents()
                }
            }
        }

        listOf<Pair<String, (Account.User) -> Account.User>>(
            "세션 확인 중" to { account -> account.copy(isSessionValid = false, isSessionPending = true) },
            "인증되지 않은 것으로 확인됨" to { account -> account.copy(isSessionValid = false, isSessionPending = false) },
        ).forEach { (label, invalidate) ->
            test("TC-DATA-SYNC-DOMAIN-092 TC-SYNC-REFRESH-FEATURE-003 같은 계정의 로그인 세션이 $label 상태였다가 다시 인증되면 동기화 계기가 다시 발생한다") {
                runTest(mainDispatcher) {
                    val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true, isSessionPending = false)
                    val accountFlow = MutableStateFlow<Account>(account)
                    val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                    viewModel.uiState.authenticatedAccountId().test {
                        awaitItem() shouldBe account.id
                        accountFlow.value = invalidate(account)
                        runCurrent()
                        expectNoEvents()
                        accountFlow.value = account
                        awaitItem() shouldBe account.id
                        expectNoEvents()
                    }
                }
            }
        }

        listOf<Pair<String, (Account.User) -> Account.User>>(
            "이메일" to { account -> account.copy(email = "changed-${account.email}") },
            "프로필 이미지" to { account -> account.copy(profileImage = "changed-${account.profileImage}") },
        ).forEach { (label, change) ->
            test("TC-DATA-SYNC-DOMAIN-093 같은 계정의 $label 만 바뀌면 동기화 계기가 발생하지 않는다") {
                runTest(mainDispatcher) {
                    val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                    val accountFlow = MutableStateFlow<Account>(account)
                    val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                    viewModel.uiState.authenticatedAccountId().test {
                        awaitItem() shouldBe account.id
                        accountFlow.value = change(account)
                        expectNoEvents()
                    }
                }
            }
        }

        test("계정 확인에 실패하면 동기화 계기가 발생하지 않는다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow(Result.failure<Account>(IllegalStateException("account error")))
                val viewModel = viewModel(accountFlow = accountFlow)

                viewModel.uiState.authenticatedAccountId().test {
                    expectNoEvents()
                    accountFlow.value = Result.success(account)
                    awaitItem() shouldBe account.id
                    expectNoEvents()
                }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-085 관측이 끊긴 지 오래된 뒤 다시 관측해도 확인된 계정이 한 번만 다시 전달된다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow<Account>(account)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                val firstJob = launch { viewModel.uiState.authenticatedAccountId().collect { } }
                advanceUntilIdle()
                firstJob.cancelAndJoin()
                advanceTimeBy(STOP_TIMEOUT_ELAPSED_MILLIS)

                val accountIdList = mutableListOf<Uuid>()
                val secondJob = launch { viewModel.uiState.authenticatedAccountId().collect { value -> accountIdList.add(value) } }
                advanceUntilIdle()
                secondJob.cancelAndJoin()

                accountIdList shouldBe listOf(account.id)
            }
        }

        test("계정을 확인하기 전에는 확인 중이고 인증된 계정이 아니면 인증되지 않은 상태다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow(Result.failure<Account>(IllegalStateException("account error")))
                val viewModel = viewModel(accountFlow = accountFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe AppSyncUiState.Loading
                    accountFlow.value = Result.success(Account.Guest)
                    awaitItem() shouldBe AppSyncUiState.Unauthenticated
                    accountFlow.value = Result.success(account.copy(isSessionValid = false))
                    runCurrent()
                    expectNoEvents()
                    accountFlow.value = Result.success(account)
                    awaitItem() shouldBe AppSyncUiState.Authenticated(accountId = account.id)
                    expectNoEvents()
                }
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-003 동기화를 요청하면 계정 확인 계기로 요청한다") {
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

        private fun Flow<AppSyncUiState>.authenticatedAccountId(): Flow<Uuid> = filterIsInstance<AppSyncUiState.Authenticated>().map { uiState -> uiState.accountId }
    }
}
