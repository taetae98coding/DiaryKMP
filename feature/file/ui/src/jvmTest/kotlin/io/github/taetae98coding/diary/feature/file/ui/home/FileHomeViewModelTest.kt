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
import io.github.taetae98coding.diary.domain.file.usecase.GetUpdatedFileUseCase
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
import kotlin.uuid.Uuid

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

        test("TC-724f16b0-07de-418f-aee5-8701186d717e 계정 상태를 확정하지 않은 동안에는 본문을 비우는 상태다") {
            runTest(mainDispatcher) {
                val accountFlow = MutableStateFlow<Result<Account>>(Result.failure(IllegalStateException("account")))
                val viewModel = viewModel(accountFlow = accountFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe FileHomeUiState.Loading
                    expectNoEvents()
                }
            }
        }

        test("TC-2e7619f5-e91c-4918-b683-b02dd5e0f339 게스트 상태면 로그인 안내 상태다") {
            runTest(mainDispatcher) {
                val accountFlow = MutableStateFlow<Result<Account>>(Result.success(Account.Guest))
                val viewModel = viewModel(accountFlow = accountFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe FileHomeUiState.Loading
                    awaitItem() shouldBe FileHomeUiState.Guest
                }
            }
        }

        test("TC-cfdfb082-926f-421d-82cb-0dc590b0b2bd 계정 상태를 확인하지 못하면 본문을 비우는 상태로 돌아간다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
                val viewModel = viewModel(accountFlow = accountFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe FileHomeUiState.Loading
                    awaitItem() shouldBe FileHomeUiState.User(accountId = account.id)

                    accountFlow.value = Result.failure(IllegalStateException("account"))
                    awaitItem() shouldBe FileHomeUiState.Loading
                }
            }
        }

        test("TC-cf2b2226-bafc-43ec-8eb6-04678edc40f3 세션 갱신 여부와 관계없이 사용자 상태에서는 사용자 화면을 제공한다") {
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
                        awaitItem() shouldBe FileHomeUiState.User(accountId = account.id)
                    }
                }
            }
        }

        test("계정이 바뀌는 중임을 표시하고 끝나면 해제한다") {
            runTest(mainDispatcher) {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val viewModel = viewModel(accountFlow = MutableStateFlow(Result.success(account)))

                viewModel.uiState.test {
                    awaitItem() shouldBe FileHomeUiState.Loading
                    awaitItem() shouldBe FileHomeUiState.User(accountId = account.id)

                    viewModel.startAccountChange()
                    awaitItem() shouldBe FileHomeUiState.User(accountId = account.id, isAccountChanging = true)

                    viewModel.finishAccountChange()
                    awaitItem() shouldBe FileHomeUiState.User(accountId = account.id, isAccountChanging = false)
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

        test("TC-7062b208-3784-4e82-b622-75fc8ac830db 고친 파일은 같은 자리에서 고친 값으로 전달하고 다른 파일은 그대로 둔다") {
            runTest(mainDispatcher) {
                val fileList = List(3) { fixtureMonkey.giveMeOne<DiaryFile>() }
                val updatedFile = fileList[1].copy(title = fixtureMonkey.giveMeOne<String>(), name = fixtureMonkey.giveMeOne<String>())
                val pageFileUseCase = mockk<PageFileUseCase>()
                every { pageFileUseCase(parameter = Unit) } returns flowOf(Result.success(PagingData.from(fileList)))
                val viewModel = viewModel(pageFileUseCase = pageFileUseCase, updatedFileMap = mapOf(updatedFile.id to updatedFile))

                flowOf(viewModel.filePagingData.first()).asSnapshot() shouldBe listOf(fileList[0], updatedFile, fileList[2])
            }
        }

        test("고친 파일 목록을 확인하지 못하면 불러온 파일을 그대로 전달한다") {
            runTest(mainDispatcher) {
                val fileList = List(2) { fixtureMonkey.giveMeOne<DiaryFile>() }
                val pageFileUseCase = mockk<PageFileUseCase>()
                every { pageFileUseCase(parameter = Unit) } returns flowOf(Result.success(PagingData.from(fileList)))
                val getUpdatedFileUseCase = mockk<GetUpdatedFileUseCase>()
                every { getUpdatedFileUseCase(parameter = Unit) } returns flowOf(Result.failure(IllegalStateException()))
                val viewModel = viewModel(pageFileUseCase = pageFileUseCase, getUpdatedFileUseCase = getUpdatedFileUseCase)

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
        updatedFileMap: Map<Uuid, DiaryFile> = emptyMap(),
        getUpdatedFileUseCase: GetUpdatedFileUseCase =
            mockk<GetUpdatedFileUseCase>().also { useCase -> every { useCase(parameter = Unit) } returns flowOf(Result.success(updatedFileMap)) },
    ): FileHomeViewModel {
        val getAccountUseCase = mockk<GetAccountUseCase>()
        every { getAccountUseCase(parameter = Unit) } returns accountFlow

        return FileHomeViewModel(
            getAccountUseCase = getAccountUseCase,
            pageFileUseCase = pageFileUseCase,
            getUpdatedFileUseCase = getUpdatedFileUseCase,
        )
    }
}
