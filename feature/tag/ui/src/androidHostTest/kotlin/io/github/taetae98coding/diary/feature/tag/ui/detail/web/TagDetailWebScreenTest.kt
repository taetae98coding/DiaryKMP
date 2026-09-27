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
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagWeb
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
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

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-006 대상 태그의 상태와 무관하게 웹 항목 추가를 시작할 수 있다`() {
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
    fun `TC-TAG-DETAIL-WEB-FEATURE-007 웹 항목 추가를 선택하면 WebAdd 이동을 요청한다`() {
        var navigateCount = 0
        setScreenOnWebTab(navigateToWebAdd = { navigateCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-DETAIL-WEB-FEATURE-007 한국어 환경 웹 항목 추가 버튼의 접근성 이름은 웹 추가다`() {
        setScreenOnWebTab(tabDescription = KOREAN_WEB_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-012 대상 태그를 조회하지 못해도 빈 상태 안내를 표시한다`() {
        setScreenOnWebTab(uiState = TagDetailUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-013 빈 상태에서도 웹 항목 추가를 실행할 수 있다`() {
        var navigateCount = 0
        setScreenOnWebTab(navigateToWebAdd = { navigateCount += 1 })
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-014 웹 항목 추가는 웹 탭에서만 제공된다`() {
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
    fun `TC-TAG-DETAIL-WEB-FEATURE-015 TC-TAG-DETAIL-WEB-DATA-005 대상 태그를 조회하지 못해도 웹 목록과 동작을 제공한다`() {
        setScreenOnWebTab(
            uiState = TagDetailUiState.Loading,
            webPagingData = tagEntityPagingData(itemList = listOf(tagWeb(title = WEB_TITLE))),
        )

        composeRule.onNodeWithText(WEB_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-005 웹 항목을 선택하면 그 웹 항목의 상세 이동을 요청한다`() {
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
    fun `TC-TAG-DETAIL-WEB-FEATURE-021 웹 카드를 삭제 방향으로 밀면 그 웹 항목 삭제를 요청하고 기본 안내와 실행 취소를 표시한다`() {
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
    fun `TC-TAG-DETAIL-WEB-FEATURE-021 웹 카드를 삭제 방향으로 밀면 한국어 안내와 실행 취소를 표시한다`() {
        val web = swipeWeb(tabDescription = KOREAN_WEB_TAB_DESCRIPTION)

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(web.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitWebEffect(WebListEffect.Deleted(id = web.id))

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-022 삭제를 실행 취소하면 그 웹 항목의 삭제 되돌리기를 요청한다`() {
        val web = swipeWeb()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(web.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitWebEffect(WebListEffect.Deleted(id = web.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { webViewModel().restore(id = web.id) }
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-026 안내가 보이는 동안 다른 웹 항목을 삭제하면 마지막 삭제에만 실행 취소가 적용된다`() {
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
    fun `TC-TAG-DETAIL-WEB-FEATURE-027 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
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
    fun `TC-TAG-DETAIL-WEB-DOMAIN-003 웹 탭의 삭제와 실행 취소는 대상 태그와 수정 중인 입력을 바꾸지 않는다`() {
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
    fun `TC-TAG-DETAIL-FEATURE-065 웹 카드 위에서 왼쪽으로 밀면 탭은 바뀌지 않고 그 웹 항목이 삭제된다`() {
        val web = swipeWeb()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(web.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { webViewModel().delete(id = web.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_TAB_DESCRIPTION).assertIsNotSelected()
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-028 안내가 보이는 동안 다른 탭으로 바꾸면 안내가 닫히고 되돌릴 수 없다`() {
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
