package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagDetailFormState
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val DEFAULT_SCOPE_BUTTON_DESCRIPTION = "Display scope"
private const val KOREAN_SCOPE_BUTTON_DESCRIPTION = "표시 범위"
private const val DEFAULT_SCOPE_TITLE = "Display scope"
private const val DEFAULT_SELF_LABEL = "This tag only"
private const val DEFAULT_CHILD_LABEL = "Direct children"
private const val DEFAULT_DESCENDANT_LABEL = "All descendants"
private const val KOREAN_SELF_LABEL = "이 태그만"
private const val KOREAN_CHILD_LABEL = "직속 하위까지"
private const val KOREAN_DESCENDANT_LABEL = "모든 하위까지"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailScopeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-048 처음 진입하면 표시 범위가 이 태그만으로 표시된다`() {
        composeRule.setTagDetailScaffoldWithScope()

        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()

        composeRule.onNodeWithText(DEFAULT_SCOPE_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_SELF_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_CHILD_LABEL).assertIsNotSelected()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-049 범위를 고르면 즉시 반영되고 선택이 닫힌다`() {
        lateinit var scopeState: TagDetailScopeState
        composeRule.setTagDetailScaffoldWithScope { state -> scopeState = state }

        listOf(
            DEFAULT_CHILD_LABEL to TagScope.CHILD,
            DEFAULT_DESCENDANT_LABEL to TagScope.DESCENDANT,
            DEFAULT_SELF_LABEL to TagScope.SELF,
        ).forEach { (label, expected) ->
            composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
            composeRule.onNodeWithText(label).performClick()
            composeRule.waitForIdle()

            composeRule.runOnIdle { scopeState.scope shouldBe expected }
            composeRule.onNodeWithText(DEFAULT_SCOPE_TITLE).assertDoesNotExist()

            composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
            composeRule.onNodeWithText(label).assertIsSelected()
            composeRule.onNodeWithText(label).performClick()
            composeRule.waitForIdle()
        }
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-050 선택한 탭과 관계없이 표시 범위를 열 수 있다`() {
        composeRule.setTagDetailScaffoldWithScope()

        listOf(
            DEFAULT_DETAIL_TAB_DESCRIPTION,
            DEFAULT_MEMO_TAB_DESCRIPTION,
            DEFAULT_WEB_TAB_DESCRIPTION,
            DEFAULT_PLACE_TAB_DESCRIPTION,
        ).forEach { tabDescription ->
            composeRule.onNodeWithContentDescription(tabDescription).performClick()
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()

            composeRule.onNodeWithText(DEFAULT_SELF_LABEL).assertIsSelected()
            composeRule.onNodeWithText(DEFAULT_CHILD_LABEL).assertExists()
            composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).assertExists()

            composeRule.onNodeWithText(DEFAULT_SELF_LABEL).performClick()
            composeRule.waitForIdle()
        }
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-051 조회 중에도 표시 범위를 확인하고 바꿀 수 있다`() {
        lateinit var scopeState: TagDetailScopeState
        composeRule.setTagDetailScaffoldWithScope(
            uiStateProvider = { TagDetailUiState.Loading },
        ) { state -> scopeState = state }

        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle { scopeState.scope shouldBe TagScope.DESCENDANT }
        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).assertIsSelected()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-052 표시 범위를 바꿔도 태그 디테일 탭의 표시는 바뀌지 않는다`() {
        composeRule.setTagDetailScaffoldWithScope(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE)) })

        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `조회 중에도 표시 범위 버튼은 표시되고 완료와 삭제 버튼은 표시되지 않는다`() {
        composeRule.setTagDetailScaffoldWithScope(uiStateProvider = { TagDetailUiState.Loading })

        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-DOMAIN-017 화면을 떠났다 다시 들어오면 표시 범위가 초기화된다`() {
        val isEntered = mutableStateOf(true)
        lateinit var scopeState: TagDetailScopeState

        composeRule.setContent {
            if (isEntered.value) {
                scopeState = rememberTagDetailScopeState()
                TagDetailScopeTestScaffold(scopeState = scopeState)
            }
        }

        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle { scopeState.scope shouldBe TagScope.DESCENDANT }

        composeRule.runOnIdle { isEntered.value = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isEntered.value = true }
        composeRule.waitForIdle()

        composeRule.runOnIdle { scopeState.scope shouldBe TagScope.SELF }
        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_SELF_LABEL).assertIsSelected()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 표시 범위 버튼과 선택지 문구를 사용한다`() {
        composeRule.setTagDetailScaffoldWithScope()

        composeRule.onNodeWithContentDescription(KOREAN_SCOPE_BUTTON_DESCRIPTION).performClick()

        composeRule.onNodeWithText(KOREAN_SELF_LABEL).assertIsSelected()
        composeRule.onNodeWithText(KOREAN_CHILD_LABEL).assertExists()
        composeRule.onNodeWithText(KOREAN_DESCENDANT_LABEL).assertExists()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailScopeRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-053 화면 재생성 후에도 고른 표시 범위가 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var scopeState: TagDetailScopeState

        restorationTester.setContent {
            scopeState = rememberTagDetailScopeState()
            TagDetailScopeTestScaffold(scopeState = scopeState)
        }

        composeRule.runOnIdle { scopeState.select(scope = TagScope.DESCENDANT) }
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            scopeState.scope shouldBe TagScope.DESCENDANT
            scopeState.isApplied shouldBe true
        }
        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).assertIsSelected()
    }
}

private fun ComposeContentTestRule.setTagDetailScaffoldWithScope(
    uiStateProvider: () -> TagDetailUiState = { tagDetailUiState(detail = tagDetail(TAG_TITLE)) },
    onScopeState: (TagDetailScopeState) -> Unit = {},
) {
    setContent {
        val scopeState = rememberTagDetailScopeState()
        onScopeState(scopeState)

        TagDetailScopeTestScaffold(
            scopeState = scopeState,
            uiStateProvider = uiStateProvider,
        )
    }
}

@Composable
private fun TagDetailScopeTestScaffold(
    scopeState: TagDetailScopeState,
    uiStateProvider: () -> TagDetailUiState = { tagDetailUiState(detail = tagDetail(TAG_TITLE)) },
) {
    val state = rememberTagDetailFormState(initialDetail = TagDetail.EMPTY)

    DiaryTheme {
        TagDetailScaffold(
            onEvent = { event ->
                if (event is TagDetailScaffoldEvent.ClickScope) {
                    scopeState.sheetState.show()
                }
            },
            uiStateProvider = uiStateProvider,
            state = state,
            scopeState = scopeState,
            tabFloatingActionButton = {},
        ) { tab ->
            TagDetailTestTabContent(
                tab = tab,
                uiStateProvider = uiStateProvider,
                state = state,
            )
        }
    }
}
