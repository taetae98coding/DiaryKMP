@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.web.ui.detail.page

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

        test("TC-be0b371e-cba7-4075-80dc-9745d8a16755 저장된 URL과 요청 헤더로 웹 페이지를 한 번 불러온다") {
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

        test("TC-43a42162-c097-4b5c-923a-12c3dae536be 대상 웹 항목이 조회되기 전에는 웹 페이지를 요청하지 않는다") {
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

        test("TC-371f5358-32db-4580-a22e-17a0533b7eed TC-0773a117-92e3-4d97-b948-d2f53547bb40 불러오는 동안 진행 상태를 유지하고 성공하면 받은 웹 페이지를 노출한다") {
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

        test("TC-acb441f8-9fa4-4db2-9d10-094a22bce6c8 TC-78770bfe-3f95-4faf-bfad-b4ad0d438fa5 불러오기에 실패하면 실패 상태를 노출한다") {
            runTest(mainDispatcher) {
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase)

                viewModel.load()
                advanceUntilIdle()

                viewModel.uiState.value shouldBe WebDetailPageUiState.Failure
            }
        }

        test("TC-04087944-e03c-4f27-a985-1fd686cb0d0b TC-971bac07-ae1d-4a33-8b69-f0ce47d67854 다시 시도하면 같은 URL과 요청 헤더로 다시 불러온다") {
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

        test("TC-63c2ef8b-e9e4-4a47-8570-154898ab0ddc 다시 시도에 실패하면 다시 실패 상태를 노출하고 또 시도할 수 있다") {
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

        test("TC-6d2cd3ce-0709-4540-830a-47fa35713946 불러오는 동안 전달된 다시 시도 요청은 처리하지 않는다") {
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

        test("TC-e174e111-11cd-479b-a1df-d6c7dff55e22 응답 본문 방식을 한 번도 열지 않았으면 수정으로 URL이 바뀌어도 요청하지 않는다") {
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

        test("TC-e174e111-11cd-479b-a1df-d6c7dff55e22 응답 본문 방식에서 불러온 뒤 URL 방식으로 되돌렸으면 수정으로 바뀐 URL로 곧바로 한 번 다시 불러온다") {
            runTest(mainDispatcher) {
                val id = Uuid.random()
                val reloadedPage = webPage()
                val webFlow = MutableStateFlow<Result<Web?>>(Result.success(web(id = id)))
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery {
                    fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = URL, headerList = HEADER_LIST))
                } returns Result.success(webPage())
                coEvery {
                    fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = OTHER_URL, headerList = HEADER_LIST))
                } returns Result.success(reloadedPage)
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase, id = id, webFlow = webFlow)
                viewModel.load()
                advanceUntilIdle()

                // URL 방식으로 되돌린 동안에는 화면이 불러오기를 요청하지 않으므로 수정 성공만 전달된다.
                webFlow.value = Result.success(web(id = id, url = OTHER_URL))
                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = OTHER_URL, headerList = HEADER_LIST))
                }

                viewModel.load()
                advanceUntilIdle()

                coVerify(exactly = 2) { fetchWebPageUseCase(parameter = any()) }
                viewModel.uiState.value shouldBe WebDetailPageUiState.Content(page = reloadedPage)
            }
        }

        test("TC-bbf59348-1a5e-45fd-9e10-c40393dd2eb5 응답 본문 방식에서 본문이 표시된 뒤 다른 경로에서 URL과 요청 헤더가 바뀌어도 다시 불러오지 않는다") {
            runTest(mainDispatcher) {
                val id = Uuid.random()
                val webPage = webPage()
                val webFlow = MutableStateFlow<Result<Web?>>(Result.success(web(id = id)))
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.success(webPage)
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase, id = id, webFlow = webFlow)
                viewModel.load()
                advanceUntilIdle()

                webFlow.value = Result.success(web(id = id, url = OTHER_URL, headerList = HEADER_LIST + WebHeader(name = "header-${fixtureMonkey.giveMeOne<String>()}", value = fixtureMonkey.giveMeOne<String>())))
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchWebPageUseCase(parameter = any()) }
                viewModel.uiState.value shouldBe WebDetailPageUiState.Content(page = webPage)
            }
        }

        test("TC-a713daf3-3ff6-4fc0-abbe-d7c41d92c3e1 다른 경로에서 URL이 바뀐 뒤 제목만 고쳐 수정해도 최신 저장 URL로 한 번 다시 불러온다") {
            runTest(mainDispatcher) {
                val id = Uuid.random()
                val webFlow = MutableStateFlow<Result<Web?>>(Result.success(web(id = id)))
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.success(webPage())
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase, id = id, webFlow = webFlow)
                viewModel.load()
                advanceUntilIdle()
                webFlow.value = Result.success(web(id = id, url = OTHER_URL))
                advanceUntilIdle()

                val externallyChanged = web(id = id, url = OTHER_URL)
                webFlow.value = Result.success(externallyChanged.copy(detail = externallyChanged.detail.copy(title = "changed-${fixtureMonkey.giveMeOne<String>()}")))
                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = OTHER_URL, headerList = HEADER_LIST))
                }
                coVerify(exactly = 2) { fetchWebPageUseCase(parameter = any()) }
            }
        }

        test("TC-4c01f467-8ee6-4c7f-a123-85e51bc5b229 URL 방식에서는 웹 페이지를 원격에 요청하지 않는다") {
            runTest(mainDispatcher) {
                val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
                val viewModel = viewModel(fetchWebPageUseCase = fetchWebPageUseCase)

                // URL 방식은 불러오기를 시작하지 않으므로 화면이 유지되고 새로고침이 와도 요청이 나가지 않는다.
                advanceUntilIdle()
                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 0) { fetchWebPageUseCase(parameter = any()) }
                viewModel.uiState.value shouldBe WebDetailPageUiState.Loading
            }
        }

        test("TC-a2c71837-5d51-4d47-986b-1aeffc65e620 TC-6f63c6a1-5218-42f2-8910-ff66d9bbc2a0 TC-729f3b16-d41f-495c-9073-6c52554ab482 TC-8b030e9d-b400-494a-b80d-348edf2757f5 다시 불러오기는 응답 본문 방식이 처음 될 때 한 번만 일어난다") {
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

        test("TC-d9d1473a-519d-46ec-8521-5665c3a60e57 수정으로 저장된 URL이나 요청 헤더가 바뀌면 새 값으로 다시 불러온다") {
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

        test("TC-63504c0a-256e-4d9c-a1c6-b84bb7fbe880 수정으로 제목이나 설명만 바뀌면 다시 불러오지 않는다") {
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

        test("TC-80a9be81-29bd-420f-baf7-0cee45d95556 불러오는 동안 수정으로 URL이 바뀌면 진행 중이던 불러오기를 대체한다") {
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

        test("TC-75700e2a-da51-4a61-b90d-83072dd5076e 다시 진입한 화면은 이전에 받은 웹 페이지 없이 시작하고 응답 본문 방식이 되면 다시 요청한다") {
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
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                createdAt = fixtureMonkey.giveMeOne<Instant>(),
            )

        private fun webPage(): WebPage = fixtureMonkey.giveMeOne<WebPage>()
    }
}
