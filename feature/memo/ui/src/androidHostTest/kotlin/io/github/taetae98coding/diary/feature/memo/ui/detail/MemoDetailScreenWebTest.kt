package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.memo.ui.TEST_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.github.taetae98coding.diary.feature.memo.ui.contact.screenTestContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlacePickerMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.DEFAULT_WEB_PICKER_ADD_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.web.DEFAULT_WEB_PICKER_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.web.DEFAULT_WEB_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.web.DOCS_WEB_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.web.DOCS_WEB_URL
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.WIKI_WEB_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.web.WIKI_WEB_URL
import io.github.taetae98coding.diary.feature.memo.ui.web.awaitWebPickerRows
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.testWeb
import io.github.taetae98coding.diary.feature.memo.ui.web.webDialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.web.webDialogSearchField
import io.github.taetae98coding.diary.feature.memo.ui.web.webPagingDataOf
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
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoDetailScreenWebTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-c3bef457-737a-433c-9dff-e2186c03a571 웹 입력에 저장된 웹 연결이 칩으로 표시된다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        composeRule.setMemoDetailScreenWithWeb(
            webViewModel = memoDetailWebViewModel(webList = listOf(web), selectedWebList = listOf(web)),
        )

        composeRule.onNodeWithText(DEFAULT_WEB_SELECT_LABEL).performScrollTo()

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
    }

    @Test
    fun `TC-2ed31841-dc81-4f33-b0cd-3bdc908a4975 다른 경로로 저장된 웹 연결이 바뀌면 웹 입력에 반영된다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val uiState = MutableStateFlow(MemoWebInputUiState())
        composeRule.setMemoDetailScreenWithWeb(
            webViewModel = screenTestWebViewModel(uiState = uiState, webPagingDataFlow = MutableStateFlow(webPagingDataOf(listOf(web)))),
        )
        composeRule.onNodeWithText(DEFAULT_WEB_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertDoesNotExist()

        composeRule.runOnIdle { uiState.value = MemoWebInputUiState(selectedWebList = listOf(web)) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
    }

    @Test
    fun `TC-817b28f2-9af9-41a0-a5e4-330b81dc444b 웹 항목을 선택하면 즉시 연결 변경을 요청하고 성공 안내는 표시하지 않는다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val webViewModel = memoDetailWebViewModel(webList = listOf(web))
        composeRule.setMemoDetailScreenWithWeb(webViewModel = webViewModel)

        composeRule.openMemoDetailWebPicker()
        composeRule.awaitWebPickerRows()
        composeRule.webDialogNodeWithText(WIKI_WEB_TITLE).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { webViewModel.selectWeb(webId = web.id) }
        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-817b28f2-9af9-41a0-a5e4-330b81dc444b 웹 선택을 해제하면 즉시 연결 해제를 요청한다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val webViewModel = memoDetailWebViewModel(webList = listOf(web), selectedWebList = listOf(web))
        composeRule.setMemoDetailScreenWithWeb(webViewModel = webViewModel)

        composeRule.openMemoDetailWebPicker()
        composeRule.awaitWebPickerRows()
        composeRule.webDialogNodeWithText(WIKI_WEB_URL).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { webViewModel.unselectWeb(webId = web.id) }
    }

    @Test
    fun `TC-93954c1c-00de-4de6-8a0c-d51dd840051c 웹 칩을 누르면 WebDetail 이동을 요청한다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val clickedIdList = mutableListOf<Uuid>()
        composeRule.setMemoDetailScreenWithWeb(
            webViewModel = memoDetailWebViewModel(webList = listOf(web), selectedWebList = listOf(web)),
            navigateToWebDetail = { id -> clickedIdList += id },
        )

        composeRule.onNodeWithText(WIKI_WEB_TITLE).performScrollTo().performClick()
        composeRule.waitForIdle()

        clickedIdList shouldBe listOf(web.id)
    }

    @Test
    fun `TC-e9c21154-d1d7-4d34-910a-a65b66a360d9 웹 항목이 하나도 없어도 추가 항목을 누르면 웹 선택 목록이 열린다`() {
        var webAddCount = 0
        composeRule.setMemoDetailScreenWithWeb(
            webViewModel = memoDetailWebViewModel(webList = emptyList()),
            navigateToWebAdd = { webAddCount += 1 },
        )

        composeRule.openMemoDetailWebPicker()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertExists()
        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_ADD_LABEL).assertExists()
        webAddCount shouldBe 0
    }

    @Test
    fun `TC-831b69ad-9b42-40fa-834e-ac184481a10b 목록의 웹 추가 항목을 누르면 목록이 닫히고 WebAdd 이동을 요청한다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        var webAddCount = 0
        composeRule.setMemoDetailScreenWithWeb(
            webViewModel = memoDetailWebViewModel(webList = listOf(web)),
            navigateToWebAdd = { webAddCount += 1 },
        )

        composeRule.openMemoDetailWebPicker()
        composeRule.awaitWebPickerRows()
        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_ADD_LABEL).performClick()
        composeRule.waitForIdle()

        webAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-55bbe837-e22f-4c82-b395-3a3e6b83bd66 삭제된 웹 항목은 웹 입력과 선택 목록에 나타나지 않는다`() {
        val keptWeb = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val deletedWeb = testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL)
        // 조회는 삭제된 웹 항목을 제외하므로 선택 목록과 칩 모두에서 빠진다.
        composeRule.setMemoDetailScreenWithWeb(
            webViewModel = memoDetailWebViewModel(webList = listOf(keptWeb), selectedWebList = listOf(keptWeb)),
        )

        composeRule.openMemoDetailWebPicker()
        composeRule.awaitWebPickerRows()

        composeRule.webDialogNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.webDialogNodeWithText(DOCS_WEB_TITLE).assertDoesNotExist()
        composeRule.closeDialogByBack()
        composeRule.onNodeWithText(DOCS_WEB_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-5b895ac6-09cd-437c-811b-d7bfbd3327d0 상세 대상이 바뀌면 열려 있던 웹 선택 목록과 검색어를 유지하지 않는다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val detailUiState = MutableStateFlow<MemoDetailUiState>(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)))
        composeRule.setMemoDetailScreenWithWeb(
            webViewModel = memoDetailWebViewModel(webList = listOf(web)),
            detailUiState = detailUiState,
        )
        composeRule.openMemoDetailWebPicker()
        composeRule.awaitWebPickerRows()
        composeRule.webDialogSearchField().performTextInput(WIKI_WEB_TITLE)
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            detailUiState.value = memoDetailUiState(id = Uuid.parse("00000000-0000-0000-0000-000000000002"), detail = memoDetail(MEMO_TITLE))
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-c8820107-cd83-40c1-a0c0-0a5ebcb6c5b8 웹 항목을 선택해도 수정 버튼이 나타나지 않는다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        composeRule.setMemoDetailScreenWithWeb(webViewModel = memoDetailWebViewModel(webList = listOf(web)))

        composeRule.openMemoDetailWebPicker()
        composeRule.awaitWebPickerRows()
        composeRule.webDialogNodeWithText(WIKI_WEB_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-c8820107-cd83-40c1-a0c0-0a5ebcb6c5b8 웹 선택을 해제해도 수정 버튼이 나타나지 않는다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        composeRule.setMemoDetailScreenWithWeb(webViewModel = memoDetailWebViewModel(webList = listOf(web), selectedWebList = listOf(web)))

        composeRule.openMemoDetailWebPicker()
        composeRule.awaitWebPickerRows()
        composeRule.webDialogNodeWithText(WIKI_WEB_URL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-0c58f267-758b-4eef-99ee-adadf057070c 다른 메모를 선택하면 웹 입력이 새 메모의 웹 연결로 바뀐다`() {
        val wikiWeb = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val docsWeb = testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL)
        val detailUiState = MutableStateFlow<MemoDetailUiState>(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)))
        val webUiState = MutableStateFlow(MemoWebInputUiState(selectedWebList = listOf(wikiWeb)))
        composeRule.setMemoDetailScreenWithWeb(
            webViewModel = screenTestWebViewModel(uiState = webUiState, webPagingDataFlow = MutableStateFlow(webPagingDataOf(listOf(wikiWeb, docsWeb)))),
            detailUiState = detailUiState,
        )
        composeRule.onNodeWithText(DEFAULT_WEB_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()

        composeRule.runOnIdle {
            detailUiState.value = memoDetailUiState(id = SECOND_WEB_MEMO_ID, detail = memoDetail(MEMO_TITLE))
            webUiState.value = MemoWebInputUiState(selectedWebList = listOf(docsWeb))
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_WEB_SELECT_LABEL).performScrollTo()

        composeRule.onNodeWithText(DOCS_WEB_TITLE).assertExists()
        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-31e93922-fc7a-4e83-b92d-5b3a06bae3cc 화면이 재생성되면 웹 입력은 저장된 웹 연결을 다시 표시한다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val webViewModel = memoDetailWebViewModel(webList = listOf(web), selectedWebList = listOf(web))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    addRequestKey = TEST_ADD_REQUEST_KEY,
                    detailViewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(detail = memoDetail(MEMO_TITLE)))),
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = webViewModel,
                    contactViewModel = screenTestContactViewModel(),
                    placeViewModel = screenTestPlaceViewModel(uiState = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true))),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    placePickerMapViewModel = screenTestPlacePickerMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
        composeRule.onNodeWithText(DEFAULT_WEB_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithText(DEFAULT_WEB_SELECT_LABEL).performScrollTo()

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
    }

    private fun ComposeContentTestRule.setMemoDetailScreenWithWeb(
        webViewModel: MemoWebViewModel,
        navigateToWebAdd: () -> Unit = {},
        navigateToWebDetail: (Uuid) -> Unit = {},
        detailUiState: MutableStateFlow<MemoDetailUiState> = MutableStateFlow(memoDetailUiState(detail = memoDetail(MEMO_TITLE))),
    ) {
        setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    addRequestKey = TEST_ADD_REQUEST_KEY,
                    detailViewModel = screenTestViewModel(uiState = detailUiState),
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = webViewModel,
                    contactViewModel = screenTestContactViewModel(),
                    placeViewModel = screenTestPlaceViewModel(uiState = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true))),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    placePickerMapViewModel = screenTestPlacePickerMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = navigateToWebAdd,
                    navigateToWebDetail = navigateToWebDetail,
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    private fun ComposeContentTestRule.openMemoDetailWebPicker() {
        onNodeWithText(DEFAULT_WEB_SELECT_LABEL).performScrollTo().performClick()
        waitForIdle()
    }

    private fun memoDetailWebViewModel(
        webList: List<Web>,
        selectedWebList: List<Web> = emptyList(),
    ): MemoWebViewModel =
        screenTestWebViewModel(
            uiState = MutableStateFlow(MemoWebInputUiState(selectedWebList = selectedWebList)),
            webPagingDataFlow = MutableStateFlow<PagingData<Web>>(webPagingDataOf(webList)),
        )

    private companion object {
        val SECOND_WEB_MEMO_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000031")
    }
}
