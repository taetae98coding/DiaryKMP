@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.more.ui.home.refresh

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.RefreshUserDataUseCase
import io.github.taetae98coding.diary.feature.more.ui.home.account.MoreHomeAccountUiState
import io.github.taetae98coding.diary.feature.more.ui.home.account.MoreHomeAccountViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class MoreHomeRefreshViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-5ce779c1-3217-4697-ae8b-04ba136bc0c1 앞선 확인이 끝난 뒤 다시 요청하면 사용자 정보 다시 확인을 다시 시작한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<RefreshUserDataUseCase>()
                coEvery { useCase(Unit) } returns Result.success(Unit)
                val viewModel = MoreHomeRefreshViewModel(refreshUserDataUseCase = useCase)

                viewModel.refresh()
                advanceUntilIdle()
                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(Unit) }
            }
        }

        test("TC-c5aea484-1261-4318-aa62-e5bc5bbfd98a 앞선 다시 확인이 끝나기 전에 화면이 다시 표시되면 새로 요청하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Unit>()
                val useCase = mockk<RefreshUserDataUseCase>()
                coEvery { useCase(Unit) } coAnswers { Result.success(completion.await()) }
                val viewModel = MoreHomeRefreshViewModel(refreshUserDataUseCase = useCase)

                viewModel.refresh()
                runCurrent()
                viewModel.refresh()
                runCurrent()
                completion.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(Unit) }
            }
        }

        test("TC-f53d767c-fd09-4084-95b0-1e6259b3ef2b 확인 중에 기다리는 동안 화면이 다시 표시되어도 새로 요청하지 않는다") {
            runTest(mainDispatcher) {
                val accountDetermined = CompletableDeferred<Unit>()
                val useCase = mockk<RefreshUserDataUseCase>()
                coEvery { useCase(Unit) } coAnswers { Result.success(accountDetermined.await()) }
                val viewModel = MoreHomeRefreshViewModel(refreshUserDataUseCase = useCase)

                viewModel.refresh()
                runCurrent()
                viewModel.refresh()
                runCurrent()
                accountDetermined.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(Unit) }
            }
        }

        test("TC-1b93af08-c181-480d-a9bb-6477302074c7 사용자 정보 다시 확인에 실패해도 계정 표시가 바뀌지 않는다") {
            runTest(mainDispatcher) {
                val email = "diary-" + fixtureMonkey.giveMeOne<String>()
                val profileImage = fixtureMonkey.giveMeOne<String>()
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(Unit) } returns
                    flowOf(
                        Result.success<Account>(
                            Account.User(
                                id = fixtureMonkey.giveMeOne<Uuid>(),
                                profileImage = profileImage,
                                email = email,
                                isSessionValid = fixtureMonkey.giveMeOne<Boolean>(),
                            ),
                        ),
                    )
                val accountViewModel = MoreHomeAccountViewModel(getAccountUseCase = getAccountUseCase)
                val useCase = mockk<RefreshUserDataUseCase>()
                coEvery { useCase(Unit) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = MoreHomeRefreshViewModel(refreshUserDataUseCase = useCase)

                accountViewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeAccountUiState.Loading
                    awaitItem() shouldBe MoreHomeAccountUiState.User(profileImage = profileImage, email = email)

                    viewModel.refresh()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { useCase(Unit) }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
