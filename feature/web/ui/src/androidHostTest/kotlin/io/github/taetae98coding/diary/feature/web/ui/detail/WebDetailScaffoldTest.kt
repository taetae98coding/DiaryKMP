package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebDetailFormState
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebDetailScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-DETAIL-FEATURE-001 조회 중에는 제목과 탭 줄, 본문 영역을 표시하지 않는다`() {
        setWebDetailScaffold(uiState = WebDetailUiState.Loading)

        composeRule.onNodeWithContentDescription(DEFAULT_FORM_TAB_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_PAGE_TAB_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithTag(WEB_DETAIL_FORM_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithTag(WEB_DETAIL_PAGE_FAILURE_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-018 조회 중에는 수정과 외부로 열기, 삭제를 실행할 수 없다`() {
        setWebDetailScaffold(uiState = WebDetailUiState.Loading)

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-016 외부로 열기를 선택하면 외부로 열기 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<WebDetailScaffoldEvent>()

        setWebDetailScaffold(onEvent = eventList::add)
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).performClick()

        eventList shouldBe listOf(WebDetailScaffoldEvent.ClickOpenInNew)
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-020 삭제를 선택하면 삭제 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<WebDetailScaffoldEvent>()

        setWebDetailScaffold(onEvent = eventList::add)
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_DESCRIPTION).performClick()

        eventList shouldBe listOf(WebDetailScaffoldEvent.ClickDelete)
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-019 삭제를 처리하는 동안 삭제 버튼이 진행 표시로 바뀐다`() {
        setWebDetailScaffold(uiState = testContentUiState().copy(isDeleteInProgress = true))

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-022 삭제를 처리하는 동안에도 탭 전환과 정보 확인, 외부로 열기, 뒤로가기를 할 수 있다`() {
        val detail = testWebDetail()
        val eventList = mutableListOf<WebDetailScaffoldEvent>()

        setWebDetailScaffold(
            uiState = testContentUiState(detail = detail).copy(isDeleteInProgress = true),
            detail = detail,
            onEvent = eventList::add,
        )

        composeRule.onNodeWithText(detail.title).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_FORM_TAB_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe
            listOf(
                WebDetailScaffoldEvent.SelectTab(tab = WebDetailTab.FORM),
                WebDetailScaffoldEvent.ClickOpenInNew,
                WebDetailScaffoldEvent.ClickNavigateUp,
            )
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-018 삭제를 처리하는 동안에도 다시 시도를 실행할 수 있다`() {
        val eventList = mutableListOf<WebDetailScaffoldEvent>()

        setWebDetailScaffold(
            uiState = testContentUiState().copy(isDeleteInProgress = true),
            pageUiState = WebDetailPageUiState.Failure,
            initialViewMode = WebDetailViewMode.RESPONSE,
            onEvent = eventList::add,
        )
        composeRule.onNodeWithText(DEFAULT_RETRY_BUTTON).performClick()

        eventList shouldBe listOf(WebDetailScaffoldEvent.ClickRetry)
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-002 조회에 성공하면 저장된 제목과 설명, URL, 요청 헤더가 입력에 채워진다`() {
        val detail =
            testWebDetail(
                headerList =
                    listOf(
                        WebHeader(name = FIRST_HEADER_NAME, value = FIRST_HEADER_VALUE),
                        WebHeader(name = SECOND_HEADER_NAME, value = SECOND_HEADER_VALUE),
                    ),
            )

        setWebDetailScaffold(uiState = testContentUiState(detail = detail), detail = detail, initialTab = WebDetailTab.FORM)

        composeRule.titleInput().assert(hasText(detail.title))
        composeRule.descriptionInput().assert(hasText(detail.description))
        composeRule.urlInput().assert(hasText(detail.url))
        composeRule.headerNameInput().assert(hasText(FIRST_HEADER_NAME))
        composeRule.headerValueInput().assert(hasText(FIRST_HEADER_VALUE))
        composeRule.headerNameInput(row = 1).assert(hasText(SECOND_HEADER_NAME))
        composeRule.headerValueInput(row = 1).assert(hasText(SECOND_HEADER_VALUE))
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-004 이름이 같은 요청 헤더도 저장된 순서대로 각각 채운다`() {
        val detail =
            testWebDetail(
                headerList =
                    listOf(
                        WebHeader(name = FIRST_HEADER_NAME, value = FIRST_HEADER_VALUE),
                        WebHeader(name = FIRST_HEADER_NAME, value = SECOND_HEADER_VALUE),
                    ),
            )

        setWebDetailScaffold(uiState = testContentUiState(detail = detail), detail = detail, initialTab = WebDetailTab.FORM)

        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER + INPUT_COUNT_PER_HEADER * 2
        composeRule.headerNameInput().assert(hasText(FIRST_HEADER_NAME))
        composeRule.headerValueInput().assert(hasText(FIRST_HEADER_VALUE))
        composeRule.headerNameInput(row = 1).assert(hasText(FIRST_HEADER_NAME))
        composeRule.headerValueInput(row = 1).assert(hasText(SECOND_HEADER_VALUE))
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-022 설명이 비어 있거나 요청 헤더가 없으면 빈 상태로 채운다`() {
        val detail = testWebDetail(description = "", headerList = emptyList())

        setWebDetailScaffold(uiState = testContentUiState(detail = detail), detail = detail, initialTab = WebDetailTab.FORM)

        composeRule.descriptionInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER

        composeRule.descriptionInput().performTextInput(TYPED_TITLE)
        composeRule.addHeaderRow()

        composeRule.descriptionInput().assert(hasText(TYPED_TITLE))
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER + INPUT_COUNT_PER_HEADER
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-024 입력이 저장 내용과 다를 때만 수정 동작을 제공한다`() {
        val detail = testWebDetail()

        setWebDetailScaffold(uiState = testContentUiState(detail = detail), detail = detail, initialTab = WebDetailTab.FORM)

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()

        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertExists()

        composeRule.titleInput().performTextReplacement(detail.title)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()

        composeRule.addHeaderRow()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-024 수정을 선택하면 수정 행동을 한 번 전달한다`() {
        val detail = testWebDetail()
        val eventList = mutableListOf<WebDetailScaffoldEvent>()

        setWebDetailScaffold(
            uiState = testContentUiState(detail = detail),
            detail = detail,
            initialTab = WebDetailTab.FORM,
            onEvent = eventList::add,
        )
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).performClick()

        eventList shouldBe listOf(WebDetailScaffoldEvent.ClickUpdate)
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-025 수정을 처리하는 동안 수정 버튼이 진행 표시로 바뀐다`() {
        val detail = testWebDetail()

        setWebDetailScaffold(
            uiState = testContentUiState(detail = detail).copy(isUpdateInProgress = true),
            detail = detail,
            initialTab = WebDetailTab.FORM,
        )
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-007 불러오지 못하면 안내와 다시 시도를 표시한다`() {
        setWebDetailScaffold(pageUiState = WebDetailPageUiState.Failure, initialViewMode = WebDetailViewMode.RESPONSE)

        composeRule.onNodeWithTag(WEB_DETAIL_PAGE_FAILURE_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_FAILURE_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_FAILURE_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_RETRY_BUTTON).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-008 불러오지 못해도 웹 항목 정보를 확인하고 수정할 수 있다`() {
        val detail = testWebDetail(headerList = listOf(WebHeader(name = FIRST_HEADER_NAME, value = FIRST_HEADER_VALUE)))

        setWebDetailScaffold(
            uiState = testContentUiState(detail = detail),
            pageUiState = WebDetailPageUiState.Failure,
            detail = detail,
            initialViewMode = WebDetailViewMode.RESPONSE,
        )
        composeRule.selectFormTab()

        composeRule.titleInput().assert(hasText(detail.title))
        composeRule.urlInput().assert(hasText(detail.url))
        composeRule.headerNameInput().assert(hasText(FIRST_HEADER_NAME))

        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-009 다시 시도를 선택하면 다시 시도 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<WebDetailScaffoldEvent>()

        setWebDetailScaffold(
            pageUiState = WebDetailPageUiState.Failure,
            initialViewMode = WebDetailViewMode.RESPONSE,
            onEvent = eventList::add,
        )
        composeRule.onNodeWithText(DEFAULT_RETRY_BUTTON).performClick()

        eventList shouldBe listOf(WebDetailScaffoldEvent.ClickRetry)
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-012 불러오는 동안에도 웹 항목 정보를 확인하고 수정하고 뒤로갈 수 있다`() {
        val detail = testWebDetail()
        val eventList = mutableListOf<WebDetailScaffoldEvent>()

        setWebDetailScaffold(
            uiState = testContentUiState(detail = detail),
            pageUiState = WebDetailPageUiState.Loading,
            detail = detail,
            initialTab = WebDetailTab.FORM,
            onEvent = eventList::add,
        )

        composeRule.titleInput().assert(hasText(detail.title))
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe listOf(WebDetailScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-015 뒤로가기를 선택하면 돌아가기 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<WebDetailScaffoldEvent>()

        setWebDetailScaffold(onEvent = eventList::add)
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe listOf(WebDetailScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `좁은 창에서는 웹 페이지 탭을 먼저 보여 주고 수정 폼을 함께 두지 않는다`() {
        setWebDetailScaffold(pageUiState = WebDetailPageUiState.Failure, initialViewMode = WebDetailViewMode.RESPONSE)

        composeRule.onNodeWithContentDescription(DEFAULT_FORM_TAB_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PAGE_TAB_DESCRIPTION).assertExists()
        composeRule.onNodeWithTag(WEB_DETAIL_PAGE_FAILURE_TEST_TAG).assertExists()
        composeRule.onNodeWithTag(WEB_DETAIL_FORM_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `좁은 창에서 수정 폼 탭을 고르면 수정 폼만 표시한다`() {
        setWebDetailScaffold(pageUiState = WebDetailPageUiState.Failure, initialViewMode = WebDetailViewMode.RESPONSE)
        composeRule.selectFormTab()

        composeRule.onNodeWithTag(WEB_DETAIL_FORM_TEST_TAG).assertExists()
        composeRule.onNodeWithTag(WEB_DETAIL_PAGE_FAILURE_TEST_TAG).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `넓은 창에서는 탭 줄 없이 수정 폼과 웹 페이지를 좌우로 나눈다`() {
        setWebDetailScaffold(pageUiState = WebDetailPageUiState.Failure, initialViewMode = WebDetailViewMode.RESPONSE)

        composeRule.onNodeWithContentDescription(DEFAULT_FORM_TAB_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_PAGE_TAB_DESCRIPTION).assertDoesNotExist()

        val formBounds = composeRule.onNodeWithTag(WEB_DETAIL_FORM_TEST_TAG).fetchSemanticsNode().boundsInRoot
        val pageBounds = composeRule.onNodeWithTag(WEB_DETAIL_PAGE_FAILURE_TEST_TAG).fetchSemanticsNode().boundsInRoot

        (pageBounds.left >= formBounds.right) shouldBe true
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-037 TC-WEB-DETAIL-FEATURE-044 URL 방식으로 시작하고 진행 표시와 실패 안내를 두지 않는다`() {
        setWebDetailScaffold(pageUiState = WebDetailPageUiState.Failure)

        composeRule.onNodeWithText(DEFAULT_URL_VIEW_MODE_LABEL).assertExists()
        composeRule.onNodeWithTag(WEB_DETAIL_PAGE_FAILURE_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_BUTTON).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-038 표시 방식 컨트롤을 누르면 두 방식과 URL 방식 안내를 표시한다`() {
        setWebDetailScaffold()
        composeRule.openViewModeSheet()

        composeRule.onNodeWithText(DEFAULT_VIEW_MODE_TITLE).assertExists()
        composeRule.onAllNodesWithText(DEFAULT_URL_VIEW_MODE_LABEL).onLast().assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_RESPONSE_VIEW_MODE_LABEL).assertIsNotSelected()
        composeRule.onNodeWithText(DEFAULT_URL_VIEW_MODE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-039 응답 본문 방식을 고르면 선택 목록이 닫히고 그 방식으로 표시된다`() {
        val eventList = mutableListOf<WebDetailScaffoldEvent>()

        setWebDetailScaffold(pageUiState = WebDetailPageUiState.Failure, onEvent = eventList::add)
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)

        eventList shouldBe
            listOf(
                WebDetailScaffoldEvent.ClickViewMode,
                WebDetailScaffoldEvent.SelectViewMode(viewMode = WebDetailViewMode.RESPONSE),
            )
        composeRule.onNodeWithText(DEFAULT_VIEW_MODE_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RESPONSE_VIEW_MODE_LABEL).assertExists()
        composeRule.onNodeWithTag(WEB_DETAIL_PAGE_FAILURE_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-041 URL 방식으로 되돌리면 응답 본문 대신 URL 방식으로 표시한다`() {
        setWebDetailScaffold(pageUiState = WebDetailPageUiState.Failure, initialViewMode = WebDetailViewMode.RESPONSE)
        composeRule.selectViewMode(label = DEFAULT_URL_VIEW_MODE_LABEL)

        composeRule.onNodeWithText(DEFAULT_URL_VIEW_MODE_LABEL).assertExists()
        composeRule.onNodeWithTag(WEB_DETAIL_PAGE_FAILURE_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-042 현재 방식과 같은 방식을 골라도 선택 목록만 닫힌다`() {
        setWebDetailScaffold()
        composeRule.selectViewMode(label = DEFAULT_URL_VIEW_MODE_LABEL)

        composeRule.onNodeWithText(DEFAULT_VIEW_MODE_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_URL_VIEW_MODE_LABEL).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-043 선택 목록을 닫으면 표시 방식이 바뀌지 않는다`() {
        setWebDetailScaffold()
        composeRule.openViewModeSheet()
        composeRule.onNodeWithText(DEFAULT_VIEW_MODE_TITLE).assertExists()

        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(DEFAULT_VIEW_MODE_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_URL_VIEW_MODE_LABEL).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-042 화면을 떠났다 다시 들어오면 기본 표시 방식으로 시작한다`() {
        val uiState = testContentUiState()
        var screenKey by mutableStateOf(0)
        composeRule.setContent {
            // 화면을 떠나 다시 들어오면 이전 화면의 상태는 사라지고 새 화면이 만들어지므로 key로 그 경계를 재현한다.
            key(screenKey) {
                WebDetailScaffoldUnderTest(
                    uiState = uiState,
                    pageUiState = WebDetailPageUiState.Failure,
                    detail = uiState.detail,
                    initialTab = WebDetailTab.PAGE,
                    initialViewMode = WebDetailViewMode.URL,
                    onEvent = {},
                )
            }
        }
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        composeRule.onNodeWithTag(WEB_DETAIL_PAGE_FAILURE_TEST_TAG).assertExists()

        composeRule.runOnIdle { screenKey += 1 }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_URL_VIEW_MODE_LABEL).assertExists()
        composeRule.onNodeWithTag(WEB_DETAIL_PAGE_FAILURE_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-046 불러오기에 실패해도 표시 방식을 바꿀 수 있다`() {
        setWebDetailScaffold(pageUiState = WebDetailPageUiState.Failure, initialViewMode = WebDetailViewMode.RESPONSE)

        composeRule.onNodeWithText(DEFAULT_RETRY_BUTTON).assertExists()

        composeRule.selectViewMode(label = DEFAULT_URL_VIEW_MODE_LABEL)

        composeRule.onNodeWithText(DEFAULT_RETRY_BUTTON).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_URL_VIEW_MODE_LABEL).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-018 조회 중에는 표시 방식을 바꿀 수 없다`() {
        setWebDetailScaffold(uiState = WebDetailUiState.Loading)

        composeRule.onNodeWithContentDescription(DEFAULT_VIEW_MODE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 표시 방식 이름과 안내 문구를 표시한다`() {
        setWebDetailScaffold()

        composeRule.onNodeWithText(KOREAN_URL_VIEW_MODE_LABEL).assertExists()

        composeRule.onNodeWithContentDescription(KOREAN_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_RESPONSE_VIEW_MODE_LABEL).assertExists()
        composeRule.onNodeWithText(KOREAN_URL_VIEW_MODE_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 상단 바 액션과 탭 이름을 표시한다`() {
        setWebDetailScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_OPEN_IN_NEW_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_DELETE_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_FORM_TAB_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_PAGE_TAB_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 수정 버튼 이름을 표시한다`() {
        val detail = testWebDetail()

        setWebDetailScaffold(uiState = testContentUiState(detail = detail), detail = detail, initialTab = WebDetailTab.FORM)
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(KOREAN_UPDATE_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 불러오기 실패 문구와 다시 시도 문구를 표시한다`() {
        setWebDetailScaffold(pageUiState = WebDetailPageUiState.Failure, initialViewMode = WebDetailViewMode.RESPONSE)

        composeRule.onNodeWithText(KOREAN_FAILURE_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_FAILURE_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(KOREAN_RETRY_BUTTON).assertExists()
    }

    private fun setWebDetailScaffold(
        uiState: WebDetailUiState = testContentUiState(),
        pageUiState: WebDetailPageUiState = WebDetailPageUiState.Loading,
        detail: WebDetail = (uiState as? WebDetailUiState.Content)?.detail ?: WebDetail.EMPTY,
        initialTab: WebDetailTab = WebDetailTab.PAGE,
        initialViewMode: WebDetailViewMode = WebDetailViewMode.URL,
        onEvent: (WebDetailScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            WebDetailScaffoldUnderTest(
                uiState = uiState,
                pageUiState = pageUiState,
                detail = detail,
                initialTab = initialTab,
                initialViewMode = initialViewMode,
                onEvent = onEvent,
            )
        }
    }

    @Composable
    private fun WebDetailScaffoldUnderTest(
        uiState: WebDetailUiState,
        pageUiState: WebDetailPageUiState,
        detail: WebDetail,
        initialTab: WebDetailTab,
        initialViewMode: WebDetailViewMode,
        onEvent: (WebDetailScaffoldEvent) -> Unit,
    ) {
        DiaryTheme {
            // Scaffold는 탭과 표시 방식 선택을 이벤트로 올리기만 하므로, 화면이 하는 반영을 테스트가 대신한다.
            val state = rememberWebDetailScaffoldState(initialTab = initialTab, initialViewMode = initialViewMode)

            WebDetailScaffold(
                onEvent = { event ->
                    when (event) {
                        is WebDetailScaffoldEvent.SelectTab -> state.select(tab = event.tab)
                        is WebDetailScaffoldEvent.SelectViewMode -> state.select(viewMode = event.viewMode)
                        is WebDetailScaffoldEvent.ClickViewMode -> state.viewModeSheetState.show()
                        else -> Unit
                    }
                    onEvent(event)
                },
                state = state,
                formState = rememberWebDetailFormState(initialDetail = detail),
                uiStateProvider = { uiState },
                pageUiStateProvider = { pageUiState },
                onFormEvent = {},
                onTagPickerEvent = {},
            )
        }
    }

    private companion object {
        private const val FIRST_HEADER_NAME = "Authorization"
        private const val FIRST_HEADER_VALUE = "Bearer first-token"
        private const val SECOND_HEADER_NAME = "Accept-Language"
        private const val SECOND_HEADER_VALUE = "ko-KR"

        private const val TYPED_TITLE = "WebDetailTypedTitle"

        private const val KOREAN_OPEN_IN_NEW_DESCRIPTION = "외부로 열기"
        private const val KOREAN_DELETE_DESCRIPTION = "웹 삭제"
        private const val KOREAN_UPDATE_DESCRIPTION = "웹 수정"
        private const val KOREAN_FORM_TAB_DESCRIPTION = "웹 정보 수정"
        private const val KOREAN_PAGE_TAB_DESCRIPTION = "웹 페이지 보기"
        private const val DEFAULT_FAILURE_TITLE = "Unable to load the web page"
        private const val DEFAULT_FAILURE_DESCRIPTION = "Check your network and try again."
        private const val KOREAN_FAILURE_TITLE = "웹 페이지를 불러오지 못했습니다"
        private const val KOREAN_FAILURE_DESCRIPTION = "네트워크 상태를 확인한 뒤 다시 시도해 주세요"
        private const val KOREAN_RETRY_BUTTON = "다시 시도"
        private const val KOREAN_VIEW_MODE_DESCRIPTION = "웹 페이지 표시 방식"
        private const val KOREAN_URL_VIEW_MODE_LABEL = "URL 방식"
        private const val KOREAN_RESPONSE_VIEW_MODE_LABEL = "응답 본문 방식"
        private const val KOREAN_URL_VIEW_MODE_DESCRIPTION = "저장한 요청 헤더가 적용되지 않습니다"
    }
}
