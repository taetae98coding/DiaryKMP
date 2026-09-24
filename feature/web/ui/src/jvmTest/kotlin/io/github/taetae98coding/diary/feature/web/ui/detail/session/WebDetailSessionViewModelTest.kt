@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.web.ui.detail.session

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.ImportChromeSessionUseCase
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

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class WebDetailSessionViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-WEB-DETAIL-FEATURE-047 로그인 정보를 가져오는 동안에는 준비 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val web = web()
                val pending = CompletableDeferred<Result<Unit>>()
                val importUseCase = mockk<ImportChromeSessionUseCase>()
                coEvery { importUseCase(web.detail.url) } coAnswers { pending.await() }
                val viewModel = viewModel(web = web, importUseCase = importUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailSessionUiState.Preparing
                    advanceUntilIdle()
                    expectNoEvents()
                }

                coVerify(exactly = 1) { importUseCase(web.detail.url) }
            }
        }

        test("TC-WEB-DETAIL-FEATURE-048 TC-WEB-DETAIL-FEATURE-050 로그인 정보를 가져오면 그 주소를 준비된 상태로 제공하고 알리지 않는다") {
            runTest(mainDispatcher) {
                val web = web()
                val importUseCase = importUseCase(web.detail.url, Result.success(Unit))
                val viewModel = viewModel(web = web, importUseCase = importUseCase)

                viewModel.effect.test {
                    viewModel.uiState.test {
                        awaitItem() shouldBe WebDetailSessionUiState.Preparing
                        awaitItem() shouldBe WebDetailSessionUiState.Prepared(url = web.detail.url)
                    }

                    advanceUntilIdle()
                    expectNoEvents()
                }

                coVerify(exactly = 1) { importUseCase(web.detail.url) }
            }
        }

        test("TC-WEB-DETAIL-FEATURE-049 로그인 정보를 가져오지 못하면 한 번 알리고 그 주소를 준비된 상태로 제공한다") {
            runTest(mainDispatcher) {
                val web = web()
                val importUseCase = importUseCase(web.detail.url, Result.failure(IllegalStateException("import failed")))
                val viewModel = viewModel(web = web, importUseCase = importUseCase)

                viewModel.effect.test {
                    viewModel.uiState.test {
                        awaitItem() shouldBe WebDetailSessionUiState.Preparing
                        awaitItem() shouldBe WebDetailSessionUiState.Prepared(url = web.detail.url)
                    }

                    awaitItem() shouldBe WebDetailSessionEffect.ImportFailed
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-WEB-DETAIL-FEATURE-051 저장된 URL이 바뀌면 새 주소의 로그인 정보를 다시 가져온 뒤 준비된 상태로 제공한다") {
            runTest(mainDispatcher) {
                val web = web()
                val changedUrl = "https://changed.example.com/${fixtureMonkey.giveMeOne<Int>()}"
                val webFlow = MutableStateFlow<Result<Web?>>(Result.success(web))
                val importUseCase = mockk<ImportChromeSessionUseCase>()
                coEvery { importUseCase(any()) } returns Result.success(Unit)
                val viewModel = viewModel(id = web.id, webFlow = webFlow, importUseCase = importUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailSessionUiState.Preparing
                    awaitItem() shouldBe WebDetailSessionUiState.Prepared(url = web.detail.url)

                    webFlow.value = Result.success(web.copy(detail = web.detail.copy(title = "changed-title")))
                    advanceUntilIdle()
                    expectNoEvents()

                    webFlow.value = Result.success(web.copy(detail = web.detail.copy(url = changedUrl)))

                    awaitItem() shouldBe WebDetailSessionUiState.Preparing
                    awaitItem() shouldBe WebDetailSessionUiState.Prepared(url = changedUrl)
                }

                coVerify(exactly = 1) { importUseCase(web.detail.url) }
                coVerify(exactly = 1) { importUseCase(changedUrl) }
            }
        }

        test("TC-WEB-DETAIL-DOMAIN-043 화면이 재생성되어 다시 구독해도 로그인 정보를 다시 가져오지 않는다") {
            runTest(mainDispatcher) {
                val web = web()
                val importUseCase = importUseCase(web.detail.url, Result.success(Unit))
                val viewModel = viewModel(web = web, importUseCase = importUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailSessionUiState.Preparing
                    awaitItem() shouldBe WebDetailSessionUiState.Prepared(url = web.detail.url)
                }

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailSessionUiState.Prepared(url = web.detail.url)
                    advanceUntilIdle()
                    expectNoEvents()
                }

                coVerify(exactly = 1) { importUseCase(web.detail.url) }
            }
        }

        test("웹 항목을 조회할 수 없으면 준비 중 상태를 유지하고 가져오지 않는다") {
            runTest(mainDispatcher) {
                val importUseCase = mockk<ImportChromeSessionUseCase>()
                val viewModel =
                    viewModel(
                        id = fixtureMonkey.giveMeOne<Uuid>(),
                        webFlow = flowOf(Result.failure(IllegalStateException()), Result.success(null)),
                        importUseCase = importUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailSessionUiState.Preparing
                    advanceUntilIdle()
                    expectNoEvents()
                }

                coVerify(exactly = 0) { importUseCase(any()) }
            }
        }
    }

    private fun viewModel(
        web: Web,
        importUseCase: ImportChromeSessionUseCase,
    ): WebDetailSessionViewModel = viewModel(id = web.id, webFlow = flowOf(Result.success(web)), importUseCase = importUseCase)

    private fun viewModel(
        id: Uuid,
        webFlow: Flow<Result<Web?>>,
        importUseCase: ImportChromeSessionUseCase,
    ): WebDetailSessionViewModel {
        val findWebUseCase = mockk<FindWebUseCase>()
        every { findWebUseCase(id) } returns webFlow

        return WebDetailSessionViewModel(
            id = id,
            findWebUseCase = findWebUseCase,
            importChromeSessionUseCase = importUseCase,
        )
    }

    private fun importUseCase(
        url: String,
        result: Result<Unit>,
    ): ImportChromeSessionUseCase {
        val useCase = mockk<ImportChromeSessionUseCase>()
        coEvery { useCase(url) } returns result

        return useCase
    }

    // FixtureMonkey가 Instant를 생성하지 못하므로 웹 항목은 직접 만든다.
    private fun web(): Web {
        val detail =
            fixtureMonkey
                .giveMeKotlinBuilder<WebDetail>()
                .setExp(WebDetail::url, "https://example.com/${fixtureMonkey.giveMeOne<Int>()}")
                .sample()

        return Web(
            id = Uuid.random(),
            detail = detail,
            isDeleted = false,
            updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
        )
    }
}
