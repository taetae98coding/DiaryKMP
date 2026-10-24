package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.activity.ComponentDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.LocalRetainedValuesStoreProvider
import androidx.compose.runtime.retain.ManagedRetainedValuesStore
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.feature.checklist.ui.actionNameList
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailTestEnvironment.Companion.CHARGER
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailTestEnvironment.Companion.PASSPORT
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailTestEnvironment.Companion.TRIP
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailTestEnvironment.Companion.UMBRELLA
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailTestEnvironment.Companion.WEEKEND
import io.github.taetae98coding.diary.feature.checklist.ui.editableText
import io.github.taetae98coding.diary.feature.checklist.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ChecklistDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-001 조회에 성공하면 저장된 내용으로 화면을 채운다`() {
        setScreen(ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT, isChecked = true), item(CHARGER))))

        composeRule.onNode(topBarTitle(TRIP), useUnmergedTree = true).assertExists()
        composeRule.titleInput().editableText() shouldBe TRIP
        composeRule.descriptionInput().editableText() shouldBe WEEKEND
        composeRule.onNode(itemRow(PASSPORT)).assertIsOn()
        composeRule.onNode(itemRow(CHARGER)).assertIsOff()
        composeRule.itemInput().editableText() shouldBe ""
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-002 진입할 때 어느 입력에도 초점을 두지 않는다`() {
        setScreen(ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT))))

        composeRule.titleInput().assertIsNotFocused()
        composeRule.descriptionInput().assertIsNotFocused()
        composeRule.itemInput().assertIsNotFocused()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-003 TC-CHECKLIST-DETAIL-FEATURE-004 조회 중에는 로딩만 보이고 입력과 동작을 실행할 수 없다`() {
        val environment = ChecklistDetailTestEnvironment()
        environment.uiState.value = ChecklistDetailUiState.Loading
        setScreen(environment)

        composeRule.onNodeWithContentDescription(DEFAULT_LOADING).assertExists()
        composeRule.onNode(hasSetTextAction()).assertDoesNotExist()
        composeRule.actionNameList() shouldBe listOf(DEFAULT_NAVIGATE_UP)
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-005 화면에는 정해진 입력과 동작만 둔다`() {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT, isChecked = true)))
        setScreen(environment)
        composeRule.titleInput().performTextInput("2")
        composeRule.waitForIdle()

        composeRule.actionNameList() shouldContainExactlyInAnyOrder
            listOf(
                DEFAULT_NAVIGATE_UP,
                DEFAULT_RESET,
                DEFAULT_DELETE,
                DEFAULT_TITLE_LABEL,
                DEFAULT_DESCRIPTION_LABEL,
                DEFAULT_UPDATE,
                DEFAULT_SORT,
                PASSPORT,
                "Remove item, $PASSPORT",
                DEFAULT_ITEM_LABEL,
                DEFAULT_ITEM_ADD,
                DEFAULT_CLEAR_TEXT,
                DEFAULT_CLEAR_TEXT,
            ) + DEFAULT_DESCRIPTION_TAB_LIST
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-006 체크 항목이 없으면 빈 상태 안내를 표시하고 다른 동작은 그대로 쓸 수 있다`() {
        setScreen(ChecklistDetailTestEnvironment(itemList = emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
        composeRule.itemInput().assertIsEnabled()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE).assertIsEnabled()
    }

    @Test
    @Config(qualifiers = "ko-w480dp-h1200dp")
    fun `TC-CHECKLIST-DETAIL-FEATURE-006 한국어 환경에서 빈 상태 안내를 표시한다`() {
        setScreen(ChecklistDetailTestEnvironment(itemList = emptyList()))

        composeRule.onNodeWithText("아직 체크 항목이 없습니다").assertExists()
        composeRule.onNodeWithText("아래 입력으로 새 항목을 넣을 수 있습니다").assertExists()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-007 체크 항목을 불러오는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setScreen(ChecklistDetailTestEnvironment(itemList = emptyList(), isLoaded = false))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-008 체크 항목을 선택하면 확인 없이 그 항목의 체크 변경을 한 번 요청한다`() {
        val unchecked = item(PASSPORT)
        val checked = item(CHARGER, isChecked = true)
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(unchecked, checked))
        setScreen(environment)

        composeRule.onNode(itemRow(PASSPORT)).performClick()
        composeRule.onNode(itemRow(CHARGER)).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.toggle(id = unchecked.id, isChecked = unchecked.isChecked) }
        verify(exactly = 1) { environment.itemViewModel.toggle(id = checked.id, isChecked = checked.isChecked) }
        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-008 저장된 체크 상태가 바뀌면 항목이 바뀐 상태로 보인다`() {
        val passport = item(PASSPORT)
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(passport))
        setScreen(environment)

        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(passport.copy(isChecked = true)))
        composeRule.waitForIdle()

        composeRule.onNode(itemRow(PASSPORT)).assertIsOn()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-009 추가순에서는 체크한 항목이 자리를 지킨다`() {
        assertCheckedItemOrder(sort = ChecklistItemSort.ADDED, expected = listOf("A", "B", "C"))
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-009 체크 안 된 항목 우선에서는 체크한 항목이 체크된 묶음으로 옮겨진다`() {
        assertCheckedItemOrder(sort = ChecklistItemSort.UNCHECKED_FIRST, expected = listOf("B", "C", "A"))
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-010 체크 변경을 저장하지 못하면 안내 없이 저장된 상태를 보여 주고 다시 선택할 수 있다`() {
        val passport = item(PASSPORT)
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(passport))
        setScreen(environment)

        composeRule.onNode(itemRow(PASSPORT)).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(itemRow(PASSPORT)).assertIsOff()
        composeRule.onNode(itemRow(PASSPORT)).performClick()
        composeRule.waitForIdle()

        verify(exactly = 2) { environment.itemViewModel.toggle(id = passport.id, isChecked = passport.isChecked) }
        composeRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-011 항목 넣기를 선택하면 넣기를 요청하고 성공하면 항목 입력이 비워지고 초점이 남는다`() {
        assertItemAdded { composeRule.onNodeWithContentDescription(DEFAULT_ITEM_ADD).performClick() }
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-011 입력을 마치는 키를 누르면 넣기를 요청하고 성공하면 항목 입력이 비워지고 초점이 남는다`() {
        assertItemAdded { composeRule.itemInput().performImeAction() }
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-012 항목 입력이 비어 있거나 공백뿐이면 항목을 넣지 않는다`() {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT)))
        setScreen(environment)

        listOf("", "   ").forEach { value ->
            composeRule.itemInput().performTextReplacement(value)
            composeRule.onNodeWithContentDescription(DEFAULT_ITEM_ADD).assertIsNotEnabled()
            composeRule.itemInput().performImeAction()
            composeRule.waitForIdle()
        }

        verify(exactly = 0) { environment.itemViewModel.add(content = any()) }
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-013 항목 넣기를 저장하지 못하면 항목 입력을 그대로 둔다`() {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT)))
        setScreen(environment)
        composeRule.itemInput().performTextInput(UMBRELLA)

        composeRule.itemInput().performImeAction()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.add(content = UMBRELLA) }
        composeRule.itemInput().editableText() shouldBe UMBRELLA
        composeRule.onNode(itemRow(UMBRELLA)).assertDoesNotExist()
        composeRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-014 항목을 빼면 확인 없이 그 항목의 빼기를 한 번 요청하고 목록에서 사라진다`() {
        val passport = item(PASSPORT)
        val charger = item(CHARGER)
        val umbrella = item(UMBRELLA)
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(passport, charger, umbrella))
        setScreen(environment)

        composeRule.onNode(removeButton(CHARGER)).performClick()
        composeRule.waitForIdle()
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(passport, umbrella))
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.delete(itemId = charger.id) }
        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.rowContentList() shouldBe listOf(PASSPORT, UMBRELLA)
        composeRule.onNodeWithText(DEFAULT_UNDO).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-015 항목 빼기를 저장하지 못하면 안내 없이 항목이 남는다`() {
        val passport = item(PASSPORT)
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(passport))
        setScreen(environment)

        composeRule.onNode(removeButton(PASSPORT)).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(itemRow(PASSPORT)).assertExists()
        composeRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-016 체크된 항목이 있을 때만 리셋을 실행할 수 있다`() {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT, isChecked = true), item(CHARGER)))
        setScreen(environment)
        composeRule.onNodeWithContentDescription(DEFAULT_RESET).assertIsEnabled()

        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(item(PASSPORT), item(CHARGER)))
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_RESET).assertIsNotEnabled()

        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = emptyList())
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_RESET).assertIsNotEnabled()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-017 리셋을 선택하면 되묻고 확정하면 리셋을 한 번 요청한다`() {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT, isChecked = true)))
        setScreen(environment)

        composeRule.onNodeWithContentDescription(DEFAULT_RESET).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_RESET_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_RESET_TEXT).assertIsDisplayed()
        verify(exactly = 0) { environment.itemViewModel.reset() }

        composeRule.onNodeWithText(DEFAULT_RESET_CONFIRM).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.reset() }
        composeRule.onNodeWithText(DEFAULT_RESET_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko-w480dp-h1200dp")
    fun `TC-CHECKLIST-DETAIL-FEATURE-017 한국어 환경에서 리셋 확인 문구를 표시한다`() {
        setScreen(ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT, isChecked = true))))

        composeRule.onNodeWithContentDescription("체크 모두 해제").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("모든 체크를 해제할까요?").assertIsDisplayed()
        composeRule.onNodeWithText("체크 항목은 그대로 남고 체크만 모두 해제됩니다.").assertIsDisplayed()
        composeRule.onNodeWithText("리셋").assertIsDisplayed()
        composeRule.onNodeWithText("취소").assertIsDisplayed()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-018 리셋 확인에서 취소를 고르면 리셋을 요청하지 않는다`() {
        assertResetCancelled { composeRule.onNodeWithText(DEFAULT_RESET_CANCEL).performClick() }
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-018 리셋 확인을 닫으면 리셋을 요청하지 않는다`() {
        assertResetCancelled {
            // 확인 밖을 누르거나 뒤로가기로 닫는 것은 모두 다이얼로그 창의 닫기 요청으로 전달된다.
            composeRule.runOnIdle { (ShadowDialog.getLatestDialog() as ComponentDialog).onBackPressedDispatcher.onBackPressed() }
        }
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-019 리셋을 처리하는 동안 진행 상태를 표시한다`() {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT, isChecked = true)))
        environment.itemUiState.value = environment.itemUiState.value.copy(isResetInProgress = true)
        setScreen(environment)

        composeRule.onNode(hasContentDescription(DEFAULT_RESET) and hasProgressDescendant(), useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-020 리셋은 입력 중이던 내용을 바꾸지 않는다`() {
        val passport = item(PASSPORT, isChecked = true)
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(passport))
        setScreen(environment)
        composeRule.titleInput().performTextReplacement(NEW_TITLE)
        composeRule.itemInput().performTextInput(UMBRELLA)
        composeRule.onNodeWithContentDescription(DEFAULT_RESET).performClick()
        composeRule.onNodeWithText(DEFAULT_RESET_CONFIRM).performClick()
        composeRule.waitForIdle()

        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(passport.copy(isChecked = false)))
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe NEW_TITLE
        composeRule.itemInput().editableText() shouldBe UMBRELLA
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).assertExists()
        composeRule.onNode(itemRow(PASSPORT)).assertIsOff()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-021 리셋을 저장하지 못하면 안내 없이 진행 상태만 해제하고 다시 리셋할 수 있다`() {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT, isChecked = true)))
        environment.itemUiState.value = environment.itemUiState.value.copy(isResetInProgress = true)
        setScreen(environment)

        environment.itemUiState.value = environment.itemUiState.value.copy(isResetInProgress = false)
        composeRule.waitForIdle()

        composeRule.onNode(hasContentDescription(DEFAULT_RESET) and hasProgressDescendant(), useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNode(itemRow(PASSPORT)).assertIsOn()
        composeRule.onNodeWithContentDescription(DEFAULT_RESET).assertIsEnabled()
        composeRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-022 처음 정렬은 추가순이고 체크 안 된 항목 우선으로 바꾸면 그 순서로 보인다`() {
        val a = item("A", isChecked = true)
        val b = item("B")
        val c = item("C", isChecked = true)
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(a, b, c))
        setScreen(environment)
        composeRule.onNodeWithText(DEFAULT_ADDED_ORDER, useUnmergedTree = true).assertExists()
        composeRule.rowContentList() shouldBe listOf("A", "B", "C")

        selectUncheckedFirst()
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(b, a, c), sort = ChecklistItemSort.UNCHECKED_FIRST)
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.selectSort(sort = ChecklistItemSort.UNCHECKED_FIRST) }
        composeRule.onNodeWithText(DEFAULT_UNCHECKED_FIRST, useUnmergedTree = true).assertExists()
        composeRule.rowContentList() shouldBe listOf("B", "A", "C")
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-041 정렬을 바꾸면 체크 항목 목록의 처음부터 새 순서를 보여 준다`() {
        val itemList = scrollItemList()
        val target = itemList[SCROLL_TARGET_INDEX]
        val environment = ChecklistDetailTestEnvironment(itemList = itemList)
        setScreen(environment)
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(itemRow(target.content))
        composeRule.waitForIdle()

        selectUncheckedFirst()
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = itemList.reversed(), sort = ChecklistItemSort.UNCHECKED_FIRST)
        composeRule.waitForIdle()

        composeRule.onNode(itemRow(itemList.last().content)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_SORT).assertIsDisplayed()
    }

    @Test
    fun `항목을 넣으면 밀려난 항목 입력이 보이도록 스크롤한다`() {
        val itemList = scrollItemList()
        val environment = ChecklistDetailTestEnvironment(itemList = itemList)
        setScreen(environment)
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(index = FORM_ITEM_COUNT + itemList.size)
        composeRule.onAllNodes(hasSetTextAction()).onLast().performTextInput(UMBRELLA)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_ITEM_ADD).performClick()
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = itemList + List(ADDED_ITEM_COUNT) { index -> item("added-$index") })
        environment.itemEffect.trySend(ChecklistDetailItemEffect.AddSucceeded)
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onLast().assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko-w480dp-h1200dp")
    fun `TC-CHECKLIST-DETAIL-FEATURE-022 한국어 환경에서 정렬 이름을 표시한다`() {
        setScreen(ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT))))

        composeRule.onNodeWithText("추가순", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-023 고를 수 있는 정렬은 추가순과 체크 안 된 항목 우선뿐이다`() {
        setScreen(ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT))))

        composeRule.onNodeWithContentDescription(DEFAULT_SORT).performClick()
        composeRule.waitForIdle()

        composeRule
            .onAllNodes(isSelectable())
            .fetchSemanticsNodes()
            .mapNotNull { node -> node.config.getOrNull(SemanticsProperties.Text)?.joinToString { text -> text.text } } shouldBe
            listOf(DEFAULT_ADDED_ORDER, DEFAULT_UNCHECKED_FIRST)
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-024 체크 항목이 없으면 정렬을 바꿀 수 없다`() {
        setScreen(ChecklistDetailTestEnvironment(itemList = emptyList()))

        composeRule.onNodeWithContentDescription(DEFAULT_SORT).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-025 입력이 저장된 내용과 다를 때만 수정 동작을 제공한다`() {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT)))
        setScreen(environment)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).assertDoesNotExist()

        composeRule.titleInput().performTextReplacement("$TRIP ")
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).assertExists()

        composeRule.titleInput().performTextReplacement(TRIP)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).assertDoesNotExist()

        composeRule.descriptionInput().performTextReplacement("$WEEKEND\n")
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).assertExists()

        composeRule.descriptionInput().performTextReplacement(WEEKEND)
        composeRule.itemInput().performTextInput(UMBRELLA)
        environment.itemUiState.value = environment.itemUiState.value.copy(sort = ChecklistItemSort.UNCHECKED_FIRST)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko-w480dp-h1200dp")
    fun `TC-CHECKLIST-DETAIL-FEATURE-026 수정에 성공하면 화면과 입력을 유지하고 성공 피드백을 제공하며 제목이 바뀐다`() {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT)))
        setScreen(environment)
        composeRule.titleInput().performTextReplacement(JEJU)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("체크리스트 수정").performClick()
        composeRule.waitForIdle()
        environment.effect.trySend(ChecklistDetailEffect.UpdateSucceeded)
        environment.uiState.value = ChecklistDetailUiState.Content(id = environment.id, detail = ChecklistDetail(title = JEJU, description = WEEKEND))
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.checklistViewModel.update(detail = ChecklistDetail(title = JEJU, description = WEEKEND)) }
        composeRule.onNodeWithText("체크리스트가 수정되었습니다.").assertExists()
        composeRule.titleInput().editableText() shouldBe JEJU
        composeRule.onNode(itemRow(PASSPORT)).assertExists()
        composeRule.onAllNodesWithText(JEJU, useUnmergedTree = true).fetchSemanticsNodes().size shouldBe 2
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-027 수정을 처리하는 동안 진행 상태를 표시한다`() {
        val environment = ChecklistDetailTestEnvironment()
        environment.uiState.value = ChecklistDetailUiState.Content(id = environment.id, detail = ChecklistDetail(TRIP, WEEKEND), isUpdateInProgress = true)
        setScreen(environment)
        composeRule.titleInput().performTextReplacement(JEJU)
        composeRule.waitForIdle()

        composeRule.onNode(hasContentDescription(DEFAULT_UPDATE) and hasProgressDescendant(), useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-028 수정에 실패하면 안내 없이 입력을 유지하고 다시 시도할 수 있다`() {
        val environment = ChecklistDetailTestEnvironment()
        setScreen(environment)
        composeRule.titleInput().performTextReplacement(JEJU)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).performClick()
        composeRule.waitForIdle()

        verify(exactly = 2) { environment.checklistViewModel.update(detail = any()) }
        composeRule.titleInput().editableText() shouldBe JEJU
        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-029 제목을 비운 채 수정하면 기존 제목이 유지되고 수정 동작이 계속 제공된다`() {
        assertBlankTitleUpdate(title = "")
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-029 제목을 공백뿐으로 수정하면 기존 제목이 유지되고 수정 동작이 계속 제공된다`() {
        assertBlankTitleUpdate(title = "   ")
    }

    private fun assertBlankTitleUpdate(title: String) {
        val environment = ChecklistDetailTestEnvironment()
        setScreen(environment)
        composeRule.titleInput().performTextReplacement(title)
        composeRule.descriptionInput().performTextReplacement(WEEKDAY)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).performClick()
        environment.effect.trySend(ChecklistDetailEffect.UpdateSucceeded)
        environment.uiState.value = ChecklistDetailUiState.Content(id = environment.id, detail = ChecklistDetail(title = TRIP, description = WEEKDAY))
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.checklistViewModel.update(detail = ChecklistDetail(title = title, description = WEEKDAY)) }
        composeRule.onNode(topBarTitle(TRIP), useUnmergedTree = true).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-030 삭제를 선택하면 확인 없이 삭제를 요청하고 성공하면 이전 화면으로 한 번 돌아간다`() {
        val environment = ChecklistDetailTestEnvironment()
        var navigateUpCount = 0
        setScreen(environment, navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(isDialog()).assertDoesNotExist()
        environment.effect.trySend(ChecklistDetailEffect.DeleteSucceeded)
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.checklistViewModel.delete() }
        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-031 삭제를 처리하는 동안 진행 상태를 표시한다`() {
        val environment = ChecklistDetailTestEnvironment()
        environment.uiState.value = ChecklistDetailUiState.Content(id = environment.id, detail = ChecklistDetail(TRIP, WEEKEND), isDeleteInProgress = true)
        setScreen(environment)

        composeRule.onNode(hasContentDescription(DEFAULT_DELETE) and hasProgressDescendant(), useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-032 삭제에 실패하면 화면에 머무르고 다시 삭제할 수 있다`() {
        val environment = ChecklistDetailTestEnvironment()
        var navigateUpCount = 0
        setScreen(environment, navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE).performClick()
        composeRule.waitForIdle()

        verify(exactly = 2) { environment.checklistViewModel.delete() }
        navigateUpCount shouldBe 0
        composeRule.titleInput().assertExists()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-034 TC-CHECKLIST-DETAIL-DOMAIN-005 저장된 제목이 다른 경로에서 바뀌면 화면 제목만 갱신한다`() {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT)))
        setScreen(environment)
        composeRule.titleInput().performTextReplacement(MY_TITLE)
        composeRule.itemInput().performTextInput(UMBRELLA)
        composeRule.waitForIdle()

        environment.uiState.value = ChecklistDetailUiState.Content(id = environment.id, detail = ChecklistDetail(title = JEJU, description = WEEKDAY))
        composeRule.waitForIdle()

        composeRule.onNode(topBarTitle(JEJU), useUnmergedTree = true).assertExists()
        composeRule.titleInput().editableText() shouldBe MY_TITLE
        composeRule.descriptionInput().editableText() shouldBe WEEKEND
        composeRule.itemInput().editableText() shouldBe UMBRELLA
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-DOMAIN-005 입력을 바꾸지 않았어도 저장된 내용이 바뀌면 처음 채운 값을 유지한다`() {
        val environment = ChecklistDetailTestEnvironment()
        setScreen(environment)

        environment.uiState.value = ChecklistDetailUiState.Content(id = environment.id, detail = ChecklistDetail(title = JEJU, description = WEEKDAY))
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe TRIP
        composeRule.descriptionInput().editableText() shouldBe WEEKEND
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-035 다른 경로에서 바뀐 체크 항목은 목록에 바로 나타난다`() {
        val passport = item(PASSPORT)
        val charger = item(CHARGER)
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(passport))
        setScreen(environment)

        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(passport, charger))
        composeRule.waitForIdle()
        composeRule.rowContentList() shouldBe listOf(PASSPORT, CHARGER)

        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(charger))
        composeRule.waitForIdle()
        composeRule.rowContentList() shouldBe listOf(CHARGER)

        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(charger.copy(isChecked = true)))
        composeRule.waitForIdle()
        composeRule.onNode(itemRow(CHARGER)).assertIsOn()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-037 뒤로가면 반영하지 않은 내용을 저장하지 않고 이전 화면으로 돌아간다`() {
        val environment = ChecklistDetailTestEnvironment()
        var navigateUpCount = 0
        setScreen(environment, navigateUp = { navigateUpCount += 1 })
        composeRule.titleInput().performTextReplacement(JEJU)
        composeRule.itemInput().performTextInput(UMBRELLA)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { environment.checklistViewModel.update(detail = any()) }
        verify(exactly = 0) { environment.itemViewModel.add(content = any()) }
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-039 수정 성공 피드백은 화면이 재생성되면 다시 표시하지 않는다`() {
        val environment = ChecklistDetailTestEnvironment()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ScreenContent(environment = environment) }
        composeRule.waitForIdle()
        environment.effect.trySend(ChecklistDetailEffect.UpdateSucceeded)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-040 변경을 처리하거나 저장된 내용이 갱신되어도 보고 있던 화면 위치를 유지한다`() {
        val itemList = scrollItemList()
        val target = itemList[SCROLL_TARGET_INDEX]
        val environment = ChecklistDetailTestEnvironment(itemList = itemList)
        setScreen(environment)
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(itemRow(target.content))
        composeRule.waitForIdle()
        val top = targetTop(target.content)

        composeRule.onNode(itemRow(target.content)).performClick()
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = itemList.map { item -> if (item == target) item.copy(isChecked = true) else item })
        composeRule.waitForIdle()
        targetTop(target.content) shouldBe top

        environment.uiState.value = ChecklistDetailUiState.Content(id = environment.id, detail = ChecklistDetail(TRIP, WEEKEND), isUpdateInProgress = true)
        composeRule.waitForIdle()
        environment.uiState.value = ChecklistDetailUiState.Content(id = environment.id, detail = ChecklistDetail(JEJU, WEEKDAY))
        composeRule.waitForIdle()
        targetTop(target.content) shouldBe top
        composeRule.onNode(itemRow(itemList.first().content)).assertDoesNotExist()

        environment.itemUiState.value = environment.itemUiState.value.copy(isAddInProgress = true)
        composeRule.waitForIdle()
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = environment.itemUiState.value.itemList + item(UMBRELLA), isAddInProgress = false)
        composeRule.waitForIdle()
        targetTop(target.content) shouldBe top

        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = environment.itemUiState.value.itemList - itemList.last())
        composeRule.waitForIdle()
        targetTop(target.content) shouldBe top

        environment.itemUiState.value = environment.itemUiState.value.copy(isResetInProgress = true)
        composeRule.waitForIdle()
        environment.itemUiState.value =
            environment.itemUiState.value.copy(
                itemList =
                    environment.itemUiState.value.itemList
                        .map { item -> item.copy(isChecked = false) },
                isResetInProgress = false,
            )
        composeRule.waitForIdle()
        targetTop(target.content) shouldBe top

        environment.uiState.value = ChecklistDetailUiState.Content(id = environment.id, detail = ChecklistDetail(JEJU, WEEKDAY), isDeleteInProgress = true)
        composeRule.waitForIdle()
        targetTop(target.content) shouldBe top
        composeRule.onNode(itemRow(itemList.first().content)).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-042 체크 안 된 항목 우선에서 체크를 해제하면 체크 안 된 묶음으로 옮겨진다`() {
        val a = item("A", isChecked = true)
        val b = item("B")
        val c = item("C", isChecked = true)
        val environment = uncheckedFirstEnvironment(listOf(b, a, c))

        composeRule.onNode(itemRow("C")).performClick()
        composeRule.waitForIdle()
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(b, c.copy(isChecked = false), a))
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.toggle(id = c.id, isChecked = true) }
        composeRule.rowContentList() shouldBe listOf("B", "C", "A")
        composeRule.onNode(itemRow("C")).assertIsOff()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-042 체크 안 된 항목 우선에서 넣은 항목은 체크 안 된 묶음의 끝에 놓인다`() {
        val a = item("A", isChecked = true)
        val b = item("B")
        val environment = uncheckedFirstEnvironment(listOf(b, a))

        composeRule.itemInput().performTextInput("D")
        composeRule.itemInput().performImeAction()
        composeRule.waitForIdle()
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(b, item("D"), a))
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.add(content = "D") }
        composeRule.rowContentList() shouldBe listOf("B", "D", "A")
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-042 체크 안 된 항목 우선에서 항목을 빼도 남은 항목은 정렬 기준을 따른다`() {
        val a = item("A", isChecked = true)
        val b = item("B")
        val c = item("C")
        val environment = uncheckedFirstEnvironment(listOf(b, c, a))

        composeRule.onNode(removeButton("B")).performClick()
        composeRule.waitForIdle()
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(c, a))
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.delete(itemId = b.id) }
        composeRule.rowContentList() shouldBe listOf("C", "A")
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-042 체크 안 된 항목 우선에서 리셋하면 추가순으로 놓인다`() {
        val a = item("A", isChecked = true)
        val b = item("B")
        val c = item("C", isChecked = true)
        val environment = uncheckedFirstEnvironment(listOf(b, a, c))

        composeRule.onNodeWithContentDescription(DEFAULT_RESET).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_RESET_CONFIRM).performClick()
        composeRule.waitForIdle()
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(a, b, c).map { item -> item.copy(isChecked = false) })
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.reset() }
        composeRule.rowContentList() shouldBe listOf("A", "B", "C")
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-043 리셋 확인이 열린 동안 체크된 항목이 모두 사라져도 확인은 열려 있고 확정해도 바뀌는 항목이 없다`() {
        val passport = item(PASSPORT, isChecked = true)
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(passport))
        setScreen(environment)
        composeRule.onNodeWithContentDescription(DEFAULT_RESET).performClick()
        composeRule.waitForIdle()

        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(passport.copy(isChecked = false)))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_RESET_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_RESET_CONFIRM).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.reset() }
        composeRule.onNodeWithText(DEFAULT_RESET_TITLE).assertDoesNotExist()
        composeRule.onNode(itemRow(PASSPORT)).assertIsOff()
    }

    private fun uncheckedFirstEnvironment(itemList: List<ChecklistItem>): ChecklistDetailTestEnvironment {
        val environment = ChecklistDetailTestEnvironment(itemList = itemList)
        environment.itemUiState.value = environment.itemUiState.value.copy(sort = ChecklistItemSort.UNCHECKED_FIRST)
        setScreen(environment)
        return environment
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-DOMAIN-014 화면이 재생성되어도 입력, 정렬과 리셋 확인을 유지한다`() {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT, isChecked = true)))
        environment.itemUiState.value = environment.itemUiState.value.copy(sort = ChecklistItemSort.UNCHECKED_FIRST)
        val restorationTester = StateRestorationTester(composeRule)
        val store = ManagedRetainedValuesStore()
        restorationTester.setContent {
            LocalRetainedValuesStoreProvider(store = store) { ScreenContent(environment = environment) }
        }
        composeRule.waitForIdle()
        fillAndOpenReset()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe JEJU
        composeRule.descriptionInput().editableText() shouldBe SUMMER
        composeRule.itemInput().editableText() shouldBe UMBRELLA
        composeRule.onNodeWithText(DEFAULT_UNCHECKED_FIRST, useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText(DEFAULT_RESET_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-DOMAIN-015 시스템이 앱을 되살리면 입력은 복원하고 정렬은 처음으로, 리셋 확인은 닫힌 채 시작한다`() {
        val before = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT, isChecked = true)))
        before.itemUiState.value = before.itemUiState.value.copy(sort = ChecklistItemSort.UNCHECKED_FIRST)
        val after = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT, isChecked = true)), id = before.id)
        var environment by mutableStateOf(before)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ScreenContent(environment = environment) }
        composeRule.waitForIdle()
        fillAndOpenReset()

        environment = after
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe JEJU
        composeRule.descriptionInput().editableText() shouldBe SUMMER
        composeRule.itemInput().editableText() shouldBe UMBRELLA
        composeRule.onNodeWithText(DEFAULT_ADDED_ORDER, useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText(DEFAULT_RESET_TITLE).assertDoesNotExist()
    }

    private fun assertCheckedItemOrder(
        sort: ChecklistItemSort,
        expected: List<String>,
    ) {
        val a = item("A")
        val b = item("B")
        val c = item("C")
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(a, b, c))
        environment.itemUiState.value = environment.itemUiState.value.copy(sort = sort)
        setScreen(environment)

        composeRule.onNode(itemRow("A")).performClick()
        composeRule.waitForIdle()
        val checked = listOf(a.copy(isChecked = true), b, c)
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = expected.map { content -> checked.first { item -> item.content == content } })
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.toggle(id = a.id, isChecked = false) }
        composeRule.rowContentList() shouldBe expected
        composeRule.onNode(itemRow("A")).assertIsOn()
    }

    private fun scrollItemList(): List<ChecklistItem> = List(SCROLL_ITEM_COUNT) { index -> item("item-${index.toString().padStart(length = 2, padChar = '0')}") }

    private fun selectUncheckedFirst() {
        composeRule.onNodeWithContentDescription(DEFAULT_SORT).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(isSelectable() and hasText(DEFAULT_UNCHECKED_FIRST)).performClick()
        composeRule.waitForIdle()
    }

    private fun fillAndOpenReset() {
        composeRule.titleInput().performTextReplacement(JEJU)
        composeRule.descriptionInput().performTextReplacement(SUMMER)
        composeRule.itemInput().performTextInput(UMBRELLA)
        composeRule.onNodeWithContentDescription(DEFAULT_RESET).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_RESET_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-036 대상 체크리스트가 삭제 상태가 되어도 입력이 남고 모든 동작을 실행할 수 있다`() {
        val passport = item(PASSPORT, isChecked = true)
        val charger = item(CHARGER)
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(passport, charger))
        setScreen(environment)
        composeRule.titleInput().performTextReplacement(JEJU)
        composeRule.waitForIdle()

        // 삭제 상태로 바뀌어도 화면 상태는 같은 내용 표시 상태로 전달된다.
        environment.uiState.value = ChecklistDetailUiState.Content(id = environment.id, detail = ChecklistDetail(TRIP, WEEKEND))
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe JEJU
        composeRule.onNode(itemRow(PASSPORT)).performClick()
        composeRule.onNode(removeButton(CHARGER)).performClick()
        composeRule.itemInput().performTextInput(UMBRELLA)
        composeRule.onNodeWithContentDescription(DEFAULT_ITEM_ADD).performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_RESET).performClick()
        composeRule.onNodeWithText(DEFAULT_RESET_CONFIRM).performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.toggle(id = passport.id, isChecked = true) }
        verify(exactly = 1) { environment.itemViewModel.delete(itemId = charger.id) }
        verify(exactly = 1) { environment.itemViewModel.add(content = UMBRELLA) }
        verify(exactly = 1) { environment.itemViewModel.reset() }
        verify(exactly = 1) { environment.checklistViewModel.update(detail = ChecklistDetail(title = JEJU, description = WEEKEND)) }
        verify(exactly = 1) { environment.checklistViewModel.delete() }
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-044 항목 넣기를 처리하는 동안 진행 상태를 표시하고 항목 입력과 다른 동작은 그대로 쓸 수 있다`() {
        val passport = item(PASSPORT)
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(passport))
        environment.itemUiState.value = environment.itemUiState.value.copy(isAddInProgress = true)
        setScreen(environment)

        composeRule.onNode(hasContentDescription(DEFAULT_ITEM_ADD) and hasProgressDescendant(), useUnmergedTree = true).assertExists()
        composeRule.itemInput().performTextInput(UMBRELLA)
        composeRule.onNode(itemRow(PASSPORT)).performClick()
        composeRule.waitForIdle()

        composeRule.itemInput().editableText() shouldBe UMBRELLA
        verify(exactly = 1) { environment.itemViewModel.toggle(id = passport.id, isChecked = false) }
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-045 지금 선택된 정렬을 다시 골라도 순서가 바뀌지 않는다`() {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item("A", isChecked = true), item("B"), item("C", isChecked = true)))
        setScreen(environment)

        composeRule.onNodeWithContentDescription(DEFAULT_SORT).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(isSelectable() and hasText(DEFAULT_ADDED_ORDER)).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(isSelectable() and hasText(DEFAULT_ADDED_ORDER)).assertDoesNotExist()
        verify(exactly = 0) { environment.itemViewModel.selectSort(sort = any()) }
        composeRule.rowContentList() shouldBe listOf("A", "B", "C")
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-046 정렬을 고른 뒤 항목이 모두 없어졌다가 다시 생겨도 고른 정렬을 유지한다`() {
        val a = item("A", isChecked = true)
        val b = item("B")
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(a, b))
        setScreen(environment)
        selectUncheckedFirst()
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = emptyList(), sort = ChecklistItemSort.UNCHECKED_FIRST)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SORT).assertDoesNotExist()

        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = listOf(b, a))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_UNCHECKED_FIRST, useUnmergedTree = true).assertExists()
        composeRule.rowContentList() shouldBe listOf("B", "A")
    }

    private fun assertItemAdded(add: () -> Unit) {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT)))
        setScreen(environment)
        composeRule.itemInput().performTextInput(UMBRELLA)
        composeRule.waitForIdle()

        add()
        composeRule.waitForIdle()
        environment.itemEffect.trySend(ChecklistDetailItemEffect.AddSucceeded)
        environment.itemUiState.value = environment.itemUiState.value.copy(itemList = environment.itemUiState.value.itemList + item(UMBRELLA))
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.itemViewModel.add(content = UMBRELLA) }
        composeRule.onNode(itemRow(UMBRELLA)).assertIsOff()
        composeRule.itemInput().editableText() shouldBe ""
        composeRule.itemInput().assertIsFocused()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE).assertDoesNotExist()
    }

    private fun assertResetCancelled(close: () -> Unit) {
        val environment = ChecklistDetailTestEnvironment(itemList = listOf(item(PASSPORT, isChecked = true)))
        setScreen(environment)
        composeRule.onNodeWithContentDescription(DEFAULT_RESET).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_RESET_TITLE).assertIsDisplayed()

        close()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_RESET_TITLE).assertDoesNotExist()
        verify(exactly = 0) { environment.itemViewModel.reset() }
        composeRule.onNode(itemRow(PASSPORT)).assertIsOn()
    }

    private fun targetTop(content: String): Float =
        composeRule
            .onNode(itemRow(content))
            .fetchSemanticsNode()
            .boundsInRoot
            .top

    private fun setScreen(
        environment: ChecklistDetailTestEnvironment,
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent { ScreenContent(environment = environment, navigateUp = navigateUp) }
        composeRule.waitForIdle()
    }

    @androidx.compose.runtime.Composable
    private fun ScreenContent(
        environment: ChecklistDetailTestEnvironment,
        navigateUp: () -> Unit = {},
    ) {
        DiaryTheme {
            ChecklistDetailScreen(
                navigateUp = navigateUp,
                componentVisibleProvider = { ChecklistDetailScaffoldComponentVisible() },
                checklistViewModel = environment.checklistViewModel,
                itemViewModel = environment.itemViewModel,
            )
        }
    }

    private fun topBarTitle(text: String): SemanticsMatcher = hasText(text) and !hasSetTextAction()

    private fun hasProgressDescendant(): androidx.compose.ui.test.SemanticsMatcher =
        androidx.compose.ui.test
            .hasAnyDescendant(progressIndicator())

    private companion object {
        const val DEFAULT_NAVIGATE_UP = "Navigate up"
        const val DEFAULT_RESET = "Uncheck all"
        const val DEFAULT_DELETE = "Delete checklist"
        const val DEFAULT_UPDATE = "Update checklist"
        const val DEFAULT_SORT = "List sort"
        const val DEFAULT_TITLE_LABEL = "Title"
        const val DEFAULT_DESCRIPTION_LABEL = "Description"
        const val DEFAULT_ITEM_LABEL = "New item"
        const val DEFAULT_ITEM_ADD = "Add item"
        const val DEFAULT_LOADING = "Loading checklist"
        const val DEFAULT_EMPTY_TITLE = "No items yet"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the field below to add an item."
        const val DEFAULT_RESET_TITLE = "Uncheck all items?"
        const val DEFAULT_RESET_TEXT = "All items stay in the checklist. Only their checks are cleared."
        const val DEFAULT_RESET_CONFIRM = "Reset"
        const val DEFAULT_RESET_CANCEL = "Cancel"
        const val DEFAULT_UPDATE_SUCCEEDED = "Checklist updated."
        const val DEFAULT_ADDED_ORDER = "Added order"
        const val DEFAULT_UNCHECKED_FIRST = "Unchecked first"
        const val DEFAULT_UNDO = "Undo"
        const val DEFAULT_CLEAR_TEXT = "Clear text"
        const val NEW_TITLE = "새 제목"
        const val MY_TITLE = "내 제목"
        const val JEJU = "제주 여행"
        const val WEEKDAY = "평일"
        const val SUMMER = "여름"
        const val SCROLL_ITEM_COUNT = 40
        const val SCROLL_TARGET_INDEX = 30
        const val ADDED_ITEM_COUNT = 10
        const val FORM_ITEM_COUNT = 3
        val DEFAULT_DESCRIPTION_TAB_LIST = listOf("Input", "Preview")
    }
}
