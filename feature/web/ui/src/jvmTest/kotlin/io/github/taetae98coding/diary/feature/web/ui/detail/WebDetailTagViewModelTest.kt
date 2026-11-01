@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.web.usecase.AddWebTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.GetWebTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.PageWebSelectableTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.RemoveWebTagUseCase
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
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

class WebDetailTagViewModelTest : FunSpec() {
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
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val item = tag()
                val parameter = PageWebSelectableTagUseCase.Parameter(webId = id, query = "")
                val useCase = mockk<PageWebSelectableTagUseCase>()
                every { useCase(parameter = parameter) } returns flowOf(Result.success(PagingData.from(listOf(item))))
                val viewModel =
                    WebDetailTagViewModel(
                        id = id,
                        pageWebSelectableTagUseCase = useCase,
                        getWebTagUseCase = mockk(relaxed = true),
                        addWebTagUseCase = mockk(relaxed = true),
                        removeWebTagUseCase = mockk(relaxed = true),
                    )

                val itemList = flowOf(viewModel.selectableTagPagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList shouldBe listOf(item)
                verify(exactly = 1) { useCase(parameter = parameter) }
            }
        }

        test("TC-806007af-1e20-415d-af6c-996d9731eb0f 저장된 연결의 태그를 연결 대상으로 표시한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val tagList = List(2) { tag() }
                val viewModel = viewModel(id = id, tagFlow = flowOf(Result.success(tagList)))

                viewModel.uiState.test {
                    awaitItem().tagList.shouldBeEmpty()
                    awaitItem().tagList shouldBe tagList
                }
            }
        }

        test("TC-6dc2aa2a-11e3-4735-b03c-d035143918ee TC-68ab9c0e-f7f0-4a43-ba2a-a7ddd5f7b8a4 저장된 연결이 바뀌면 연결 대상 표시도 바뀐다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val tag = tag()
                val tagFlow = MutableStateFlow(emptyList<Tag>())
                val viewModel =
                    viewModel(
                        id = id,
                        tagFlow = tagFlow.map { tagList -> Result.success(tagList) },
                    )

                viewModel.uiState.test {
                    awaitItem().tagList.shouldBeEmpty()

                    tagFlow.value = listOf(tag)

                    awaitItem().tagList shouldBe listOf(tag)
                }
            }
        }

        test("TC-6dc2aa2a-11e3-4735-b03c-d035143918ee 태그를 연결하면 상세 대상 웹 항목과 그 태그의 연결을 만든다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val addWebTagUseCase = mockk<AddWebTagUseCase>()
                coEvery { addWebTagUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(id = id, addWebTagUseCase = addWebTagUseCase)

                viewModel.add(tagId = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    addWebTagUseCase(parameter = AddWebTagUseCase.Parameter(webId = id, tagId = tagId))
                }
            }
        }

        test("TC-6dc2aa2a-11e3-4735-b03c-d035143918ee 연결을 해제하면 상세 대상 웹 항목과 그 태그의 연결을 해제한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val removeWebTagUseCase = mockk<RemoveWebTagUseCase>()
                coEvery { removeWebTagUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(id = id, removeWebTagUseCase = removeWebTagUseCase)

                viewModel.remove(tagId = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    removeWebTagUseCase(parameter = RemoveWebTagUseCase.Parameter(webId = id, tagId = tagId))
                }
            }
        }

        test("TC-be36a7a4-1f17-455f-b5bf-edbbb2ccb0ba 연결과 해제는 이어서 실행해도 모두 반영된다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val firstTagId = fixtureMonkey.giveMeOne<Uuid>()
                val secondTagId = fixtureMonkey.giveMeOne<Uuid>()
                val addWebTagUseCase = mockk<AddWebTagUseCase>()
                coEvery { addWebTagUseCase(parameter = any()) } returns Result.success(Unit)
                val removeWebTagUseCase = mockk<RemoveWebTagUseCase>()
                coEvery { removeWebTagUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel =
                    viewModel(
                        id = id,
                        addWebTagUseCase = addWebTagUseCase,
                        removeWebTagUseCase = removeWebTagUseCase,
                    )

                viewModel.add(tagId = firstTagId)
                viewModel.add(tagId = secondTagId)
                viewModel.remove(tagId = firstTagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    addWebTagUseCase(parameter = AddWebTagUseCase.Parameter(webId = id, tagId = firstTagId))
                }
                coVerify(exactly = 1) {
                    addWebTagUseCase(parameter = AddWebTagUseCase.Parameter(webId = id, tagId = secondTagId))
                }
                coVerify(exactly = 1) {
                    removeWebTagUseCase(
                        parameter = RemoveWebTagUseCase.Parameter(webId = id, tagId = firstTagId),
                    )
                }
            }
        }

        test("TC-be36a7a4-1f17-455f-b5bf-edbbb2ccb0ba 같은 태그의 연결이나 해제가 진행 중일 때 다시 요청하면 한 번만 실행한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val addWebTagUseCase = mockk<AddWebTagUseCase>()
                coEvery { addWebTagUseCase(parameter = any()) } returns Result.success(Unit)
                val removeWebTagUseCase = mockk<RemoveWebTagUseCase>()
                coEvery { removeWebTagUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel =
                    viewModel(
                        id = id,
                        addWebTagUseCase = addWebTagUseCase,
                        removeWebTagUseCase = removeWebTagUseCase,
                    )

                viewModel.add(tagId = tagId)
                viewModel.add(tagId = tagId)
                viewModel.remove(tagId = tagId)
                viewModel.remove(tagId = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) { addWebTagUseCase(parameter = any()) }
                coVerify(exactly = 1) { removeWebTagUseCase(parameter = any()) }
            }
        }

        test("TC-ba22acd9-8027-4e07-b190-d38fceafa918 연결이나 해제의 저장에 실패하면 저장된 연결을 그대로 보여 주고 다시 연결할 수 있다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val storedTag = tag()
                val newTagId = fixtureMonkey.giveMeOne<Uuid>()
                val addWebTagUseCase = mockk<AddWebTagUseCase>()
                coEvery { addWebTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException("연결 저장 실패"))
                val removeWebTagUseCase = mockk<RemoveWebTagUseCase>()
                coEvery { removeWebTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException("해제 저장 실패"))
                val viewModel =
                    viewModel(
                        id = id,
                        tagFlow = flowOf(Result.success(listOf(storedTag))),
                        addWebTagUseCase = addWebTagUseCase,
                        removeWebTagUseCase = removeWebTagUseCase,
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

                    coVerify(exactly = 2) {
                        addWebTagUseCase(parameter = AddWebTagUseCase.Parameter(webId = id, tagId = newTagId))
                    }
                    coVerify(exactly = 1) {
                        removeWebTagUseCase(parameter = RemoveWebTagUseCase.Parameter(webId = id, tagId = storedTag.id))
                    }
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
        searchTests()
        restorationTests()
    }

    private fun searchTests() {
        test("TC-bb7ebc0b-c1f0-4e8f-89da-93c26ac97993 검색어가 바뀌면 새 검색어 기준으로 선택 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val allTagList = List(2) { tag() }
                val matchedTagList = listOf(allTagList.first())
                val pageWebSelectableTagUseCase = mockk<PageWebSelectableTagUseCase>()
                every {
                    pageWebSelectableTagUseCase(parameter = PageWebSelectableTagUseCase.Parameter(webId = id, query = ""))
                } returns flowOf(Result.success(PagingData.from(allTagList)))
                every {
                    pageWebSelectableTagUseCase(
                        parameter = PageWebSelectableTagUseCase.Parameter(webId = id, query = SEARCH_QUERY),
                    )
                } returns flowOf(Result.success(PagingData.from(matchedTagList)))
                val viewModel = viewModel(id = id, pageWebSelectableTagUseCase = pageWebSelectableTagUseCase)

                viewModel.tagPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allTagList

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedTagList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-5d358731-189d-4dc8-a021-587dc2651495 검색어를 바꿔도 연결 대상으로 표시하는 태그는 그대로다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val tagList = List(2) { tag() }
                val viewModel = viewModel(id = id, tagFlow = flowOf(Result.success(tagList)))

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().tagList shouldBe tagList

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private fun restorationTests() {
        test("TC-600fa417-a608-4189-bd8c-409ea8506e2e 복원 뒤 새로 만든 화면이 되살린 검색어를 알리면 입력 정지 대기 시간 뒤 그 검색어로 좁힌 목록을 보여 준다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val query = "Query${fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)}"
                val allTagList = List(2) { tag() }
                val matchedTagList = listOf(allTagList.first())
                val useCase = mockk<PageWebSelectableTagUseCase>()
                every {
                    useCase(parameter = PageWebSelectableTagUseCase.Parameter(webId = id, query = ""))
                } returns flowOf(Result.success(PagingData.from(allTagList)))
                every {
                    useCase(parameter = PageWebSelectableTagUseCase.Parameter(webId = id, query = query))
                } returns flowOf(Result.success(PagingData.from(matchedTagList)))
                val viewModel = viewModel(id = id, pageWebSelectableTagUseCase = useCase)

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
        id: Uuid,
        tagFlow: Flow<Result<List<Tag>>> = flowOf(Result.success(emptyList())),
        addWebTagUseCase: AddWebTagUseCase = mockk(relaxed = true),
        removeWebTagUseCase: RemoveWebTagUseCase = mockk(relaxed = true),
        pageWebSelectableTagUseCase: PageWebSelectableTagUseCase =
            mockk<PageWebSelectableTagUseCase>().apply {
                every { this@apply(parameter = any()) } returns flowOf(Result.success(PagingData.empty()))
            },
    ): WebDetailTagViewModel {
        val getWebTagUseCase = mockk<GetWebTagUseCase>()
        every { getWebTagUseCase(parameter = id) } returns tagFlow

        return WebDetailTagViewModel(
            id = id,
            pageWebSelectableTagUseCase = pageWebSelectableTagUseCase,
            getWebTagUseCase = getWebTagUseCase,
            addWebTagUseCase = addWebTagUseCase,
            removeWebTagUseCase = removeWebTagUseCase,
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
                .sample()
    }
}
