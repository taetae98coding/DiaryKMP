@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
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
import kotlin.time.Instant
import kotlin.uuid.Uuid

class TagAddLinkViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-TAG-LINK-INPUT-FEATURE-033 선택 목록을 열지 않아도 목록에 나타낼 태그 전체를 빈 검색어로 조회한다") {
            runTest(mainDispatcher) {
                val item = tag()
                val useCase = mockk<PageTagUseCase>()
                every { useCase(parameter = "") } returns flowOf(Result.success(PagingData.from(listOf(item))))
                val viewModel = TagAddLinkViewModel(pageTagUseCase = useCase, getSelectedTagUseCase = mockk(relaxed = true))

                val itemList = flowOf(viewModel.selectableTagPagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList shouldBe listOf(item)
                verify(exactly = 1) { useCase(parameter = "") }
            }
        }

        test("TC-TAG-ADD-FEATURE-018 처음에는 연결 대상으로 표시하는 태그가 없다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(selectedTagList = { emptyList() })

                viewModel.uiState.test {
                    awaitItem().linkedTagList.shouldBeEmpty()
                    viewModel.linkedTagIdSet.value.shouldBeEmpty()
                }
            }
        }

        test("TC-TAG-ADD-FEATURE-019 태그를 고르면 고른 태그가 연결 대상으로 표시된다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(selectedTagList = { tagIdSet -> listOf(tag).filter { it.id in tagIdSet } })

                viewModel.uiState.test {
                    awaitItem().linkedTagList.shouldBeEmpty()

                    viewModel.link(id = tag.id)

                    awaitItem().linkedTagList shouldBe listOf(tag)
                    viewModel.linkedTagIdSet.value shouldBe setOf(tag.id)
                }
            }
        }

        test("고른 태그의 연결을 해제하면 연결 대상에서 빠진다") {
            runTest(mainDispatcher) {
                val firstTag = tag()
                val secondTag = tag()
                val viewModel =
                    viewModel(
                        selectedTagList = { tagIdSet -> listOf(firstTag, secondTag).filter { it.id in tagIdSet } },
                    )

                viewModel.uiState.test {
                    awaitItem()
                    viewModel.link(id = firstTag.id)
                    viewModel.link(id = secondTag.id)
                    awaitItem()

                    viewModel.unlink(id = firstTag.id)

                    awaitItem().linkedTagList shouldBe listOf(secondTag)
                    viewModel.linkedTagIdSet.value shouldBe setOf(secondTag.id)
                }
            }
        }

        test("TC-TAG-ADD-FEATURE-020 연결을 비우면 연결 대상으로 표시하는 태그가 없다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(selectedTagList = { tagIdSet -> listOf(tag).filter { it.id in tagIdSet } })

                viewModel.uiState.test {
                    awaitItem()
                    viewModel.link(id = tag.id)
                    awaitItem().linkedTagList shouldBe listOf(tag)

                    viewModel.clear()

                    awaitItem().linkedTagList.shouldBeEmpty()
                    viewModel.linkedTagIdSet.value.shouldBeEmpty()
                }
            }
        }

        test("TC-TAG-LINK-INPUT-DOMAIN-003 표시 기준에서 빠진 태그는 연결에서 제외된 것으로 표시한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val displayedTagList = MutableStateFlow(listOf(tag))
                val viewModel =
                    viewModel(
                        selectedTagListFlow = { tagIdSet ->
                            displayedTagList.map { tagList -> Result.success(tagList.filter { it.id in tagIdSet }) }
                        },
                    )

                viewModel.uiState.test {
                    awaitItem()
                    viewModel.link(id = tag.id)
                    awaitItem().linkedTagList shouldBe listOf(tag)

                    displayedTagList.value = emptyList()

                    awaitItem().linkedTagList.shouldBeEmpty()
                }
            }
        }

        test("TC-TAG-LINK-INPUT-DOMAIN-004 태그가 복구되면 유지되어 있던 연결이 다시 나타난다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val displayedTagList = MutableStateFlow(listOf(tag))
                val viewModel =
                    viewModel(
                        selectedTagListFlow = { tagIdSet ->
                            displayedTagList.map { tagList -> Result.success(tagList.filter { it.id in tagIdSet }) }
                        },
                    )

                viewModel.uiState.test {
                    awaitItem()
                    viewModel.link(id = tag.id)
                    awaitItem().linkedTagList shouldBe listOf(tag)
                    displayedTagList.value = emptyList()
                    awaitItem().linkedTagList.shouldBeEmpty()

                    displayedTagList.value = listOf(tag)

                    awaitItem().linkedTagList shouldBe listOf(tag)
                }
            }
        }

        test("TC-TAG-ADD-DOMAIN-004 고른 태그가 완료되거나 삭제되면 연결 대상에서 제외된 것으로 표시하고 선택은 유지한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val selectableTagList = MutableStateFlow(listOf(tag))
                val viewModel =
                    viewModel(
                        selectedTagListFlow = { tagIdSet ->
                            selectableTagList.map { tagList -> Result.success(tagList.filter { it.id in tagIdSet }) }
                        },
                    )

                viewModel.uiState.test {
                    awaitItem().linkedTagList.shouldBeEmpty()
                    viewModel.link(id = tag.id)
                    awaitItem().linkedTagList shouldBe listOf(tag)

                    selectableTagList.value = emptyList()

                    awaitItem().linkedTagList.shouldBeEmpty()
                    viewModel.linkedTagIdSet.value shouldBe setOf(tag.id)
                }
            }
        }
        searchTests()
    }

    private fun searchTests() {
        test("TC-TAG-LINK-INPUT-DATA-005 검색어가 바뀌면 새 검색어 기준으로 선택 목록을 다시 조회한다") {
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

        test("TC-TAG-LINK-INPUT-DOMAIN-009 검색어를 바꿔도 연결 대상으로 표시하는 태그는 그대로다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(selectedTagList = { tagIdSet -> listOf(tag).filter { value -> value.id in tagIdSet } })
                viewModel.link(id = tag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().linkedTagList shouldBe listOf(tag)

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private fun viewModel(
        selectedTagList: (Set<Uuid>) -> List<Tag> = { emptyList() },
        selectedTagListFlow: (Set<Uuid>) -> Flow<Result<List<Tag>>> = { tagIdSet ->
            flowOf(Result.success(selectedTagList(tagIdSet)))
        },
        pageTagUseCase: PageTagUseCase =
            mockk<PageTagUseCase>().apply {
                every { this@apply(parameter = any()) } returns flowOf(Result.success(PagingData.empty()))
            },
        isListOpened: Boolean = true,
    ): TagAddLinkViewModel {
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            selectedTagListFlow(firstArg())
        }

        return TagAddLinkViewModel(
            pageTagUseCase = pageTagUseCase,
            getSelectedTagUseCase = getSelectedTagUseCase,
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
                .setExp(Tag::isFinished, false)
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
