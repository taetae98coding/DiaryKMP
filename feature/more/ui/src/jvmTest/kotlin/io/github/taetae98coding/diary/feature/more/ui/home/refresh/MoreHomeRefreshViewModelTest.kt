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

        test("TC-MORE-HOME-FEATURE-033 앞선 확인이 끝난 뒤 다시 요청하면 사용자 정보 다시 확인을 다시 시작한다") {
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

        test("TC-MORE-HOME-DOMAIN-014 앞선 다시 확인이 끝나기 전에 화면이 다시 표시되면 새로 요청하지 않는다") {
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

        test("TC-MORE-HOME-DOMAIN-013 사용자 정보 다시 확인에 실패해도 계정 표시가 바뀌지 않는다") {
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
