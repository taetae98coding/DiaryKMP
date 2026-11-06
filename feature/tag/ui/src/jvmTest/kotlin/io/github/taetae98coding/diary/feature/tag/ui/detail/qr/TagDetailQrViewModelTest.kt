@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.qr

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.qr.QrListEffect
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.domain.qr.usecase.DeleteQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageTagQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.RestoreQrUseCase
import io.github.taetae98coding.diary.feature.tag.ui.appendFailingPagingData
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class TagDetailQrViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-5fff354a-21c6-411a-b65b-7f197f29cc97 TC-f05da0f4-a05a-4132-b0f2-d5ffaf5d75dc 상세 대상 태그로 조회한 QR 목록을 순서 그대로 전달한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(3) { item() }
                val page = mockk<PageTagQrUseCase>()
                every { page(parameter = PageTagQrUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns flowOf(Result.success(PagingData.from(itemList)))
                val viewModel = TagDetailQrViewModel(tagId = tagId, pageQrUseCase = page, deleteQrUseCase = mockk(), restoreQrUseCase = mockk())

                viewModel.qrPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { page(parameter = PageTagQrUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) }
            }
        }

        test("TC-e60cad61-9194-44dc-9a62-9e41bf7edd10 상세 대상 태그로만 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val otherTagId = Arb.kotlinUuid().next()
                val page = mockk<PageTagQrUseCase>()
                every { page(parameter = any()) } returns flowOf(Result.success(PagingData.from(emptyList<Qr>())))
                val viewModel = TagDetailQrViewModel(tagId = tagId, pageQrUseCase = page, deleteQrUseCase = mockk(), restoreQrUseCase = mockk())

                viewModel.qrPagingData.test {
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { page(parameter = PageTagQrUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) }
                verify(exactly = 0) { page(parameter = PageTagQrUseCase.Parameter(tagId = otherTagId, scope = TagScope.SELF)) }
            }
        }

        test("TC-6e914ebb-0357-4604-87f2-6dc4438a9e03 표시 범위를 넓혔다 되돌리면 QR 목록을 각 범위 기준으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val selfList = List(2) { item() }
                val childList = selfList + List(2) { item() }
                val page = mockk<PageTagQrUseCase>()
                every { page(parameter = PageTagQrUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns flowOf(Result.success(PagingData.from(selfList)))
                every { page(parameter = PageTagQrUseCase.Parameter(tagId = tagId, scope = TagScope.CHILD)) } returns flowOf(Result.success(PagingData.from(childList)))
                val viewModel = TagDetailQrViewModel(tagId = tagId, pageQrUseCase = page, deleteQrUseCase = mockk(), restoreQrUseCase = mockk())

                viewModel.qrPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe selfList

                    viewModel.select(scope = TagScope.CHILD)
                    flowOf(awaitItem()).asSnapshot() shouldBe childList

                    viewModel.select(scope = TagScope.SELF)
                    flowOf(awaitItem()).asSnapshot() shouldBe selfList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-47d4bfa3-5421-437e-a3a8-54024453fc20 최초 조회가 실패하면 조회가 끝난 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val page = mockk<PageTagQrUseCase>()
                every { page(parameter = PageTagQrUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns flowOf(Result.failure(IllegalStateException(Arb.string().next())))
                val viewModel = TagDetailQrViewModel(tagId = tagId, pageQrUseCase = page, deleteQrUseCase = mockk(), restoreQrUseCase = mockk())

                viewModel.qrPagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList.shouldBeEmpty()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-47d4bfa3-5421-437e-a3a8-54024453fc20 이어서 불러오기에 실패해도 이미 불러온 QR을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagQrUseCase>()
                every { page(parameter = PageTagQrUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns
                    appendFailingPagingData(firstPage = itemList).map { pagingData -> Result.success(pagingData) }
                val viewModel = TagDetailQrViewModel(tagId = tagId, pageQrUseCase = page, deleteQrUseCase = mockk(), restoreQrUseCase = mockk())

                val snapshot =
                    flowOf(viewModel.qrPagingData.first()).asSnapshot(onError = { ErrorRecovery.RETURN_CURRENT_SNAPSHOT }) {
                        appendScrollWhile { true }
                    }

                snapshot shouldBe itemList
            }
        }

        test("TC-47d4bfa3-5421-437e-a3a8-54024453fc20 조회가 성공한 뒤 다시 조회하다 실패하면 이전 QR을 남기지 않고 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagQrUseCase>()
                val resultFlow = MutableStateFlow<Result<PagingData<Qr>>>(Result.success(PagingData.from(itemList)))
                every { page(parameter = PageTagQrUseCase.Parameter(tagId = tagId, scope = TagScope.SELF)) } returns resultFlow
                val viewModel = TagDetailQrViewModel(tagId = tagId, pageQrUseCase = page, deleteQrUseCase = mockk(), restoreQrUseCase = mockk())

                viewModel.qrPagingData.test {
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList

                    resultFlow.value = Result.failure(IllegalStateException(Arb.string().next()))
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    expectNoEvents()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("삭제를 처리하는 동안 같은 QR의 삭제를 다시 요청해도 삭제 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val delete = mockk<DeleteQrUseCase>()
                coEvery { delete(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailQrViewModel(tagId = Arb.kotlinUuid().next(), pageQrUseCase = mockk(), deleteQrUseCase = delete, restoreQrUseCase = mockk())

                viewModel.delete(id = id)
                viewModel.delete(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("복구를 처리하는 동안 같은 QR의 복구를 다시 요청해도 복구 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val restore = mockk<RestoreQrUseCase>()
                coEvery { restore(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailQrViewModel(tagId = Arb.kotlinUuid().next(), pageQrUseCase = mockk(), deleteQrUseCase = mockk(), restoreQrUseCase = restore)

                viewModel.restore(id = id)
                viewModel.restore(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }

        test("TC-e08dea60-9f1d-4018-b2b0-785fd14aa058 삭제하면 태그 연결 해제가 아니라 그 QR의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteQrUseCase>()
                coEvery { delete(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailQrViewModel(tagId = Arb.kotlinUuid().next(), pageQrUseCase = mockk(), deleteQrUseCase = delete, restoreQrUseCase = mockk())

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe QrListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("TC-569294fc-3d8d-4379-b64f-c5bdac476ee6 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteQrUseCase>()
                coEvery { delete(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = TagDetailQrViewModel(tagId = Arb.kotlinUuid().next(), pageQrUseCase = mockk(), deleteQrUseCase = delete, restoreQrUseCase = mockk())

                viewModel.effect.test {
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-4f6d4638-975b-4fb6-a3bf-96b0e7567c9b 실행 취소하면 그 QR의 삭제를 되돌리는 요청을 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val restore = mockk<RestoreQrUseCase>()
                coEvery { restore(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailQrViewModel(tagId = Arb.kotlinUuid().next(), pageQrUseCase = mockk(), deleteQrUseCase = mockk(), restoreQrUseCase = restore)

                viewModel.restore(id = id)
                advanceUntilIdle()

                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }
    }

    private companion object {
        private fun item(): Qr = Arb.qr(isDeleted = false).next()
    }
}
