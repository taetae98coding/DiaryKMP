@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.add

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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class PlaceAddTagViewModelTest : FunSpec() {
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
                val viewModel = PlaceAddTagViewModel(initialTagId = null, pageTagUseCase = useCase, getSelectedTagUseCase = mockk(relaxed = true))

                val itemList = flowOf(viewModel.selectableTagPagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList shouldBe listOf(item)
                verify(exactly = 1) { useCase(parameter = "") }
            }
        }

        test("TC-758840d9-7f22-4450-86af-2db4f22b0bee 초기 태그가 없으면 태그를 하나도 고르지 않은 상태로 시작한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(initialTagId = null)

                viewModel.selectionUiState.value.tagIdSet
                    .shouldBeEmpty()
            }
        }

        test("TC-8835af89-2ffc-42a4-8b05-e98346c1dc89 고른 태그는 선택 상태로만 반영되고 장소·태그 연결은 만들어지지 않는다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val pageTagUseCase = pageTagUseCase()
                val getSelectedTagUseCase = getSelectedTagUseCase(selectedTagList = listOf(tag))
                val viewModel =
                    PlaceAddTagViewModel(
                        initialTagId = null,
                        pageTagUseCase = pageTagUseCase,
                        getSelectedTagUseCase = getSelectedTagUseCase,
                    )

                viewModel.add(id = tag.id)
                advanceUntilIdle()

                viewModel.selectionUiState.value.tagIdSet shouldBe setOf(tag.id)
                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().tagList shouldBe listOf(tag)
                    cancelAndIgnoreRemainingEvents()
                }

                // 태그 입력이 쓰는 기능은 고를 수 있는 태그와 고른 태그를 읽는 것뿐이므로, 읽기 외의 요청이 없으면 저장된 연결도 바뀌지 않는다.
                verify { getSelectedTagUseCase(parameter = setOf(tag.id)) }
                excludeRecords {
                    pageTagUseCase(parameter = any())
                    getSelectedTagUseCase(parameter = any())
                }
                confirmVerified(pageTagUseCase, getSelectedTagUseCase)
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

                viewModel.selectionUiState.value.tagIdSet shouldBe setOf(secondTag.id)
            }
        }

        test("TC-77c6a3f5-8288-4cc9-a13c-9866c7e24b2b 초기 태그가 있으면 그 태그를 고른 상태로 시작한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(initialTagId = tag.id, selectedTagList = listOf(tag))

                viewModel.selectionUiState.value.tagIdSet shouldBe setOf(tag.id)

                viewModel.uiState.test {
                    awaitItem().tagList.shouldBeEmpty()
                    awaitItem().tagList shouldBe listOf(tag)
                }
            }
        }

        test("TC-2355ae81-e9cb-4203-86cb-23b6bbca72f6 TC-9f847acb-00d3-4f40-9d6a-12868f996ac5 TC-341e2c33-996a-4083-8855-689652ba9c56 표시 기준에서 빠진 태그는 제외된 것으로 표시되지만 선택은 남아 있다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(initialTagId = tag.id, selectedTagList = emptyList())

                viewModel.selectionUiState.value.tagIdSet shouldBe setOf(tag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().tagList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
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
                    PlaceAddTagViewModel(
                        initialTagId = null,
                        pageTagUseCase = pageTagUseCase,
                        getSelectedTagUseCase = getSelectedTagUseCase(),
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
    ): PlaceAddTagViewModel =
        PlaceAddTagViewModel(
            initialTagId = initialTagId,
            pageTagUseCase = pageTagUseCase(selectableTagList = selectableTagList),
            getSelectedTagUseCase = getSelectedTagUseCase(selectedTagList = selectedTagList),
        )

    private fun pageTagUseCase(selectableTagList: List<Tag> = emptyList()): PageTagUseCase {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(selectableTagList)))
        return pageTagUseCase
    }

    private fun getSelectedTagUseCase(selectedTagList: List<Tag> = emptyList()): GetSelectedTagUseCase {
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } returns flowOf(Result.success(selectedTagList))
        return getSelectedTagUseCase
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
