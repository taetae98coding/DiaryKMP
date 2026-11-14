package io.github.taetae98coding.diary.feature.routine.ui.home.filter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterEvent
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.tag.tagDetail
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 필터의 보기 범위 영역을 확인한다. 고른 값의 저장과 목록 반영은 ViewModel과 조회 테스트가 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoutineHomeViewScopeSectionTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val eventList = mutableListOf<RoutineHomeFilterBottomSheetEvent>()
    private val tagEventList = mutableListOf<TagFilterEvent>()
    private var uiState by mutableStateOf(RoutineHomeFilterUiState())

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-829e32df-b32a-4101-812c-58ee288ec037 필터에 태그 필터와 함께 보기 범위 두 값이 있고 처음에는 오늘 할 루틴이 골라져 있다`() {
        setBottomSheetContent(tagList = listOf(tag(title = TAG_TITLE)))

        composeRule.onNodeWithText(VIEW_SCOPE_TITLE).assertExists()
        composeRule.onNodeWithText(TAG_FILTER_TITLE).assertExists()
        composeRule.onNodeWithText(TAG_TITLE).assertExists()
        composeRule.onNode(viewScopeOption(TODAY_LABEL)).assertIsSelected()
        composeRule.onNode(viewScopeOption(ALL_LABEL)).assertIsNotSelected()
        viewScopeOptionCount() shouldBe 2
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-829e32df-b32a-4101-812c-58ee288ec037 한국어 환경에서 보기 범위는 오늘 할 루틴과 모든 루틴이다`() {
        setBottomSheetContent()

        composeRule.onNodeWithText(KOREAN_VIEW_SCOPE_TITLE).assertExists()
        composeRule.onNode(viewScopeOption(KOREAN_TODAY_LABEL)).assertIsSelected()
        composeRule.onNode(viewScopeOption(KOREAN_ALL_LABEL)).assertIsNotSelected()
    }

    @Test
    fun `TC-07fef83b-4cac-4e3a-8c90-ed97fcd1cf84 보기 범위를 누르면 확정 없이 고른 값을 전달하고 반영되면 그 값 하나만 골라진 채 필터가 남는다`() {
        setBottomSheetContent(isViewScopeApplied = true)

        composeRule.onNode(viewScopeOption(ALL_LABEL)).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(RoutineHomeFilterBottomSheetEvent.SetViewScope(viewScope = RoutineViewScope.ALL))
        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNode(viewScopeOption(ALL_LABEL)).assertIsSelected()
        composeRule.onNode(viewScopeOption(TODAY_LABEL)).assertIsNotSelected()
        composeRule.onNodeWithText(VIEW_SCOPE_TITLE).assertExists()

        composeRule.onNode(viewScopeOption(TODAY_LABEL)).performClick()
        composeRule.waitForIdle()

        eventList.last() shouldBe RoutineHomeFilterBottomSheetEvent.SetViewScope(viewScope = RoutineViewScope.TODAY)
        composeRule.onNode(viewScopeOption(TODAY_LABEL)).assertIsSelected()
        composeRule.onNode(viewScopeOption(ALL_LABEL)).assertIsNotSelected()
        composeRule.onNodeWithText(VIEW_SCOPE_TITLE).assertExists()
    }

    @Test
    fun `TC-8389da95-7521-4222-9754-2e7007fb3d13 이미 고른 보기 범위를 다시 눌러도 골라진 값은 그대로다`() {
        listOf(RoutineViewScope.TODAY to TODAY_LABEL, RoutineViewScope.ALL to ALL_LABEL).forEachIndexed { index, (viewScope, label) ->
            uiState = RoutineHomeFilterUiState(viewScope = viewScope)
            if (index == 0) setBottomSheetContent(isViewScopeApplied = true)
            composeRule.waitForIdle()

            composeRule.onNode(viewScopeOption(label)).performClick()
            composeRule.waitForIdle()

            // 같은 값을 저장하는 요청은 저장된 선택을 바꾸지 않는다.
            eventList.last() shouldBe RoutineHomeFilterBottomSheetEvent.SetViewScope(viewScope = viewScope)
            uiState.viewScope shouldBe viewScope
            composeRule.onNode(viewScopeOption(label)).assertIsSelected()
        }
    }

    @Test
    fun `TC-ab4d2410-0156-42b1-b2f5-e77016b9d15e 고른 보기 범위가 저장되지 않으면 바꾸기 전 값이 골라진 채 안내 없이 남는다`() {
        setBottomSheetContent(isViewScopeApplied = false)

        composeRule.onNode(viewScopeOption(ALL_LABEL)).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(RoutineHomeFilterBottomSheetEvent.SetViewScope(viewScope = RoutineViewScope.ALL))
        composeRule.onNode(viewScopeOption(TODAY_LABEL)).assertIsSelected()
        composeRule.onNode(viewScopeOption(ALL_LABEL)).assertIsNotSelected()
        composeRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun `TC-2272cec0-2dd9-4487-91cc-6502d3ca705d 보기 범위가 무엇이어도 태그 선택과 태그 추가를 실행할 수 있다`() {
        val tag = tag(title = TAG_TITLE)
        setBottomSheetContent(tagList = listOf(tag), isViewScopeApplied = true)

        // 오늘 할 루틴에서는 태그를 선택하고, 모든 루틴에서는 그 선택을 해제한다.
        listOf(
            TODAY_LABEL to TagFilterEvent.Select(id = tag.id),
            ALL_LABEL to TagFilterEvent.Unselect(id = tag.id),
        ).forEach { (label, tagEvent) ->
            composeRule.onNode(viewScopeOption(label)).performClick()
            composeRule.waitForIdle()
            tagEventList.clear()

            composeRule.onNodeWithText(TAG_TITLE).performClick()
            composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled().performClick()
            composeRule.waitForIdle()

            tagEventList shouldBe listOf(tagEvent, TagFilterEvent.ClickAdd)
            composeRule.onNode(viewScopeOption(label)).assertIsSelected()
        }
    }

    @Test
    fun `TC-d86bcda0-2d05-4e0b-b4d8-57ad3926daa0 선택 전체 해제를 눌러도 보기 범위는 그대로 골라져 있다`() {
        val tag = tag(title = TAG_TITLE)

        listOf(RoutineViewScope.ALL to ALL_LABEL, RoutineViewScope.TODAY to TODAY_LABEL).forEachIndexed { index, (viewScope, label) ->
            uiState = RoutineHomeFilterUiState(selectedTagIdSet = setOf(tag.id), viewScope = viewScope)
            if (index == 0) setBottomSheetContent(tagList = listOf(tag), isViewScopeApplied = true)
            composeRule.waitForIdle()
            eventList.clear()

            composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).performClick()
            composeRule.waitForIdle()

            eventList.shouldBeEmpty()
            tagEventList.last() shouldBe TagFilterEvent.UnselectAll
            composeRule.onNodeWithText(TAG_TITLE).assertIsNotSelected()
            composeRule.onNode(viewScopeOption(label)).assertIsSelected()
        }
    }

    private fun viewScopeOption(label: String): SemanticsMatcher = hasText(label) and hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)

    // 보기 범위에서 고를 수 있는 값의 수다. 고르는 값은 하나만 골라지는 단추로 놓인다.
    private fun viewScopeOptionCount(): Int = composeRule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)).fetchSemanticsNodes().size

    // 저장이 성공하면 고른 값이 저장된 선택으로 돌아오고, 실패하면 저장된 선택이 그대로다.
    private fun setBottomSheetContent(
        tagList: List<Tag> = emptyList(),
        isViewScopeApplied: Boolean = true,
    ) {
        composeRule.setContent {
            DiaryTheme {
                RoutineHomeFilterBottomSheetContent(
                    onEvent = { event ->
                        eventList += event
                        if (isViewScopeApplied && event is RoutineHomeFilterBottomSheetEvent.SetViewScope) uiState = uiState.copy(viewScope = event.viewScope)
                    },
                    onTagFilterEvent = { event ->
                        tagEventList += event
                        when (event) {
                            is TagFilterEvent.Select -> uiState = uiState.copy(selectedTagIdSet = uiState.selectedTagIdSet + event.id)
                            is TagFilterEvent.Unselect -> uiState = uiState.copy(selectedTagIdSet = uiState.selectedTagIdSet - event.id)
                            is TagFilterEvent.UnselectAll -> uiState = uiState.copy(selectedTagIdSet = emptySet<Uuid>())
                            is TagFilterEvent.ClickAdd -> Unit
                        }
                    },
                    tagPagingItems = MutableStateFlow(tagPagingDataOf(tagList)).collectAsLazyPagingItems(),
                    uiStateProvider = { uiState },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val VIEW_SCOPE_TITLE = "Show"
        const val TODAY_LABEL = "Today's routines"
        const val ALL_LABEL = "All routines"
        const val KOREAN_VIEW_SCOPE_TITLE = "보기 범위"
        const val KOREAN_TODAY_LABEL = "오늘 할 루틴"
        const val KOREAN_ALL_LABEL = "모든 루틴"
        const val TAG_FILTER_TITLE = "Tag Filter"
        const val TAG_TITLE = "RoutineViewScopeTag"
        const val ADD_LABEL = "Add tag"
        const val UNSELECT_ALL_CONTENT_DESCRIPTION = "Clear tag filter"

        fun tag(title: String): Tag =
            Arb
                .tag()
                .next()
                .copy(
                    detail = Arb.tagDetail().next().copy(emoji = "", title = title),
                    isFinished = false,
                    isDeleted = false,
                )

        fun tagPagingDataOf(tagList: List<Tag>): PagingData<Tag> =
            PagingData.from(
                data = tagList,
                sourceLoadStates =
                    LoadStates(
                        refresh = LoadState.NotLoading(endOfPaginationReached = true),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true),
                    ),
            )
    }
}
