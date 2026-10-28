@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.form

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.AddTagLinkUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetLinkedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagLinkSelectableTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveTagLinkUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.INPUT_IDLE_DELAY
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class TagDetailLinkViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-35995748-4049-436d-af96-2e0466241b9e 저장된 연결의 도착 태그를 연결 대상으로 표시한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val linkedTagList = List(2) { tag() }
                val viewModel = viewModel(id = id, linkedTagFlow = flowOf(Result.success(linkedTagList)))

                viewModel.uiState.test {
                    awaitItem().linkedTagList.shouldBeEmpty()
                    awaitItem().linkedTagList shouldBe linkedTagList
                }
            }
        }

        test("TC-f24a43a2-e275-4227-bca6-8d2f9793e98f 저장된 연결이 바뀌면 연결 대상 표시도 바뀐다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val tag = tag()
                val linkedTagFlow = MutableStateFlow(emptyList<Tag>())
                val viewModel =
                    viewModel(
                        id = id,
                        linkedTagFlow = linkedTagFlow.map { tagList -> Result.success(tagList) },
                    )

                viewModel.uiState.test {
                    awaitItem().linkedTagList.shouldBeEmpty()

                    linkedTagFlow.value = listOf(tag)

                    awaitItem().linkedTagList shouldBe listOf(tag)
                }
            }
        }

        test("TC-664344b5-d1b5-422d-a12c-ea7dcbe45e2e 태그를 연결하면 상세 대상 태그를 출발 태그로 연결을 만든다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val toTagId = fixtureMonkey.giveMeOne<Uuid>()
                val addTagLinkUseCase = mockk<AddTagLinkUseCase>()
                coEvery { addTagLinkUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(id = id, addTagLinkUseCase = addTagLinkUseCase)

                viewModel.link(tagId = toTagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    addTagLinkUseCase(parameter = AddTagLinkUseCase.Parameter(fromTagId = id, toTagId = toTagId))
                }
            }
        }

        test("TC-6e01ef75-9bf3-4f5d-9dc4-60ea526233da 연결을 해제하면 상세 대상 태그를 출발 태그로 연결을 해제한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val toTagId = fixtureMonkey.giveMeOne<Uuid>()
                val removeTagLinkUseCase = mockk<RemoveTagLinkUseCase>()
                coEvery { removeTagLinkUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(id = id, removeTagLinkUseCase = removeTagLinkUseCase)

                viewModel.unlink(tagId = toTagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    removeTagLinkUseCase(parameter = RemoveTagLinkUseCase.Parameter(fromTagId = id, toTagId = toTagId))
                }
            }
        }

        test("TC-7b2f9da6-18a5-4195-a84e-51403986e506 연결이나 해제의 저장에 실패하면 저장된 연결을 그대로 보여 주고 다시 연결할 수 있다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val linkedTag = tag()
                val otherTagId = fixtureMonkey.giveMeOne<Uuid>()
                val addTagLinkUseCase = mockk<AddTagLinkUseCase>()
                coEvery { addTagLinkUseCase(parameter = any()) } returns Result.failure(IllegalStateException("저장 실패"))
                val removeTagLinkUseCase = mockk<RemoveTagLinkUseCase>()
                coEvery { removeTagLinkUseCase(parameter = any()) } returns Result.failure(IllegalStateException("저장 실패"))
                val viewModel =
                    viewModel(
                        id = id,
                        linkedTagFlow = flowOf(Result.success(listOf(linkedTag))),
                        addTagLinkUseCase = addTagLinkUseCase,
                        removeTagLinkUseCase = removeTagLinkUseCase,
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().linkedTagList shouldBe listOf(linkedTag)

                    viewModel.link(tagId = otherTagId)
                    advanceUntilIdle()
                    viewModel.unlink(tagId = linkedTag.id)
                    advanceUntilIdle()
                    viewModel.link(tagId = otherTagId)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value.linkedTagList shouldBe listOf(linkedTag)
                }

                coVerify(exactly = 2) {
                    addTagLinkUseCase(parameter = AddTagLinkUseCase.Parameter(fromTagId = id, toTagId = otherTagId))
                }
                coVerify(exactly = 1) {
                    removeTagLinkUseCase(parameter = RemoveTagLinkUseCase.Parameter(fromTagId = id, toTagId = linkedTag.id))
                }
            }
        }

        test("연결과 해제는 이어서 실행해도 모두 반영된다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val firstToTagId = fixtureMonkey.giveMeOne<Uuid>()
                val secondToTagId = fixtureMonkey.giveMeOne<Uuid>()
                val addTagLinkUseCase = mockk<AddTagLinkUseCase>()
                coEvery { addTagLinkUseCase(parameter = any()) } returns Result.success(Unit)
                val removeTagLinkUseCase = mockk<RemoveTagLinkUseCase>()
                coEvery { removeTagLinkUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel =
                    viewModel(
                        id = id,
                        addTagLinkUseCase = addTagLinkUseCase,
                        removeTagLinkUseCase = removeTagLinkUseCase,
                    )

                viewModel.link(tagId = firstToTagId)
                viewModel.link(tagId = secondToTagId)
                viewModel.unlink(tagId = firstToTagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    addTagLinkUseCase(parameter = AddTagLinkUseCase.Parameter(fromTagId = id, toTagId = firstToTagId))
                }
                coVerify(exactly = 1) {
                    addTagLinkUseCase(parameter = AddTagLinkUseCase.Parameter(fromTagId = id, toTagId = secondToTagId))
                }
                coVerify(exactly = 1) {
                    removeTagLinkUseCase(
                        parameter = RemoveTagLinkUseCase.Parameter(fromTagId = id, toTagId = firstToTagId),
                    )
                }
            }
        }
        test("같은 태그의 연결이나 해제가 진행 중일 때 다시 요청하면 한 번만 실행한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val toTagId = fixtureMonkey.giveMeOne<Uuid>()
                val addTagLinkUseCase = mockk<AddTagLinkUseCase>()
                coEvery { addTagLinkUseCase(parameter = any()) } returns Result.success(Unit)
                val removeTagLinkUseCase = mockk<RemoveTagLinkUseCase>()
                coEvery { removeTagLinkUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel =
                    viewModel(
                        id = id,
                        addTagLinkUseCase = addTagLinkUseCase,
                        removeTagLinkUseCase = removeTagLinkUseCase,
                    )

                viewModel.link(tagId = toTagId)
                viewModel.link(tagId = toTagId)
                viewModel.unlink(tagId = toTagId)
                viewModel.unlink(tagId = toTagId)
                advanceUntilIdle()

                coVerify(exactly = 1) { addTagLinkUseCase(parameter = AddTagLinkUseCase.Parameter(fromTagId = id, toTagId = toTagId)) }
                coVerify(exactly = 1) { removeTagLinkUseCase(parameter = RemoveTagLinkUseCase.Parameter(fromTagId = id, toTagId = toTagId)) }
            }
        }

        searchTests()
        restorationTests()
    }

    private fun searchTests() {
        test("TC-6c8807b6-281d-4167-abf4-d79272febec2 검색어가 바뀌면 새 검색어 기준으로 선택 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val allTagList = List(2) { tag() }
                val matchedTagList = listOf(allTagList.first())
                val pageTagLinkSelectableTagUseCase = mockk<PageTagLinkSelectableTagUseCase>()
                every {
                    pageTagLinkSelectableTagUseCase(parameter = PageTagLinkSelectableTagUseCase.Parameter(fromTagId = id, query = ""))
                } returns flowOf(Result.success(PagingData.from(allTagList)))
                every {
                    pageTagLinkSelectableTagUseCase(
                        parameter = PageTagLinkSelectableTagUseCase.Parameter(fromTagId = id, query = SEARCH_QUERY),
                    )
                } returns flowOf(Result.success(PagingData.from(matchedTagList)))
                val viewModel = viewModel(id = id, pageTagLinkSelectableTagUseCase = pageTagLinkSelectableTagUseCase)

                viewModel.tagPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allTagList

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedTagList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-88b1423e-e248-41cb-bd1a-a0f70736ca67 검색어를 바꿔도 연결 대상으로 표시하는 태그는 그대로다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val linkedTagList = List(2) { tag() }
                val viewModel = viewModel(id = id, linkedTagFlow = flowOf(Result.success(linkedTagList)))

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().linkedTagList shouldBe linkedTagList

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private fun restorationTests() {
        test("TC-2a30f51f-399f-4eb9-8448-dd3b3dad1c32 복원 뒤 새로 만든 화면이 되살린 검색어를 알리면 입력 정지 대기 시간 뒤 그 검색어로 좁힌 목록을 보여 준다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val allTagList = List(2) { tag() }
                val matchedTagList = listOf(allTagList.first())
                val pageTagLinkSelectableTagUseCase = mockk<PageTagLinkSelectableTagUseCase>()
                every {
                    pageTagLinkSelectableTagUseCase(parameter = PageTagLinkSelectableTagUseCase.Parameter(fromTagId = id, query = ""))
                } returns flowOf(Result.success(PagingData.from(allTagList)))
                every {
                    pageTagLinkSelectableTagUseCase(
                        parameter = PageTagLinkSelectableTagUseCase.Parameter(fromTagId = id, query = SEARCH_QUERY),
                    )
                } returns flowOf(Result.success(PagingData.from(matchedTagList)))
                val viewModel = viewModel(id = id, pageTagLinkSelectableTagUseCase = pageTagLinkSelectableTagUseCase)

                viewModel.tagPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allTagList

                    // 복원된 화면은 목록을 다시 열면서 되살린 검색어를 알려 준다.
                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceTimeBy(INPUT_IDLE_DELAY / 2)
                    expectNoEvents()

                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe matchedTagList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private fun viewModel(
        id: Uuid,
        linkedTagFlow: Flow<Result<List<Tag>>> = flowOf(Result.success(emptyList())),
        addTagLinkUseCase: AddTagLinkUseCase = mockk(relaxed = true),
        removeTagLinkUseCase: RemoveTagLinkUseCase = mockk(relaxed = true),
        pageTagLinkSelectableTagUseCase: PageTagLinkSelectableTagUseCase =
            mockk<PageTagLinkSelectableTagUseCase>().apply {
                every { this@apply(parameter = any()) } returns flowOf(Result.success(PagingData.empty()))
            },
    ): TagDetailLinkViewModel {
        val getLinkedTagUseCase = mockk<GetLinkedTagUseCase>()
        every { getLinkedTagUseCase(parameter = id) } returns linkedTagFlow

        return TagDetailLinkViewModel(
            id = id,
            pageTagLinkSelectableTagUseCase = pageTagLinkSelectableTagUseCase,
            getLinkedTagUseCase = getLinkedTagUseCase,
            addTagLinkUseCase = addTagLinkUseCase,
            removeTagLinkUseCase = removeTagLinkUseCase,
        )
    }

    public companion object {
        private const val SEARCH_QUERY = "Travel"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
