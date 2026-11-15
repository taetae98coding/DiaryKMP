package io.github.taetae98coding.diary.feature.tag.ui.detail.scope

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_CHECKLIST_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_CONTACT_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DDAY_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DELETE_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_FINISH_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MUSIC_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_PLACE_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_QR_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_ROUTINE_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_WEB_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScaffold
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScaffoldEvent
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailTestTabContent
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.colorHexText
import io.github.taetae98coding.diary.feature.tag.ui.detail.descriptionInput
import io.github.taetae98coding.diary.feature.tag.ui.detail.emojiInput
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.setTagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.titleInput
import io.github.taetae98coding.diary.feature.tag.ui.detail.toHexColorText
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagDetailFormState
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInputUiState
import io.github.taetae98coding.diary.feature.tag.ui.link.WORK_TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.testTag
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
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
private const val STORED_EMOJI = "\uD83C\uDFC3"
private const val STORED_DESCRIPTION = "TagDetailScopeDescription"
private val STORED_COLOR: Long = 0xFFFF0000.toInt().toLong()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailScopeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-1110555d-8e6c-4822-b18a-29ed6e4bd4bd 처음 진입하면 표시 범위가 이 태그만으로 표시된다`() {
        composeRule.setTagDetailScaffoldWithScope()

        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()

        composeRule.onNodeWithText(DEFAULT_SCOPE_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_SELF_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_CHILD_LABEL).assertIsNotSelected()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-8d95a383-bdbc-441b-9966-8a94d0a4620c 범위를 고르면 즉시 반영되고 선택이 닫힌다`() {
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
    fun `TC-8d95a383-bdbc-441b-9966-8a94d0a4620c 지금 범위와 같은 이 태그만을 골라도 선택이 닫히고 범위는 그대로다`() {
        lateinit var scopeState: TagDetailScopeState
        composeRule.setTagDetailScaffoldWithScope { state -> scopeState = state }
        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_SELF_LABEL).assertIsSelected()

        composeRule.onNodeWithText(DEFAULT_SELF_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SCOPE_TITLE).assertDoesNotExist()
        composeRule.runOnIdle { scopeState.scope shouldBe TagScope.SELF }
    }

    @Test
    fun `TC-6cfa9b59-3a7e-4b7f-a7fd-4c645e3e0979 선택한 탭과 관계없이 표시 범위를 열 수 있다`() {
        composeRule.setTagDetailScaffoldWithScope()

        listOf(
            DEFAULT_DETAIL_TAB_DESCRIPTION,
            DEFAULT_MEMO_TAB_DESCRIPTION,
            DEFAULT_WEB_TAB_DESCRIPTION,
            DEFAULT_PLACE_TAB_DESCRIPTION,
            DEFAULT_CONTACT_TAB_DESCRIPTION,
            DEFAULT_CHECKLIST_TAB_DESCRIPTION,
            DEFAULT_MUSIC_TAB_DESCRIPTION,
            DEFAULT_QR_TAB_DESCRIPTION,
            DEFAULT_DDAY_TAB_DESCRIPTION,
            DEFAULT_ROUTINE_TAB_DESCRIPTION,
        ).forEach { tabDescription ->
            composeRule.onNodeWithContentDescription(tabDescription).performScrollTo().performClick()
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
    fun `TC-e757dd29-c1f4-4ffe-960e-cd9c0a68354c 조회 중에도 표시 범위를 확인하고 바꿀 수 있다`() {
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
    fun `TC-8815deac-e38a-4281-bf0c-5a7e09d982c3 표시 범위를 바꿔도 태그 디테일 탭의 표시는 바뀌지 않는다`() {
        val storedDetail = tagDetail(title = TAG_TITLE, emoji = STORED_EMOJI, description = STORED_DESCRIPTION, color = STORED_COLOR)
        val linkedTag = testTag(title = WORK_TAG_TITLE)
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = storedDetail))),
            linkUiState = TagLinkInputUiState(linkedTagList = listOf(linkedTag)),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(storedDetail.emojiWithTitle).assertExists()
        composeRule.emojiInput().assert(hasText(STORED_EMOJI))
        composeRule.titleInput().assert(hasText(TAG_TITLE))
        composeRule.descriptionInput().assert(hasText(STORED_DESCRIPTION))
        composeRule.colorHexText() shouldBe STORED_COLOR.toHexColorText()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
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
    fun `TC-34dcc543-8a22-4ce7-906a-2cdae59d0e40 화면을 떠났다 다시 들어오면 표시 범위가 초기화된다`() {
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
    fun `TC-e58a5c84-bea3-4c47-b205-f53da902e523 백그라운드에 다녀와도 고른 표시 범위가 유지된다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                TagDetailScopeTestScaffold(scopeState = rememberTagDetailScopeState())
            }
        }
        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_SELF_LABEL).assertIsNotSelected()
    }

    // 앱을 다시 실행하면 저장해 둔 화면 상태도, 화면 상태를 들고 있던 객체도 없으므로 새 키로 화면을 다시 구성한다.
    @Test
    fun `TC-c63a8493-76a6-41d6-b07f-8d269e98a310 앱을 다시 실행하면 표시 범위가 초기화된다`() {
        val launchCount = mutableIntStateOf(0)
        composeRule.setContent {
            key(launchCount.intValue) {
                TagDetailScopeTestScaffold(scopeState = rememberTagDetailScopeState())
            }
        }
        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle { launchCount.intValue += 1 }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_SELF_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_LABEL).assertIsNotSelected()
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
    fun `TC-ba6be41a-6837-4298-99ea-5e536ac3bb16 화면 재생성 후에도 고른 직속 하위까지 범위가 유지된다`() {
        assertScopeRestored(scope = TagScope.CHILD, label = DEFAULT_CHILD_LABEL)
    }

    @Test
    fun `TC-ba6be41a-6837-4298-99ea-5e536ac3bb16 화면 재생성 후에도 고른 모든 하위까지 범위가 유지된다`() {
        assertScopeRestored(scope = TagScope.DESCENDANT, label = DEFAULT_DESCENDANT_LABEL)
    }

    private fun assertScopeRestored(
        scope: TagScope,
        label: String,
    ) {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var scopeState: TagDetailScopeState

        restorationTester.setContent {
            scopeState = rememberTagDetailScopeState()
            TagDetailScopeTestScaffold(scopeState = scopeState)
        }

        composeRule.runOnIdle { scopeState.select(scope = scope) }
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            scopeState.scope shouldBe scope
            scopeState.isApplied shouldBe true
        }
        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(label).assertIsSelected()
    }

    @Test
    fun `TC-9c7ffc60-4033-405a-84b8-01883c673e7b 표시 범위 선택을 연 채 화면이 재생성되면 선택이 열린 상태로 돌아온다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var scopeState: TagDetailScopeState

        restorationTester.setContent {
            scopeState = rememberTagDetailScopeState()
            TagDetailScopeTestScaffold(scopeState = scopeState)
        }

        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_CHILD_LABEL).assertExists()
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { scopeState.sheetState.isVisible shouldBe true }
        composeRule.onNodeWithText(DEFAULT_CHILD_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_SELF_LABEL).assertIsSelected()
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
