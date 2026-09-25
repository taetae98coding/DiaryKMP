@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.app.shared

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.SubmitFcmTokenUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
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
import java.util.Locale
import java.util.TimeZone

class AppFcmTokenViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-FCM-TOKEN-DOMAIN-005 인증된 계정이 확인되면 제출 계기가 한 번 발생한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val viewModel = viewModel(accountFlow = flowOf(Result.success(account)))

                viewModel.account.test {
                    awaitItem() shouldBe account
                    expectNoEvents()
                }
            }
        }

        test("TC-FCM-TOKEN-DOMAIN-005 로그인이 완료되어 인증된 계정으로 바뀌면 제출 계기가 발생한다") {
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

        test("TC-FCM-TOKEN-DOMAIN-020 로그아웃되어 게스트로 바뀌면 앞선 제출을 기다리지 않고 제출 계기가 한 번 발생한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow<Account>(account)
                val submitFcmTokenUseCase = mockk<SubmitFcmTokenUseCase>()
                coEvery { submitFcmTokenUseCase(parameter = Unit) } coAnswers { awaitCancellation() }
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow(), submitFcmTokenUseCase = submitFcmTokenUseCase)

                viewModel.account.test {
                    awaitItem() shouldBe account
                    viewModel.submit()
                    advanceUntilIdle()

                    accountFlow.value = Account.Guest
                    awaitItem() shouldBe Account.Guest
                    viewModel.submit()
                    advanceUntilIdle()
                    expectNoEvents()
                }

                coVerify(exactly = 2) { submitFcmTokenUseCase(parameter = Unit) }
            }
        }

        test("TC-FCM-TOKEN-DOMAIN-003 같은 계정의 세션이 인증된 상태로 바뀌면 제출 계기가 발생한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = false)
                val validAccount = account.copy(isSessionValid = true)
                val accountFlow = MutableStateFlow<Account>(account)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                viewModel.account.test {
                    awaitItem() shouldBe account
                    accountFlow.value = validAccount
                    awaitItem() shouldBe validAccount
                    expectNoEvents()
                }
            }
        }

        context("TC-FCM-TOKEN-DOMAIN-026 같은 계정의 계정 정보가 바뀌면 제출 계기가 발생한다") {
            listOf<Pair<String, (Account.User) -> Account.User>>(
                "이메일" to { account -> account.copy(email = account.email + "-changed") },
                "프로필 이미지" to { account -> account.copy(profileImage = account.profileImage + "-changed") },
            ).forEach { (name, change) ->
                test(name) {
                    runTest(mainDispatcher) {
                        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                        val changedAccount = change(account)
                        val accountFlow = MutableStateFlow<Account>(account)
                        val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                        viewModel.account.test {
                            awaitItem() shouldBe account
                            accountFlow.value = changedAccount
                            awaitItem() shouldBe changedAccount
                            expectNoEvents()
                        }
                    }
                }
            }
        }

        test("TC-FCM-TOKEN-DOMAIN-027 화면이 재생성되어 다시 관측하면 같은 계정이어도 제출 계기가 한 번 다시 발생한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow<Account>(account)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                val firstJob = launch { viewModel.account.collect { } }
                advanceUntilIdle()
                firstJob.cancelAndJoin()

                val accountList = mutableListOf<Account>()
                val secondJob = launch { viewModel.account.collect { value -> accountList.add(value) } }
                advanceUntilIdle()
                secondJob.cancelAndJoin()

                accountList shouldBe listOf(account)
            }
        }

        test("TC-FCM-TOKEN-DOMAIN-005 확인된 계정이 다른 계정으로 바뀌면 제출 계기가 발생한다") {
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

        test("TC-FCM-TOKEN-DOMAIN-025 같은 계정이 같은 정보로 다시 확인되기만 하면 제출 계기가 발생하지 않는다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow<Account>(account)
                val viewModel = viewModel(accountFlow = accountFlow.toResultFlow())

                viewModel.account.test {
                    awaitItem() shouldBe account
                    accountFlow.value = account
                    expectNoEvents()
                }
            }
        }

        test("TC-FCM-TOKEN-DOMAIN-008 기기의 시간대나 언어가 바뀌는 것만으로는 제출 계기가 발생하지 않는다") {
            val originalTimeZone = TimeZone.getDefault()
            val originalLocale = Locale.getDefault()

            try {
                runTest(mainDispatcher) {
                    val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                    val viewModel = viewModel(accountFlow = flowOf(Result.success(account)))

                    viewModel.account.test {
                        awaitItem() shouldBe account
                        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
                        Locale.setDefault(Locale.US)
                        expectNoEvents()
                    }
                }
            } finally {
                TimeZone.setDefault(originalTimeZone)
                Locale.setDefault(originalLocale)
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

        test("제출 계기가 발생하면 토큰 제출을 요청한다") {
            runTest(mainDispatcher) {
                val submitFcmTokenUseCase = mockk<SubmitFcmTokenUseCase>()
                coEvery { submitFcmTokenUseCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel = viewModel(submitFcmTokenUseCase = submitFcmTokenUseCase)

                viewModel.submit()
                advanceUntilIdle()

                coVerify(exactly = 1) { submitFcmTokenUseCase(parameter = Unit) }
            }
        }
    }

    public companion object {
        private const val STOP_TIMEOUT_ELAPSED_MILLIS = 10_000L

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            accountFlow: Flow<Result<Account>> = flowOf(Result.success(Account.Guest)),
            submitFcmTokenUseCase: SubmitFcmTokenUseCase = mockk(relaxed = true),
        ): AppFcmTokenViewModel {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow

            return AppFcmTokenViewModel(
                getAccountUseCase = getAccountUseCase,
                submitFcmTokenUseCase = submitFcmTokenUseCase,
            )
        }

        private fun Flow<Account>.toResultFlow(): Flow<Result<Account>> = map { account -> Result.success(account) }
    }
}
