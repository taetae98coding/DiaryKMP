@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.web.ui.detail

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.web.exception.WebHeaderNameBlankException
import io.github.taetae98coding.diary.domain.web.usecase.DeleteWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.UpdateWebUseCase
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class WebDetailViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-845589d7-8df3-4e2c-8fa5-445b64cc7c43 조회에 성공하면 저장된 내용을 내용 표시 상태로 노출한다") {
            runTest(mainDispatcher) {
                val web = web()
                val viewModel = viewModel(id = web.id, webFlow = flowOf(Result.success(web)))

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailUiState.Loading
                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-43a42162-c097-4b5c-923a-12c3dae536be TC-e5efe348-ca20-44cd-a171-8a705ae43ad0 웹 항목을 조회할 수 없으면 조회 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()

                listOf(
                    Result.failure(IllegalStateException()),
                    Result.success(null),
                ).forEach { result ->
                    val viewModel = viewModel(id = id, webFlow = flowOf(result))

                    viewModel.uiState.test {
                        awaitItem() shouldBe WebDetailUiState.Loading
                        expectNoEvents()
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-1e17e1be-6bfc-43c0-a465-18f55e142110 저장 내용이 바뀌면 화면에 표시하는 저장 내용을 갱신한다") {
            runTest(mainDispatcher) {
                val web = web()
                val changedWeb = web.copy(detail = web.detail.copy(title = "changed-${fixtureMonkey.giveMeOne<String>()}"))
                val webFlow = MutableStateFlow<Result<Web?>>(Result.success(web))
                val viewModel = viewModel(id = web.id, webFlow = webFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailUiState.Loading
                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail)

                    webFlow.value = Result.success(changedWeb)

                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = changedWeb.detail)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-dd1f0b31-bb28-44ee-bdb8-ba10d4882649 내용 표시 상태가 된 뒤 삭제 상태가 되어도 내용 표시를 유지한다") {
            runTest(mainDispatcher) {
                val web = web()
                val webFlow = MutableStateFlow<Result<Web?>>(Result.success(web))
                val viewModel = viewModel(id = web.id, webFlow = webFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailUiState.Loading
                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail)

                    // 조회는 삭제 여부를 가리지 않으므로 삭제 상태가 되어도 같은 웹 항목이 계속 조회된다.
                    webFlow.value = Result.success(web.copy(isDeleted = true))

                    expectNoEvents()
                    viewModel.uiState.value shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-f72a8b9d-ec5c-4038-bf4e-d657de3c7e7e 삭제 상태인 웹 항목으로 진입해도 내용 표시 상태가 된다") {
            runTest(mainDispatcher) {
                val web = web().copy(isDeleted = true)
                val viewModel = viewModel(id = web.id, webFlow = flowOf(Result.success(web)))

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailUiState.Loading
                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-20f251f9-ef69-48b6-bf83-9574256c4eb5 수정을 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val web = web()
                val response = CompletableDeferred<Result<Int>>()
                val updateWebUseCase = mockk<UpdateWebUseCase>()
                coEvery { updateWebUseCase(parameter = any()) } coAnswers { response.await() }
                val viewModel = viewModel(id = web.id, webFlow = flowOf(Result.success(web)), updateWebUseCase = updateWebUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailUiState.Loading
                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail)

                    viewModel.update(detail = web.detail)

                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail, isUpdateInProgress = true)

                    response.complete(Result.success(1))

                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail, isUpdateInProgress = false)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-d837592b-4ade-4baa-be57-80723a8a8fc0 수정에 성공하면 수정 성공을 한 번 알린다") {
            runTest(mainDispatcher) {
                val web = web()
                val detail = web.detail.copy(title = "changed-${fixtureMonkey.giveMeOne<String>()}")
                val updateWebUseCase = mockk<UpdateWebUseCase>()
                coEvery { updateWebUseCase(parameter = UpdateWebUseCase.Parameter(id = web.id, detail = detail)) } returns Result.success(1)
                val viewModel = viewModel(id = web.id, webFlow = flowOf(Result.success(web)), updateWebUseCase = updateWebUseCase)

                viewModel.effect.test {
                    viewModel.update(detail = detail)

                    awaitItem() shouldBe WebDetailEffect.UpdateSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-80d1bb60-0307-4a05-a303-6dc83c8d888e 수정에 실패하면 화면을 떠나지 않고 진행 상태만 해제한다") {
            runTest(mainDispatcher) {
                val web = web()
                val updateWebUseCase = mockk<UpdateWebUseCase>()
                coEvery { updateWebUseCase(parameter = any()) } returnsMany
                    listOf(Result.failure(IllegalStateException()), Result.success(1))
                val viewModel = viewModel(id = web.id, webFlow = flowOf(Result.success(web)), updateWebUseCase = updateWebUseCase)
                val effectList = mutableListOf<WebDetailEffect>()
                backgroundScope.launch { viewModel.effect.toList(effectList) }

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailUiState.Loading
                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail)

                    viewModel.update(detail = web.detail)
                    advanceUntilIdle()

                    expectNoEvents()
                    effectList.shouldBeEmpty()

                    viewModel.update(detail = web.detail)
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                advanceUntilIdle()
                effectList shouldBe listOf(WebDetailEffect.UpdateSucceeded)
            }
        }

        test("TC-05af06b9-5c30-4045-9213-f5dc39a90a0c 이름이 비어 있는 헤더 항목이 있으면 헤더 이름 안내를 알린다") {
            runTest(mainDispatcher) {
                val web = web()
                val updateWebUseCase = mockk<UpdateWebUseCase>()
                coEvery { updateWebUseCase(parameter = any()) } returns Result.failure(WebHeaderNameBlankException())
                val viewModel = viewModel(id = web.id, webFlow = flowOf(Result.success(web)), updateWebUseCase = updateWebUseCase)

                viewModel.effect.test {
                    viewModel.update(detail = web.detail)

                    awaitItem() shouldBe WebDetailEffect.HeaderNameBlank
                    expectNoEvents()
                }
            }
        }

        test("TC-ec9f0505-2026-44e7-a5a7-f0c2a69dd91a 수정을 처리하는 동안 전달된 수정 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val web = web()
                val response = CompletableDeferred<Result<Int>>()
                val updateWebUseCase = mockk<UpdateWebUseCase>()
                coEvery { updateWebUseCase(parameter = any()) } coAnswers { response.await() }
                val viewModel = viewModel(id = web.id, webFlow = flowOf(Result.success(web)), updateWebUseCase = updateWebUseCase)

                viewModel.update(detail = web.detail)
                advanceUntilIdle()
                viewModel.update(detail = web.detail)
                advanceUntilIdle()

                coVerify(exactly = 1) { updateWebUseCase(parameter = any()) }

                response.complete(Result.success(1))
                advanceUntilIdle()
                viewModel.update(detail = web.detail)
                advanceUntilIdle()

                coVerify(exactly = 2) { updateWebUseCase(parameter = any()) }
            }
        }

        test("TC-f50242c8-bbbb-429a-89b0-db9792e02cdb 수정과 삭제는 서로를 제한하지 않는다") {
            runTest(mainDispatcher) {
                val web = web()
                val updateResponse = CompletableDeferred<Result<Int>>()
                val updateWebUseCase = mockk<UpdateWebUseCase>()
                coEvery { updateWebUseCase(parameter = any()) } coAnswers { updateResponse.await() }
                val deleteWebUseCase = mockk<DeleteWebUseCase>()
                coEvery { deleteWebUseCase(parameter = web.id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = web.id,
                        webFlow = flowOf(Result.success(web)),
                        updateWebUseCase = updateWebUseCase,
                        deleteWebUseCase = deleteWebUseCase,
                    )

                viewModel.update(detail = web.detail)
                advanceUntilIdle()
                viewModel.delete()
                advanceUntilIdle()

                coVerify(exactly = 1) { deleteWebUseCase(parameter = web.id) }

                updateResponse.complete(Result.success(1))
                advanceUntilIdle()
            }
        }

        test("TC-2d129da1-13b5-4b78-8c21-7f4d7b3e30ce 삭제를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val web = web()
                val response = CompletableDeferred<Result<Int>>()
                val deleteWebUseCase = mockk<DeleteWebUseCase>()
                coEvery { deleteWebUseCase(parameter = web.id) } coAnswers { response.await() }
                val viewModel = viewModel(id = web.id, webFlow = flowOf(Result.success(web)), deleteWebUseCase = deleteWebUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailUiState.Loading
                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail)

                    viewModel.delete()

                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail, isDeleteInProgress = true)

                    response.complete(Result.success(1))

                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail, isDeleteInProgress = false)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-35ae4b6d-4860-445b-9469-595ebb1fac84 삭제에 성공하면 삭제 성공을 한 번 알린다") {
            runTest(mainDispatcher) {
                val web = web()
                val deleteWebUseCase = mockk<DeleteWebUseCase>()
                coEvery { deleteWebUseCase(parameter = web.id) } returns Result.success(1)
                val viewModel = viewModel(id = web.id, webFlow = flowOf(Result.success(web)), deleteWebUseCase = deleteWebUseCase)

                viewModel.effect.test {
                    viewModel.delete()

                    awaitItem() shouldBe WebDetailEffect.DeleteSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-194e9db6-56b9-4867-bf54-be413b7526cd 삭제에 실패하면 화면을 떠나지 않고 진행 상태만 해제한다") {
            runTest(mainDispatcher) {
                val web = web()
                val deleteWebUseCase = mockk<DeleteWebUseCase>()
                coEvery { deleteWebUseCase(parameter = web.id) } returnsMany
                    listOf(Result.failure(IllegalStateException()), Result.success(1))
                val viewModel = viewModel(id = web.id, webFlow = flowOf(Result.success(web)), deleteWebUseCase = deleteWebUseCase)
                val effectList = mutableListOf<WebDetailEffect>()
                backgroundScope.launch { viewModel.effect.toList(effectList) }

                viewModel.uiState.test {
                    awaitItem() shouldBe WebDetailUiState.Loading
                    awaitItem() shouldBe WebDetailUiState.Content(id = web.id, detail = web.detail)

                    viewModel.delete()
                    advanceUntilIdle()

                    expectNoEvents()
                    effectList.shouldBeEmpty()

                    viewModel.delete()
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                advanceUntilIdle()
                effectList shouldBe listOf(WebDetailEffect.DeleteSucceeded)
            }
        }

        test("TC-08d45c00-a313-4303-9ccf-077bafbe007a 삭제를 처리하는 동안 전달된 삭제 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val web = web()
                val response = CompletableDeferred<Result<Int>>()
                val deleteWebUseCase = mockk<DeleteWebUseCase>()
                coEvery { deleteWebUseCase(parameter = web.id) } coAnswers { response.await() }
                val viewModel = viewModel(id = web.id, webFlow = flowOf(Result.success(web)), deleteWebUseCase = deleteWebUseCase)

                viewModel.delete()
                advanceUntilIdle()
                viewModel.delete()
                advanceUntilIdle()

                coVerify(exactly = 1) { deleteWebUseCase(parameter = web.id) }

                response.complete(Result.success(1))
                advanceUntilIdle()
                viewModel.delete()
                advanceUntilIdle()

                coVerify(exactly = 2) { deleteWebUseCase(parameter = web.id) }
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            id: Uuid,
            webFlow: Flow<Result<Web?>>,
            updateWebUseCase: UpdateWebUseCase = mockk(relaxed = true),
            deleteWebUseCase: DeleteWebUseCase = mockk(relaxed = true),
        ): WebDetailViewModel {
            val findWebUseCase = mockk<FindWebUseCase>()
            every { findWebUseCase(parameter = id) } returns webFlow

            return WebDetailViewModel(
                id = id,
                updateWebUseCase = updateWebUseCase,
                deleteWebUseCase = deleteWebUseCase,
                findWebUseCase = findWebUseCase,
            )
        }

        private fun web(): Web =
            Web(
                id = Uuid.random(),
                detail = fixtureMonkey.giveMeKotlinBuilder<WebDetail>().sample(),
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                createdAt = fixtureMonkey.giveMeOne<Instant>(),
            )
    }
}
