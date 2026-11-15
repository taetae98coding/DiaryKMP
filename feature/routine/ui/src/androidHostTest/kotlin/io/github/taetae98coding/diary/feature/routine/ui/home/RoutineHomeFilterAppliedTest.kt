package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeFilterAppliedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-77c3b7f7-809e-4f01-a76a-7f35727644d4 선택한 태그가 없으면 두 보기 모두 필터 열기가 적용 상태를 알리지 않는다`() {
        setRoutineHomeScaffold(filterUiState = RoutineHomeScaffoldFilterUiState())

        assertFilterStateDescriptionInBothViewModes(expected = null)
    }

    @Test
    fun `TC-77c3b7f7-809e-4f01-a76a-7f35727644d4 태그를 하나 이상 선택하면 두 보기 모두 필터 열기가 적용 상태를 알린다`() {
        val tagId = Arb.kotlinUuid().next()
        setRoutineHomeScaffold(filterUiState = RoutineHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)))

        assertFilterStateDescriptionInBothViewModes(expected = DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION)
    }

    @Test
    fun `TC-77c3b7f7-809e-4f01-a76a-7f35727644d4 선택한 태그가 모두 무시되고 있으면 두 보기 모두 필터 열기가 적용 상태를 알리지 않는다`() {
        setRoutineHomeScaffold(filterUiState = RoutineHomeScaffoldFilterUiState(selectedTagIdSet = emptySet(), storedTagIdSet = setOf(Arb.kotlinUuid().next())))

        assertFilterStateDescriptionInBothViewModes(expected = null)
    }

    @Test
    fun `TC-7d9ad72e-b2d5-4ab4-9a3f-3fc34c3b23cb 선택한 태그가 없으면 보기 범위가 무엇이어도 필터 열기가 적용 상태를 알리지 않는다`() {
        assertFilterStateDescriptionByViewScope(tagIdSet = emptySet(), expected = null)
    }

    @Test
    fun `TC-7d9ad72e-b2d5-4ab4-9a3f-3fc34c3b23cb 태그를 선택하면 보기 범위가 무엇이어도 필터 열기가 적용 상태를 알린다`() {
        assertFilterStateDescriptionByViewScope(tagIdSet = setOf(Arb.kotlinUuid().next()), expected = DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION)
    }

    private fun assertFilterStateDescriptionByViewScope(
        tagIdSet: Set<Uuid>,
        expected: String?,
    ) {
        var filterUiState by mutableStateOf(RoutineHomeScaffoldFilterUiState(selectedTagIdSet = tagIdSet, storedTagIdSet = tagIdSet, viewScope = RoutineViewScope.TODAY))
        val pagingDataFlow = MutableStateFlow(filterCaseRoutinePagingDataOf(listOf(filterCaseRoutineSummaryOf(title = ROUTINE_TITLE))))
        composeRule.setContent {
            DiaryTheme {
                RoutineHomeScaffold(
                    onEvent = {},
                    onRoutineListEvent = {},
                    routinePagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    filterUiStateProvider = { filterUiState },
                )
            }
        }
        composeRule.waitForIdle()

        filterStateDescription() shouldBe expected
        composeRule.runOnIdle { filterUiState = filterUiState.copy(viewScope = RoutineViewScope.ALL) }
        composeRule.waitForIdle()
        filterStateDescription() shouldBe expected

        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_TODAY_DESCRIPTION).assertExists()

        filterStateDescription() shouldBe expected
        composeRule.runOnIdle { filterUiState = filterUiState.copy(viewScope = RoutineViewScope.TODAY) }
        composeRule.waitForIdle()
        filterStateDescription() shouldBe expected
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-77c3b7f7-809e-4f01-a76a-7f35727644d4 한국어 환경에서 필터 열기는 태그 필터 적용됨으로 알린다`() {
        val tagId = Arb.kotlinUuid().next()
        setRoutineHomeScaffold(filterUiState = RoutineHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)))

        filterStateDescription(filterDescription = KOREAN_FILTER_DESCRIPTION) shouldBe KOREAN_FILTER_APPLIED_STATE_DESCRIPTION
    }

    private fun assertFilterStateDescriptionInBothViewModes(expected: String?) {
        filterStateDescription() shouldBe expected

        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_TODAY_DESCRIPTION).assertExists()

        filterStateDescription() shouldBe expected
    }

    private fun filterStateDescription(filterDescription: String = DEFAULT_FILTER_DESCRIPTION): String? =
        composeRule
            .onNodeWithContentDescription(filterDescription)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.StateDescription)

    private fun setRoutineHomeScaffold(filterUiState: RoutineHomeScaffoldFilterUiState) {
        val pagingDataFlow = MutableStateFlow(filterCaseRoutinePagingDataOf(listOf(filterCaseRoutineSummaryOf(title = ROUTINE_TITLE))))

        composeRule.setContent {
            DiaryTheme {
                RoutineHomeScaffold(
                    onEvent = {},
                    onRoutineListEvent = {},
                    routinePagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    filterUiStateProvider = { filterUiState },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val DEFAULT_FILTER_DESCRIPTION = "Filter"
        const val DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION = "Tag filter applied"
        const val KOREAN_FILTER_DESCRIPTION = "필터"
        const val KOREAN_FILTER_APPLIED_STATE_DESCRIPTION = "태그 필터 적용됨"
        const val DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION = "Show calendar"
        const val DEFAULT_TODAY_DESCRIPTION = "Go to today"
        const val ROUTINE_TITLE = "Water"
    }
}
