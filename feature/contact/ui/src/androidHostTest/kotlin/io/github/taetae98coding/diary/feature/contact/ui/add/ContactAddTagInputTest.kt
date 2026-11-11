package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.contact.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.contact.ui.addTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.contact.ui.clickEntityTagInput
import io.github.taetae98coding.diary.feature.contact.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.contact.ui.sendTagAddedResult
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ContactAddTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-d1765324-0bf7-4ee3-9a1c-d2a06e906fb0 추가에 성공해도 고른 태그는 유지한다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        // 성공 뒤 화면이 태그 선택을 비우면 표시가 사라지도록, 고른 태그를 실제로 들고 있는 태그 선택 상태를 쓴다.
        val tagViewModel = tagViewModel(initialTagId = tag.id, allTagList = listOf(tag))
        composeRule.setContactAddScreen(
            viewModel = effectViewModel(effect = ContactAddEffect.AddSucceeded(id = Arb.kotlinUuid().next())),
            tagViewModel = tagViewModel,
        )
        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()

        composeRule.clickAdd()

        composeRule.nameInput().assert(hasText(""))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
        composeRule.runOnIdle { tagViewModel.selectionUiState.value.tagIdSet shouldBe setOf(tag.id) }
    }

    @Test
    fun `TC-7363f1df-65aa-42bc-a6b3-890868995701 태그를 골라도 연락처 추가를 요청하지 않는다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        val viewModel = screenTestViewModel()
        val tagViewModel = tagViewModel(initialTagId = null, allTagList = listOf(tag))
        composeRule.setContactAddScreen(viewModel = viewModel, tagViewModel = tagViewModel)

        composeRule.clickEntityTagInput()
        awaitTagTitle(title = SELECTED_TAG_TITLE, count = 1)
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).performClick()

        // 고른 태그는 선택 목록의 행과 태그 입력의 연결 대상 표시에 모두 나타난다.
        awaitTagTitle(title = SELECTED_TAG_TITLE, count = 2)
        composeRule.runOnIdle { tagViewModel.selectionUiState.value.tagIdSet shouldBe setOf(tag.id) }
        // 연락처와 태그의 연결은 연락처 추가와 함께만 저장되므로, 추가를 요청하지 않았으면 저장된 연결도 없다.
        verify(exactly = 0) { viewModel.add(detail = any(), tagIdSet = any()) }
    }

    @Test
    fun `TC-c7408895-4227-4d3e-b9ce-5293a99f8aa4 추가할 때 고른 태그를 함께 넘긴다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        val viewModel = effectViewModel(effect = ContactAddEffect.AddSucceeded(id = Uuid.random()))
        composeRule.setContactAddScreen(
            viewModel = viewModel,
            tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)),
        )
        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.waitForIdle()

        composeRule.clickAdd()

        verify(exactly = 1) { viewModel.add(detail = any(), tagIdSet = setOf(tag.id)) }
    }

    @Test
    fun `TC-223a71ea-06b8-4070-b4f0-043702313017 화면이 재생성되어도 고른 태그가 유지된다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        val tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            ContactAddScreenTestTheme {
                ContactAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addedResultRequestKey = null,
                    componentVisibleProvider = { ContactAddScaffoldComponentVisible() },
                    addViewModel = screenTestViewModel(),
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.nameInput().assert(hasText(TYPED_NAME))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-d63f156f-e530-4cda-b97b-2e62b8332e3f TagDetail 연락처 탭에서 진입했으면 복원 뒤 입력은 남고 태그는 그 탭의 태그만 선택된다`() {
        assertMemoryRestore(
            initialTag = Arb.tag(title = INITIAL_TAG_TITLE).next(),
            expectedTitleListAfterRestore = listOf(INITIAL_TAG_TITLE),
        )
    }

    @Test
    fun `TC-d63f156f-e530-4cda-b97b-2e62b8332e3f 그 밖의 경로로 진입했으면 복원 뒤 입력은 남고 선택된 태그가 없다`() {
        assertMemoryRestore(
            initialTag = null,
            expectedTitleListAfterRestore = emptyList(),
        )
    }

    @Test
    fun `TagAdd 화면에서 추가한 태그가 돌아오면 고른 태그로 더한다`() {
        val addedTag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        val tagViewModel = addTagScreenTestViewModel()
        val resultEventBus = ResultEventBus()
        composeRule.setContactAddScreen(resultEventBus = resultEventBus, tagViewModel = tagViewModel)

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        verify(exactly = 1) { tagViewModel.add(tagId = addedTag.id) }
    }

    @Test
    fun `고를 수 있는 태그가 없으면 태그 추가 칩이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        composeRule.setContactAddScreen(navigateToTagAdd = { tagAddCount += 1 })

        composeRule.clickEntityTagInput()

        tagAddCount shouldBe 1
    }

    // 선택 목록은 페이지 단위로 준비되므로 항목이 나타날 때까지 프레임과 실제 시간을 함께 진행시킨다.
    private fun awaitTagTitle(
        title: String,
        count: Int,
    ) {
        repeat(PICKER_WAIT_ATTEMPT_COUNT) {
            composeRule.waitForIdle()
            if (composeRule.onAllNodesWithText(title).fetchSemanticsNodes().size == count) return
            composeRule.mainClock.advanceTimeByFrame()
            @Suppress("ForbiddenMethodCall")
            Thread.sleep(PICKER_WAIT_INTERVAL_MILLIS)
        }

        error("$title 이 $count 번 나타나지 않았다")
    }

    private fun assertMemoryRestore(
        initialTag: Tag?,
        expectedTitleListAfterRestore: List<String>,
    ) {
        val otherTag = Arb.tag(title = OTHER_TAG_TITLE).next()
        val allTagList = listOfNotNull(initialTag, otherTag)
        val tagViewModelList = mutableListOf<EntityAddTagViewModel>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 메모리 정리 뒤에는 태그 선택을 들고 있던 객체도 새로 만들어지므로 복원할 때마다 진입 경로의 초기값으로 새로 만든다.
            val tagViewModel =
                remember {
                    tagViewModel(initialTagId = initialTag?.id, allTagList = allTagList).also(tagViewModelList::add)
                }

            ContactAddScreenTestTheme {
                ContactAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addedResultRequestKey = null,
                    componentVisibleProvider = { ContactAddScaffoldComponentVisible() },
                    addViewModel = remember { screenTestViewModel() },
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.runOnIdle { tagViewModelList.last().add(tagId = otherTag.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        tagViewModelList.size shouldBe 2
        composeRule.nameInput().assert(hasText(TYPED_NAME))
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertDoesNotExist()
        expectedTitleListAfterRestore.forEach { title -> composeRule.onNodeWithText(title).assertExists() }
        composeRule.runOnIdle {
            tagViewModelList
                .last()
                .selectionUiState.value.tagIdSet shouldBe setOfNotNull(initialTag?.id)
        }
    }

    private fun tagViewModel(
        initialTagId: Uuid?,
        allTagList: List<Tag>,
    ): EntityAddTagViewModel {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(allTagList)))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            val idSet = firstArg<Set<Uuid>>()
            flowOf(Result.success(allTagList.filter { tag -> tag.id in idSet }))
        }

        return EntityAddTagViewModel(
            initialTagId = initialTagId,
            pageTagUseCase = pageTagUseCase,
            getSelectedTagUseCase = getSelectedTagUseCase,
        )
    }

    private companion object {
        const val PICKER_WAIT_ATTEMPT_COUNT = 500
        const val PICKER_WAIT_INTERVAL_MILLIS = 10L
        const val SELECTED_TAG_TITLE = "ContactAddSelectedTag"
        const val INITIAL_TAG_TITLE = "ContactAddRestoreInitialTag"
        const val OTHER_TAG_TITLE = "ContactAddRestoreOtherTag"
    }
}
