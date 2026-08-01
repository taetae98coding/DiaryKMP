@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoWebUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoWebViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MEMO-DETAIL-FEATURE-059 웹 입력에 저장된 웹 연결이 표시된다") {
            runTest(mainDispatcher) {
                val connectedWeb = web()
                val otherWeb = web()
                val viewModel =
                    viewModel(
                        webPagingFlow = flowOf(Result.success(PagingData.from(listOf(connectedWeb, otherWeb)))),
                        memoWebFlow = flowOf(Result.success(listOf(connectedWeb))),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoWebInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoWebInputUiState(selectedWebList = listOf(connectedWeb))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-060 다른 경로로 저장된 웹 연결이 바뀌면 웹 입력에 반영된다") {
            runTest(mainDispatcher) {
                val web = web()
                val memoWebFlow = MutableStateFlow(Result.success(emptyList<Web>()))
                val viewModel =
                    viewModel(
                        webPagingFlow = flowOf(Result.success(PagingData.from(listOf(web)))),
                        memoWebFlow = memoWebFlow,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoWebInputUiState()
                    advanceUntilIdle()

                    // 연결된 웹 항목이 없는 조회 결과는 초기 상태와 같으므로 새 항목이 방출되지 않는다.
                    viewModel.uiState.value shouldBe MemoWebInputUiState(selectedWebList = emptyList())

                    memoWebFlow.value = Result.success(listOf(web))
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoWebInputUiState(selectedWebList = listOf(web))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-061 웹 연결 변경에 실패하면 별도 안내 없이 저장된 연결을 그대로 표시한다") {
            runTest(mainDispatcher) {
                val connectedWeb = web()
                val otherWeb = web()
                val addMemoWebUseCase = mockk<AddMemoWebUseCase>()
                coEvery { addMemoWebUseCase(any<AddMemoWebUseCase.Parameter>()) } returns Result.failure(IllegalStateException("save error"))
                val removeMemoWebUseCase = mockk<RemoveMemoWebUseCase>()
                coEvery { removeMemoWebUseCase(any<RemoveMemoWebUseCase.Parameter>()) } returns Result.failure(IllegalStateException("save error"))
                val viewModel =
                    viewModel(
                        webPagingFlow = flowOf(Result.success(PagingData.from(listOf(connectedWeb, otherWeb)))),
                        memoWebFlow = flowOf(Result.success(listOf(connectedWeb))),
                        addMemoWebUseCase = addMemoWebUseCase,
                        removeMemoWebUseCase = removeMemoWebUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoWebInputUiState()
                    advanceUntilIdle()

                    val savedUiState = expectMostRecentItem()
                    savedUiState shouldBe MemoWebInputUiState(selectedWebList = listOf(connectedWeb))

                    viewModel.selectWeb(webId = otherWeb.id)
                    viewModel.unselectWeb(webId = connectedWeb.id)
                    advanceUntilIdle()

                    // 변경 실패는 오류 상태로 노출하지 않고 저장된 연결 표시를 그대로 둔다.
                    expectNoEvents()
                    viewModel.uiState.value shouldBe savedUiState
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-WEB-INPUT-DOMAIN-005 삭제된 웹 항목은 선택한 것으로 표시하지 않는다") {
            runTest(mainDispatcher) {
                val deletedWeb = web()
                val memoWebFlow = MutableStateFlow(Result.success(listOf(deletedWeb)))
                val viewModel = viewModel(memoWebFlow = memoWebFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoWebInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoWebInputUiState(selectedWebList = listOf(deletedWeb))

                    // 웹 항목이 삭제되면 조회 결과에서 함께 빠진다.
                    memoWebFlow.value = Result.success(emptyList())
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoWebInputUiState(selectedWebList = emptyList())
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-WEB-INPUT-DOMAIN-006 웹 항목이 복구되면 유지되어 있던 선택이 다시 나타난다") {
            runTest(mainDispatcher) {
                val web = web()
                val memoWebFlow = MutableStateFlow(Result.success(emptyList<Web>()))
                val viewModel = viewModel(memoWebFlow = memoWebFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoWebInputUiState()
                    advanceUntilIdle()
                    viewModel.uiState.value shouldBe MemoWebInputUiState(selectedWebList = emptyList())

                    memoWebFlow.value = Result.success(listOf(web))
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoWebInputUiState(selectedWebList = listOf(web))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-WEB-INPUT-DATA-002 TC-MEMO-WEB-INPUT-DOMAIN-007 선택 목록이 준비되지 않아도 선택한 웹 항목을 표시한다") {
            runTest(mainDispatcher) {
                val connectedWeb = web()
                val viewModel =
                    viewModel(
                        webPagingFlow = emptyFlow(),
                        memoWebFlow = flowOf(Result.success(listOf(connectedWeb))),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoWebInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoWebInputUiState(selectedWebList = listOf(connectedWeb))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("웹 선택 목록 조회 결과를 그대로 전달한다") {
            runTest(mainDispatcher) {
                val webList = List(2) { web() }
                val viewModel = viewModel(webPagingFlow = flowOf(Result.success(PagingData.from(webList))))

                val itemList = flowOf(viewModel.webPagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList shouldBe webList
            }
        }

        test("TC-MEMO-WEB-INPUT-FEATURE-020 웹 선택 목록 조회에 실패하면 선택 상태만 그대로 표시한다") {
            runTest(mainDispatcher) {
                val connectedWeb = web()
                val viewModel =
                    viewModel(
                        webPagingFlow = flowOf(Result.failure(IllegalStateException("web error"))),
                        memoWebFlow = flowOf(Result.success(listOf(connectedWeb))),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoWebInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoWebInputUiState(selectedWebList = listOf(connectedWeb))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("저장된 웹 연결 조회에 실패하면 선택한 웹 항목이 없는 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(memoWebFlow = flowOf(Result.failure(IllegalStateException("memo web error"))))

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoWebInputUiState()
                    advanceUntilIdle()

                    viewModel.uiState.value.selectedWebList
                        .shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-WEB-INPUT-FEATURE-011 웹 항목을 선택하면 그 메모와 웹 항목의 연결 추가를 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val webId = fixtureMonkey.giveMeOne<Uuid>()
                val addMemoWebUseCase = mockk<AddMemoWebUseCase>(relaxed = true)
                val viewModel = viewModel(id = id, addMemoWebUseCase = addMemoWebUseCase)

                viewModel.selectWeb(webId = webId)
                advanceUntilIdle()

                coVerify(exactly = 1) { addMemoWebUseCase(AddMemoWebUseCase.Parameter(memoId = id, webId = webId)) }
            }
        }

        test("TC-MEMO-WEB-INPUT-FEATURE-012 웹 선택을 해제하면 그 메모와 웹 항목의 연결 제거를 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val webId = fixtureMonkey.giveMeOne<Uuid>()
                val removeMemoWebUseCase = mockk<RemoveMemoWebUseCase>(relaxed = true)
                val viewModel = viewModel(id = id, removeMemoWebUseCase = removeMemoWebUseCase)

                viewModel.unselectWeb(webId = webId)
                advanceUntilIdle()

                coVerify(exactly = 1) { removeMemoWebUseCase(RemoveMemoWebUseCase.Parameter(memoId = id, webId = webId)) }
            }
        }

        test("TC-MEMO-DETAIL-DOMAIN-011 앞선 웹 연결 변경이 처리 중이어도 뒤이은 웹 연결 변경을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val firstWebId = fixtureMonkey.giveMeOne<Uuid>()
                val secondWebId = fixtureMonkey.giveMeOne<Uuid>()
                val completion = CompletableDeferred<Result<Unit>>()
                val addMemoWebUseCase = mockk<AddMemoWebUseCase>()
                coEvery { addMemoWebUseCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(id = id, addMemoWebUseCase = addMemoWebUseCase)

                viewModel.selectWeb(webId = firstWebId)
                runCurrent()
                viewModel.selectWeb(webId = secondWebId)
                runCurrent()
                completion.complete(Result.success(Unit))
                advanceUntilIdle()

                coVerify(exactly = 1) { addMemoWebUseCase(AddMemoWebUseCase.Parameter(memoId = id, webId = firstWebId)) }
                coVerify(exactly = 1) { addMemoWebUseCase(AddMemoWebUseCase.Parameter(memoId = id, webId = secondWebId)) }
            }
        }

        test("TC-MEMO-WEB-INPUT-DATA-004 검색어가 바뀌면 새 검색어 기준으로 선택 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val allWebList = List(2) { web() }
                val matchedWebList = listOf(allWebList.first())
                val pageMemoSelectableWebUseCase = mockk<PageMemoSelectableWebUseCase>()
                every { pageMemoSelectableWebUseCase(parameter = "") } returns flowOf(Result.success(PagingData.from(allWebList)))
                every { pageMemoSelectableWebUseCase(parameter = SEARCH_QUERY) } returns flowOf(Result.success(PagingData.from(matchedWebList)))
                val viewModel = viewModel(pageMemoSelectableWebUseCase = pageMemoSelectableWebUseCase)

                viewModel.webPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allWebList

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedWebList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-WEB-INPUT-DOMAIN-011 검색어를 바꿔도 웹 입력에 표시하는 웹 항목은 그대로다") {
            runTest(mainDispatcher) {
                val connectedWebList = List(2) { web() }
                val viewModel = viewModel(memoWebFlow = flowOf(Result.success(connectedWebList)))

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoWebInputUiState()
                    advanceUntilIdle()
                    expectMostRecentItem() shouldBe MemoWebInputUiState(selectedWebList = connectedWebList)

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    public companion object {
        private const val SEARCH_QUERY = "Wiki"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun web(): Web =
            fixtureMonkey
                .giveMeKotlinBuilder<Web>()
                .setExp(Web::isDeleted, false)
                .setExp(Web::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Web::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun viewModel(
            id: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            webPagingFlow: Flow<Result<PagingData<Web>>> = emptyFlow(),
            memoWebFlow: Flow<Result<List<Web>>> = emptyFlow(),
            addMemoWebUseCase: AddMemoWebUseCase = mockk(relaxed = true),
            removeMemoWebUseCase: RemoveMemoWebUseCase = mockk(relaxed = true),
            pageMemoSelectableWebUseCase: PageMemoSelectableWebUseCase =
                mockk<PageMemoSelectableWebUseCase>().apply {
                    every { this@apply(parameter = any()) } returns webPagingFlow
                },
        ): MemoWebViewModel {
            val getMemoWebUseCase = mockk<GetMemoWebUseCase>()
            every { getMemoWebUseCase(any()) } returns memoWebFlow

            return MemoWebViewModel(
                id = id,
                pageMemoSelectableWebUseCase = pageMemoSelectableWebUseCase,
                getMemoWebUseCase = getMemoWebUseCase,
                addMemoWebUseCase = addMemoWebUseCase,
                removeMemoWebUseCase = removeMemoWebUseCase,
            )
        }
    }
}
