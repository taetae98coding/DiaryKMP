package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagWeb
import io.kotest.matchers.shouldBe
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
    }
}
