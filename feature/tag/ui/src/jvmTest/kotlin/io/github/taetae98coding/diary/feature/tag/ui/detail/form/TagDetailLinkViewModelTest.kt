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
import io.mockk.verify
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
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Duration.Companion.milliseconds
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

        test("TC-TAG-DETAIL-FEATURE-030 저장된 연결의 도착 태그를 연결 대상으로 표시한다") {
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

        test("TC-TAG-DETAIL-FEATURE-035 저장된 연결이 바뀌면 연결 대상 표시도 바뀐다") {
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

        test("TC-TAG-DETAIL-FEATURE-031 태그를 연결하면 상세 대상 태그를 출발 태그로 연결을 만든다") {
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

        test("TC-TAG-DETAIL-FEATURE-032 연결을 해제하면 상세 대상 태그를 출발 태그로 연결을 해제한다") {
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
        searchTests()
        restorationTests()
    }

    private fun searchTests() {
        test("TC-TAG-LINK-INPUT-DATA-005 검색어가 바뀌면 새 검색어 기준으로 선택 목록을 다시 조회한다") {
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

        test("TC-TAG-LINK-INPUT-DOMAIN-009 검색어를 바꿔도 연결 대상으로 표시하는 태그는 그대로다") {
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
        test("TC-TAG-LINK-INPUT-DOMAIN-015 복원 뒤 새로 만든 화면은 되살린 검색어로 좁힌 목록을 기다리지 않고 바로 보여 주고 대상 전체를 거치지 않는다") {
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
                val viewModel = viewModel(id = id, pageTagLinkSelectableTagUseCase = pageTagLinkSelectableTagUseCase, isListOpened = false)

                // 복원된 화면은 목록을 다시 열면서 되살린 검색어를 처음으로 알려 준다.
                viewModel.tagPagingData.test {
                    viewModel.updateQuery(SEARCH_QUERY)
                    runCurrent()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedTagList
                    cancelAndIgnoreRemainingEvents()
                }
                verify(exactly = 0) { pageTagLinkSelectableTagUseCase(parameter = PageTagLinkSelectableTagUseCase.Parameter(fromTagId = id, query = "")) }
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
        isListOpened: Boolean = true,
    ): TagDetailLinkViewModel {
        val getLinkedTagUseCase = mockk<GetLinkedTagUseCase>()
        every { getLinkedTagUseCase(parameter = id) } returns linkedTagFlow

        return TagDetailLinkViewModel(
            id = id,
            pageTagLinkSelectableTagUseCase = pageTagLinkSelectableTagUseCase,
            getLinkedTagUseCase = getLinkedTagUseCase,
            addTagLinkUseCase = addTagLinkUseCase,
            removeTagLinkUseCase = removeTagLinkUseCase,
        ).apply {
            // 화면은 선택 목록을 열 때 검색어를 알려 주므로, 목록이 열린 상태를 만든다.
            if (isListOpened) updateQuery(query = "")
        }
    }

    public companion object {
        private const val SEARCH_QUERY = "Travel"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
