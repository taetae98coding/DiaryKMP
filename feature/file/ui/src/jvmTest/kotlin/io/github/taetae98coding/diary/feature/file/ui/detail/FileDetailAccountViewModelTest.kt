@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.detail

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileDetailAccountViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-97c06e67-2ee1-4376-8ad9-61e5b2b76adc 처음 확인한 사용자에서 테스트 데이터의 계정으로 바뀌면 계정이 바뀐 상태다") {
            runTest(mainDispatcher) {
                listOf<Account>(Account.Guest, fixtureMonkey.giveMeOne<Account.User>()).forEach { changed ->
                    val owner = fixtureMonkey.giveMeOne<Account.User>()
                    val accountFlow = MutableStateFlow<Result<Account>>(Result.success(owner))
                    val viewModel = viewModel(accountFlow = accountFlow)

                    viewModel.uiState.test {
                        awaitItem() shouldBe FileDetailAccountUiState(isAccountChanged = false)
                        runCurrent()
                        expectNoEvents()

                        accountFlow.value = Result.success(changed)
                        awaitItem() shouldBe FileDetailAccountUiState(isAccountChanged = true)
                    }
                }
            }
        }

        test("TC-dd6a353f-2bc5-4709-a362-218ee5cdfd32 계정을 확인하지 못하거나 같은 사용자의 세션 상태만 바뀌면 계정이 바뀐 것으로 보지 않는다") {
            runTest(mainDispatcher) {
                val owner = fixtureMonkey.giveMeOne<Account.User>()
                val accountFlow = MutableStateFlow<Result<Account>>(Result.success(owner))
                val viewModel = viewModel(accountFlow = accountFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe FileDetailAccountUiState(isAccountChanged = false)

                    accountFlow.value = Result.failure(IllegalStateException("account"))
                    runCurrent()
                    accountFlow.value = Result.success(owner.copy(isSessionValid = !owner.isSessionValid))
                    runCurrent()

                    expectNoEvents()
                }
            }
        }
    }

    private fun viewModel(accountFlow: MutableStateFlow<Result<Account>>): FileDetailAccountViewModel {
        val getAccountUseCase = mockk<GetAccountUseCase>()
        every { getAccountUseCase(parameter = Unit) } returns accountFlow

        return FileDetailAccountViewModel(getAccountUseCase = getAccountUseCase)
    }
}
