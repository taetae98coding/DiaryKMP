package io.github.taetae98coding.diary.feature.tag.ui.detail.contact

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.contact.CONTACT_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.contact.ContactListEffect
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_CONTACT_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_WEB_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.EDIT_SUFFIX
import io.github.taetae98coding.diary.feature.tag.ui.detail.KOREAN_CONTACT_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.contactEffectFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.contactPagingDataFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.contactViewModelRef
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.selectTagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.setTagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.syncViewModelRef
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.titleInput
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagContact
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.verify
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
class TagDetailContactScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-f0c1a252-d9b9-43ba-a788-9397dd91244a 대상 태그의 상태와 무관하게 연락처 추가를 시작할 수 있다`() {
        val uiStateFlow = MutableStateFlow<TagDetailUiState>(tagDetailUiState(detail = tagDetail(TAG_TITLE)))
        composeRule.setTagDetailScreen(viewModel = screenTestViewModel(uiStateFlow))
        composeRule.selectTagDetailTab(DEFAULT_CONTACT_TAB_DESCRIPTION)

        listOf(
            tagDetailUiState(detail = tagDetail(TAG_TITLE)),
            tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = true),
            TagDetailUiState.Loading,
        ).forEach { uiState ->
            composeRule.runOnIdle { uiStateFlow.value = uiState }
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-18e828ae-0886-448b-b24f-9fc2c285bab8 연락처 추가를 선택하면 추가 화면 이동을 요청한다`() {
        var navigateCount = 0
        setScreenOnTab(navigateToAdd = { navigateCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-18e828ae-0886-448b-b24f-9fc2c285bab8 한국어 환경 연락처 추가 버튼의 접근성 이름은 연락처 추가다`() {
        setScreenOnTab(tabDescription = KOREAN_CONTACT_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-200a23a2-477a-4fa5-b0e1-2582e2ca7d5f 대상 태그를 조회하지 못해도 빈 상태 안내를 표시한다`() {
        setScreenOnTab(uiState = TagDetailUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-b6378de0-f847-4050-83fd-bad70cc7438f 빈 상태에서도 연락처 추가를 실행할 수 있다`() {
        var navigateCount = 0
        setScreenOnTab(navigateToAdd = { navigateCount += 1 })
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-bbb96df7-2f97-46c3-a278-5748ce2cd639 연락처 추가는 연락처 탭에서만 제공된다`() {
        setScreen()

        listOf(
            DEFAULT_DETAIL_TAB_DESCRIPTION,
            DEFAULT_MEMO_TAB_DESCRIPTION,
            DEFAULT_WEB_TAB_DESCRIPTION,
        ).forEach { tabDescription ->
            composeRule.selectTagDetailTab(tabDescription)

            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        }

        composeRule.selectTagDetailTab(DEFAULT_CONTACT_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-f7ba42d5-94a5-4efa-b657-12c037bd7497 TC-14d22365-8ad5-46ce-9b62-43bdadc77097 대상 태그를 조회하지 못해도 연락처 목록과 동작을 제공한다`() {
        setScreenOnTab(
            uiState = TagDetailUiState.Loading,
            pagingData = tagEntityPagingData(itemList = listOf(tagContact(name = ITEM_TITLE))),
        )

        composeRule.onNodeWithText(ITEM_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-3d3d1f17-09df-420f-8a4f-cb47542ec96c 연락처를 선택하면 그 연락처의 상세 이동을 요청한다`() {
        val item = tagContact(name = ITEM_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setScreenOnTab(
            pagingData = tagEntityPagingData(itemList = listOf(item)),
            navigateToDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(ITEM_TITLE).performClick()

        navigatedIdList shouldBe listOf(item.contact.id)
    }

    @Test
    fun `TC-f76e8090-9cf9-4cba-bf68-84a5dcb0bcc8 연락처 탭에서 목록을 당기면 계정 데이터 동기화를 한 번 요청한다`() {
        setScreenOnTab(pagingData = tagEntityPagingData(itemList = listOf(tagContact(name = ITEM_TITLE))))
        waitForItem()

        composeRule.refreshableList().performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(syncViewModelRef).refresh() }
    }

    @Test
    fun `TC-f76e8090-9cf9-4cba-bf68-84a5dcb0bcc8 연락처 탭이 빈 상태여도 목록을 당기면 계정 데이터 동기화를 한 번 요청한다`() {
        setScreenOnTab()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(syncViewModelRef).refresh() }
    }

    @Test
    fun `TC-139803d7-0f5d-470b-b383-b638e02b0e04 TC-5c5b0403-8a85-454d-b486-04ff8a8c2c23 연락처 카드를 삭제 방향으로 밀면 탭은 바뀌지 않고 그 연락처 삭제를 요청하며 기본 안내와 실행 취소를 표시한다`() {
        val item = swipeItem()

        swipeCardLeft()

        verify(exactly = 1) { viewModel().delete(id = item.contact.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_CONTACT_TAB_DESCRIPTION).assertIsSelected()
        emitEffect(ContactListEffect.Deleted(id = item.contact.id))

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-139803d7-0f5d-470b-b383-b638e02b0e04 연락처 카드를 삭제 방향으로 밀면 한국어 안내와 실행 취소를 표시한다`() {
        val item = swipeItem(tabDescription = KOREAN_CONTACT_TAB_DESCRIPTION)

        swipeCardLeft()
        emitEffect(ContactListEffect.Deleted(id = item.contact.id))

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-2d42ad22-79c8-408b-be0d-0019b721cd2a TC-0ad9ffe8-1686-4386-9d4d-397acbd57d8c 연락처 카드 위에서 오른쪽으로 밀면 탭은 바뀌지 않고 아무 상태 변경도 요청하지 않는다`() {
        swipeItem()

        composeRule.onNode(hasTestTag(CONTACT_CARD_TEST_TAG) and hasText(ITEM_TITLE)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 0) { viewModel().delete(id = any()) }
        verify(exactly = 0) { viewModel().restore(id = any()) }
        composeRule.onNodeWithText(ITEM_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_CONTACT_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-527999d8-fe2f-4834-8ac7-9ba1efd7ee41 삭제를 실행 취소하면 그 연락처의 삭제 되돌리기를 요청한다`() {
        val item = swipeItem()

        swipeCardLeft()
        emitEffect(ContactListEffect.Deleted(id = item.contact.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel().restore(id = item.contact.id) }
    }

    @Test
    fun `TC-74c89a4c-02cf-4dc4-8bc7-0e330b6afb78 안내가 보이는 동안 다른 연락처를 삭제하면 마지막 삭제에만 실행 취소가 적용된다`() {
        val item = swipeItem()
        val otherId = Arb.kotlinUuid().next()

        swipeCardLeft()
        emitEffect(ContactListEffect.Deleted(id = item.contact.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        emitEffect(ContactListEffect.Deleted(id = otherId))

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel().restore(id = otherId) }
        verify(exactly = 0) { viewModel().restore(id = item.contact.id) }
    }

    @Test
    fun `TC-7430e81c-0adc-4112-ae84-c9108b0c83be 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val item = swipeItem()

        swipeCardLeft()
        emitEffect(ContactListEffect.Deleted(id = item.contact.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { viewModel().restore(id = any()) }
    }

    @Test
    fun `TC-2b82ba84-c5a1-42ce-a2d3-876acb3d6826 연락처 탭의 삭제와 실행 취소는 대상 태그와 수정 중인 입력을 바꾸지 않는다`() {
        val item = tagContact(name = ITEM_TITLE)
        // 대상 태그의 수정이나 삭제를 요청하면 엄격한 mock이 실패하므로, 요청하지 않았음을 함께 확인한다.
        setScreen(pagingData = tagEntityPagingData(itemList = listOf(item)))
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_CONTACT_TAB_DESCRIPTION)
        waitForItem()

        swipeCardLeft()
        emitEffect(ContactListEffect.Deleted(id = item.contact.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        verify(exactly = 1) { viewModel().delete(id = item.contact.id) }
        verify(exactly = 1) { viewModel().restore(id = item.contact.id) }
        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-4ed7ff37-d5f0-44bb-8efc-10325d10b44c 안내가 보이는 동안 다른 탭으로 바꾸면 안내가 닫히고 되돌릴 수 없다`() {
        val item = swipeItem()

        swipeCardLeft()
        emitEffect(ContactListEffect.Deleted(id = item.contact.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.mainClock.advanceTimeBy(TAB_CHANGE_SETTLE_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        composeRule.selectTagDetailTab(DEFAULT_CONTACT_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        verify(exactly = 0) { viewModel().restore(id = any()) }
    }

    @Test
    fun `연락처 탭에서 Cmd A 단축키를 입력하면 연락처 추가 이동을 한 번 요청하고 다른 탭에서는 요청하지 않는다`() {
        var navigateCount = 0
        setScreen(navigateToAdd = { navigateCount += 1 })
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onRoot().performAddShortcut()
        composeRule.waitForIdle()
        navigateCount shouldBe 0

        composeRule.selectTagDetailTab(DEFAULT_CONTACT_TAB_DESCRIPTION)
        composeRule.onRoot().performAddShortcut()

        navigateCount shouldBe 1
    }

    private fun swipeItem(tabDescription: String = DEFAULT_CONTACT_TAB_DESCRIPTION): ContactListItem {
        val item = tagContact(name = ITEM_TITLE)
        setScreenOnTab(
            pagingData = tagEntityPagingData(itemList = listOf(item)),
            tabDescription = tabDescription,
        )
        waitForItem()

        return item
    }

    private fun swipeCardLeft() {
        composeRule.onNode(hasTestTag(CONTACT_CARD_TEST_TAG) and hasText(ITEM_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun waitForItem() {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(ITEM_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // 목록은 스와이프 결과를 저장소 갱신으로 확인하므로, 사라진 목록과 결과 Effect를 함께 넣는다.
    private fun emitEffect(effect: ContactListEffect) {
        contactPagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        contactEffectFlow.tryEmit(effect)
        composeRule.waitForIdle()
    }

    private fun viewModel(): TagDetailContactViewModel = requireNotNull(contactViewModelRef)

    private fun setScreenOnTab(
        uiState: TagDetailUiState = tagDetailUiState(detail = tagDetail(TAG_TITLE)),
        pagingData: PagingData<ContactListItem> = tagEntityPagingData(itemList = emptyList()),
        navigateToAdd: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
        tabDescription: String = DEFAULT_CONTACT_TAB_DESCRIPTION,
    ) {
        setScreen(
            uiState = uiState,
            pagingData = pagingData,
            navigateToAdd = navigateToAdd,
            navigateToDetail = navigateToDetail,
        )
        composeRule.selectTagDetailTab(tabDescription)
    }

    private fun setScreen(
        uiState: TagDetailUiState = tagDetailUiState(detail = tagDetail(TAG_TITLE)),
        pagingData: PagingData<ContactListItem> = tagEntityPagingData(itemList = emptyList()),
        navigateToAdd: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(uiState)),
            contactPagingData = pagingData,
            navigateToContactAdd = navigateToAdd,
            navigateToContactDetail = navigateToDetail,
        )
    }

    private companion object {
        const val ITEM_TITLE = "TagDetailScreenContact"
        const val DEFAULT_EMPTY_TITLE = "No contacts linked to this tag"
        const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add contact"
        const val KOREAN_ADD_BUTTON_DESCRIPTION = "연락처 추가"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val DEFAULT_DELETED_MESSAGE = "Contact deleted."
        const val KOREAN_DELETED_MESSAGE = "연락처가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val TAB_CHANGE_SETTLE_MILLIS = 1_000L
    }
}

private fun SemanticsNodeInteraction.performAddShortcut() {
    performKeyInput {
        keyDown(Key.MetaLeft)
        keyDown(Key.A)
        keyUp(Key.A)
        keyUp(Key.MetaLeft)
    }
}
