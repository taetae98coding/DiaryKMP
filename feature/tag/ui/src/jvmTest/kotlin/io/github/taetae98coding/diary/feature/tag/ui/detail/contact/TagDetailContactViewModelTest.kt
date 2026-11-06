@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.contact

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.contact.ContactListEffect
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.contact.toListItem
import io.github.taetae98coding.diary.domain.contact.usecase.DeleteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.PageTagContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.RestoreContactUseCase
import io.github.taetae98coding.diary.feature.tag.ui.appendFailingPagingData
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
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

class TagDetailContactViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-abaecd8e-df2a-4d8f-aa27-07fbf75f288e TC-2410a5de-600f-4b03-acea-5a08ebced30b 상세 대상 태그로 조회한 연락처 목록을 순서 그대로 전달한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(3) { item() }
                val page = mockk<PageTagContactUseCase>()
                every { page(parameter = PageTagContactUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.NAME)) } returns flowOf(Result.success(PagingData.from(itemList)))
                val viewModel = TagDetailContactViewModel(tagId = tagId, pageContactUseCase = page, deleteContactUseCase = mockk(), restoreContactUseCase = mockk())

                viewModel.contactPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { page(parameter = PageTagContactUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.NAME)) }
            }
        }

        test("TC-6a3a8cbd-f292-4ba7-959b-09070c139824 상세 대상 태그로만 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val otherTagId = Arb.kotlinUuid().next()
                val page = mockk<PageTagContactUseCase>()
                every { page(parameter = any()) } returns flowOf(Result.success(PagingData.from(emptyList<ContactListItem>())))
                val viewModel = TagDetailContactViewModel(tagId = tagId, pageContactUseCase = page, deleteContactUseCase = mockk(), restoreContactUseCase = mockk())

                viewModel.contactPagingData.test {
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { page(parameter = PageTagContactUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.NAME)) }
                verify(exactly = 0) { page(parameter = PageTagContactUseCase.Parameter(tagId = otherTagId, scope = TagScope.SELF, sort = ListSort.NAME)) }
            }
        }

        test("TC-6e914ebb-0357-4604-87f2-6dc4438a9e03 표시 범위를 넓혔다 되돌리면 연락처 목록을 각 범위 기준으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val selfList = List(2) { item() }
                val childList = selfList + List(2) { item() }
                val page = mockk<PageTagContactUseCase>()
                every { page(parameter = PageTagContactUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.NAME)) } returns flowOf(Result.success(PagingData.from(selfList)))
                every { page(parameter = PageTagContactUseCase.Parameter(tagId = tagId, scope = TagScope.CHILD, sort = ListSort.NAME)) } returns flowOf(Result.success(PagingData.from(childList)))
                val viewModel = TagDetailContactViewModel(tagId = tagId, pageContactUseCase = page, deleteContactUseCase = mockk(), restoreContactUseCase = mockk())

                viewModel.contactPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe selfList

                    viewModel.select(scope = TagScope.CHILD)
                    flowOf(awaitItem()).asSnapshot() shouldBe childList

                    viewModel.select(scope = TagScope.SELF)
                    flowOf(awaitItem()).asSnapshot() shouldBe selfList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-8544fcb0-883c-48ea-9d50-9643ddad53b8 정렬을 바꾸면 고른 기준으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val defaultList = List(2) { item() }
                val changedList = defaultList.reversed()
                val page = mockk<PageTagContactUseCase>()
                every { page(parameter = PageTagContactUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.NAME)) } returns flowOf(Result.success(PagingData.from(defaultList)))
                every { page(parameter = PageTagContactUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.RECENTLY_UPDATED)) } returns flowOf(Result.success(PagingData.from(changedList)))
                val viewModel = TagDetailContactViewModel(tagId = tagId, pageContactUseCase = page, deleteContactUseCase = mockk(), restoreContactUseCase = mockk())

                viewModel.contactPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe defaultList

                    viewModel.select(sort = ListSort.RECENTLY_UPDATED)
                    flowOf(awaitItem()).asSnapshot() shouldBe changedList
                    cancelAndIgnoreRemainingEvents()
                }
                viewModel.sortUiState.value.sort shouldBe ListSort.RECENTLY_UPDATED
            }
        }

        test("TC-5ced89c9-a106-47ec-8c29-a056dbd587f3 최초 조회가 실패하면 조회가 끝난 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val page = mockk<PageTagContactUseCase>()
                every { page(parameter = PageTagContactUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.NAME)) } returns flowOf(Result.failure(IllegalStateException(Arb.string().next())))
                val viewModel = TagDetailContactViewModel(tagId = tagId, pageContactUseCase = page, deleteContactUseCase = mockk(), restoreContactUseCase = mockk())

                viewModel.contactPagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList.shouldBeEmpty()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-5ced89c9-a106-47ec-8c29-a056dbd587f3 이어서 불러오기에 실패해도 이미 불러온 연락처을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagContactUseCase>()
                every { page(parameter = PageTagContactUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.NAME)) } returns
                    appendFailingPagingData(firstPage = itemList).map { pagingData -> Result.success(pagingData) }
                val viewModel = TagDetailContactViewModel(tagId = tagId, pageContactUseCase = page, deleteContactUseCase = mockk(), restoreContactUseCase = mockk())

                val snapshot =
                    flowOf(viewModel.contactPagingData.first()).asSnapshot(onError = { ErrorRecovery.RETURN_CURRENT_SNAPSHOT }) {
                        appendScrollWhile { true }
                    }

                snapshot shouldBe itemList
            }
        }

        test("TC-5ced89c9-a106-47ec-8c29-a056dbd587f3 조회가 성공한 뒤 다시 조회하다 실패하면 이전 연락처을 남기지 않고 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = Arb.kotlinUuid().next()
                val itemList = List(2) { item() }
                val page = mockk<PageTagContactUseCase>()
                val resultFlow = MutableStateFlow<Result<PagingData<ContactListItem>>>(Result.success(PagingData.from(itemList)))
                every { page(parameter = PageTagContactUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.NAME)) } returns resultFlow
                val viewModel = TagDetailContactViewModel(tagId = tagId, pageContactUseCase = page, deleteContactUseCase = mockk(), restoreContactUseCase = mockk())

                viewModel.contactPagingData.test {
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

        test("삭제를 처리하는 동안 같은 연락처의 삭제를 다시 요청해도 삭제 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val delete = mockk<DeleteContactUseCase>()
                coEvery { delete(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailContactViewModel(tagId = Arb.kotlinUuid().next(), pageContactUseCase = mockk(), deleteContactUseCase = delete, restoreContactUseCase = mockk())

                viewModel.delete(id = id)
                viewModel.delete(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("복구를 처리하는 동안 같은 연락처의 복구를 다시 요청해도 복구 UseCase는 한 번만 호출한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val restore = mockk<RestoreContactUseCase>()
                coEvery { restore(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = TagDetailContactViewModel(tagId = Arb.kotlinUuid().next(), pageContactUseCase = mockk(), deleteContactUseCase = mockk(), restoreContactUseCase = restore)

                viewModel.restore(id = id)
                viewModel.restore(id = id)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }

        test("TC-139803d7-0f5d-470b-b383-b638e02b0e04 삭제하면 태그 연결 해제가 아니라 그 연락처의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteContactUseCase>()
                coEvery { delete(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailContactViewModel(tagId = Arb.kotlinUuid().next(), pageContactUseCase = mockk(), deleteContactUseCase = delete, restoreContactUseCase = mockk())

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe ContactListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { delete(parameter = id) }
            }
        }

        test("TC-5d539a05-4286-48d4-a612-c38c7c1b9be2 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val delete = mockk<DeleteContactUseCase>()
                coEvery { delete(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = TagDetailContactViewModel(tagId = Arb.kotlinUuid().next(), pageContactUseCase = mockk(), deleteContactUseCase = delete, restoreContactUseCase = mockk())

                viewModel.effect.test {
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-527999d8-fe2f-4834-8ac7-9ba1efd7ee41 실행 취소하면 그 연락처의 삭제를 되돌리는 요청을 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val restore = mockk<RestoreContactUseCase>()
                coEvery { restore(parameter = id) } returns Result.success(1)
                val viewModel = TagDetailContactViewModel(tagId = Arb.kotlinUuid().next(), pageContactUseCase = mockk(), deleteContactUseCase = mockk(), restoreContactUseCase = restore)

                viewModel.restore(id = id)
                advanceUntilIdle()

                coVerify(exactly = 1) { restore(parameter = id) }
            }
        }
    }

    private companion object {
        private fun item(): ContactListItem =
            Arb
                .contact()
                .next()
                .copy(
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                ).toListItem()
    }
}
