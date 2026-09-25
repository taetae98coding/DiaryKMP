@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.place.usecase.AddPlaceTagUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceTagUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceSelectableTagUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RemovePlaceTagUseCase
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
import kotlin.uuid.Uuid

class PlaceDetailTagViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("저장된 연결의 태그를 연결 대상으로 표시한다") {
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

        test("TC-PLACE-DETAIL-FEATURE-033 저장된 연결이 바뀌면 연결 대상 표시도 바뀐다") {
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

        test("TC-PLACE-DETAIL-FEATURE-033 TC-PLACE-DETAIL-FEATURE-038 태그를 연결하면 상세 대상 장소와 그 태그의 연결을 만든다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val addPlaceTagUseCase = mockk<AddPlaceTagUseCase>()
                coEvery { addPlaceTagUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(id = id, addPlaceTagUseCase = addPlaceTagUseCase)

                viewModel.add(tagId = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    addPlaceTagUseCase(parameter = AddPlaceTagUseCase.Parameter(placeId = id, tagId = tagId))
                }
            }
        }

        test("TC-PLACE-DETAIL-FEATURE-033 TC-PLACE-DETAIL-FEATURE-038 연결을 해제하면 상세 대상 장소와 그 태그의 연결을 해제한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val removePlaceTagUseCase = mockk<RemovePlaceTagUseCase>()
                coEvery { removePlaceTagUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = viewModel(id = id, removePlaceTagUseCase = removePlaceTagUseCase)

                viewModel.remove(tagId = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    removePlaceTagUseCase(parameter = RemovePlaceTagUseCase.Parameter(placeId = id, tagId = tagId))
                }
            }
        }

        test("TC-PLACE-DETAIL-DOMAIN-031 연결과 해제는 이어서 실행해도 모두 반영된다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val firstTagId = fixtureMonkey.giveMeOne<Uuid>()
                val secondTagId = fixtureMonkey.giveMeOne<Uuid>()
                val addPlaceTagUseCase = mockk<AddPlaceTagUseCase>()
                coEvery { addPlaceTagUseCase(parameter = any()) } returns Result.success(Unit)
                val removePlaceTagUseCase = mockk<RemovePlaceTagUseCase>()
                coEvery { removePlaceTagUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel =
                    viewModel(
                        id = id,
                        addPlaceTagUseCase = addPlaceTagUseCase,
                        removePlaceTagUseCase = removePlaceTagUseCase,
                    )

                viewModel.add(tagId = firstTagId)
                viewModel.add(tagId = secondTagId)
                viewModel.remove(tagId = firstTagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    addPlaceTagUseCase(parameter = AddPlaceTagUseCase.Parameter(placeId = id, tagId = firstTagId))
                }
                coVerify(exactly = 1) {
                    addPlaceTagUseCase(parameter = AddPlaceTagUseCase.Parameter(placeId = id, tagId = secondTagId))
                }
                coVerify(exactly = 1) {
                    removePlaceTagUseCase(
                        parameter = RemovePlaceTagUseCase.Parameter(placeId = id, tagId = firstTagId),
                    )
                }
            }
        }
        searchTests()
        restorationTests()
    }

    private fun searchTests() {
        test("TC-ENTITY-TAG-INPUT-DATA-005 검색어가 바뀌면 새 검색어 기준으로 선택 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val allTagList = List(2) { tag() }
                val matchedTagList = listOf(allTagList.first())
                val pagePlaceSelectableTagUseCase = mockk<PagePlaceSelectableTagUseCase>()
                every {
                    pagePlaceSelectableTagUseCase(parameter = PagePlaceSelectableTagUseCase.Parameter(placeId = id, query = ""))
                } returns flowOf(Result.success(PagingData.from(allTagList)))
                every {
                    pagePlaceSelectableTagUseCase(
                        parameter = PagePlaceSelectableTagUseCase.Parameter(placeId = id, query = SEARCH_QUERY),
                    )
                } returns flowOf(Result.success(PagingData.from(matchedTagList)))
                val viewModel = viewModel(id = id, pagePlaceSelectableTagUseCase = pagePlaceSelectableTagUseCase)

                viewModel.tagPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allTagList

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedTagList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-009 검색어를 바꿔도 연결 대상으로 표시하는 태그는 그대로다") {
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
        test("TC-ENTITY-TAG-INPUT-DOMAIN-017 복원 뒤 새로 만든 화면은 되살린 검색어로 좁힌 목록을 기다리지 않고 바로 보여 주고 대상 전체를 거치지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val query = "Query${fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)}"
                val allTagList = List(2) { tag() }
                val matchedTagList = listOf(allTagList.first())
                val useCase = mockk<PagePlaceSelectableTagUseCase>()
                every {
                    useCase(parameter = PagePlaceSelectableTagUseCase.Parameter(placeId = id, query = ""))
                } returns flowOf(Result.success(PagingData.from(allTagList)))
                every {
                    useCase(parameter = PagePlaceSelectableTagUseCase.Parameter(placeId = id, query = query))
                } returns flowOf(Result.success(PagingData.from(matchedTagList)))
                val viewModel = viewModel(id = id, pagePlaceSelectableTagUseCase = useCase, isListOpened = false)

                // 복원된 화면은 목록을 다시 열면서 되살린 검색어를 처음으로 알려 준다.
                viewModel.tagPagingData.test {
                    viewModel.updateQuery(query)
                    runCurrent()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedTagList
                    cancelAndIgnoreRemainingEvents()
                }
                verify(exactly = 0) { useCase(parameter = PagePlaceSelectableTagUseCase.Parameter(placeId = id, query = "")) }
            }
        }
    }

    private fun viewModel(
        id: Uuid,
        tagFlow: Flow<Result<List<Tag>>> = flowOf(Result.success(emptyList())),
        addPlaceTagUseCase: AddPlaceTagUseCase = mockk(relaxed = true),
        removePlaceTagUseCase: RemovePlaceTagUseCase = mockk(relaxed = true),
        pagePlaceSelectableTagUseCase: PagePlaceSelectableTagUseCase =
            mockk<PagePlaceSelectableTagUseCase>().apply {
                every { this@apply(parameter = any()) } returns flowOf(Result.success(PagingData.empty()))
            },
        isListOpened: Boolean = true,
    ): PlaceDetailTagViewModel {
        val getPlaceTagUseCase = mockk<GetPlaceTagUseCase>()
        every { getPlaceTagUseCase(parameter = id) } returns tagFlow

        return PlaceDetailTagViewModel(
            id = id,
            pagePlaceSelectableTagUseCase = pagePlaceSelectableTagUseCase,
            getPlaceTagUseCase = getPlaceTagUseCase,
            addPlaceTagUseCase = addPlaceTagUseCase,
            removePlaceTagUseCase = removePlaceTagUseCase,
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
                .sample()
    }
}
