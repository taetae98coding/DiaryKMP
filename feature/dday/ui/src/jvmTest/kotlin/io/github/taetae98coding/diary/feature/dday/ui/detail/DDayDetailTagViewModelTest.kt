@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.dday.ui.detail

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageEntityTagSelectableTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class DDayDetailTagViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-9c3b45db-e816-4692-9c08-404006eab365 태그를 연결하거나 해제하면 상세 대상 디데이와 그 태그의 연결을 바로 저장한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val linkTagId = Arb.kotlinUuid().next()
                val unlinkTagId = Arb.kotlinUuid().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(id = id, addEntityTagUseCase = addEntityTagUseCase, removeEntityTagUseCase = removeEntityTagUseCase)

                viewModel.add(tagId = linkTagId)
                advanceUntilIdle()
                viewModel.remove(tagId = unlinkTagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = id, tagId = linkTagId))
                }
                coVerify(exactly = 1) {
                    removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = id, tagId = unlinkTagId))
                }
            }
        }

        test("TC-a7f12cde-89ba-4d73-a748-f99fd0cd4c12 태그 연결이 다른 경로에서 바뀌면 태그 입력에 최신 연결을 반영한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val storedTag = tag()
                val newTag = tag()
                val tagFlow = MutableStateFlow(listOf(storedTag))
                val viewModel = viewModel(id = id, getEntityTagUseCase = getEntityTagUseCase(id = id, tagFlow = tagFlow.map { tagList -> Result.success(tagList) }))

                viewModel.uiState.test {
                    awaitItem().tagList.shouldBeEmpty()
                    awaitItem().tagList shouldBe listOf(storedTag)

                    tagFlow.value = listOf(storedTag, newTag)

                    awaitItem().tagList shouldBe listOf(storedTag, newTag)
                }
            }
        }

        test("TC-60798db3-cb43-4f8f-ad5d-5d8393d74d24 연결이나 해제의 저장에 실패하면 저장된 연결을 그대로 보여 주고 다시 연결을 요청한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val storedTag = tag()
                val newTagId = Arb.kotlinUuid().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel =
                    viewModel(
                        id = id,
                        getEntityTagUseCase = getEntityTagUseCase(id = id, tagFlow = flowOf(Result.success(listOf(storedTag)))),
                        addEntityTagUseCase = addEntityTagUseCase,
                        removeEntityTagUseCase = removeEntityTagUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem().tagList.shouldBeEmpty()
                    awaitItem().tagList shouldBe listOf(storedTag)

                    viewModel.add(tagId = newTagId)
                    viewModel.remove(tagId = storedTag.id)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value.tagList shouldBe listOf(storedTag)

                    viewModel.add(tagId = newTagId)
                    advanceUntilIdle()

                    coVerify(exactly = 2) { addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = id, tagId = newTagId)) }
                    coVerify(exactly = 1) { removeEntityTagUseCase(parameter = any()) }
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-4ef02a1f-ffb0-4b91-91ce-729ed71bf352 태그 A 연결을 처리하는 동안 태그 A 연결은 다시 요청하지 않고 태그 B 연결은 요청한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val tagAId = Arb.kotlinUuid().next()
                val tagBId = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(Unit)
                }
                val viewModel = viewModel(id = id, addEntityTagUseCase = addEntityTagUseCase)

                viewModel.add(tagId = tagAId)
                runCurrent()
                viewModel.add(tagId = tagAId)
                viewModel.add(tagId = tagBId)
                runCurrent()

                coVerify(exactly = 1) { addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = id, tagId = tagAId)) }
                coVerify(exactly = 1) { addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = id, tagId = tagBId)) }
                gate.complete(Unit)
                advanceUntilIdle()
            }
        }
    }

    private fun getEntityTagUseCase(
        id: Uuid,
        tagFlow: Flow<Result<List<Tag>>>,
    ): GetEntityTagUseCase {
        val useCase = mockk<GetEntityTagUseCase>()
        every { useCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = id)) } returns tagFlow
        return useCase
    }

    private fun viewModel(
        id: Uuid,
        getEntityTagUseCase: GetEntityTagUseCase = getEntityTagUseCase(id = id, tagFlow = flowOf(Result.success(emptyList()))),
        addEntityTagUseCase: AddEntityTagUseCase = mockk(relaxed = true),
        removeEntityTagUseCase: RemoveEntityTagUseCase = mockk(relaxed = true),
    ): EntityDetailTagViewModel =
        EntityDetailTagViewModel(
            kind = EntityTagKind.DDAY,
            entityId = id,
            pageEntityTagSelectableTagUseCase = mockk<PageEntityTagSelectableTagUseCase>(relaxed = true),
            getEntityTagUseCase = getEntityTagUseCase,
            addEntityTagUseCase = addEntityTagUseCase,
            removeEntityTagUseCase = removeEntityTagUseCase,
        )

    private companion object {
        fun tag(): Tag = Arb.tag().next().copy(isDeleted = false)
    }
}
