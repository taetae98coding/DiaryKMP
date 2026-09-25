@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagSelection
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoAddTagViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-022 메모리 정리 뒤 새로 만든 화면은 되살린 검색어로 좁힌 목록을 기다리지 않고 바로 보여 주고 대상 전체를 거치지 않는다") {
            runTest(mainDispatcher) {
                val query = "Query${fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)}"
                val allList = List(2) { tag() }
                val matchedList = listOf(allList.first())
                val useCase = mockk<PageTagUseCase>()
                every { useCase(parameter = "") } returns flowOf(Result.success(PagingData.from(allList)))
                every { useCase(parameter = query) } returns flowOf(Result.success(PagingData.from(matchedList)))
                val viewModel = viewModel(pageTagUseCase = useCase, isListOpened = false)

                // 복원된 화면은 목록을 다시 열면서 되살린 검색어를 처음으로 알려 준다.
                viewModel.tagPagingData.test {
                    viewModel.updateQuery(query)
                    runCurrent()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedList
                    cancelAndIgnoreRemainingEvents()
                }
                verify(exactly = 0) { useCase(parameter = "") }
            }
        }

        test("TC-MEMO-TAG-INPUT-FEATURE-008 선택한 태그를 즉시 선택 태그 목록으로 노출한다") {
            runTest(mainDispatcher) {
                val firstTag = tag()
                val secondTag = tag()
                val viewModel = viewModel(tagListFlow = flowOf(Result.success(listOf(firstTag, secondTag))))

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedTagList.shouldBeEmpty()

                    viewModel.selectTag(id = firstTag.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedTagList shouldBe listOf(firstTag)

                    viewModel.selectTag(id = secondTag.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedTagList shouldBe listOf(firstTag, secondTag)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-TAG-INPUT-FEATURE-009 선택을 해제한 태그를 즉시 선택 태그 목록에서 제외한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(tagListFlow = flowOf(Result.success(listOf(tag))))

                viewModel.selectTag(id = tag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().selectedTagList shouldBe listOf(tag)

                    viewModel.unselectTag(id = tag.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedTagList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-TAG-INPUT-FEATURE-015 선택하지 않은 태그를 대표로 지정하면 함께 선택된다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(tagListFlow = flowOf(Result.success(listOf(tag))))

                viewModel.selectPrimaryTag(id = tag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldBe listOf(tag)
                    uiState.primaryTagId shouldBe tag.id
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-TAG-INPUT-FEATURE-016 대표 태그 지정을 해제해도 태그 선택은 유지된다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(tagListFlow = flowOf(Result.success(listOf(tag))))

                viewModel.selectPrimaryTag(id = tag.id)
                viewModel.unselectPrimaryTag()

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldBe listOf(tag)
                    uiState.primaryTagId shouldBe null
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-FEATURE-045 화면 구성이 변경되어도 선택한 태그와 대표 태그 지정을 유지한다") {
            runTest(mainDispatcher) {
                val primaryTag = tag()
                val secondaryTag = tag()
                val viewModel = viewModel(tagListFlow = flowOf(Result.success(listOf(primaryTag, secondaryTag))))

                viewModel.uiState.test {
                    advanceUntilIdle()
                    viewModel.selectTag(id = secondaryTag.id)
                    viewModel.selectPrimaryTag(id = primaryTag.id)
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldContainExactlyInAnyOrder listOf(primaryTag, secondaryTag)
                    uiState.primaryTagId shouldBe primaryTag.id
                    cancelAndIgnoreRemainingEvents()
                }

                // 화면 구성 변경으로 UI가 재생성되어 같은 ViewModel을 새로 구독해도 선택과 대표 지정이 유지된다.
                viewModel.uiState.test {
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldContainExactlyInAnyOrder listOf(primaryTag, secondaryTag)
                    uiState.primaryTagId shouldBe primaryTag.id
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-007 TC-MEMO-TAG-INPUT-FEATURE-017 대표 태그는 항상 하나이며 선택한 태그에 포함된다") {
            runTest(mainDispatcher) {
                val firstTag = tag()
                val secondTag = tag()
                val viewModel = viewModel(tagListFlow = flowOf(Result.success(listOf(firstTag, secondTag))))

                viewModel.selectPrimaryTag(id = firstTag.id)
                viewModel.selectPrimaryTag(id = secondTag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldBe listOf(firstTag, secondTag)
                    uiState.primaryTagId shouldBe secondTag.id
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-TAG-INPUT-FEATURE-018 대표 태그의 선택을 해제하면 대표 지정도 해제된다") {
            runTest(mainDispatcher) {
                val primaryTag = tag()
                val otherTag = tag()
                val viewModel = viewModel(tagListFlow = flowOf(Result.success(listOf(primaryTag, otherTag))))

                viewModel.selectTag(id = otherTag.id)
                viewModel.selectPrimaryTag(id = primaryTag.id)
                viewModel.unselectTag(id = primaryTag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldBe listOf(otherTag)
                    uiState.primaryTagId shouldBe null
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("대표가 아닌 태그의 선택을 해제해도 대표 지정은 유지된다") {
            runTest(mainDispatcher) {
                val primaryTag = tag()
                val otherTag = tag()
                val viewModel = viewModel(tagListFlow = flowOf(Result.success(listOf(primaryTag, otherTag))))

                viewModel.selectTag(id = otherTag.id)
                viewModel.selectPrimaryTag(id = primaryTag.id)
                viewModel.unselectTag(id = otherTag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldBe listOf(primaryTag)
                    uiState.primaryTagId shouldBe primaryTag.id
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("선택한 태그 조회에 실패하면 선택한 태그를 표시하지 않는다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(tagListFlow = flowOf(Result.failure(IllegalStateException())))

                viewModel.selectPrimaryTag(id = tag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList.shouldBeEmpty()
                    uiState.primaryTagId shouldBe null
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-TAG-INPUT-DATA-004 선택 상태로 표시하는 태그는 선택 목록 조회와 분리해 조회한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel =
                    viewModel(
                        tagListFlow = flowOf(Result.success(listOf(tag))),
                        pageTagListFlow = flowOf(Result.success(emptyList())),
                    )

                viewModel.selectPrimaryTag(id = tag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldBe listOf(tag)
                    uiState.primaryTagId shouldBe tag.id
                    cancelAndIgnoreRemainingEvents()
                }

                flowOf(viewModel.tagPagingData.first()).asSnapshot().shouldBeEmpty()
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

        test("TC-MEMO-TAG-INPUT-FEATURE-014 TC-MEMO-ADD-DOMAIN-007 선택할 수 있는 태그에서 빠진 태그는 선택과 대표 지정에서 제외된다") {
            runTest(mainDispatcher) {
                val remainingTag = tag()
                val removedTag = tag()
                val tagListFlow = MutableStateFlow(Result.success(listOf(remainingTag, removedTag)))
                val viewModel = viewModel(tagListFlow = tagListFlow)

                viewModel.selectTag(id = remainingTag.id)
                viewModel.selectPrimaryTag(id = removedTag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val selected = expectMostRecentItem()
                    selected.selectedTagList shouldBe listOf(remainingTag, removedTag)
                    selected.primaryTagId shouldBe removedTag.id

                    tagListFlow.value = Result.success(listOf(remainingTag))
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldBe listOf(remainingTag)
                    uiState.primaryTagId shouldBe null
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-DATA-012 TC-MEMO-ADD-DATA-014 선택할 수 있는 태그에 없는 태그와 대표 지정도 선택으로 남는다") {
            runTest(mainDispatcher) {
                val selectableTag = tag()
                val unselectableTag = tag()
                val viewModel = viewModel(tagListFlow = flowOf(Result.success(listOf(selectableTag))))

                viewModel.uiState.test {
                    advanceUntilIdle()
                    viewModel.selectTag(id = selectableTag.id)
                    viewModel.selectPrimaryTag(id = unselectableTag.id)
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.selection.value shouldBe
                    MemoTagSelection(
                        tagIdSet = setOf(selectableTag.id, unselectableTag.id),
                        primaryTagId = unselectableTag.id,
                    )
            }
        }

        test("TC-TAG-DETAIL-MEMO-DATA-005 저장 시점의 선택 태그와 대표 태그를 선택으로 유지한다") {
            runTest(mainDispatcher) {
                val tagA = tag()
                val tagB = tag()
                val viewModel =
                    viewModel(
                        initialPrimaryTagId = tagA.id,
                        tagListFlow = flowOf(Result.success(listOf(tagA, tagB))),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    viewModel.selectPrimaryTag(id = tagB.id)
                    viewModel.unselectTag(id = tagA.id)
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.selection.value shouldBe MemoTagSelection(tagIdSet = setOf(tagB.id), primaryTagId = tagB.id)
            }
        }

        test("TC-MEMO-ADD-FEATURE-042 진입 경로가 전달한 대표 태그를 선택하고 대표로 지정한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel =
                    viewModel(
                        initialPrimaryTagId = tag.id,
                        tagListFlow = flowOf(Result.success(listOf(tag))),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldBe listOf(tag)
                    uiState.primaryTagId shouldBe tag.id
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("진입 경로가 전달한 대표 태그도 사용자가 바꾼 선택으로 대체된다") {
            runTest(mainDispatcher) {
                val initialTag = tag()
                val changedTag = tag()
                val viewModel =
                    viewModel(
                        initialPrimaryTagId = initialTag.id,
                        tagListFlow = flowOf(Result.success(listOf(initialTag, changedTag))),
                    )

                viewModel.unselectTag(id = initialTag.id)
                viewModel.selectPrimaryTag(id = changedTag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList shouldBe listOf(changedTag)
                    uiState.primaryTagId shouldBe changedTag.id
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-FEATURE-016 진입 경로가 대표 태그를 전달하지 않으면 선택한 태그 없이 시작한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel =
                    viewModel(
                        initialPrimaryTagId = null,
                        tagListFlow = flowOf(Result.success(listOf(tag))),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val uiState = expectMostRecentItem()
                    uiState.selectedTagList.shouldBeEmpty()
                    uiState.primaryTagId shouldBe null
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-TAG-DETAIL-MEMO-FEATURE-015 전달된 대상 태그를 초기 선택과 초기 대표로 시작하고 저장 전까지 바꿀 수 있다") {
            runTest(mainDispatcher) {
                val targetTag = tag()
                val otherTag = tag()
                val viewModel =
                    viewModel(
                        initialPrimaryTagId = targetTag.id,
                        tagListFlow = flowOf(Result.success(listOf(targetTag, otherTag))),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    val initialUiState = expectMostRecentItem()
                    initialUiState.selectedTagList shouldBe listOf(targetTag)
                    initialUiState.primaryTagId shouldBe targetTag.id

                    viewModel.selectPrimaryTag(id = otherTag.id)
                    viewModel.unselectTag(id = targetTag.id)
                    advanceUntilIdle()

                    val changedUiState = expectMostRecentItem()
                    changedUiState.selectedTagList shouldBe listOf(otherTag)
                    changedUiState.primaryTagId shouldBe otherTag.id
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-ADD-DOMAIN-016 완료되었거나 삭제된 대상 태그는 선택과 대표 지정 없이 시작한다") {
            runTest(mainDispatcher) {
                val selectableTag = tag()
                val finishedTag = tag().copy(isFinished = true)
                val deletedTag = tag().copy(isDeleted = true)

                listOf(finishedTag, deletedTag).forEach { targetTag ->
                    // 선택한 태그 조회는 선택할 수 있는 태그만 돌려주므로, 완료되거나 삭제된 대상 태그는 조회 결과에 없다.
                    val viewModel =
                        viewModel(
                            initialPrimaryTagId = targetTag.id,
                            tagListFlow = flowOf(Result.success(listOf(selectableTag, finishedTag, deletedTag).filter { tag -> !tag.isFinished && !tag.isDeleted })),
                        )

                    viewModel.uiState.test {
                        advanceUntilIdle()

                        val uiState = expectMostRecentItem()
                        uiState.selectedTagList.shouldBeEmpty()
                        uiState.primaryTagId shouldBe null
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }
        searchTests()
    }

    private fun searchTests() {
        test("TC-MEMO-TAG-INPUT-DATA-006 검색어가 바뀌면 새 검색어 기준으로 선택 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val allTagList = List(2) { tag() }
                val matchedTagList = listOf(allTagList.first())
                val pageTagUseCase = mockk<PageTagUseCase>()
                every { pageTagUseCase(parameter = "") } returns flowOf(Result.success(PagingData.from(allTagList)))
                every { pageTagUseCase(parameter = SEARCH_QUERY) } returns flowOf(Result.success(PagingData.from(matchedTagList)))
                val viewModel = viewModel(pageTagUseCase = pageTagUseCase)

                viewModel.tagPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allTagList

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedTagList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-018 검색어를 바꿔도 선택한 것으로 표시하는 태그는 그대로다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(tagListFlow = flowOf(Result.success(listOf(tag))))
                viewModel.selectTag(id = tag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().selectedTagList shouldBe listOf(tag)

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private companion object {
        private const val SEARCH_QUERY = "Travel"
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::isFinished, false)
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun viewModel(
            initialPrimaryTagId: Uuid? = null,
            tagListFlow: Flow<Result<List<Tag>>> = emptyFlow(),
            pageTagListFlow: Flow<Result<List<Tag>>> = tagListFlow,
            pageTagUseCase: PageTagUseCase =
                mockk<PageTagUseCase>().apply {
                    every { this@apply(parameter = any()) } returns
                        pageTagListFlow.map { result -> result.map { tagList -> PagingData.from(tagList) } }
                },
            isListOpened: Boolean = true,
        ): MemoAddTagViewModel {
            // 선택 상태 조회는 선택할 수 있는 태그 가운데 요청한 식별자의 태그만 돌려준다.
            val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
            every { getSelectedTagUseCase(parameter = any()) } answers {
                val tagIdSet = firstArg<Set<Uuid>>()

                tagListFlow.map { result -> result.map { tagList -> tagList.filter { tag -> tag.id in tagIdSet } } }
            }

            return MemoAddTagViewModel(
                initialPrimaryTagId = initialPrimaryTagId,
                pageTagUseCase = pageTagUseCase,
                getSelectedTagUseCase = getSelectedTagUseCase,
            ).apply {
                // 화면은 선택 목록을 열 때 검색어를 알려 주므로, 목록이 열린 상태를 만든다.
                if (isListOpened) updateQuery(query = "")
            }
        }
    }
}
