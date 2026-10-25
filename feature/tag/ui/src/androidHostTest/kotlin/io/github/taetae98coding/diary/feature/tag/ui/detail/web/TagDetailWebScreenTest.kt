package io.github.taetae98coding.diary.feature.tag.ui.detail.web

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.web.WebListEffect
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_PLACE_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_WEB_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.EDIT_SUFFIX
import io.github.taetae98coding.diary.feature.tag.ui.detail.KOREAN_WEB_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.selectTagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.setTagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.titleInput
import io.github.taetae98coding.diary.feature.tag.ui.detail.webEffectFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.webPagingDataFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.webViewModelRef
import io.github.taetae98coding.diary.feature.tag.ui.fixtureId
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagWeb
import io.kotest.matchers.shouldBe
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
class TagDetailWebScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-958295e2-51d8-4b1c-944a-5036f8a606e1 대상 태그의 상태와 무관하게 웹 항목 추가를 시작할 수 있다`() {
        val uiStateFlow = MutableStateFlow<TagDetailUiState>(tagDetailUiState(detail = tagDetail(TAG_TITLE)))
        composeRule.setTagDetailScreen(viewModel = screenTestViewModel(uiStateFlow))
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)

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
    fun `TC-dc429c83-68b0-4c53-b340-398a98a8f6c3 웹 항목 추가를 선택하면 WebAdd 이동을 요청한다`() {
        var navigateCount = 0
        setScreenOnWebTab(navigateToWebAdd = { navigateCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-dc429c83-68b0-4c53-b340-398a98a8f6c3 한국어 환경 웹 항목 추가 버튼의 접근성 이름은 웹 추가다`() {
        setScreenOnWebTab(tabDescription = KOREAN_WEB_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-ae09a03c-049b-4084-8e16-9aac1e17a318 대상 태그를 조회하지 못해도 빈 상태 안내를 표시한다`() {
        setScreenOnWebTab(uiState = TagDetailUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-a6b7abee-41a7-4ce6-9f91-3126500ae888 빈 상태에서도 웹 항목 추가를 실행할 수 있다`() {
        var navigateCount = 0
        setScreenOnWebTab(navigateToWebAdd = { navigateCount += 1 })
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-4a2f74b6-5b2c-45d6-b870-9bb23542de9f 웹 항목 추가는 웹 탭에서만 제공된다`() {
        setScreen()

        listOf(
            DEFAULT_DETAIL_TAB_DESCRIPTION,
            DEFAULT_MEMO_TAB_DESCRIPTION,
            DEFAULT_PLACE_TAB_DESCRIPTION,
        ).forEach { tabDescription ->
            composeRule.selectTagDetailTab(tabDescription)

            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        }

        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-be4f277c-2a3e-48ac-a25b-c64e4b4b22d4 TC-c0b62845-91d8-42d5-b0f6-7fccf8c2a973 대상 태그를 조회하지 못해도 웹 목록과 동작을 제공한다`() {
        setScreenOnWebTab(
            uiState = TagDetailUiState.Loading,
            webPagingData = tagEntityPagingData(itemList = listOf(tagWeb(title = WEB_TITLE))),
        )

        composeRule.onNodeWithText(WEB_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-8c0c98af-b6f0-466d-ad7e-53f726ef3d7b 웹 항목을 선택하면 그 웹 항목의 상세 이동을 요청한다`() {
        val web = tagWeb(title = WEB_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setScreenOnWebTab(
            webPagingData = tagEntityPagingData(itemList = listOf(web)),
            navigateToWebDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(WEB_TITLE).performClick()

        navigatedIdList shouldBe listOf(web.id)
    }

    @Test
    fun `TC-365c5415-2b42-4722-83fe-adb910874e83 웹 카드를 삭제 방향으로 밀면 그 웹 항목 삭제를 요청하고 기본 안내와 실행 취소를 표시한다`() {
        val web = swipeWeb()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(web.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { webViewModel().delete(id = web.id) }
        emitWebEffect(WebListEffect.Deleted(id = web.id))

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-365c5415-2b42-4722-83fe-adb910874e83 웹 카드를 삭제 방향으로 밀면 한국어 안내와 실행 취소를 표시한다`() {
        val web = swipeWeb(tabDescription = KOREAN_WEB_TAB_DESCRIPTION)

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(web.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitWebEffect(WebListEffect.Deleted(id = web.id))

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-194bbfc5-3cfb-45f1-86b2-35f848bdd00f 삭제를 실행 취소하면 그 웹 항목의 삭제 되돌리기를 요청한다`() {
        val web = swipeWeb()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(web.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitWebEffect(WebListEffect.Deleted(id = web.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { webViewModel().restore(id = web.id) }
    }

    @Test
    fun `TC-bb214b6b-c3fc-4773-8703-c50f436d53a8 안내가 보이는 동안 다른 웹 항목을 삭제하면 마지막 삭제에만 실행 취소가 적용된다`() {
        val web = swipeWeb()
        val otherId = fixtureId()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(web.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitWebEffect(WebListEffect.Deleted(id = web.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        emitWebEffect(WebListEffect.Deleted(id = otherId))

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { webViewModel().restore(id = otherId) }
        verify(exactly = 0) { webViewModel().restore(id = web.id) }
    }

    @Test
    fun `TC-47e07a14-62da-43dc-aaa1-2259ddc531ca 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val web = swipeWeb()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(web.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitWebEffect(WebListEffect.Deleted(id = web.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { webViewModel().restore(id = any()) }
    }

    @Test
    fun `TC-466e4a24-f7ba-4f16-8e3a-2f7b9902566a 웹 탭의 삭제와 실행 취소는 대상 태그와 수정 중인 입력을 바꾸지 않는다`() {
        val web = tagWeb(title = WEB_TITLE)
        // 대상 태그의 수정이나 삭제를 요청하면 엄격한 mock이 실패하므로, 요청하지 않았음을 함께 확인한다.
        setScreen(webPagingData = tagEntityPagingData(itemList = listOf(web)))
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(WEB_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(WEB_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitWebEffect(WebListEffect.Deleted(id = web.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        verify(exactly = 1) { webViewModel().delete(id = web.id) }
        verify(exactly = 1) { webViewModel().restore(id = web.id) }
        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-5c5b0403-8a85-454d-b486-04ff8a8c2c23 웹 카드 위에서 왼쪽으로 밀면 탭은 바뀌지 않고 그 웹 항목이 삭제된다`() {
        val web = swipeWeb()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(web.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { webViewModel().delete(id = web.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_TAB_DESCRIPTION).assertIsNotSelected()
    }

    @Test
    fun `TC-8ac990bf-12fd-4f6d-8f2c-bda3f22445b0 안내가 보이는 동안 다른 탭으로 바꾸면 안내가 닫히고 되돌릴 수 없다`() {
        val web = swipeWeb()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(web.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitWebEffect(WebListEffect.Deleted(id = web.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.mainClock.advanceTimeBy(TAB_CHANGE_SETTLE_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        verify(exactly = 0) { webViewModel().restore(id = any()) }
    }

    private fun swipeWeb(tabDescription: String = DEFAULT_WEB_TAB_DESCRIPTION): Web {
        val web = tagWeb(title = WEB_TITLE)
        setScreenOnWebTab(
            webPagingData = tagEntityPagingData(itemList = listOf(web)),
            tabDescription = tabDescription,
        )
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(WEB_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        return web
    }

    // 목록은 스와이프 결과를 저장소 갱신으로 확인하므로, 사라진 목록과 결과 Effect를 함께 넣는다.
    private fun emitWebEffect(effect: WebListEffect) {
        webPagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        webEffectFlow.tryEmit(effect)
        composeRule.waitForIdle()
    }

    private fun webViewModel(): TagDetailWebViewModel = requireNotNull(webViewModelRef)

    private fun setScreenOnWebTab(
        uiState: TagDetailUiState = tagDetailUiState(detail = tagDetail(TAG_TITLE)),
        webPagingData: PagingData<Web> = tagEntityPagingData(itemList = emptyList()),
        navigateToWebAdd: () -> Unit = {},
        navigateToWebDetail: (Uuid) -> Unit = {},
        tabDescription: String = DEFAULT_WEB_TAB_DESCRIPTION,
    ) {
        setScreen(
            uiState = uiState,
            webPagingData = webPagingData,
            navigateToWebAdd = navigateToWebAdd,
            navigateToWebDetail = navigateToWebDetail,
        )
        composeRule.selectTagDetailTab(tabDescription)
    }

    private fun setScreen(
        uiState: TagDetailUiState = tagDetailUiState(detail = tagDetail(TAG_TITLE)),
        webPagingData: PagingData<Web> = tagEntityPagingData(itemList = emptyList()),
        navigateToWebAdd: () -> Unit = {},
        navigateToWebDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(uiState)),
            webPagingData = webPagingData,
            navigateToWebAdd = navigateToWebAdd,
            navigateToWebDetail = navigateToWebDetail,
        )
    }

    private companion object {
        const val WEB_TITLE = "TagDetailScreenWeb"
        const val DEFAULT_EMPTY_TITLE = "No webs linked to this tag"
        const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add web"
        const val KOREAN_ADD_BUTTON_DESCRIPTION = "웹 추가"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val DEFAULT_DELETED_MESSAGE = "Web deleted."
        const val KOREAN_DELETED_MESSAGE = "웹이 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val TAB_CHANGE_SETTLE_MILLIS = 1_000L
        const val KOREAN_UNDO_ACTION = "실행 취소"
    }
}
