@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.web.ui.detail

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.domain.web.usecase.FetchWebPageUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class WebDetailPageViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-WEB-DETAIL-FEATURE-004 저장된 URL과 요청 헤더로 웹 페이지를 한 번 불러온다") {
            runTest(mainDispatcher) {
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.success(webPage())
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase)

                viewModel.load()
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = URL, headerList = HEADER_LIST))
                }
            }
        }

        test("TC-WEB-DETAIL-FEATURE-001 대상 웹 항목이 조회되기 전에는 웹 페이지를 요청하지 않는다") {
            runTest(mainDispatcher) {
                val id = Uuid.random()
                val webFlow = MutableStateFlow<Result<Web?>>(Result.success(null))
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.success(webPage())
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase, id = id, webFlow = webFlow)

                viewModel.load()
                advanceUntilIdle()

                coVerify(exactly = 0) { fetchWebPageUseCase(parameter = any()) }
                viewModel.uiState.value shouldBe WebDetailPageUiState.Loading

                webFlow.value = Result.success(web(id = id))
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = URL, headerList = HEADER_LIST))
                }
            }
        }

        test("TC-WEB-DETAIL-FEATURE-005 TC-WEB-DETAIL-FEATURE-006 불러오는 동안 진행 상태를 유지하고 성공하면 받은 웹 페이지를 노출한다") {
            runTest(mainDispatcher) {
                val webPage = webPage()
                val response = CompletableDeferred<Result<WebPage>>()
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } coAnswers { response.await() }
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailPageUiState.Loading

                    viewModel.load()
                    advanceUntilIdle()
                    expectNoEvents()

                    response.complete(Result.success(webPage))
                    advanceUntilIdle()

                    awaitItem() shouldBe WebDetailPageUiState.Content(page = webPage)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-WEB-DETAIL-FEATURE-007 TC-WEB-DETAIL-DOMAIN-010 불러오기에 실패하면 실패 상태를 노출한다") {
            runTest(mainDispatcher) {
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase)

                viewModel.load()
                advanceUntilIdle()

                viewModel.uiState.value shouldBe WebDetailPageUiState.Failure
            }
        }

        test("TC-WEB-DETAIL-FEATURE-009 TC-WEB-DETAIL-FEATURE-010 다시 시도하면 같은 URL과 요청 헤더로 다시 불러온다") {
            runTest(mainDispatcher) {
                val webPage = webPage()
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returnsMany
                    listOf(Result.failure(IllegalStateException()), Result.success(webPage))
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase)

                viewModel.load()
                advanceUntilIdle()
                viewModel.uiState.value shouldBe WebDetailPageUiState.Failure

                viewModel.retry()
                advanceUntilIdle()

                viewModel.uiState.value shouldBe WebDetailPageUiState.Content(page = webPage)
                coVerify(exactly = 2) {
                    fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = URL, headerList = HEADER_LIST))
                }
            }
        }

        test("TC-WEB-DETAIL-FEATURE-011 다시 시도에 실패하면 다시 실패 상태를 노출하고 또 시도할 수 있다") {
            runTest(mainDispatcher) {
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase)

                viewModel.load()
                advanceUntilIdle()

                repeat(2) {
                    viewModel.retry()
                    advanceUntilIdle()
                    viewModel.uiState.value shouldBe WebDetailPageUiState.Failure
                }

                coVerify(exactly = 3) { fetchWebPageUseCase(parameter = any()) }
            }
        }

        test("TC-WEB-DETAIL-DOMAIN-011 불러오는 동안 전달된 다시 시도 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val response = CompletableDeferred<Result<WebPage>>()
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } coAnswers { response.await() }
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase)

                viewModel.load()
                advanceUntilIdle()
                viewModel.retry()
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchWebPageUseCase(parameter = any()) }

                response.complete(Result.success(webPage()))
                advanceUntilIdle()
                viewModel.retry()
                advanceUntilIdle()

                coVerify(exactly = 2) { fetchWebPageUseCase(parameter = any()) }
            }
        }

        test("코루틴이 실행되기 전에 다시 시도가 이어져도 중복 요청하지 않는다") {
            runTest(mainDispatcher) {
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.success(webPage())
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase)

                viewModel.load()
                viewModel.retry()
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchWebPageUseCase(parameter = any()) }
            }
        }

        test("TC-WEB-DETAIL-DOMAIN-040 불러온 적이 없으면 수정으로 URL이 바뀌어도 요청하지 않는다") {
            runTest(mainDispatcher) {
                val id = Uuid.random()
                val webFlow = MutableStateFlow<Result<Web?>>(Result.success(web(id = id)))
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.success(webPage())
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase, id = id, webFlow = webFlow)

                webFlow.value = Result.success(web(id = id, url = OTHER_URL))
                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 0) { fetchWebPageUseCase(parameter = any()) }
                viewModel.uiState.value shouldBe WebDetailPageUiState.Loading

                viewModel.load()
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = OTHER_URL, headerList = HEADER_LIST))
                }
            }
        }

        test("TC-WEB-DETAIL-FEATURE-013 TC-WEB-DETAIL-DOMAIN-007 TC-WEB-DETAIL-DOMAIN-012 TC-WEB-DETAIL-DOMAIN-039 다시 불러오기는 응답 본문 방식이 처음 될 때 한 번만 일어난다") {
            runTest(mainDispatcher) {
                val webPage = webPage()
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.success(webPage)
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase)

                viewModel.load()
                advanceUntilIdle()
                viewModel.load()
                viewModel.load()
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchWebPageUseCase(parameter = any()) }
                viewModel.uiState.value shouldBe WebDetailPageUiState.Content(page = webPage)
            }
        }

        test("TC-WEB-DETAIL-FEATURE-030 수정으로 저장된 URL이나 요청 헤더가 바뀌면 새 값으로 다시 불러온다") {
            runTest(mainDispatcher) {
                val id = Uuid.random()
                val webFlow = MutableStateFlow<Result<Web?>>(Result.success(web(id = id)))
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.success(webPage())
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase, id = id, webFlow = webFlow)

                viewModel.load()
                advanceUntilIdle()

                webFlow.value = Result.success(web(id = id, url = OTHER_URL))
                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = OTHER_URL, headerList = HEADER_LIST))
                }

                val changedHeaderList = HEADER_LIST + WebHeader(name = "Accept", value = "text/html")
                webFlow.value = Result.success(web(id = id, url = OTHER_URL, headerList = changedHeaderList))
                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = OTHER_URL, headerList = changedHeaderList))
                }
                coVerify(exactly = 3) { fetchWebPageUseCase(parameter = any()) }
            }
        }

        test("TC-WEB-DETAIL-FEATURE-031 수정으로 제목이나 설명만 바뀌면 다시 불러오지 않는다") {
            runTest(mainDispatcher) {
                val id = Uuid.random()
                val webPage = webPage()
                val webFlow = MutableStateFlow<Result<Web?>>(Result.success(web(id = id)))
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.success(webPage)
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase, id = id, webFlow = webFlow)

                viewModel.load()
                advanceUntilIdle()

                val changed = web(id = id)
                webFlow.value = Result.success(changed.copy(detail = changed.detail.copy(title = "changed-title", description = "changed")))
                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchWebPageUseCase(parameter = any()) }
                viewModel.uiState.value shouldBe WebDetailPageUiState.Content(page = webPage)
            }
        }

        test("TC-WEB-DETAIL-DOMAIN-030 불러오는 동안 수정으로 URL이 바뀌면 진행 중이던 불러오기를 대체한다") {
            runTest(mainDispatcher) {
                val id = Uuid.random()
                val firstPage = webPage()
                val secondPage = webPage()
                val firstResponse = CompletableDeferred<Result<WebPage>>()
                val webFlow = MutableStateFlow<Result<Web?>>(Result.success(web(id = id)))
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery {
                    fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = URL, headerList = HEADER_LIST))
                } coAnswers { firstResponse.await() }
                coEvery {
                    fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = OTHER_URL, headerList = HEADER_LIST))
                } returns Result.success(secondPage)
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase, id = id, webFlow = webFlow)

                viewModel.load()
                advanceUntilIdle()
                viewModel.uiState.value shouldBe WebDetailPageUiState.Loading

                webFlow.value = Result.success(web(id = id, url = OTHER_URL))
                viewModel.refresh()
                advanceUntilIdle()

                viewModel.uiState.value shouldBe WebDetailPageUiState.Content(page = secondPage)

                firstResponse.complete(Result.success(firstPage))
                advanceUntilIdle()

                viewModel.uiState.value shouldBe WebDetailPageUiState.Content(page = secondPage)
            }
        }

        test("TC-WEB-DETAIL-DATA-006 화면에 다시 진입하면 이전에 받은 웹 페이지를 복원하지 않고 다시 요청한다") {
            runTest(mainDispatcher) {
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.success(webPage())

                val first = viewModel(fetchWebPageUseCase = fetchWebPageUseCase)
                first.load()
                advanceUntilIdle()

                val second = viewModel(fetchWebPageUseCase = fetchWebPageUseCase)

                second.uiState.value shouldBe WebDetailPageUiState.Loading

                second.load()
                advanceUntilIdle()

                coVerify(exactly = 2) { fetchWebPageUseCase(parameter = any()) }
            }
        }
    }

    private companion object {
        private const val URL = "https://developer.android.com/"
        private const val OTHER_URL = "https://kotlinlang.org/"

        private val HEADER_LIST =
            listOf(
                WebHeader(name = "X-Diary", value = "first"),
                WebHeader(name = "X-Diary", value = "second"),
            )

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            fetchWebPageUseCase: FetchWebPageUseCase,
            id: Uuid = Uuid.random(),
            webFlow: Flow<Result<Web?>> = flowOf(Result.success(web(id = id))),
        ): WebDetailPageViewModel {
            val findWebUseCase = mockk<FindWebUseCase>()
            every { findWebUseCase(parameter = id) } returns webFlow

            return WebDetailPageViewModel(
                id = id,
                findWebUseCase = findWebUseCase,
                fetchWebPageUseCase = fetchWebPageUseCase,
            )
        }

        // FixtureMonkey가 Instant를 생성하지 못하므로 웹 항목은 직접 만든다.
        private fun web(
            id: Uuid,
            url: String = URL,
            headerList: List<WebHeader> = HEADER_LIST,
        ): Web =
            Web(
                id = id,
                detail =
                    WebDetail(
                        title = "제목-${fixtureMonkey.giveMeOne<String>()}",
                        description = fixtureMonkey.giveMeOne<String>(),
                        url = url,
                        headerList = headerList,
                    ),
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            )

        private fun webPage(): WebPage = fixtureMonkey.giveMeOne<WebPage>()
    }
}
