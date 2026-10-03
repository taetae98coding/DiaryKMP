package io.github.taetae98coding.diary.app.shared

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.usecase.ReconcileFileUploadUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class AppFileUploadViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("확인된 계정 상태를 바뀔 때마다 한 번씩 전달하고 확인하지 못한 동안은 전달하지 않는다") {
            runTest(mainDispatcher) {
                val user = fixtureMonkey.giveMeOne<Account.User>()
                val viewModel =
                    viewModel(
                        accountFlow =
                            flowOf(
                                Result.success(user),
                                Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())),
                                Result.success(user),
                                Result.success(Account.Guest),
                            ),
                    )

                viewModel.uiState.confirmedAccount().test {
                    awaitItem() shouldBe user
                    awaitItem() shouldBe Account.Guest
                    expectNoEvents()
                }
            }
        }

        test("계정을 확인하기 전에는 확인 중 상태다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(accountFlow = flowOf(Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))))

                viewModel.uiState.test {
                    awaitItem() shouldBe AppFileUploadUiState.Loading
                    runCurrent()
                    expectNoEvents()
                }
            }
        }

        test("전달받은 계정 상태로 올리기를 맞춘다") {
            runTest(mainDispatcher) {
                val reconcileFileUploadUseCase = mockk<ReconcileFileUploadUseCase>()
                coEvery { reconcileFileUploadUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(accountFlow = flowOf(), reconcileFileUploadUseCase = reconcileFileUploadUseCase)

                viewModel.reconcile(account = Account.Guest)
                advanceUntilIdle()

                coVerify(exactly = 1) { reconcileFileUploadUseCase(parameter = Account.Guest) }
            }
        }
    }

    private fun viewModel(
        accountFlow: Flow<Result<Account>>,
        reconcileFileUploadUseCase: ReconcileFileUploadUseCase = mockk(),
    ): AppFileUploadViewModel {
        val getAccountUseCase = mockk<GetAccountUseCase>()
        every { getAccountUseCase(parameter = Unit) } returns accountFlow

        return AppFileUploadViewModel(
            getAccountUseCase = getAccountUseCase,
            reconcileFileUploadUseCase = reconcileFileUploadUseCase,
        )
    }

    private fun Flow<AppFileUploadUiState>.confirmedAccount(): Flow<Account> = filterIsInstance<AppFileUploadUiState.Confirmed>().map { uiState -> uiState.account }
}
