@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.add

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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Duration.Companion.milliseconds
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

        test("TC-PLACE-ADD-FEATURE-035 초기 태그가 없으면 태그를 하나도 고르지 않은 상태로 시작한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(initialTagId = null)

                viewModel.tagIdSet.value.shouldBeEmpty()
            }
        }

        test("TC-PLACE-ADD-FEATURE-036 고른 태그는 선택 상태로만 반영되고 장소·태그 연결은 만들어지지 않는다") {
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

                viewModel.tagIdSet.value shouldBe setOf(tag.id)
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

        test("TC-ENTITY-TAG-INPUT-FEATURE-009 연결된 태그의 해제를 요청하면 선택에서 빠진다") {
            runTest(mainDispatcher) {
                val firstTag = tag()
                val secondTag = tag()
                val viewModel = viewModel(initialTagId = null)

                viewModel.add(id = firstTag.id)
                viewModel.add(id = secondTag.id)
                viewModel.remove(id = firstTag.id)
                advanceUntilIdle()

                viewModel.tagIdSet.value shouldBe setOf(secondTag.id)
            }
        }

        test("TC-TAG-DETAIL-PLACE-FEATURE-008 초기 태그가 있으면 그 태그를 고른 상태로 시작한다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(initialTagId = tag.id, selectedTagList = listOf(tag))

                viewModel.tagIdSet.value shouldBe setOf(tag.id)

                viewModel.uiState.test {
                    awaitItem().tagList.shouldBeEmpty()
                    awaitItem().tagList shouldBe listOf(tag)
                }
            }
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-003 TC-TAG-DETAIL-PLACE-FEATURE-009 TC-PLACE-ADD-DOMAIN-024 표시 기준에서 빠진 태그는 연결 대상에서 제외된다") {
            runTest(mainDispatcher) {
                val tag = tag()
                val viewModel = viewModel(initialTagId = tag.id, selectedTagList = emptyList())

                viewModel.tagIdSet.value shouldBe setOf(tag.id)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().tagList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-016 복원 뒤 새로 만든 화면은 대상 전체를 먼저 보여 주고 되살린 검색어는 입력 정지 대기 시간이 지나야 반영한다") {
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

                    viewModel.updateQuery(query)
                    advanceTimeBy(INPUT_IDLE_DELAY - 1.milliseconds)
                    runCurrent()

                    expectNoEvents()

                    advanceTimeBy(2.milliseconds)
                    runCurrent()

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
