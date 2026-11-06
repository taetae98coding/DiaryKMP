@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.contact.ui.detail

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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class ContactDetailTagViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-aec8ea46-3fa8-4477-9fa6-7cdaa5dd73e6 상세 대상 연락처에 저장된 연결의 태그를 연결 대상으로 표시한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val tagList = List(2) { tag() }
                val getEntityTagUseCase = getEntityTagUseCase(id = id, tagFlow = flowOf(Result.success(tagList)))
                val viewModel = viewModel(id = id, getEntityTagUseCase = getEntityTagUseCase)

                viewModel.uiState.test {
                    awaitItem().tagList.shouldBeEmpty()
                    awaitItem().tagList shouldBe tagList
                }
                verify(exactly = 1) { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.CONTACT, entityId = id)) }
            }
        }

        test("TC-09618443-81e9-47f0-b10c-34c95bdbd604 저장된 연결이 바뀌면 연결 대상 표시도 바뀐다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val tag = tag()
                val tagFlow = MutableStateFlow(emptyList<Tag>())
                val viewModel =
                    viewModel(
                        id = id,
                        getEntityTagUseCase = getEntityTagUseCase(id = id, tagFlow = tagFlow.map { tagList -> Result.success(tagList) }),
                    )

                viewModel.uiState.test {
                    awaitItem().tagList.shouldBeEmpty()

                    tagFlow.value = listOf(tag)

                    awaitItem().tagList shouldBe listOf(tag)
                }
            }
        }

        test("TC-d5e3a8b9-6ed0-47d7-8c7c-9b0b8fcc02b9 태그를 연결하거나 해제하면 상세 대상 연락처과 그 태그의 연결을 바로 저장한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val tagId = Arb.kotlinUuid().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(id = id, addEntityTagUseCase = addEntityTagUseCase, removeEntityTagUseCase = removeEntityTagUseCase)

                viewModel.add(tagId = tagId)
                advanceUntilIdle()
                viewModel.remove(tagId = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.CONTACT, entityId = id, tagId = tagId))
                }
                coVerify(exactly = 1) {
                    removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.CONTACT, entityId = id, tagId = tagId))
                }
            }
        }

        test("TC-dec1f632-db8d-41d7-98a2-272ec0a13778 연결이나 해제의 저장에 실패하면 저장된 연결을 그대로 보여 주고 다시 연결할 수 있다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val storedTag = tag()
                val newTagId = Arb.kotlinUuid().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException("연결 저장 실패"))
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException("해제 저장 실패"))
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

                    coVerify(exactly = 2) { addEntityTagUseCase(parameter = any()) }
                    coVerify(exactly = 1) { removeEntityTagUseCase(parameter = any()) }
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private fun getEntityTagUseCase(
        id: Uuid,
        tagFlow: Flow<Result<List<Tag>>>,
    ): GetEntityTagUseCase {
        val useCase = mockk<GetEntityTagUseCase>()
        every { useCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.CONTACT, entityId = id)) } returns tagFlow
        return useCase
    }

    private fun viewModel(
        id: Uuid,
        getEntityTagUseCase: GetEntityTagUseCase = getEntityTagUseCase(id = id, tagFlow = flowOf(Result.success(emptyList()))),
        addEntityTagUseCase: AddEntityTagUseCase = mockk(relaxed = true),
        removeEntityTagUseCase: RemoveEntityTagUseCase = mockk(relaxed = true),
    ): EntityDetailTagViewModel =
        EntityDetailTagViewModel(
            kind = EntityTagKind.CONTACT,
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
