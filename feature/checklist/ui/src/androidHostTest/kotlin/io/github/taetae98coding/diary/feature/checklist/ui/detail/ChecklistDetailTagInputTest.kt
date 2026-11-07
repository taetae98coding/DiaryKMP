package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.checklist.ui.ChecklistTagTestTheme
import io.github.taetae98coding.diary.feature.checklist.ui.DEFAULT_ENTITY_TAG_LABEL
import io.github.taetae98coding.diary.feature.checklist.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.checklist.ui.clickEntityTagInput
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailTestEnvironment.Companion.PASSPORT
import io.github.taetae98coding.diary.feature.checklist.ui.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.checklist.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.checklist.ui.sendTagAddedResult
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ChecklistDetailTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-4763e614-8116-4664-9dc4-be0727aad334 조회 중에는 태그 입력을 표시하지 않는다`() {
        val environment = ChecklistDetailTestEnvironment()
        environment.uiState.value = ChecklistDetailUiState.Loading
        setScreen(environment)

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-4763e614-8116-4664-9dc4-be0727aad334 조회가 끝나면 태그 입력을 표시한다`() {
        setScreen(ChecklistDetailTestEnvironment())

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-8df74b94-56d6-4ecf-8f21-4ade9e5b2604 태그를 연결해도 수정 동작의 제공 여부와 리셋 버튼의 활성 상태는 바뀌지 않는다`() {
        val selectableTag = Arb.tag(title = SELECTABLE_TAG_TITLE).next()
        val environment =
            ChecklistDetailTestEnvironment(
                itemList = listOf(item(PASSPORT, isChecked = true)),
                tagViewModel = detailTagScreenTestViewModel(selectableTagList = listOf(selectableTag)),
            )
        setScreen(environment)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).assertDoesNotExist()

        linkTag(title = SELECTABLE_TAG_TITLE)

        verify(exactly = 1) { environment.tagViewModel.add(tagId = selectableTag.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_RESET).assertIsEnabled()
    }

    @Test
    fun `TC-8df74b94-56d6-4ecf-8f21-4ade9e5b2604 태그를 해제해도 수정 동작의 제공 여부는 바뀌지 않는다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = ChecklistDetailTestEnvironment(tagViewModel = detailTagScreenTestViewModel(tagList = listOf(linkedTag), selectableTagList = listOf(linkedTag)))
        setScreen(environment)

        unlinkTag(title = LINKED_TAG_TITLE)

        verify(exactly = 1) { environment.tagViewModel.remove(tagId = linkedTag.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).assertDoesNotExist()
    }

    @Test
    fun `TC-c5640071-c725-443a-ba6b-f1ea81ca8be6 연결한 태그를 누르면 그 태그의 상세 이동을 한 번 요청한다`() {
        val linkedTag = Arb.tag(title = LINKED_TAG_TITLE).next()
        val environment = ChecklistDetailTestEnvironment(tagViewModel = detailTagScreenTestViewModel(tagList = listOf(linkedTag)))
        setScreen(environment)

        composeRule.onNodeWithText(LINKED_TAG_TITLE).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        environment.navigatedTagIdList shouldBe listOf(linkedTag.id)
    }

    @Test
    fun `TC-433cb567-9a25-46fa-9b44-a048897fc4f5 태그를 연결해도 입력 중인 내용과 체크 항목의 체크 여부는 바뀌지 않는다`() {
        val selectableTag = Arb.tag(title = SELECTABLE_TAG_TITLE).next()
        val environment =
            ChecklistDetailTestEnvironment(
                itemList = listOf(item(PASSPORT, isChecked = true)),
                tagViewModel = detailTagScreenTestViewModel(selectableTagList = listOf(selectableTag)),
            )
        setScreen(environment)
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()

        linkTag(title = SELECTABLE_TAG_TITLE)

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.onNode(itemRow(PASSPORT)).assertIsOn()
        verify(exactly = 0) { environment.itemViewModel.toggle(id = any(), isChecked = any()) }
        verify(exactly = 0) { environment.checklistViewModel.update(detail = any()) }
    }

    @Test
    fun `TagAdd 화면에서 추가한 태그가 돌아오면 상세 대상 체크리스트에 연결한다`() {
        val addedTag = Arb.tag(title = SELECTABLE_TAG_TITLE).next()
        val environment = ChecklistDetailTestEnvironment()
        setScreen(environment)

        environment.resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.tagViewModel.add(tagId = addedTag.id) }
    }

    @Test
    fun `연결할 태그가 없으면 태그 추가 칩이 TagAdd 이동을 요청한다`() {
        val environment = ChecklistDetailTestEnvironment()
        setScreen(environment)

        composeRule.clickEntityTagInput()

        environment.tagAddCount shouldBe 1
    }

    private fun linkTag(title: String) {
        composeRule.clickEntityTagInput()
        awaitPickerRow(title = title)
        composeRule.onNodeWithText(title).performClick()
        composeRule.waitForIdle()
    }

    // 연결된 태그는 연결 대상 표시와 선택 목록에 모두 나타나므로, 선택 목록의 마지막 노드를 눌러 해제한다.
    private fun unlinkTag(title: String) {
        composeRule.clickEntityTagInput()
        awaitPickerRow(title = title, count = 2)
        composeRule.onAllNodesWithText(title).onLast().performClick()
        composeRule.waitForIdle()
    }

    // 선택 목록은 페이지 단위로 준비되므로 항목이 나타날 때까지 프레임과 실제 시간을 함께 진행시킨다.
    private fun awaitPickerRow(
        title: String,
        count: Int = 1,
    ) {
        repeat(PICKER_WAIT_ATTEMPT_COUNT) {
            composeRule.waitForIdle()
            if (composeRule.onAllNodesWithText(title).fetchSemanticsNodes().size >= count) return
            composeRule.mainClock.advanceTimeByFrame()
            @Suppress("ForbiddenMethodCall")
            Thread.sleep(PICKER_WAIT_INTERVAL_MILLIS)
        }

        error("태그 선택 목록에 $title 이 나타나지 않았다")
    }

    private fun setScreen(environment: ChecklistDetailTestEnvironment) {
        composeRule.setContent {
            ChecklistTagTestTheme(resultEventBus = environment.resultEventBus) {
                ChecklistDetailScreen(
                    navigateUp = {},
                    navigateToTagAdd = { environment.tagAddCount += 1 },
                    navigateToTagDetail = environment.navigatedTagIdList::add,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    componentVisibleProvider = { ChecklistDetailScaffoldComponentVisible() },
                    checklistViewModel = environment.checklistViewModel,
                    itemViewModel = environment.itemViewModel,
                    tagViewModel = environment.tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val PICKER_WAIT_ATTEMPT_COUNT = 500
        const val PICKER_WAIT_INTERVAL_MILLIS = 10L
        const val DEFAULT_UPDATE = "Update checklist"
        const val DEFAULT_RESET = "Uncheck all"
        const val LINKED_TAG_TITLE = "ChecklistDetailLinkedTag"
        const val SELECTABLE_TAG_TITLE = "ChecklistDetailSelectableTag"
        const val TYPED_TITLE = "ChecklistDetailTagTypedTitle"
    }
}
