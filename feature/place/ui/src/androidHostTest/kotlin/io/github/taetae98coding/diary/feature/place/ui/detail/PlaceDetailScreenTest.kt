package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.core.model.location.Coordinate
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
@Config(sdk = [36])
class PlaceDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-001 조회 중에는 상세 내용과 동작을 제공하지 않는다`() {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(uiState = MutableStateFlow(PlaceDetailUiState.Loading)),
        )

        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasSetTextAction())
            .fetchSemanticsNodes()
            .size shouldBe 0
        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-002 조회에 성공하면 저장된 제목, 설명, 주소, 좌표를 표시한다`() {
        val detail = placeDetail()

        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel(uiState = MutableStateFlow(content(detail = detail))))

        composeRule.waitForIdle()

        composeRule.input(TITLE_INDEX).assert(hasText(detail.title))
        composeRule.input(DESCRIPTION_INDEX).assert(hasText(detail.description))
        composeRule.input(ADDRESS_INDEX).assert(hasText(detail.address))
        composeRule.input(LATITUDE_INDEX).assert(hasText(SAVED_LATITUDE_TEXT))
        composeRule.input(LONGITUDE_INDEX).assert(hasText(SAVED_LONGITUDE_TEXT))
        // 저장된 제목은 상단 바와 제목 입력 두 곳에 표시된다.
        composeRule
            .onAllNodes(hasText(detail.title))
            .fetchSemanticsNodes()
            .size shouldBe TITLE_DISPLAY_COUNT
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-004 입력이 저장된 내용과 같으면 수정 동작을 제공하지 않는다`() {
        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-059 여섯째 자리보다 긴 자리수로 저장된 좌표도 바꾸지 않으면 수정 동작을 제공하지 않는다`() {
        val detail = placeDetail(coordinate = Coordinate(latitude = 37.1234567, longitude = 127.1234564))

        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel(uiState = MutableStateFlow(content(detail = detail))))
        composeRule.waitForIdle()

        composeRule.input(LATITUDE_INDEX).assert(hasText("37.123457"))
        composeRule.input(LONGITUDE_INDEX).assert(hasText("127.123456"))
        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-005 입력이 저장된 내용과 다르면 수정 동작을 제공한다`() {
        val detail = placeDetail()

        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel(uiState = MutableStateFlow(content(detail = detail))))

        listOf(
            Triple(TITLE_INDEX, CHANGED_TITLE, detail.title),
            Triple(DESCRIPTION_INDEX, CHANGED_DESCRIPTION, detail.description),
            Triple(ADDRESS_INDEX, CHANGED_ADDRESS, detail.address),
            Triple(LATITUDE_INDEX, CHANGED_LATITUDE, detail.coordinate.latitude.toString()),
            Triple(LONGITUDE_INDEX, CHANGED_LONGITUDE, detail.coordinate.longitude.toString()),
            Triple(LATITUDE_INDEX, "", detail.coordinate.latitude.toString()),
        ).forEach { (index, changedText, savedText) ->
            composeRule.input(index).performTextReplacement(changedText)
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())

            composeRule.input(index).performTextReplacement(savedText)
            composeRule.waitForIdle()

            composeRule
                .onAllNodes(hasContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION))
                .fetchSemanticsNodes()
                .size shouldBe 0
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-011 좌표가 성립하지 않아도 수정 중인 내용을 유지한다`() {
        val detail = placeDetail()

        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel(uiState = MutableStateFlow(content(detail = detail))))

        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.input(DESCRIPTION_INDEX).performTextReplacement(CHANGED_DESCRIPTION)
        composeRule.input(LATITUDE_INDEX).performTextReplacement(INVALID_LATITUDE)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.input(TITLE_INDEX).assert(hasText(CHANGED_TITLE))
        composeRule.input(DESCRIPTION_INDEX).assert(hasText(CHANGED_DESCRIPTION))
        composeRule.input(LATITUDE_INDEX).assert(hasText(INVALID_LATITUDE))
        composeRule.input(LONGITUDE_INDEX).assert(hasText(SAVED_LONGITUDE_TEXT))
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-017 바꾼 내용을 반영하지 않고 뒤로가면 수정을 요청하지 않고 뒤로가기를 한 번 실행한다`() {
        var navigateUpCount = 0
        val viewModel = screenTestViewModel()

        composeRule.setPlaceDetailScreen(
            viewModel = viewModel,
            navigateUp = { navigateUpCount++ },
        )
        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.input(DESCRIPTION_INDEX).performTextReplacement(CHANGED_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { viewModel.update(any()) }
        verify(exactly = 0) { viewModel.delete() }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-019 대상 장소를 조회하기 전에는 지도를 표시하지 않는다`() {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(uiState = MutableStateFlow(PlaceDetailUiState.Loading)),
        )

        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_MAP_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-020 기본 지도를 확인하지 못하면 지도 영역과 로딩 안내 없이 입력 영역만 표시하고 동작은 사용할 수 있다`() {
        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .fetchSemanticsNodes()
            .size shouldBe 0

        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_MAP_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
        composeRule
            .onAllNodes(hasSetTextAction())
            .fetchSemanticsNodes()
            .size shouldBe INPUT_COUNT
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLACE-DETAIL-DOMAIN-003 저장된 내용이 바뀌어도 입력 내용을 다시 채우지 않는다`() {
        val detail = placeDetail()
        val id = Uuid.random()
        val uiState = MutableStateFlow(content(id = id, detail = detail))

        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel(uiState = uiState))

        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.waitForIdle()

        uiState.value = content(id = id, detail = detail.copy(description = CHANGED_DESCRIPTION))
        composeRule.waitForIdle()

        composeRule.input(TITLE_INDEX).assert(hasText(CHANGED_TITLE))
        composeRule.input(DESCRIPTION_INDEX).assert(hasText(detail.description))
    }
}
