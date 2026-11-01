@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.web.ui.add

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
import io.github.taetae98coding.diary.library.coroutines.flow.INPUT_IDLE_DELAY
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class WebAddTagViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-275e3447-faf3-4877-861a-c88b99f5dc75 선택 목록을 열지 않아도 목록에 나타낼 태그 전체를 빈 검색어로 조회한다") {
            runTest(mainDispatcher) {
                val item = tag()
                val useCase = mockk<PageTagUseCase>()
                every { useCase(parameter = "") } returns flowOf(Result.success(PagingData.from(listOf(item))))
                val viewModel = WebAddTagViewModel(initialTagId = null, pageTagUseCase = useCase, getSelectedTagUseCase = mockk(relaxed = true))

                val itemList = flowOf(viewModel.selectableTagPagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList shouldBe listOf(item)
                verify(exactly = 1) { useCase(parameter = "") }
            }
        }

        test("TC-1f7abc2c-d694-4f46-be86-7ea3ce9a83bf 초기 태그가 없으면 태그를 하나도 고르지 않은 상태로 시작한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(initialTagId = null)

                viewModel.tagSelectionUiState.value.tagIdSet
                    .shouldBeEmpty()
            }
        }

        test("TC-479957d3-8ce6-49fd-9ead-e1d414718bf1 고른 태그는 선택 상태로만 반영된다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(initialTagId = null, selectedTagList = listOf(tag))

                viewModel.add(id = tag.id)
                advanceUntilIdle()

                viewModel.tagSelectionUiState.value.tagIdSet shouldBe setOf(tag.id)
                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().tagList shouldBe listOf(tag)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-eba5daa8-8d3e-4003-9b60-82720604c453 연결된 태그의 해제를 요청하면 선택에서 빠진다") {
            runTest(mainDispatcher) {
                val firstTag = tag()
                val secondTag = tag()
                val viewModel = viewModel(initialTagId = null)

                viewModel.add(id = firstTag.id)
                viewModel.add(id = secondTag.id)
                viewModel.remove(id = firstTag.id)
                advanceUntilIdle()

                viewModel.tagSelectionUiState.value.tagIdSet shouldBe setOf(secondTag.id)
            }
        }

        test("TC-cd13a80c-3c6b-42a4-8f18-ee542e37eac9 초기 태그가 있으면 그 태그를 고른 상태로 시작한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(initialTagId = tag.id, selectedTagList = listOf(tag))

                viewModel.tagSelectionUiState.value.tagIdSet shouldBe setOf(tag.id)

                viewModel.uiState.test {
                    awaitItem().tagList.shouldBeEmpty()
                    awaitItem().tagList shouldBe listOf(tag)
                }
            }
        }

        test("TC-2355ae81-e9cb-4203-86cb-23b6bbca72f6 TC-ca3c3ae8-e1a0-4f98-942a-f044f063ee8b TC-ef341fba-ece0-44e0-a0a4-21dbeaa96dbb 표시 기준에서 빠진 태그는 제외된 것으로 표시되지만 선택은 남아 있다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(initialTagId = tag.id, selectedTagList = emptyList())

                viewModel.tagSelectionUiState.value.tagIdSet shouldBe setOf(tag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().tagList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-8b85f5ae-dd5e-4598-b6e8-78d9a31ec096 고른 뒤 완료되거나 삭제되어 제외된 것으로 표시된 태그도 연결할 선택에 그대로 남는다") {
            listOf<(Tag) -> Tag>(
                { value -> value.copy(isFinished = true) },
                { value -> value.copy(isDeleted = true) },
            ).forEach { change ->
                runTest(mainDispatcher) {
                    val tag = tag()
                    val storedTag = MutableStateFlow(tag)
                    val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
                    every { getSelectedTagUseCase(parameter = any()) } answers {
                        val idSet = firstArg<Set<Uuid>>()
                        storedTag.map { value ->
                            Result.success(listOf(value).filter { selected -> selected.id in idSet && !selected.isFinished && !selected.isDeleted })
                        }
                    }
                    val viewModel =
                        WebAddTagViewModel(
                            initialTagId = null,
                            pageTagUseCase = mockk(relaxed = true),
                            getSelectedTagUseCase = getSelectedTagUseCase,
                        )

                    viewModel.uiState.test {
                        viewModel.add(id = tag.id)
                        advanceUntilIdle()
                        expectMostRecentItem().tagList shouldBe listOf(tag)

                        storedTag.value = change(tag)
                        advanceUntilIdle()
                        expectMostRecentItem().tagList.shouldBeEmpty()
                        cancelAndIgnoreRemainingEvents()
                    }

                    viewModel.tagSelectionUiState.value.tagIdSet shouldBe setOf(tag.id)
                }
            }
        }

        test("TC-600fa417-a608-4189-bd8c-409ea8506e2e 복원 뒤 새로 만든 화면이 되살린 검색어를 알리면 입력 정지 대기 시간 뒤 그 검색어로 좁힌 목록을 보여 준다") {
            runTest(mainDispatcher) {
                val query = "Query${fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)}"
                val allTagList = List(2) { tag() }
                val matchedTagList = listOf(allTagList.first())
                val pageTagUseCase = mockk<PageTagUseCase>()
                every { pageTagUseCase(parameter = "") } returns flowOf(Result.success(PagingData.from(allTagList)))
                every { pageTagUseCase(parameter = query) } returns flowOf(Result.success(PagingData.from(matchedTagList)))
                val viewModel =
                    WebAddTagViewModel(
                        initialTagId = null,
                        pageTagUseCase = pageTagUseCase,
                        getSelectedTagUseCase = mockk(relaxed = true),
                    )

                viewModel.tagPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allTagList

                    // 복원된 화면은 목록을 다시 열면서 되살린 검색어를 알려 준다.
                    viewModel.updateQuery(query)
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
        initialTagId: Uuid?,
        selectedTagList: List<Tag> = emptyList(),
        selectableTagList: List<Tag> = emptyList(),
    ): WebAddTagViewModel {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(selectableTagList)))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } returns flowOf(Result.success(selectedTagList))

        return WebAddTagViewModel(
            initialTagId = initialTagId,
            pageTagUseCase = pageTagUseCase,
            getSelectedTagUseCase = getSelectedTagUseCase,
        )
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::isFinished, false)
                .setExp(Tag::isDeleted, false)
                .sample()
    }
}
