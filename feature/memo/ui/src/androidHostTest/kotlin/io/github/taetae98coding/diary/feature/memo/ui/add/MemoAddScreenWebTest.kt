package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.feature.memo.ui.TEST_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.memo.ui.sendWebAddedResult
import io.github.taetae98coding.diary.feature.memo.ui.web.DEFAULT_WEB_PICKER_ADD_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.web.DEFAULT_WEB_PICKER_SEARCH_EMPTY_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.web.DEFAULT_WEB_PICKER_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.web.DEFAULT_WEB_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.web.DOCS_WEB_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.web.DOCS_WEB_URL
import io.github.taetae98coding.diary.feature.memo.ui.web.WIKI_WEB_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.web.WIKI_WEB_URL
import io.github.taetae98coding.diary.feature.memo.ui.web.awaitWebPickerRows
import io.github.taetae98coding.diary.feature.memo.ui.web.refreshFailedWebPagingData
import io.github.taetae98coding.diary.feature.memo.ui.web.refreshingWebPagingData
import io.github.taetae98coding.diary.feature.memo.ui.web.testWeb
import io.github.taetae98coding.diary.feature.memo.ui.web.webDialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.web.webPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.web.webPickerList
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private const val FIRST_ADDED_WEB_TITLE: String = "MemoWebFirstAdded"
private const val SECOND_ADDED_WEB_TITLE: String = "MemoWebSecondAdded"
private const val WEB_TEST_TYPED_TITLE: String = "MemoWebTypedTitle"
private const val WEB_TEST_ADD_BUTTON_DESCRIPTION: String = "Add memo"
private const val WEB_TEST_PICKER_TITLE_PREFIX: String = "MemoWebPicker"
private const val WEB_TEST_PICKER_COUNT: Int = 30

private fun ResultEventBus.sendWebAddedResult(
    web: Web,
    requestKey: Uuid = TEST_ADD_REQUEST_KEY,
) {
    sendWebAddedResult(id = web.id, requestKey = requestKey)
}

private fun ComposeContentTestRule.openWebPicker() {
    onNodeWithText(DEFAULT_WEB_SELECT_LABEL).performScrollTo().performClick()
    waitForIdle()
}

private fun ComposeContentTestRule.setMemoAddScreenForWeb(
    viewModels: MemoAddScreenViewModels,
    navigateToWebAdd: () -> Unit = {},
    navigateToWebDetail: (Uuid) -> Unit = {},
    resultEventBus: ResultEventBus = ResultEventBus(),
) {
    setContent {
        MemoAddScreenTestTheme(resultEventBus = resultEventBus) {
            MemoAddScreen(
                addRequestKey = TEST_ADD_REQUEST_KEY,
                addViewModel = viewModels.viewModel,
                tagViewModel = viewModels.tagViewModel,
                webViewModel = viewModels.webViewModel,
                contactViewModel = viewModels.contactViewModel,
                placeViewModel = viewModels.placeViewModel,
                placeMapViewModel = screenTestPlaceMapViewModel(),
                geminiViewModel = screenTestGeminiViewModel(),
                navigateUp = {},
                navigateToTagAdd = {},
                navigateToTagDetail = {},
                navigateToWebAdd = navigateToWebAdd,
                navigateToWebDetail = navigateToWebDetail,
                navigateToContactAdd = {},
                navigateToContactDetail = {},
                navigateToPlaceAdd = {},
                navigateToPlaceDetail = {},
                initialDateTime = null,
                componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                isStandalone = true,
            )
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoAddScreenWebTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-001 추가 항목을 누르면 선택할 수 있는 웹 목록이 열린다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL), testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL))
        var webAddCount = 0
        composeRule.setMemoAddScreenForWeb(
            viewModels = screenTestRealViewModel(webList = webList),
            navigateToWebAdd = { webAddCount += 1 },
        )

        composeRule.openWebPicker()
        composeRule.awaitWebPickerRows()

        webAddCount shouldBe 0
        composeRule.webDialogNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.webDialogNodeWithText(DOCS_WEB_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-003 TC-MEMO-WEB-INPUT-FEATURE-031 선택할 수 있는 웹 항목이 없으면 목록을 연 적이 없어도 첫 누름에 추가 항목이 WebAdd 이동을 요청한다`() {
        var webAddCount = 0
        composeRule.setMemoAddScreenForWeb(
            viewModels = screenTestRealViewModel(webList = emptyList()),
            navigateToWebAdd = { webAddCount += 1 },
        )

        composeRule.openWebPicker()

        webAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-004 목록의 대상을 확인하는 중에는 추가 항목이 목록을 연다`() {
        var webAddCount = 0
        composeRule.setMemoAddScreenForWeb(
            viewModels = screenTestViewModel(webPagingData = MutableStateFlow(refreshingWebPagingData())),
            navigateToWebAdd = { webAddCount += 1 },
        )

        composeRule.openWebPicker()

        webAddCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-032 목록의 대상을 처음 불러오지 못하면 추가 항목이 목록을 연다`() {
        var webAddCount = 0
        composeRule.setMemoAddScreenForWeb(
            viewModels = screenTestViewModel(webPagingData = MutableStateFlow(refreshFailedWebPagingData())),
            navigateToWebAdd = { webAddCount += 1 },
        )

        composeRule.openWebPicker()

        webAddCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-005 확인 중에 연 목록이 대상 없음으로 확정되면 목록 영역이 비어 있는 채로 유지된다`() {
        val webPagingData = MutableStateFlow(refreshingWebPagingData())
        composeRule.setMemoAddScreenForWeb(viewModels = screenTestViewModel(webPagingData = webPagingData))
        composeRule.openWebPicker()

        webPagingData.value = webPagingDataOf(emptyList())
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertExists()
        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_SEARCH_EMPTY_TITLE).assertDoesNotExist()
        composeRule.onAllNodes(isToggleable() and hasAnyAncestor(isDialog())).assertCountEquals(0)
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-011 목록에서 고른 웹 항목이 웹 입력의 칩으로 나타난다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        composeRule.setMemoAddScreenForWeb(viewModels = screenTestRealViewModel(webList = listOf(web)))

        composeRule.selectWeb(title = WIKI_WEB_TITLE)

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-063 WebDetail 메모 탭에서 진입하면 대상 웹 항목이 웹 입력에 선택되어 있다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        composeRule.setMemoAddScreenForWeb(viewModels = screenTestRealViewModel(initialWebId = web.id, webList = listOf(web)))

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-014 목록을 닫아도 반영한 선택이 유지된다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL), testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL))
        composeRule.setMemoAddScreenForWeb(viewModels = screenTestRealViewModel(webList = webList))

        composeRule.selectWeb(title = WIKI_WEB_TITLE)

        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.onNodeWithText(DOCS_WEB_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-007 목록의 웹 추가 항목을 누르면 목록이 닫히고 WebAdd 이동을 요청한다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL))
        var webAddCount = 0
        composeRule.setMemoAddScreenForWeb(
            viewModels = screenTestRealViewModel(webList = webList),
            navigateToWebAdd = { webAddCount += 1 },
        )

        composeRule.openWebPicker()
        composeRule.awaitWebPickerRows()
        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_ADD_LABEL).performClick()
        composeRule.waitForIdle()

        webAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-016 웹 칩을 누르면 WebDetail 이동을 요청한다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val clickedIdList = mutableListOf<Uuid>()
        composeRule.setMemoAddScreenForWeb(
            viewModels = screenTestRealViewModel(webList = listOf(web)),
            navigateToWebDetail = { id -> clickedIdList += id },
        )
        composeRule.selectWeb(title = WIKI_WEB_TITLE)

        composeRule.onNodeWithText(WIKI_WEB_TITLE).performScrollTo().performClick()
        composeRule.waitForIdle()

        clickedIdList shouldBe listOf(web.id)
    }

    @Test
    fun `TC-MEMO-ADD-DATA-017 웹 항목을 선택하는 것만으로는 저장된 메모가 바뀌지 않는다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val addMemoUseCase = mockk<AddMemoUseCase>()
        composeRule.setMemoAddScreenForWeb(viewModels = screenTestRealViewModel(webList = listOf(web), addMemoUseCase = addMemoUseCase))

        composeRule.selectWeb(title = WIKI_WEB_TITLE)

        coVerify(exactly = 0) { addMemoUseCase(any<AddMemoUseCase.Parameter>()) }
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-056 추가에 성공해도 선택한 웹 항목이 유지된다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val addMemoUseCase = mockk<AddMemoUseCase>()
        coEvery { addMemoUseCase(any<AddMemoUseCase.Parameter>()) } returns Result.success(Uuid.random())
        composeRule.setMemoAddScreenForWeb(viewModels = screenTestRealViewModel(webList = listOf(web), addMemoUseCase = addMemoUseCase))
        composeRule.selectWeb(title = WIKI_WEB_TITLE)
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(WEB_TEST_TYPED_TITLE)

        composeRule.onNodeWithContentDescription(WEB_TEST_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { addMemoUseCase(any<AddMemoUseCase.Parameter>()) }
        composeRule.onNodeWithText(WIKI_WEB_TITLE).performScrollTo().assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-021 목록을 닫았다가 다시 열면 앞부분부터 나타난다`() {
        val webList =
            List(WEB_TEST_PICKER_COUNT) { index ->
                testWeb(title = "$WEB_TEST_PICKER_TITLE_PREFIX${index.toString().padStart(length = 3, padChar = '0')}")
            }
        composeRule.setMemoAddScreenForWeb(viewModels = screenTestRealViewModel(webList = webList))

        composeRule.openWebPicker()
        composeRule.awaitWebPickerRows()
        composeRule.webPickerList().performScrollToIndex(webList.lastIndex)
        composeRule.waitForIdle()
        composeRule.webDialogNodeWithText(webList.first().detail.title).assertIsNotDisplayed()

        composeRule.closeDialogByBack()
        composeRule.openWebPicker()
        composeRule.awaitWebPickerRows()

        composeRule.webDialogNodeWithText(webList.first().detail.title).assertIsDisplayed()
    }

    private fun ComposeContentTestRule.selectWeb(title: String) {
        openWebPicker()
        awaitWebPickerRows()
        webDialogNodeWithText(title).performClick()
        closeDialogByBack()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoAddScreenWebAddedResultTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-008 WebAdd 화면에서 추가한 웹 항목 하나가 돌아왔을 때 선택된다`() {
        val selectedWeb = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val addedWeb = testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForWeb(
            viewModels = screenTestRealViewModel(webList = listOf(selectedWeb, addedWeb)),
            resultEventBus = resultEventBus,
        )
        composeRule.selectWeb(title = WIKI_WEB_TITLE)

        resultEventBus.sendWebAddedResult(addedWeb)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.onNodeWithText(DOCS_WEB_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-008 WebAdd 화면에서 추가한 웹 항목 여러 개가 돌아왔을 때 모두 선택된다`() {
        val selectedWeb = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val firstAddedWeb = testWeb(title = FIRST_ADDED_WEB_TITLE)
        val secondAddedWeb = testWeb(title = SECOND_ADDED_WEB_TITLE)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForWeb(
            viewModels = screenTestRealViewModel(webList = listOf(selectedWeb, firstAddedWeb, secondAddedWeb)),
            resultEventBus = resultEventBus,
        )
        composeRule.selectWeb(title = WIKI_WEB_TITLE)

        resultEventBus.sendWebAddedResult(firstAddedWeb)
        resultEventBus.sendWebAddedResult(secondAddedWeb)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.onNodeWithText(FIRST_ADDED_WEB_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_ADDED_WEB_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-009 웹 항목을 하나도 추가하지 않고 돌아오면 선택이 그대로 유지된다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL), testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL))
        var webAddCount = 0
        composeRule.setMemoAddScreenForWeb(
            viewModels = screenTestRealViewModel(webList = webList),
            navigateToWebAdd = { webAddCount += 1 },
        )
        composeRule.selectWeb(title = WIKI_WEB_TITLE)

        composeRule.openWebPicker()
        composeRule.awaitWebPickerRows()
        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_ADD_LABEL).performClick()
        composeRule.waitForIdle()

        webAddCount shouldBe 1
        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.onNodeWithText(DOCS_WEB_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-010 WebAdd 화면에서 돌아와도 웹 선택 목록이 저절로 열리지 않는다`() {
        val addedWeb = testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForWeb(
            viewModels = screenTestRealViewModel(webList = listOf(addedWeb)),
            resultEventBus = resultEventBus,
        )
        composeRule.openWebPicker()
        composeRule.awaitWebPickerRows()
        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_ADD_LABEL).performClick()
        composeRule.waitForIdle()

        resultEventBus.sendWebAddedResult(addedWeb)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DOCS_WEB_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-DOMAIN-019 이 입력에서 이동하지 않은 WebAdd 화면의 웹 항목은 자동 선택되지 않는다`() {
        val addedWeb = testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForWeb(
            viewModels = screenTestRealViewModel(webList = listOf(addedWeb)),
            resultEventBus = resultEventBus,
        )

        resultEventBus.sendWebAddedResult(addedWeb, requestKey = OTHER_ADD_REQUEST_KEY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DOCS_WEB_TITLE).assertDoesNotExist()
    }

    private fun ComposeContentTestRule.selectWeb(title: String) {
        openWebPicker()
        awaitWebPickerRows()
        webDialogNodeWithText(title).performClick()
        closeDialogByBack()
    }
}
