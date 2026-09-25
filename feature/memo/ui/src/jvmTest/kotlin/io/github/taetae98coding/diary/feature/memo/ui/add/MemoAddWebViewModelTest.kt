@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.GetSelectedWebUseCase
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebInputUiState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoAddWebViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MEMO-WEB-INPUT-DOMAIN-017 메모리 정리 뒤 새로 만든 화면은 되살린 검색어로 좁힌 목록을 기다리지 않고 바로 보여 주고 대상 전체를 거치지 않는다") {
            runTest(mainDispatcher) {
                val query = "Query${fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)}"
                val allList = List(2) { web() }
                val matchedList = listOf(allList.first())
                val useCase = mockk<PageMemoSelectableWebUseCase>()
                every { useCase(parameter = "") } returns flowOf(Result.success(PagingData.from(allList)))
                every { useCase(parameter = query) } returns flowOf(Result.success(PagingData.from(matchedList)))
                val viewModel = viewModel(pageMemoSelectableWebUseCase = useCase, isListOpened = false)

                // 복원된 화면은 목록을 다시 열면서 되살린 검색어를 처음으로 알려 준다.
                viewModel.webPagingData.test {
                    viewModel.updateQuery(query)
                    runCurrent()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedList
                    cancelAndIgnoreRemainingEvents()
                }
                verify(exactly = 0) { useCase(parameter = "") }
            }
        }

        test("선택한 웹 항목 조회에 실패하면 선택한 웹 항목이 없는 상태를 유지한다") {
            runTest(mainDispatcher) {
                val web = web()
                val viewModel = viewModel(savedWebListFlow = flowOf(Result.failure(IllegalStateException("web error"))))

                viewModel.selectWeb(id = web.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoWebInputUiState()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("웹 선택 목록은 페이지 조회 결과를 그대로 전달한다") {
            runTest(mainDispatcher) {
                val webList = listOf(web(), web())
                val viewModel = viewModel(webPagingFlow = flowOf(Result.success(PagingData.from(webList))))

                val itemList = flowOf(viewModel.webPagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList shouldBe webList
            }
        }

        test("TC-MEMO-WEB-INPUT-FEATURE-020 웹 선택 목록 페이지 조회에 실패해도 선택 상태는 그대로 표시한다") {
            runTest(mainDispatcher) {
                val web = web()
                val viewModel =
                    viewModel(
                        webPagingFlow = flowOf(Result.failure(IllegalStateException("web error"))),
                        savedWebListFlow = flowOf(Result.success(listOf(web))),
                    )
                viewModel.selectWeb(id = web.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList shouldBe listOf(web)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-FEATURE-063 WebDetail 메모 탭에서 진입하면 그 웹 항목만 선택된 상태로 시작한다") {
            runTest(mainDispatcher) {
                val target = web()
                val other = web()
                val viewModel =
                    viewModel(
                        initialWebId = target.id,
                        webPagingFlow = flowOf(Result.success(PagingData.from(listOf(target, other)))),
                        savedWebListFlow = flowOf(Result.success(listOf(target, other))),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList shouldBe listOf(target)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-FEATURE-064 WebDetail 메모 탭에서 진입해도 초기 선택을 해제하거나 더 선택할 수 있다") {
            runTest(mainDispatcher) {
                val target = web()
                val other = web()
                val viewModel =
                    viewModel(
                        initialWebId = target.id,
                        savedWebListFlow = flowOf(Result.success(listOf(target, other))),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().selectedWebList shouldBe listOf(target)

                    viewModel.unselectWeb(id = target.id)
                    viewModel.selectWeb(id = other.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList shouldBe listOf(other)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-DOMAIN-015 삭제된 대상 웹 항목는 WebDetail 메모 탭에서 진입해도 선택되지 않은 상태로 시작한다") {
            runTest(mainDispatcher) {
                val deletedTarget = web()
                val viewModel =
                    viewModel(
                        initialWebId = deletedTarget.id,
                        savedWebListFlow = flowOf(Result.success(emptyList())),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-FEATURE-055 진입하면 선택한 웹 항목이 없는 상태로 시작한다") {
            runTest(mainDispatcher) {
                val webList = listOf(web(), web())
                val viewModel =
                    viewModel(
                        webPagingFlow = flowOf(Result.success(PagingData.from(webList))),
                        savedWebListFlow = flowOf(Result.success(webList)),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-WEB-INPUT-FEATURE-011 TC-MEMO-WEB-INPUT-FEATURE-012 선택한 웹 항목을 즉시 노출하고 해제하면 즉시 제외한다") {
            runTest(mainDispatcher) {
                val web = web()
                val viewModel = viewModel(savedWebListFlow = flowOf(Result.success(listOf(web))))

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList.shouldBeEmpty()

                    viewModel.selectWeb(id = web.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList shouldBe listOf(web)

                    viewModel.unselectWeb(id = web.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-WEB-INPUT-FEATURE-013 한 번에 여러 웹 항목을 선택할 수 있다") {
            runTest(mainDispatcher) {
                val firstWeb = web()
                val secondWeb = web()
                val viewModel = viewModel(savedWebListFlow = flowOf(Result.success(listOf(firstWeb, secondWeb))))

                viewModel.uiState.test {
                    advanceUntilIdle()
                    viewModel.selectWeb(id = firstWeb.id)
                    viewModel.selectWeb(id = secondWeb.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList shouldBe listOf(firstWeb, secondWeb)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-FEATURE-057 화면 구성이 변경되어도 선택한 웹 항목을 유지한다") {
            runTest(mainDispatcher) {
                val web = web()
                val viewModel = viewModel(savedWebListFlow = flowOf(Result.success(listOf(web))))

                viewModel.uiState.test {
                    advanceUntilIdle()
                    viewModel.selectWeb(id = web.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList shouldBe listOf(web)
                    cancelAndIgnoreRemainingEvents()
                }

                // 화면 구성 변경으로 UI가 재생성되어 같은 ViewModel을 새로 구독해도 선택이 유지된다.
                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList shouldBe listOf(web)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-DOMAIN-011 선택한 웹 항목이 삭제되면 선택에서 제외된 것으로 표시된다") {
            runTest(mainDispatcher) {
                val remainingWeb = web()
                val removedWeb = web()
                val savedWebListFlow = MutableStateFlow(Result.success(listOf(remainingWeb, removedWeb)))
                val viewModel = viewModel(savedWebListFlow = savedWebListFlow)

                viewModel.selectWeb(id = removedWeb.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList shouldBe listOf(removedWeb)

                    savedWebListFlow.value = Result.success(listOf(remainingWeb))
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-DATA-018 선택한 뒤 삭제된 웹 항목도 선택으로 남는다") {
            runTest(mainDispatcher) {
                val remainingWeb = web()
                val deletedWeb = web()
                val savedWebListFlow = MutableStateFlow(Result.success(listOf(remainingWeb, deletedWeb)))
                val viewModel = viewModel(savedWebListFlow = savedWebListFlow)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    viewModel.selectWeb(id = remainingWeb.id)
                    viewModel.selectWeb(id = deletedWeb.id)
                    advanceUntilIdle()

                    savedWebListFlow.value = Result.success(listOf(remainingWeb))
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.webIdSet.value shouldBe setOf(remainingWeb.id, deletedWeb.id)
            }
        }

        test("TC-MEMO-WEB-INPUT-DOMAIN-013 자동 선택된 웹 항목의 선택을 해제하면 다시 선택되지 않는다") {
            runTest(mainDispatcher) {
                val addedWeb = web()
                val viewModel = viewModel(savedWebListFlow = flowOf(Result.success(listOf(addedWeb))))

                // WebAdd 화면에서 돌아와 자동 선택된 상태를 만든다.
                viewModel.selectWeb(id = addedWeb.id)
                viewModel.unselectWeb(id = addedWeb.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedWebList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.webIdSet.value.shouldBeEmpty()
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
                val web = web()
                val viewModel = viewModel(savedWebListFlow = flowOf(Result.success(listOf(web))))
                viewModel.selectWeb(id = web.id)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem() shouldBe MemoWebInputUiState(selectedWebList = listOf(web))

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private companion object {
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
            initialWebId: Uuid? = null,
            webPagingFlow: Flow<Result<PagingData<Web>>> = emptyFlow(),
            savedWebListFlow: Flow<Result<List<Web>>> = flowOf(Result.success(emptyList())),
            pageMemoSelectableWebUseCase: PageMemoSelectableWebUseCase =
                mockk<PageMemoSelectableWebUseCase>().apply {
                    every { this@apply(parameter = any()) } returns webPagingFlow
                },
            isListOpened: Boolean = true,
        ): MemoAddWebViewModel {
            // 선택한 식별자로 저장소를 조회하는 동작을 저장된 웹 목록에서 골라내는 방식으로 대신한다.
            val getSelectedWebUseCase = mockk<GetSelectedWebUseCase>()
            every { getSelectedWebUseCase(parameter = any()) } answers {
                val webIdSet = firstArg<Set<Uuid>>()

                if (webIdSet.isEmpty()) {
                    flowOf(Result.success(emptyList()))
                } else {
                    savedWebListFlow.map { result -> result.map { webList -> webList.filter { web -> web.id in webIdSet } } }
                }
            }

            return MemoAddWebViewModel(
                initialWebId = initialWebId,
                pageMemoSelectableWebUseCase = pageMemoSelectableWebUseCase,
                getSelectedWebUseCase = getSelectedWebUseCase,
            ).apply {
                // 화면은 선택 목록을 열 때 검색어를 알려 주므로, 목록이 열린 상태를 만든다.
                if (isListOpened) updateQuery(query = "")
            }
        }
    }
}
