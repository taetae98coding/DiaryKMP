package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceAddFormState
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchUiState
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val KOREAN_TITLE = "장소 추가"
private const val DEFAULT_TITLE = "Add Place"
private const val KOREAN_ADDRESS_LABEL = "주소"
private const val DEFAULT_ADDRESS_LABEL = "Address"
private const val KOREAN_LATITUDE_LABEL = "위도"
private const val KOREAN_LONGITUDE_LABEL = "경도"
private const val DEFAULT_LATITUDE_LABEL = "Latitude"
private const val DEFAULT_LONGITUDE_LABEL = "Longitude"
private const val NEGATIVE_DECIMAL = "-37.5665"
private const val POSITIVE_DECIMAL = "126.9780"
private const val TEXT_INPUT_COUNT = 5

private fun ComposeContentTestRule.setPlaceAddScaffold(
    uiStateProvider: () -> PlaceAddUiState = { PlaceAddUiState() },
    onEvent: (PlaceAddScaffoldEvent) -> Unit = {},
) {
    setContent {
        DiaryTheme {
            PlaceAddScaffold(
                state = rememberPlaceAddFormState(initialColor = Color.Red),
                uiStateProvider = uiStateProvider,
                searchUiStateProvider = { PlaceSearchUiState.Idle },
                onEvent = onEvent,
                onFormEvent = {},
                onSearchEvent = {},
                onTagPickerEvent = {},
            )
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceAddScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목은 장소 추가이다`() {
        composeRule.setPlaceAddScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `기본 환경에서 제목은 Add Place이다`() {
        composeRule.setPlaceAddScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    fun `기본 뒤로가기 버튼 이름은 Navigate up이다`() {
        composeRule.setPlaceAddScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 추가 버튼 이름은 Add place이다`() {
        composeRule.setPlaceAddScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-001 처음 표시되면 제목 설명 좌표 입력이 모두 비어 있다`() {
        composeRule.setPlaceAddScaffold()

        composeRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size shouldBe TEXT_INPUT_COUNT
        repeat(TEXT_INPUT_COUNT) { index ->
            composeRule.onAllNodes(hasSetTextAction())[index].assert(hasText(""))
        }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-003 좌표에 음수와 소수점을 입력할 수 있다`() {
        composeRule.setPlaceAddScaffold()

        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].performTextInput(NEGATIVE_DECIMAL)
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].performTextInput(POSITIVE_DECIMAL)

        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].assert(hasText(NEGATIVE_DECIMAL))
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].assert(hasText(POSITIVE_DECIMAL))
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-006 추가 처리 중 추가 버튼이 진행 표시로 바뀐다`() {
        composeRule.setPlaceAddScaffold(uiStateProvider = { PlaceAddUiState(isInProgress = true) })

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    fun `추가 버튼을 누르면 추가 이벤트를 한 번 전달한다`() {
        val eventList = mutableListOf<PlaceAddScaffoldEvent>()
        composeRule.setPlaceAddScaffold(onEvent = { event -> eventList.add(event) })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(PlaceAddScaffoldEvent.ClickAdd)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceAddScaffoldLabelTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-PLACE-ADD-FEATURE-002 한국어 좌표 입력 이름은 위도와 경도이다`() {
        composeRule.setPlaceAddScaffold()

        composeRule.onNodeWithText(KOREAN_LATITUDE_LABEL).assertExists()
        composeRule.onNodeWithText(KOREAN_LONGITUDE_LABEL).assertExists()
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-002 기본 좌표 입력 이름은 Latitude와 Longitude이다`() {
        composeRule.setPlaceAddScaffold()

        composeRule.onNodeWithText(DEFAULT_LATITUDE_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_LONGITUDE_LABEL).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-PLACE-ADD-FEATURE-002 한국어 주소 입력 이름은 주소이다`() {
        composeRule.setPlaceAddScaffold()

        composeRule.onNodeWithText(KOREAN_ADDRESS_LABEL).assertExists()
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-002 기본 주소 입력 이름은 Address이다`() {
        composeRule.setPlaceAddScaffold()

        composeRule.onNodeWithText(DEFAULT_ADDRESS_LABEL).assertExists()
    }
}
