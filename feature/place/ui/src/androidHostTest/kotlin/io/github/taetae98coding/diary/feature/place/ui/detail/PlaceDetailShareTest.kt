package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ShareResult
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SAVED_TITLE = "저장된 장소"

private val SAVED_COORDINATE = Coordinate(latitude = 37.5665, longitude = 126.978)
private val CHANGED_COORDINATE = Coordinate(latitude = CHANGED_LATITUDE.toDouble(), longitude = CHANGED_LONGITUDE.toDouble())

private val INVALID_COORDINATE_LIST =
    listOf(
        "" to "",
        "abc" to "127.0",
        "90.1" to "127.0",
        "37.5" to "180.1",
    )

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailShareTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-d1dbb6b8-f714-4c33-be4b-69f68122607e 입력된 제목, 주소와 지도 링크를 글로 보낸다`() {
        val contentSharer = testContentSharer()
        setScreen(contentSharer = contentSharer)

        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.input(ADDRESS_INDEX).performTextReplacement(CHANGED_ADDRESS)
        composeRule.input(LATITUDE_INDEX).performTextReplacement(CHANGED_LATITUDE)
        composeRule.input(LONGITUDE_INDEX).performTextReplacement(CHANGED_LONGITUDE)
        clickShare()

        val content = composeRule.sharedContent(contentSharer = contentSharer)
        content.text shouldBe
            listOf(
                CHANGED_TITLE,
                CHANGED_ADDRESS,
                webMapUri(coordinate = CHANGED_COORDINATE, title = CHANGED_TITLE, address = CHANGED_ADDRESS),
            ).joinToString(separator = "\n")
        content.title shouldBe CHANGED_TITLE
    }

    @Test
    fun `TC-ae13c84e-0f28-4793-b40d-6615d385d67c 제목을 비우면 저장된 제목으로 공유한다`() {
        assertSavedTitleShared(inputTitle = "")
    }

    @Test
    fun `TC-ae13c84e-0f28-4793-b40d-6615d385d67c 제목이 공백뿐이면 저장된 제목으로 공유한다`() {
        assertSavedTitleShared(inputTitle = "   ")
    }

    @Test
    fun `TC-083a8739-bcbc-4346-a099-588e763e6092 주소가 비어 있으면 주소 줄 없이 공유한다`() {
        val contentSharer = testContentSharer()
        val detail = placeDetail(title = SAVED_TITLE, coordinate = SAVED_COORDINATE)
        setScreen(contentSharer = contentSharer, detail = detail)

        composeRule.input(ADDRESS_INDEX).performTextReplacement("")
        clickShare()

        composeRule.sharedText(contentSharer = contentSharer) shouldBe
            listOf(detail.title, webMapUri(coordinate = SAVED_COORDINATE, title = detail.title, address = "")).joinToString(separator = "\n")
    }

    @Test
    fun `TC-eb79afc3-0ec5-4cbf-850c-fddef8123625 좌표가 성립하지 않으면 공유하지 않고 좌표를 알린다`() {
        val contentSharer = testContentSharer()
        setScreen(contentSharer = contentSharer)

        INVALID_COORDINATE_LIST.forEach { (latitude, longitude) ->
            composeRule.input(LATITUDE_INDEX).performTextReplacement(latitude)
            composeRule.input(LONGITUDE_INDEX).performTextReplacement(longitude)
            clickShare()

            composeRule.onNodeWithText(DEFAULT_COORDINATE_INVALID_MESSAGE).assertIsDisplayed()
        }

        composeRule.runOnIdle { coVerify(exactly = 0) { contentSharer.share(any()) } }
    }

    @Test
    fun `TC-8ca05126-bcaf-4820-be86-dabc4c550ae8 복사로 대신해도 좌표가 성립하지 않으면 복사하지 않는다`() {
        val contentSharer = testContentSharer(result = ShareResult.COPIED)
        setScreen(contentSharer = contentSharer)

        composeRule.input(LATITUDE_INDEX).performTextReplacement("")
        clickShare()

        composeRule.onNodeWithText(DEFAULT_COORDINATE_INVALID_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_SHARE_COPIED_MESSAGE).assertDoesNotExist()
        composeRule.runOnIdle { coVerify(exactly = 0) { contentSharer.share(any()) } }
    }

    @Test
    fun `TC-2c39f883-bffa-4e37-abee-88c1f9b2c401 좌표 오류로 공유해도 초점을 옮기지 않는다`() {
        setScreen()

        composeRule.input(LATITUDE_INDEX).performTextReplacement(INVALID_LATITUDE)
        composeRule.input(ADDRESS_INDEX).performTextReplacement(CHANGED_ADDRESS)
        composeRule.input(ADDRESS_INDEX).assertIsFocused()

        clickShare()

        composeRule.onNodeWithText(DEFAULT_COORDINATE_INVALID_MESSAGE).assertIsDisplayed()
        composeRule.input(ADDRESS_INDEX).assertIsFocused()
        composeRule.input(TITLE_INDEX).assertIsNotFocused()
        composeRule.input(LATITUDE_INDEX).assertIsNotFocused()
    }

    @Test
    fun `TC-dcdde13b-7bcb-4322-941a-a2c9d0e063cf 공유해도 입력과 화면을 유지하고 수정하지 않는다`() {
        val contentSharer = testContentSharer()
        val viewModel = setScreen(contentSharer = contentSharer)

        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.input(DESCRIPTION_INDEX).performTextReplacement(CHANGED_DESCRIPTION)
        composeRule.input(ADDRESS_INDEX).performTextReplacement(CHANGED_ADDRESS)
        composeRule.input(LATITUDE_INDEX).performTextReplacement(CHANGED_LATITUDE)
        composeRule.input(LONGITUDE_INDEX).performTextReplacement(CHANGED_LONGITUDE)
        composeRule.changeColor()
        clickShare()

        composeRule.runOnIdle { coVerify(exactly = 1) { contentSharer.share(any()) } }
        composeRule.input(TITLE_INDEX).assert(hasText(CHANGED_TITLE))
        composeRule.input(DESCRIPTION_INDEX).assert(hasText(CHANGED_DESCRIPTION))
        composeRule.input(ADDRESS_INDEX).assert(hasText(CHANGED_ADDRESS))
        composeRule.input(LATITUDE_INDEX).assert(hasText(CHANGED_LATITUDE))
        composeRule.input(LONGITUDE_INDEX).assert(hasText(CHANGED_LONGITUDE))
        composeRule.onNodeWithText(CHANGED_COLOR_HEX).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertIsDisplayed()
        composeRule.runOnIdle { verify(exactly = 0) { viewModel.update(any()) } }
    }

    @Test
    fun `TC-262a080c-94d7-4baa-b66c-1faa1ad10412 공유를 마치면 아무 안내도 표시하지 않는다`() {
        assertNoFeedback(result = ShareResult.COMPLETED)
    }

    @Test
    fun `TC-262a080c-94d7-4baa-b66c-1faa1ad10412 공유 시트를 닫으면 아무 안내도 표시하지 않는다`() {
        assertNoFeedback(result = ShareResult.DISMISSED)
    }

    @Test
    fun `TC-6231ea5b-3c99-4919-abdb-1943e5f22e9e 공유 시트를 열지 못하면 공유하지 못했다고 알린다`() {
        setScreen(contentSharer = testContentSharer(result = ShareResult.FAILED))

        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        clickShare()

        composeRule.onNodeWithText(DEFAULT_SHARE_FAILED_MESSAGE).assertIsDisplayed()
        composeRule.input(TITLE_INDEX).assert(hasText(CHANGED_TITLE))
        composeRule.input(LATITUDE_INDEX).assert(hasText(SAVED_LATITUDE_TEXT))
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-957d93ca-a945-4ec6-b4cb-f36b0b28bdf4 복사로 대신하면 클립보드에 복사했다고 알린다`() {
        val contentSharer = testContentSharer(result = ShareResult.COPIED)
        setScreen(contentSharer = contentSharer)

        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        clickShare()

        composeRule.runOnIdle { coVerify(exactly = 1) { contentSharer.share(any()) } }
        composeRule.onNodeWithText(DEFAULT_SHARE_COPIED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_SHARE_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.input(TITLE_INDEX).assert(hasText(CHANGED_TITLE))
        composeRule.input(LATITUDE_INDEX).assert(hasText(SAVED_LATITUDE_TEXT))
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-e1b96580-e392-49bc-994f-b388656424b1 조회 중에는 공유를 실행할 수 없다`() {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(uiState = MutableStateFlow(PlaceDetailUiState.Loading)),
            contentSharerFactory = contentSharerFactoryOf(),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-d8019678-944e-4b23-b2e7-c05e4be477e2 공유는 선택한 탭과 관계없이 제공된다`() {
        val contentSharer = testContentSharer()
        setScreen(contentSharer = contentSharer)

        listOf(DEFAULT_DETAIL_TAB_DESCRIPTION, DEFAULT_MEMO_TAB_DESCRIPTION).forEachIndexed { index, tabDescription ->
            composeRule.selectPlaceDetailTab(tabDescription)
            clickShare()

            composeRule.runOnIdle { coVerify(exactly = index + 1) { contentSharer.share(any()) } }
        }
    }

    @Test
    fun `TC-7428a79f-f09e-4647-9f6f-3ce53d724d73 수정을 처리하는 동안에도 공유할 수 있다`() {
        val contentSharer = testContentSharer()
        setScreen(contentSharer = contentSharer, isUpdateInProgress = true)

        clickShare()

        composeRule.runOnIdle { coVerify(exactly = 1) { contentSharer.share(any()) } }
    }

    @Test
    fun `TC-7428a79f-f09e-4647-9f6f-3ce53d724d73 삭제를 처리하는 동안에도 공유할 수 있다`() {
        val contentSharer = testContentSharer()
        setScreen(contentSharer = contentSharer, isDeleteInProgress = true)

        clickShare()

        composeRule.runOnIdle { coVerify(exactly = 1) { contentSharer.share(any()) } }
    }

    @Test
    fun `TC-d1364934-d1e1-4d9a-8b0e-587bc9e607f8 공유 시트가 열린 동안 재생성되어도 공유를 다시 시작하지 않는다`() {
        val contentSharer = testContentSharer()
        coEvery { contentSharer.share(any()) } coAnswers { awaitCancellation() }
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(content(detail = placeDetail(coordinate = SAVED_COORDINATE))))
        val restorationTester = StateRestorationTester(composeRule)
        preparePlaceDetailTabViewModels()
        restorationTester.setContent {
            PlaceDetailScreenTestTheme {
                PlaceDetailScreen(
                    navigateToTagAdd = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    navigateToMemoFinishedList = {},
                    id = FIRST_PLACE_ID,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = viewModel,
                    searchViewModel = searchScreenTestViewModel(),
                    navigateUp = {},
                    navigateToTagDetail = {},
                    tagViewModel = detailTagScreenTestViewModel(),
                    contentSharerFactory = contentSharerFactoryOf(contentSharer = contentSharer),
                )
            }
        }
        composeRule.waitForIdle()
        clickShare()
        composeRule.runOnIdle { coVerify(exactly = 1) { contentSharer.share(any()) } }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.runOnIdle { coVerify(exactly = 1) { contentSharer.share(any()) } }
    }

    @Test
    fun `TC-e48e7251-2a94-435c-9e10-dcbc23926df3 공유를 제공하지 않는 환경에서는 공유 버튼이 없다`() {
        setScreen(contentSharer = testContentSharer(isTextSupported = false))

        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    private fun assertSavedTitleShared(inputTitle: String) {
        val contentSharer = testContentSharer()
        val detail = placeDetail(title = SAVED_TITLE, coordinate = SAVED_COORDINATE)
        setScreen(contentSharer = contentSharer, detail = detail)

        composeRule.input(TITLE_INDEX).performTextReplacement(inputTitle)
        clickShare()

        val content = composeRule.sharedContent(contentSharer = contentSharer)
        content.text shouldBe
            listOf(
                detail.title,
                detail.address,
                webMapUri(coordinate = SAVED_COORDINATE, title = detail.title, address = detail.address),
            ).joinToString(separator = "\n")
        content.title shouldBe detail.title
    }

    private fun assertNoFeedback(result: ShareResult) {
        val contentSharer = testContentSharer(result = result)
        setScreen(contentSharer = contentSharer)

        clickShare()

        composeRule.runOnIdle { coVerify(exactly = 1) { contentSharer.share(any()) } }
        composeRule.onNodeWithText(DEFAULT_SHARE_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SHARE_COPIED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_COORDINATE_INVALID_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertIsDisplayed()
    }

    private fun setScreen(
        contentSharer: ContentSharer = testContentSharer(),
        detail: PlaceDetail = placeDetail(coordinate = SAVED_COORDINATE),
        isUpdateInProgress: Boolean = false,
        isDeleteInProgress: Boolean = false,
    ): PlaceDetailViewModel {
        val viewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(content(detail = detail, isUpdateInProgress = isUpdateInProgress, isDeleteInProgress = isDeleteInProgress)),
            )
        composeRule.setPlaceDetailScreen(viewModel = viewModel, contentSharerFactory = contentSharerFactoryOf(contentSharer = contentSharer))

        return viewModel
    }

    private fun clickShare() {
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun webMapUri(
        coordinate: Coordinate,
        title: String,
        address: String,
    ): String =
        externalMapWebUri(
            provider = DiaryMapProvider.NAVER,
            coordinate = coordinate.toDiaryMapCoordinate(),
            title = title,
            address = address,
        )
}
