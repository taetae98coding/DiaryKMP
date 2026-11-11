package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.compose.checklist.CHECKLIST_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.checklist.ChecklistListEffect
import io.github.taetae98coding.diary.compose.contact.CONTACT_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.contact.ContactListEffect
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.music.MUSIC_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.music.MusicListEffect
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.place.PlaceListEffect
import io.github.taetae98coding.diary.compose.qr.QR_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.qr.QrListEffect
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.web.WebListEffect
import io.github.taetae98coding.diary.core.testing.checklist.checklistSummary
import io.github.taetae98coding.diary.feature.tag.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagContact
import io.github.taetae98coding.diary.feature.tag.ui.tagDDay
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagMemo
import io.github.taetae98coding.diary.feature.tag.ui.tagMemoPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagMusic
import io.github.taetae98coding.diary.feature.tag.ui.tagPlace
import io.github.taetae98coding.diary.feature.tag.ui.tagQr
import io.github.taetae98coding.diary.feature.tag.ui.tagWeb
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.viewmodel.koinViewModel
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 내비게이션은 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태와 ViewModel만 보관한다.
// 이동한 화면이 위에 놓였다가 뒤로가기나 삭제 성공으로 닫히는 것을 같은 방식으로 재현한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailReturnTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var isTagDetailOnTop by mutableStateOf(true)
    private val destinationList = mutableListOf<String>()
    private val memo = tagMemo(title = MEMO_TITLE)
    private val web = tagWeb(title = WEB_TITLE)
    private val place = tagPlace(title = PLACE_TITLE)
    private val contact = tagContact(name = CONTACT_TITLE)
    private val checklist = Arb.checklistSummary(title = CHECKLIST_TITLE).next()
    private val music = tagMusic(title = MUSIC_TITLE)
    private val qr = tagQr(title = QR_TITLE)
    private val dday = tagDDay(title = DDAY_TITLE)

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-79bfb102-ae53-4af0-abfd-7b9e8c7ca2e9 메모 추가에서 뒤로가면 메모 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertLeftTo(MEMO_ADD)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        composeRule.refreshableList().assertExists()
    }

    // 메모 상세는 삭제에 성공하면 뒤로가기와 같은 닫기를 부른다(TC-231b6a8e-83cc-4cf4-8c2d-558f5715280a, MemoDetailScreenTest).
    // TagDetail에서 본 두 경우의 결과는 위에 놓인 메모 상세가 닫히는 것으로 같다.
    @Test
    fun `TC-79bfb102-ae53-4af0-abfd-7b9e8c7ca2e9 메모 상세가 닫히면 메모 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(MEMO_TITLE).performClick()
        composeRule.waitForIdle()
        assertLeftTo(MEMO_DETAIL)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithText(MEMO_TITLE).assertExists()
    }

    // 완료된 메모 목록의 뒤로가기가 그 목록과 상세를 함께 닫는 이력은 TagNavigationTest가, 넓은 창의 배치는 TagMemoFinishedListLayoutTest가 확인한다.
    @Test
    fun `TC-7e9aa6ec-0f1d-4fb3-8a3a-43b9083e1c5d TC-16070905-ee83-4cd0-81c4-a2b2df72c7be 완료된 메모 목록이 닫히면 메모 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()
        composeRule.waitForIdle()
        assertLeftTo(MEMO_FINISHED_LIST)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithText(MEMO_TITLE).assertExists()
    }

    @Test
    fun `TC-0cc096fc-4d70-45db-9581-b20bdee05bb5 웹 추가에서 뒤로가면 웹 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_WEB_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertLeftTo(WEB_ADD)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()
        composeRule.refreshableList().assertExists()
    }

    // 웹 상세는 삭제에 성공하면 뒤로가기와 같은 닫기를 부른다(TC-35ae4b6d-4860-445b-9469-595ebb1fac84, WebDetailScreenTest).
    // TagDetail에서 본 두 경우의 결과는 위에 놓인 웹 상세가 닫히는 것으로 같다.
    @Test
    fun `TC-0cc096fc-4d70-45db-9581-b20bdee05bb5 웹 상세가 닫히면 웹 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onNodeWithText(WEB_TITLE).performClick()
        composeRule.waitForIdle()
        assertLeftTo(WEB_DETAIL)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithText(WEB_TITLE).assertExists()
    }

    @Test
    fun `TC-15b17f0c-6eac-4859-9a8f-3879dd932c64 장소 추가에서 뒤로가면 장소 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)
        waitForText(PLACE_TITLE)

        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertLeftTo(PLACE_ADD)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_TAB_DESCRIPTION).assertIsSelected()
        waitForText(PLACE_TITLE)
        composeRule.onNodeWithText(PLACE_TITLE).assertIsDisplayed()
    }

    // 장소 상세는 삭제에 성공하면 뒤로가기와 같은 닫기를 부른다(TC-b223dd7a-4b41-4381-8278-095cd1231671, PlaceDetailScreenMessageTest).
    // TagDetail에서 본 두 경우의 결과는 위에 놓인 장소 상세가 닫히는 것으로 같다.
    @Test
    fun `TC-15b17f0c-6eac-4859-9a8f-3879dd932c64 장소 상세가 닫히면 장소 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)
        waitForText(PLACE_TITLE)

        composeRule.onNodeWithText(PLACE_TITLE).performClick()
        composeRule.waitForIdle()
        assertLeftTo(PLACE_DETAIL)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_TAB_DESCRIPTION).assertIsSelected()
        waitForText(PLACE_TITLE)
        composeRule.onNodeWithText(PLACE_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-893dce78-89db-4a28-9c2d-e581d0447a1c 메모 탭의 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        waitForText(MEMO_TITLE)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        memoPagingDataFlow.value = tagMemoPagingData(itemList = emptyList())
        memoEffectFlow.tryEmit(MemoListEffect.Deleted(id = memo.id))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_MEMO_DELETED_MESSAGE).assertIsDisplayed()

        leaveAndReturn()

        composeRule.onNodeWithText(DEFAULT_MEMO_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { requireNotNull(memoViewModelRef).restore(id = any()) }
    }

    @Test
    fun `TC-9fef8f7e-2734-4000-b53a-d8ea9e9805fa 웹 탭의 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)
        waitForText(WEB_TITLE)

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(WEB_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        webPagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        webEffectFlow.tryEmit(WebListEffect.Deleted(id = web.id))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_WEB_DELETED_MESSAGE).assertIsDisplayed()

        leaveAndReturn()

        composeRule.onNodeWithText(DEFAULT_WEB_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { requireNotNull(webViewModelRef).restore(id = any()) }
    }

    @Test
    fun `TC-9253b4c5-ccb6-44ad-8aee-8cbd8c3d1927 장소 탭의 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)
        waitForText(PLACE_TITLE)

        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(PLACE_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        placePagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        placeEffectFlow.tryEmit(PlaceListEffect.Deleted(id = place.id))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_PLACE_DELETED_MESSAGE).assertIsDisplayed()

        leaveAndReturn()

        composeRule.onNodeWithText(DEFAULT_PLACE_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { requireNotNull(placeViewModelRef).restore(id = any()) }
    }

    @Test
    fun `TC-edf3173e-f6df-4ff7-8119-71c689fd43e6 연락처 추가에서 뒤로가면 연락처 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_CONTACT_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_CONTACT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertLeftTo(CONTACT_ADD)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_CONTACT_TAB_DESCRIPTION).assertIsSelected()
        composeRule.refreshableList().assertExists()
    }

    // 상세 화면은 삭제에 성공하면 뒤로가기와 같은 닫기를 부르므로, TagDetail에서 본 두 경우의 결과는 위에 놓인 상세가 닫히는 것으로 같다.
    @Test
    fun `TC-edf3173e-f6df-4ff7-8119-71c689fd43e6 연락처 상세가 닫히면 연락처 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_CONTACT_TAB_DESCRIPTION)
        waitForText(CONTACT_TITLE)

        composeRule.onNodeWithText(CONTACT_TITLE).performClick()
        composeRule.waitForIdle()
        assertLeftTo(CONTACT_DETAIL)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_CONTACT_TAB_DESCRIPTION).assertIsSelected()
        waitForText(CONTACT_TITLE)
        composeRule.onNodeWithText(CONTACT_TITLE).assertExists()
    }

    @Test
    fun `TC-6e82f86e-fdec-48eb-b316-1063cb3b8199 연락처 탭의 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_CONTACT_TAB_DESCRIPTION)
        waitForText(CONTACT_TITLE)

        composeRule.onNode(hasTestTag(CONTACT_CARD_TEST_TAG) and hasText(CONTACT_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        contactPagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        contactEffectFlow.tryEmit(ContactListEffect.Deleted(id = contact.contact.id))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONTACT_DELETED_MESSAGE).assertIsDisplayed()

        leaveAndReturn()

        composeRule.onNodeWithText(DEFAULT_CONTACT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { requireNotNull(contactViewModelRef).restore(id = any()) }
    }

    @Test
    fun `TC-d3c66547-7287-4a52-93bf-940ce1fabdcf 체크리스트 추가에서 뒤로가면 체크리스트 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_CHECKLIST_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_CHECKLIST_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertLeftTo(CHECKLIST_ADD)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_CHECKLIST_TAB_DESCRIPTION).assertIsSelected()
        composeRule.refreshableList().assertExists()
    }

    // 상세 화면은 삭제에 성공하면 뒤로가기와 같은 닫기를 부르므로, TagDetail에서 본 두 경우의 결과는 위에 놓인 상세가 닫히는 것으로 같다.
    @Test
    fun `TC-d3c66547-7287-4a52-93bf-940ce1fabdcf 체크리스트 상세가 닫히면 체크리스트 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_CHECKLIST_TAB_DESCRIPTION)
        waitForText(CHECKLIST_TITLE)

        composeRule.onNodeWithText(CHECKLIST_TITLE).performClick()
        composeRule.waitForIdle()
        assertLeftTo(CHECKLIST_DETAIL)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_CHECKLIST_TAB_DESCRIPTION).assertIsSelected()
        waitForText(CHECKLIST_TITLE)
        composeRule.onNodeWithText(CHECKLIST_TITLE).assertExists()
    }

    @Test
    fun `TC-779dda5b-da8e-4975-baa0-f5befb301e3d 체크리스트 탭의 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_CHECKLIST_TAB_DESCRIPTION)
        waitForText(CHECKLIST_TITLE)

        composeRule.onNode(hasTestTag(CHECKLIST_CARD_TEST_TAG) and hasText(CHECKLIST_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        checklistPagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        checklistEffectFlow.tryEmit(ChecklistListEffect.Deleted(id = checklist.checklist.id))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CHECKLIST_DELETED_MESSAGE).assertIsDisplayed()

        leaveAndReturn()

        composeRule.onNodeWithText(DEFAULT_CHECKLIST_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { requireNotNull(checklistViewModelRef).restore(id = any()) }
    }

    @Test
    fun `TC-3f2d8487-f5f8-4d75-b2eb-e47e4de14374 곡 추가에서 뒤로가면 곡 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_MUSIC_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_MUSIC_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertLeftTo(MUSIC_ADD)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_MUSIC_TAB_DESCRIPTION).assertIsSelected()
        composeRule.refreshableList().assertExists()
    }

    // 상세 화면은 삭제에 성공하면 뒤로가기와 같은 닫기를 부르므로, TagDetail에서 본 두 경우의 결과는 위에 놓인 상세가 닫히는 것으로 같다.
    @Test
    fun `TC-3f2d8487-f5f8-4d75-b2eb-e47e4de14374 곡 상세가 닫히면 곡 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_MUSIC_TAB_DESCRIPTION)
        waitForText(MUSIC_TITLE)

        composeRule.onNodeWithText(MUSIC_TITLE).performClick()
        composeRule.waitForIdle()
        assertLeftTo(MUSIC_DETAIL)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_MUSIC_TAB_DESCRIPTION).assertIsSelected()
        waitForText(MUSIC_TITLE)
        composeRule.onNodeWithText(MUSIC_TITLE).assertExists()
    }

    @Test
    fun `TC-a807e00e-3e24-4b47-9855-a9b00a2fb9bb 곡 탭의 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_MUSIC_TAB_DESCRIPTION)
        waitForText(MUSIC_TITLE)

        composeRule.onNode(hasTestTag(MUSIC_CARD_TEST_TAG) and hasText(MUSIC_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        musicPagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        musicEffectFlow.tryEmit(MusicListEffect.Deleted(id = music.id))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_MUSIC_DELETED_MESSAGE).assertIsDisplayed()

        leaveAndReturn()

        composeRule.onNodeWithText(DEFAULT_MUSIC_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { requireNotNull(musicViewModelRef).restore(id = any()) }
    }

    @Test
    fun `TC-fccb952d-91ae-4828-a2b7-b5832a467381 QR 추가에서 뒤로가면 QR 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_QR_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_QR_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertLeftTo(QR_ADD)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_QR_TAB_DESCRIPTION).assertIsSelected()
        composeRule.refreshableList().assertExists()
    }

    // 상세 화면은 삭제에 성공하면 뒤로가기와 같은 닫기를 부르므로, TagDetail에서 본 두 경우의 결과는 위에 놓인 상세가 닫히는 것으로 같다.
    @Test
    fun `TC-fccb952d-91ae-4828-a2b7-b5832a467381 QR 상세가 닫히면 QR 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_QR_TAB_DESCRIPTION)
        waitForText(QR_TITLE)

        composeRule.onNodeWithText(QR_TITLE).performClick()
        composeRule.waitForIdle()
        assertLeftTo(QR_DETAIL)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_QR_TAB_DESCRIPTION).assertIsSelected()
        waitForText(QR_TITLE)
        composeRule.onNodeWithText(QR_TITLE).assertExists()
    }

    @Test
    fun `TC-4650188a-877b-40ba-afda-cfdd6f23aff1 QR 탭의 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_QR_TAB_DESCRIPTION)
        waitForText(QR_TITLE)

        composeRule.onNode(hasTestTag(QR_CARD_TEST_TAG) and hasText(QR_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        qrPagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        qrEffectFlow.tryEmit(QrListEffect.Deleted(id = qr.id))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_QR_DELETED_MESSAGE).assertIsDisplayed()

        leaveAndReturn()

        composeRule.onNodeWithText(DEFAULT_QR_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { requireNotNull(qrViewModelRef).restore(id = any()) }
    }

    @Test
    fun `TC-ebb8b7df-cfa8-4d4b-a4bf-50b386655656 디데이 추가에서 뒤로가면 디데이 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_DDAY_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_DDAY_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertLeftTo(DDAY_ADD)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_DDAY_TAB_DESCRIPTION).assertIsSelected()
        composeRule.refreshableList().assertExists()
    }

    // 디데이 상세는 삭제에 성공하면 뒤로가기와 같은 닫기를 부르므로, TagDetail에서 본 두 경우의 결과는 위에 놓인 상세가 닫히는 것으로 같다.
    @Test
    fun `TC-ebb8b7df-cfa8-4d4b-a4bf-50b386655656 디데이 상세가 닫히면 디데이 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_DDAY_TAB_DESCRIPTION)
        waitForText(DDAY_TITLE)

        composeRule.onNodeWithText(DDAY_TITLE).performClick()
        composeRule.waitForIdle()
        assertLeftTo(DDAY_DETAIL)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_DDAY_TAB_DESCRIPTION).assertIsSelected()
        waitForText(DDAY_TITLE)
        composeRule.onNodeWithText(DDAY_TITLE).assertExists()
    }

    @Test
    fun `TC-1464fa8f-b917-4750-84ea-601467c15dad 디데이 탭의 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_DDAY_TAB_DESCRIPTION)
        waitForText(DDAY_TITLE)

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(DDAY_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        ddayPagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        ddayEffectFlow.tryEmit(DDayListEffect.Deleted(id = dday.id))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_DDAY_DELETED_MESSAGE).assertIsDisplayed()

        leaveAndReturn()

        composeRule.onNodeWithText(DEFAULT_DDAY_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { requireNotNull(ddayViewModelRef).restore(id = any()) }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun leaveAndReturn() {
        composeRule.runOnIdle { leaveTo(DESTINATION_CONTENT) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DESTINATION_CONTENT).assertExists()
        returnToTagDetail()
    }

    private fun assertLeftTo(destination: String) {
        destinationList.last() shouldBe destination
        composeRule.onNodeWithText(DESTINATION_CONTENT).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertDoesNotExist()
    }

    private fun returnToTagDetail() {
        composeRule.runOnIdle { isTagDetailOnTop = true }
        composeRule.waitForIdle()
    }

    private fun leaveTo(destination: String) {
        destinationList += destination
        isTagDetailOnTop = false
    }

    private fun setReturnableTagDetailScreen() {
        val detailViewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE))))
        prepareTagDetailTabViewModels(
            memoPagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            webPagingData = tagEntityPagingData(itemList = listOf(web)),
            placePagingData = tagEntityPagingData(itemList = listOf(place)),
            contactPagingData = tagEntityPagingData(itemList = listOf(contact)),
            checklistPagingData = tagEntityPagingData(itemList = listOf(checklist)),
            musicPagingData = tagEntityPagingData(itemList = listOf(music)),
            qrPagingData = tagEntityPagingData(itemList = listOf(qr)),
            ddayPagingData = tagEntityPagingData(itemList = listOf(dday)),
        )

        composeRule.setContent {
            TagDetailScreenTestHost {
                val saveableStateHolder = rememberSaveableStateHolder()

                if (isTagDetailOnTop) {
                    saveableStateHolder.SaveableStateProvider(key = TAG_DETAIL_ENTRY_KEY) {
                        TagDetailScreen(
                            navigateToTagAdd = {},
                            tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                            navigateUp = {},
                            navigateToDetail = {},
                            navigateToMemoAdd = { leaveTo(MEMO_ADD) },
                            navigateToMemoDetail = { leaveTo(MEMO_DETAIL) },
                            navigateToMemoFinishedList = { leaveTo(MEMO_FINISHED_LIST) },
                            id = FIRST_TAG_ID,
                            componentVisibleProvider = { TagDetailScaffoldComponentVisible() },
                            detailViewModel = detailViewModel,
                            placeMapViewModel = koinViewModel(),
                            navigateToWebAdd = { leaveTo(WEB_ADD) },
                            navigateToWebDetail = { leaveTo(WEB_DETAIL) },
                            navigateToPlaceAdd = { leaveTo(PLACE_ADD) },
                            navigateToPlaceDetail = { leaveTo(PLACE_DETAIL) },
                            navigateToContactAdd = { leaveTo(CONTACT_ADD) },
                            navigateToContactDetail = { leaveTo(CONTACT_DETAIL) },
                            navigateToChecklistAdd = { leaveTo(CHECKLIST_ADD) },
                            navigateToChecklistDetail = { leaveTo(CHECKLIST_DETAIL) },
                            navigateToMusicAdd = { leaveTo(MUSIC_ADD) },
                            navigateToMusicDetail = { leaveTo(MUSIC_DETAIL) },
                            navigateToQrAdd = { leaveTo(QR_ADD) },
                            navigateToQrDetail = { leaveTo(QR_DETAIL) },
                            navigateToDDayAdd = { leaveTo(DDAY_ADD) },
                            navigateToDDayDetail = { leaveTo(DDAY_DETAIL) },
                        )
                    }
                } else {
                    Text(text = DESTINATION_CONTENT)
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val TAG_DETAIL_ENTRY_KEY = "TagDetail"
        const val DESTINATION_CONTENT = "DestinationContent"
        const val MEMO_ADD = "MemoAdd"
        const val MEMO_DETAIL = "MemoDetail"
        const val MEMO_FINISHED_LIST = "TagMemoFinishedList"
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
        const val WEB_ADD = "WebAdd"
        const val WEB_DETAIL = "WebDetail"
        const val PLACE_ADD = "PlaceAdd"
        const val PLACE_DETAIL = "PlaceDetail"
        const val MEMO_TITLE = "TagDetailReturnMemo"
        const val WEB_TITLE = "TagDetailReturnWeb"
        const val PLACE_TITLE = "TagDetailReturnPlace"
        const val DEFAULT_MEMO_ADD_DESCRIPTION = "Add memo"
        const val DEFAULT_WEB_ADD_DESCRIPTION = "Add web"
        const val DEFAULT_PLACE_ADD_DESCRIPTION = "Add place"
        const val DEFAULT_MEMO_DELETED_MESSAGE = "Memo deleted."
        const val DEFAULT_WEB_DELETED_MESSAGE = "Web deleted."
        const val DEFAULT_PLACE_DELETED_MESSAGE = "Place deleted."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val CONTACT_ADD = "ContactAdd"
        const val CONTACT_DETAIL = "ContactDetail"
        const val CONTACT_TITLE = "TagDetailReturnContact"
        const val DEFAULT_CONTACT_ADD_DESCRIPTION = "Add contact"
        const val DEFAULT_CONTACT_DELETED_MESSAGE = "Contact deleted."
        const val CHECKLIST_ADD = "ChecklistAdd"
        const val CHECKLIST_DETAIL = "ChecklistDetail"
        const val CHECKLIST_TITLE = "TagDetailReturnChecklist"
        const val DEFAULT_CHECKLIST_ADD_DESCRIPTION = "Add checklist"
        const val DEFAULT_CHECKLIST_DELETED_MESSAGE = "Checklist deleted."
        const val MUSIC_ADD = "MusicAdd"
        const val MUSIC_DETAIL = "MusicDetail"
        const val MUSIC_TITLE = "TagDetailReturnMusic"
        const val DEFAULT_MUSIC_ADD_DESCRIPTION = "Add music"
        const val DEFAULT_MUSIC_DELETED_MESSAGE = "Song deleted."
        const val QR_ADD = "QrAdd"
        const val QR_DETAIL = "QrDetail"
        const val QR_TITLE = "TagDetailReturnQr"
        const val DEFAULT_QR_ADD_DESCRIPTION = "Add QR code"
        const val DEFAULT_QR_DELETED_MESSAGE = "QR code deleted."
        const val DDAY_ADD = "DDayAdd"
        const val DDAY_DETAIL = "DDayDetail"
        const val DDAY_TITLE = "TagDetailReturnDDay"
        const val DEFAULT_DDAY_ADD_DESCRIPTION = "Add D-Day"
        const val DEFAULT_DDAY_DELETED_MESSAGE = "D-Day deleted."
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
    }
}
