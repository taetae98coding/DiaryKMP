package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_PLACE_PICKER_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_PLACE_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.place.HOME_PLACE_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.MEMO_PLACE_PICKER_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.memo.ui.place.OFFICE_PLACE_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.placeDialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.place.placePagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.place.refreshingPlacePagingData
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.testPlace
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAddScreenPlaceTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-010 추가 항목을 누르면 장소 선택 목록이 열린다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val officePlace = testPlace(title = OFFICE_PLACE_TITLE)
        var placeAddCount = 0
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(placeList = listOf(homePlace, officePlace)),
            navigateToPlaceAdd = { placeAddCount += 1 },
        )

        composeRule.selectPlace(homePlace.detail.title)

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_TITLE).assertExists()
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertIsOn()
        composeRule.placeDialogNodeWithText(OFFICE_PLACE_TITLE).assertIsOff()
        placeAddCount shouldBe 0
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-022 TC-MEMO-PLACE-CARD-FEATURE-042 선택할 수 있는 장소가 없으면 목록을 연 적이 없어도 첫 누름에 추가 항목이 PlaceAdd 이동을 요청한다`() {
        var placeAddCount = 0
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(placeList = emptyList()),
            navigateToPlaceAdd = { placeAddCount += 1 },
        )

        composeRule.openPlacePicker()

        placeAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PLACE_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-016 목록을 준비하는 동안 추가 항목을 누르면 목록이 열리고 안내를 표시하지 않는다`() {
        var placeAddCount = 0
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(placePagingDataFlow = MutableStateFlow(refreshingPlacePagingData())),
            navigateToPlaceAdd = { placeAddCount += 1 },
        )

        composeRule.openPlacePicker()

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_TITLE).assertExists()
        placeAddCount shouldBe 0
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-023 확인 중에 연 목록이 대상 없음으로 확정되면 목록 영역이 비어 있는 채로 유지된다`() {
        val placePagingDataFlow = MutableStateFlow(refreshingPlacePagingData())
        setMemoAddScreen(viewModels = screenTestRealViewModel(placePagingDataFlow = placePagingDataFlow))
        composeRule.openPlacePicker()

        placePagingDataFlow.value = placePagingDataOf(emptyList())
        composeRule.waitForIdle()

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_TITLE).assertExists()
        composeRule.onAllNodes(isToggleable() and hasAnyAncestor(isDialog())).assertCountEquals(0)
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-012 목록에서 장소를 선택하면 즉시 반영된다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        setMemoAddScreen(viewModels = screenTestRealViewModel(placeList = listOf(homePlace)))

        composeRule.openPlacePicker()
        composeRule.onAllNodesWithText(HOME_PLACE_TITLE).assertCountEquals(1)
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertIsOn()
        // 목록이 열린 채로 카드의 장소 목록에도 칩이 나타난다.
        composeRule.onAllNodesWithText(HOME_PLACE_TITLE).assertCountEquals(2)
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-040 목록의 장소를 눌러도 장소 상세로 이동하지 않는다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        var placeDetailCount = 0
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(placeList = listOf(homePlace)),
            navigateToPlaceDetail = { placeDetailCount += 1 },
        )

        composeRule.selectPlace(homePlace.detail.title)

        placeDetailCount shouldBe 0
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertIsOn()
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-063 PlaceDetail 메모 탭에서 진입하면 대상 장소가 장소 카드에 선택되어 있다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        setMemoAddScreen(viewModels = screenTestRealViewModel(initialPlaceId = homePlace.id, placeList = listOf(homePlace)))

        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-013 목록에서 장소 선택을 해제하면 즉시 반영된다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        setMemoAddScreen(viewModels = screenTestRealViewModel(placeList = listOf(homePlace)))
        composeRule.selectPlace(homePlace.detail.title)
        composeRule.onAllNodesWithText(HOME_PLACE_TITLE).assertCountEquals(2)

        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertIsOff()
        // 목록이 열린 채로 카드의 장소 목록에서 칩이 사라진다.
        composeRule.onAllNodesWithText(HOME_PLACE_TITLE).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-014 목록을 닫아도 반영한 선택이 유지된다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        setMemoAddScreen(viewModels = screenTestRealViewModel(placeList = listOf(homePlace)))
        composeRule.selectPlace(homePlace.detail.title)

        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(DEFAULT_PLACE_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-ADD-DATA-011 장소를 선택하는 것만으로는 저장된 메모가 바뀌지 않는다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val addMemoUseCase = mockk<AddMemoUseCase>()
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(placeList = listOf(homePlace), addMemoUseCase = addMemoUseCase),
        )

        composeRule.selectPlace(homePlace.detail.title)

        coVerify(exactly = 0) { addMemoUseCase(any<AddMemoUseCase.Parameter>()) }
    }

    @Test
    @Config(qualifiers = "w480dp-h1200dp")
    fun `TC-MEMO-ADD-FEATURE-049 추가에 성공해도 선택한 장소가 유지된다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val addMemoUseCase = mockk<AddMemoUseCase>()
        coEvery { addMemoUseCase(any<AddMemoUseCase.Parameter>()) } returns Result.success(Uuid.random())
        setMemoAddScreen(
            viewModels =
                screenTestRealViewModel(
                    placeList = listOf(homePlace),
                    addMemoUseCase = addMemoUseCase,
                ),
        )
        composeRule.selectPlace(homePlace.detail.title)
        composeRule.closeDialogByBack()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { addMemoUseCase(any<AddMemoUseCase.Parameter>()) }
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-038 목록을 닫았다가 다시 열면 앞부분부터 나타난다`() {
        val placeList =
            List(PICKER_PLACE_COUNT) { index ->
                testPlace(title = "$PICKER_PLACE_TITLE_PREFIX${index.toString().padStart(length = 3, padChar = '0')}")
            }
        setMemoAddScreen(viewModels = screenTestRealViewModel(placeList = placeList))

        composeRule.openPlacePicker()
        composeRule.awaitPlacePickerRows()
        composeRule.onNode(hasTestTag(MEMO_PLACE_PICKER_LIST_TEST_TAG)).performScrollToIndex(placeList.lastIndex)
        composeRule.waitForIdle()
        composeRule.placeDialogNodeWithText(placeList.first().detail.title).assertIsNotDisplayed()

        composeRule.closeDialogByBack()
        composeRule.openPlacePicker()
        composeRule.awaitPlacePickerRows()

        composeRule.placeDialogNodeWithText(placeList.first().detail.title).assertIsDisplayed()
    }

    /**
     * 선택 목록은 나누어 준비되므로 첫 구간이 목록에 나타날 때까지 프레임과 실제 시간을 함께 진행시킨다.
     */
    private fun ComposeContentTestRule.awaitPlacePickerRows() {
        repeat(PICKER_WAIT_ATTEMPT_COUNT) {
            waitForIdle()
            if (onNode(hasTestTag(MEMO_PLACE_PICKER_LIST_TEST_TAG)).fetchSemanticsNode().children.isNotEmpty()) return
            mainClock.advanceTimeByFrame()
            @Suppress("ForbiddenMethodCall")
            Thread.sleep(PICKER_WAIT_INTERVAL_MILLIS)
        }

        error("장소 선택 목록의 첫 구간이 준비되지 않았다")
    }

    /**
     * 장소 추가 칩은 칩 영역의 자체 스크롤 안에 있어 본문 스크롤로 표시 영역까지 옮길 수 없으므로 클릭 동작을 직접 실행한다.
     */
    private fun ComposeContentTestRule.openPlacePicker() {
        onNodeWithText(DEFAULT_PLACE_SELECT_LABEL).performSemanticsAction(SemanticsActions.OnClick)
        waitForIdle()
    }

    /**
     * 목록을 열어 장소를 선택한 상태를 만든다. 목록은 열린 채로 둔다.
     */
    private fun ComposeContentTestRule.selectPlace(title: String) {
        openPlacePicker()
        placeDialogNodeWithText(title).performClick()
        waitForIdle()
    }

    private fun setMemoAddScreen(
        viewModels: MemoAddScreenViewModels,
        navigateToPlaceAdd: (Coordinate?) -> Unit = {},
        navigateToPlaceDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setContent {
            MemoAddScreenTestTheme {
                MemoAddScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
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
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = navigateToPlaceAdd,
                    navigateToPlaceDetail = navigateToPlaceDetail,
                    initialDateRange = null,
                    componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    private companion object {
        private const val TYPED_TITLE = "MemoTitleInput"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add memo"
        private const val PICKER_PLACE_TITLE_PREFIX = "MemoPlacePicker"
        private const val PICKER_PLACE_COUNT: Int = 30
        private const val PICKER_WAIT_ATTEMPT_COUNT: Int = 500
        private const val PICKER_WAIT_INTERVAL_MILLIS: Long = 10
    }
}
