@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnsetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.INPUT_IDLE_DELAY
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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoTagViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-005 선택한 태그가 삭제되면 선택과 대표 지정이 해제된 것으로 표시된다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val memo = memo().copy(primaryTagId = tag.id)
                val memoTagFlow = MutableStateFlow(Result.success(listOf(tag)))
                val viewModel =
                    viewModel(
                        id = memo.id,
                        tagListFlow = flowOf(Result.success(listOf(tag))),
                        memoTagFlow = memoTagFlow,
                        memoFlow = flowOf(Result.success(memo)),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val selectedUiState = expectMostRecentItem()
                    selectedUiState.selectedTagList shouldBe listOf(tag)
                    selectedUiState.primaryTagId shouldBe tag.id

                    memoTagFlow.value = Result.success(emptyList())
                    advanceUntilIdle()

                    val deletedUiState = expectMostRecentItem()
                    deletedUiState.selectedTagList shouldBe emptyList()
                    deletedUiState.primaryTagId shouldBe null
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-006 태그가 복구되면 유지되어 있던 선택과 대표 지정이 다시 나타난다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val memo = memo().copy(primaryTagId = tag.id)
                val memoTagFlow = MutableStateFlow(Result.success(emptyList<Tag>()))
                val viewModel =
                    viewModel(
                        id = memo.id,
                        tagListFlow = flowOf(Result.success(listOf(tag))),
                        memoTagFlow = memoTagFlow,
                        memoFlow = flowOf(Result.success(memo)),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val deletedUiState = expectMostRecentItem()
                    deletedUiState.selectedTagList shouldBe emptyList()
                    deletedUiState.primaryTagId shouldBe null

                    memoTagFlow.value = Result.success(listOf(tag))
                    advanceUntilIdle()

                    val restoredUiState = expectMostRecentItem()
                    restoredUiState.selectedTagList shouldBe listOf(tag)
                    restoredUiState.primaryTagId shouldBe tag.id
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-042 삭제된 메모에서도 태그 선택을 반영한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val deletedMemo = memo().copy(isDeleted = true, primaryTagId = null)
                val memoTagFlow = MutableStateFlow(Result.success(emptyList<Tag>()))
                val addMemoTagUseCase = mockk<AddMemoTagUseCase>(relaxed = true)
                coEvery { addMemoTagUseCase(any<AddMemoTagUseCase.Parameter>()) } coAnswers {
                    memoTagFlow.value = Result.success(listOf(tag))
                    Result.success(Unit)
                }
                val viewModel =
                    viewModel(
                        id = deletedMemo.id,
                        tagListFlow = flowOf(Result.success(listOf(tag))),
                        memoTagFlow = memoTagFlow,
                        memoFlow = flowOf(Result.success(deletedMemo)),
                        addMemoTagUseCase = addMemoTagUseCase,
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    viewModel.selectTag(tagId = tag.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedTagList shouldBe listOf(tag)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("저장된 태그 연결이 조회되지 않으면 선택한 태그 없이 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(memoTagFlow = emptyFlow())

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoTagInputUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-TAG-INPUT-DATA-003 태그 선택 목록은 페이지 단위 조회 결과를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagList = listOf(tag(), tag())
                val viewModel = viewModel(tagListFlow = flowOf(Result.success(tagList)))

                flowOf(viewModel.tagPagingData.first()).asSnapshot() shouldBe tagList
            }
        }

        test("TC-MEMO-TAG-INPUT-FEATURE-026 태그 선택 목록 조회가 실패하면 마지막 목록을 유지한다") {
            runTest(mainDispatcher) {
                val tagList = listOf(tag(), tag())
                val viewModel =
                    viewModel(
                        tagListFlow =
                            flowOf(
                                Result.success(tagList),
                                Result.failure(IllegalStateException()),
                            ),
                    )

                viewModel.tagPagingData.test {
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe tagList
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-033 저장된 태그 연결과 대표 태그를 상태에 채운다") {
            runTest(mainDispatcher) {
                val workTag = tag()
                val exerciseTag = tag()
                val memo = memo().copy(primaryTagId = workTag.id)
                val viewModel =
                    viewModel(
                        id = memo.id,
                        tagListFlow = flowOf(Result.success(listOf(workTag, exerciseTag))),
                        memoTagFlow = flowOf(Result.success(listOf(workTag, exerciseTag))),
                        memoFlow = flowOf(Result.success(memo)),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoTagInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe
                        MemoTagInputUiState(selectedTagList = listOf(workTag, exerciseTag), primaryTagId = workTag.id)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-004 표시하는 태그에 없는 대표 태그 지정은 해제한다") {
            runTest(mainDispatcher) {
                val selectableTag = tag()
                val unselectableTagId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo().copy(primaryTagId = unselectableTagId)
                val viewModel =
                    viewModel(
                        id = memo.id,
                        tagListFlow = flowOf(Result.success(listOf(selectableTag))),
                        memoTagFlow = flowOf(Result.success(listOf(selectableTag))),
                        memoFlow = flowOf(Result.success(memo)),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoTagInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe
                        MemoTagInputUiState(selectedTagList = listOf(selectableTag), primaryTagId = null)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-047 TC-MEMO-DETAIL-FEATURE-048 연결된 완료된 태그의 연결과 대표 태그 지정도 상태에 채운다") {
            runTest(mainDispatcher) {
                val selectableTag = tag(title = FIRST_TAG_TITLE).copy(isFinished = false)
                val finishedTag = tag(title = SECOND_TAG_TITLE).copy(isFinished = true)
                val memo = memo().copy(primaryTagId = finishedTag.id)
                val viewModel =
                    viewModel(
                        id = memo.id,
                        tagListFlow = flowOf(Result.success(listOf(selectableTag))),
                        memoTagFlow = flowOf(Result.success(listOf(selectableTag, finishedTag))),
                        memoFlow = flowOf(Result.success(memo)),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoTagInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe
                        MemoTagInputUiState(selectedTagList = listOf(selectableTag, finishedTag), primaryTagId = finishedTag.id)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-051 TC-MEMO-TAG-INPUT-DOMAIN-009 연결된 완료된 태그를 선택 목록에 함께 담는다") {
            runTest(mainDispatcher) {
                val selectableTag = tag(title = FIRST_TAG_TITLE).copy(isFinished = false)
                val finishedTag = tag(title = SECOND_TAG_TITLE).copy(isFinished = true)
                val memo = memo().copy(primaryTagId = null)
                val viewModel =
                    viewModel(
                        id = memo.id,
                        tagListFlow = flowOf(Result.success(listOf(selectableTag))),
                        memoTagFlow = flowOf(Result.success(listOf(finishedTag))),
                        memoFlow = flowOf(Result.success(memo)),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoTagInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe
                        MemoTagInputUiState(selectedTagList = listOf(finishedTag), primaryTagId = null)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-053 연결이 해제되면 선택 상태에서도 빠진다") {
            runTest(mainDispatcher) {
                val finishedTag = tag(title = SECOND_TAG_TITLE).copy(isFinished = true)
                val memo = memo().copy(primaryTagId = null)
                val memoTagFlow = MutableStateFlow(Result.success(listOf(finishedTag)))
                val viewModel =
                    viewModel(
                        id = memo.id,
                        memoTagFlow = memoTagFlow,
                        memoFlow = flowOf(Result.success(memo)),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoTagInputUiState()
                    advanceUntilIdle()
                    expectMostRecentItem() shouldBe MemoTagInputUiState(selectedTagList = listOf(finishedTag), primaryTagId = null)

                    memoTagFlow.value = Result.success(emptyList())
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoTagInputUiState(selectedTagList = emptyList(), primaryTagId = null)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-DATA-004 태그를 선택하면 그 메모와 태그의 연결 추가를 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val addMemoTagUseCase = mockk<AddMemoTagUseCase>(relaxed = true)
                val viewModel = viewModel(id = id, addMemoTagUseCase = addMemoTagUseCase)

                viewModel.selectTag(tagId = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) { addMemoTagUseCase(AddMemoTagUseCase.Parameter(memoId = id, tagId = tagId)) }
            }
        }

        test("TC-MEMO-DETAIL-DATA-005 태그 선택을 해제하면 그 메모와 태그의 연결 제거를 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val removeMemoTagUseCase = mockk<RemoveMemoTagUseCase>(relaxed = true)
                val viewModel = viewModel(id = id, removeMemoTagUseCase = removeMemoTagUseCase)

                viewModel.unselectTag(tagId = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) { removeMemoTagUseCase(RemoveMemoTagUseCase.Parameter(memoId = id, tagId = tagId)) }
            }
        }

        test("TC-MEMO-DETAIL-DATA-008 대표 태그를 지정하면 그 메모의 대표 태그 지정을 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val setMemoPrimaryTagUseCase = mockk<SetMemoPrimaryTagUseCase>(relaxed = true)
                val viewModel = viewModel(id = id, setMemoPrimaryTagUseCase = setMemoPrimaryTagUseCase)

                viewModel.selectPrimaryTag(tagId = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) { setMemoPrimaryTagUseCase(SetMemoPrimaryTagUseCase.Parameter(memoId = id, tagId = tagId)) }
            }
        }

        test("TC-MEMO-DETAIL-DATA-009 대표 태그 지정을 해제하면 그 메모의 대표 태그 해제를 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val unsetMemoPrimaryTagUseCase = mockk<UnsetMemoPrimaryTagUseCase>(relaxed = true)
                val viewModel = viewModel(id = id, unsetMemoPrimaryTagUseCase = unsetMemoPrimaryTagUseCase)

                viewModel.unselectPrimaryTag()
                advanceUntilIdle()

                coVerify(exactly = 1) { unsetMemoPrimaryTagUseCase(id) }
            }
        }

        test("TC-MEMO-DETAIL-DOMAIN-005 앞선 태그 변경이 처리 중이어도 뒤이은 태그 변경을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val firstTagId = fixtureMonkey.giveMeOne<Uuid>()
                val secondTagId = fixtureMonkey.giveMeOne<Uuid>()
                val completion = CompletableDeferred<Result<Unit>>()
                val addMemoTagUseCase = mockk<AddMemoTagUseCase>()
                coEvery { addMemoTagUseCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(id = id, addMemoTagUseCase = addMemoTagUseCase)

                viewModel.selectTag(tagId = firstTagId)
                runCurrent()
                viewModel.selectTag(tagId = secondTagId)
                runCurrent()
                completion.complete(Result.success(Unit))
                advanceUntilIdle()

                coVerify(exactly = 1) { addMemoTagUseCase(AddMemoTagUseCase.Parameter(memoId = id, tagId = firstTagId)) }
                coVerify(exactly = 1) { addMemoTagUseCase(AddMemoTagUseCase.Parameter(memoId = id, tagId = secondTagId)) }
            }
        }
        searchTests()
        applyDelayTests()
        blankQueryTests()
    }

    private fun searchTests() {
        test("TC-MEMO-TAG-INPUT-DATA-006 검색어가 바뀌면 새 검색어 기준으로 선택 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val allTagList = List(2) { tag() }
                val matchedTagList = listOf(allTagList.first())
                val pageMemoSelectableTagUseCase = mockk<PageMemoSelectableTagUseCase>()
                every {
                    pageMemoSelectableTagUseCase(parameter = PageMemoSelectableTagUseCase.Parameter(memoId = id, query = ""))
                } returns flowOf(Result.success(PagingData.from(allTagList)))
                every {
                    pageMemoSelectableTagUseCase(parameter = PageMemoSelectableTagUseCase.Parameter(memoId = id, query = SEARCH_QUERY))
                } returns flowOf(Result.success(PagingData.from(matchedTagList)))
                val viewModel = viewModel(id = id, pageMemoSelectableTagUseCase = pageMemoSelectableTagUseCase)

                viewModel.tagPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allTagList

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedTagList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private fun applyDelayTests() {
        test("TC-MEMO-TAG-INPUT-FEATURE-044 연속으로 입력하는 동안에는 마지막 검색어만 선택 목록에 반영된다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val allTagList = List(3) { tag() }
                val middleTagList = allTagList.take(2)
                val matchedTagList = allTagList.take(1)
                val pageMemoSelectableTagUseCase = mockk<PageMemoSelectableTagUseCase>()
                every {
                    pageMemoSelectableTagUseCase(parameter = PageMemoSelectableTagUseCase.Parameter(memoId = id, query = ""))
                } returns flowOf(Result.success(PagingData.from(allTagList)))
                every {
                    pageMemoSelectableTagUseCase(parameter = PageMemoSelectableTagUseCase.Parameter(memoId = id, query = MIDDLE_SEARCH_QUERY))
                } returns flowOf(Result.success(PagingData.from(middleTagList)))
                every {
                    pageMemoSelectableTagUseCase(parameter = PageMemoSelectableTagUseCase.Parameter(memoId = id, query = SEARCH_QUERY))
                } returns flowOf(Result.success(PagingData.from(matchedTagList)))
                val viewModel = viewModel(id = id, pageMemoSelectableTagUseCase = pageMemoSelectableTagUseCase)

                viewModel.tagPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allTagList

                    viewModel.updateQuery(MIDDLE_SEARCH_QUERY)
                    // 입력을 멈추지 않은 상태를 만들려고 반영 시간보다 짧게 둔다.
                    advanceTimeBy(INPUT_IDLE_DELAY / 2)
                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedTagList
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private fun blankQueryTests() {
        test("TC-MEMO-TAG-INPUT-FEATURE-045 검색어를 지우면 기다리지 않고 선택 목록이 되돌아온다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val allTagList = List(2) { tag() }
                val matchedTagList = listOf(allTagList.first())
                val pageMemoSelectableTagUseCase = mockk<PageMemoSelectableTagUseCase>()
                every {
                    pageMemoSelectableTagUseCase(parameter = PageMemoSelectableTagUseCase.Parameter(memoId = id, query = ""))
                } returns flowOf(Result.success(PagingData.from(allTagList)))
                every {
                    pageMemoSelectableTagUseCase(parameter = PageMemoSelectableTagUseCase.Parameter(memoId = id, query = SEARCH_QUERY))
                } returns flowOf(Result.success(PagingData.from(matchedTagList)))
                val viewModel = viewModel(id = id, pageMemoSelectableTagUseCase = pageMemoSelectableTagUseCase)

                viewModel.tagPagingData.test {
                    awaitItem()
                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe matchedTagList

                    viewModel.updateQuery("")
                    runCurrent()

                    flowOf(awaitItem()).asSnapshot() shouldBe allTagList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-018 검색어를 바꿔도 선택한 것으로 표시하는 태그는 그대로다") {
            runTest(mainDispatcher) {
                val selectedTagList = List(2) { tag() }
                val viewModel =
                    viewModel(
                        memoTagFlow = flowOf(Result.success(selectedTagList)),
                        memoFlow = flowOf(Result.success(null)),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoTagInputUiState()
                    advanceUntilIdle()
                    expectMostRecentItem().selectedTagList shouldBe selectedTagList

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    public companion object {
        private const val FIRST_TAG_TITLE = "AAA"
        private const val SECOND_TAG_TITLE = "BBB"
        private const val THIRD_TAG_TITLE = "CCC"
        private const val SEARCH_QUERY = "Travel"
        private const val MIDDLE_SEARCH_QUERY = "Trav"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        // 정렬 순서가 검증 대상인 케이스에서는 제목만 고정한다.
        private fun tag(title: String): Tag = tag().let { tag -> tag.copy(detail = tag.detail.copy(title = title)) }

        private fun viewModel(
            id: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            tagListFlow: Flow<Result<List<Tag>>> = emptyFlow(),
            memoTagFlow: Flow<Result<List<Tag>>> = emptyFlow(),
            memoFlow: Flow<Result<Memo?>> = emptyFlow(),
            addMemoTagUseCase: AddMemoTagUseCase = mockk(relaxed = true),
            removeMemoTagUseCase: RemoveMemoTagUseCase = mockk(relaxed = true),
            setMemoPrimaryTagUseCase: SetMemoPrimaryTagUseCase = mockk(relaxed = true),
            unsetMemoPrimaryTagUseCase: UnsetMemoPrimaryTagUseCase = mockk(relaxed = true),
            pageMemoSelectableTagUseCase: PageMemoSelectableTagUseCase =
                mockk<PageMemoSelectableTagUseCase>().apply {
                    every { this@apply(any()) } returns tagListFlow.map { result -> result.map { tagList -> PagingData.from(tagList) } }
                },
        ): MemoTagViewModel {
            val getMemoTagUseCase = mockk<GetMemoTagUseCase>()
            every { getMemoTagUseCase(any()) } returns memoTagFlow

            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(any()) } returns memoFlow

            return MemoTagViewModel(
                id = id,
                pageMemoSelectableTagUseCase = pageMemoSelectableTagUseCase,
                getMemoTagUseCase = getMemoTagUseCase,
                findMemoUseCase = findMemoUseCase,
                addMemoTagUseCase = addMemoTagUseCase,
                removeMemoTagUseCase = removeMemoTagUseCase,
                setMemoPrimaryTagUseCase = setMemoPrimaryTagUseCase,
                unsetMemoPrimaryTagUseCase = unsetMemoPrimaryTagUseCase,
            )
        }
    }
}
