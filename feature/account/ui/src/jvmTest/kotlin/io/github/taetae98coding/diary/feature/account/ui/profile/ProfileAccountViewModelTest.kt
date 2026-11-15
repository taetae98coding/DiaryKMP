package io.github.taetae98coding.diary.feature.account.ui.profile

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class ProfileAccountViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-135005f9-705b-4f5f-9663-76b1c2abb1da 계정이 게스트가 되면 게스트 상태를 알린다") {
            runTest(mainDispatcher) {
                val accountFlow = MutableStateFlow(Result.success<Account>(Arb.accountUser().next()))
                val viewModel = ProfileAccountViewModel(getAccountUseCase = getAccountUseCase(accountFlow))

                viewModel.uiState.test {
                    awaitItem() shouldBe ProfileAccountUiState.Loading
                    awaitItem() shouldBe ProfileAccountUiState.User

                    accountFlow.value = Result.success(Account.Guest)

                    awaitItem() shouldBe ProfileAccountUiState.Guest
                }
            }
        }

        test("TC-39cad1b0-fe53-4dc0-b394-b2d34e99ad42 세션 갱신 여부만 바뀌거나 확인에 실패하면 게스트로 알리지 않는다") {
            runTest(mainDispatcher) {
                val user = Arb.accountUser().next().copy(isSessionValid = true)
                val accountFlow = MutableStateFlow(Result.success<Account>(user))
                val viewModel = ProfileAccountViewModel(getAccountUseCase = getAccountUseCase(accountFlow))

                viewModel.uiState.test {
                    awaitItem() shouldBe ProfileAccountUiState.Loading
                    awaitItem() shouldBe ProfileAccountUiState.User

                    accountFlow.value = Result.success(user.copy(isSessionValid = false))
                    expectNoEvents()

                    accountFlow.value = Result.failure(IllegalStateException("account"))
                    awaitItem() shouldBe ProfileAccountUiState.Loading
                }
            }
        }
    }
}

private fun getAccountUseCase(accountFlow: MutableStateFlow<Result<Account>>): GetAccountUseCase =
    mockk<GetAccountUseCase>().also { useCase ->
        every { useCase() } returns accountFlow
    }
