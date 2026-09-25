@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.usecase.PageFileUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-FILE-HOME-FEATURE-006 TC-FILE-HOME-FEATURE-005 계정 상태에 따라 확인 중, 게스트, 사용자 화면을 정한다") {
            runTest(mainDispatcher) {
                val accountFlow = MutableStateFlow<Result<Account>>(Result.failure(IllegalStateException()))
                val viewModel = viewModel(accountFlow = accountFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe FileHomeUiState.Loading

                    accountFlow.value = Result.success(Account.Guest)
                    awaitItem() shouldBe FileHomeUiState.Guest

                    accountFlow.value = Result.success(fixtureMonkey.giveMeOne<Account.User>())
                    awaitItem() shouldBe FileHomeUiState.User

                    accountFlow.value = Result.failure(IllegalStateException())
                    awaitItem() shouldBe FileHomeUiState.Loading
                }
            }
        }

        test("TC-FILE-HOME-DOMAIN-003 세션 갱신 여부와 관계없이 사용자 상태에서는 사용자 화면을 제공한다") {
            runTest(mainDispatcher) {
                listOf(
                    true to false,
                    false to true,
                    false to false,
                ).forEach { (isSessionValid, isSessionPending) ->
                    val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = isSessionValid, isSessionPending = isSessionPending)
                    val viewModel = viewModel(accountFlow = MutableStateFlow(Result.success(account)))

                    viewModel.uiState.test {
                        awaitItem() shouldBe FileHomeUiState.Loading
                        awaitItem() shouldBe FileHomeUiState.User
                    }
                }
            }
        }

        test("조회한 파일을 페이지로 전달한다") {
            runTest(mainDispatcher) {
                val fileList = List(2) { fixtureMonkey.giveMeOne<DiaryFile>() }
                val pageFileUseCase = mockk<PageFileUseCase>()
                every { pageFileUseCase(parameter = Unit) } returns flowOf(Result.success(PagingData.from(fileList)))
                val viewModel = viewModel(pageFileUseCase = pageFileUseCase)

                flowOf(viewModel.filePagingData.first()).asSnapshot() shouldBe fileList
            }
        }

        test("파일 조회가 실패하면 빈 목록을 전달한다") {
            runTest(mainDispatcher) {
                val pageFileUseCase = mockk<PageFileUseCase>()
                every { pageFileUseCase(parameter = Unit) } returns flowOf(Result.failure(IllegalStateException()))
                val viewModel = viewModel(pageFileUseCase = pageFileUseCase)

                flowOf(viewModel.filePagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }
    }

    private fun viewModel(
        accountFlow: MutableStateFlow<Result<Account>> = MutableStateFlow(Result.success(Account.Guest)),
        pageFileUseCase: PageFileUseCase = mockk<PageFileUseCase>().also { useCase -> every { useCase(parameter = Unit) } returns emptyFlow() },
    ): FileHomeViewModel {
        val getAccountUseCase = mockk<GetAccountUseCase>()
        every { getAccountUseCase(parameter = Unit) } returns accountFlow

        return FileHomeViewModel(
            getAccountUseCase = getAccountUseCase,
            pageFileUseCase = pageFileUseCase,
        )
    }
}
