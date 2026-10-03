@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.add

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileAddAccountViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-FILE-ADD-FEATURE-015 게스트가 되면 게스트 상태가 되고 다른 사용자나 확정하지 않은 상태는 게스트가 아니다") {
            runTest(mainDispatcher) {
                val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
                val viewModel = viewModel(accountFlow = accountFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe FileAddAccountUiState(isGuest = false)

                    accountFlow.value = Result.success(fixtureMonkey.giveMeOne<Account.User>())
                    accountFlow.value = Result.failure(IllegalStateException("account"))
                    runCurrent()
                    expectNoEvents()

                    accountFlow.value = Result.success(Account.Guest)
                    awaitItem() shouldBe FileAddAccountUiState(isGuest = true)
                    expectNoEvents()
                }
            }
        }

        test("게스트인 동안 계정이 다시 확인되어도 상태가 다시 나오지 않는다") {
            runTest(mainDispatcher) {
                val accountFlow = MutableSharedFlow<Result<Account>>(replay = 1)
                val viewModel = viewModel(accountFlow = accountFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe FileAddAccountUiState(isGuest = false)

                    accountFlow.emit(Result.success(Account.Guest))
                    awaitItem() shouldBe FileAddAccountUiState(isGuest = true)

                    accountFlow.emit(Result.success(Account.Guest))
                    runCurrent()
                    expectNoEvents()
                }
            }
        }
    }
}

private fun viewModel(accountFlow: Flow<Result<Account>>): FileAddAccountViewModel {
    val getAccountUseCase = mockk<GetAccountUseCase>()
    every { getAccountUseCase(parameter = Unit) } returns accountFlow

    return FileAddAccountViewModel(getAccountUseCase = getAccountUseCase)
}
