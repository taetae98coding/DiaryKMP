package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoPlaceCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 장소 카드의 문구를 표시한다`() {
        val place = testPlace(title = HOME_PLACE_TITLE)
        composeRule.setMemoPlaceCard(uiStateProvider = { placeCardUiState(selectedPlaceList = listOf(place)) })

        composeRule.onNodeWithContentDescription(KOREAN_MAP_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assert(hasClickLabel(KOREAN_SHOW_ON_MAP_ACTION))
        composeRule.onNodeWithText(KOREAN_PLACE_SELECT_LABEL).assert(hasClickLabel(KOREAN_PLACE_SELECT_LABEL))
    }

    @Test
    fun `기본 환경에서 장소 카드의 문구를 표시한다`() {
        val place = testPlace(title = HOME_PLACE_TITLE)
        composeRule.setMemoPlaceCard(uiStateProvider = { placeCardUiState(selectedPlaceList = listOf(place)) })

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assert(hasClickLabel(DEFAULT_SHOW_ON_MAP_ACTION))
        composeRule.onNodeWithText(DEFAULT_PLACE_SELECT_LABEL).assert(hasClickLabel(DEFAULT_PLACE_SELECT_LABEL))
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-003 기본 지도를 확인하지 못하면 지도를 표시하지 않고 카드와 장소 목록은 유지한다`() {
        val place = testPlace(title = HOME_PLACE_TITLE)

        composeRule.setMemoPlaceCard(uiStateProvider = { placeCardUiState(selectedPlaceList = listOf(place)) })

        // 지도가 표시되면 지도 위에 제공자 전환 컨트롤이 함께 나타난다.
        composeRule.onNodeWithText(NAVER_PROVIDER_NAME).assertDoesNotExist()
        composeRule.onNodeWithText(GOOGLE_PROVIDER_NAME).assertDoesNotExist()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_MAP_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-004 연결된 장소가 제목으로 모두 표시된다`() {
        var uiState by mutableStateOf(MemoPlaceCardUiState())
        composeRule.setMemoPlaceCard(uiStateProvider = { uiState })

        listOf(
            listOf(testPlace(title = HOME_PLACE_TITLE)),
            listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE)),
        ).forEach { placeList ->
            uiState = placeCardUiState(selectedPlaceList = placeList)

            placeList.forEach { place ->
                composeRule.onNodeWithText(place.detail.title).assertExists()
            }
        }
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-006 지도가 표시되지 않는 동안 장소를 선택해도 지도 이동이 일어나지 않는다`() {
        val place = testPlace(title = HOME_PLACE_TITLE)

        composeRule.setMemoPlaceCard(uiStateProvider = { placeCardUiState(selectedPlaceList = listOf(place)) })
        composeRule.onNodeWithText(HOME_PLACE_TITLE).performClick()

        composeRule.onNodeWithText(NAVER_PROVIDER_NAME).assertDoesNotExist()
        composeRule.onNodeWithText(GOOGLE_PROVIDER_NAME).assertDoesNotExist()
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-037 목록의 장소를 눌러도 장소 상세로 이동하지 않고 선택이 바뀌지 않는다`() {
        val placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE))
        var placeClickCount = 0
        var addClickCount = 0

        composeRule.setMemoPlaceCard(
            uiStateProvider = { placeCardUiState(selectedPlaceList = placeList) },
            onPlaceClick = { placeClickCount += 1 },
            onAddClick = { addClickCount += 1 },
        )
        composeRule.onNodeWithText(HOME_PLACE_TITLE).performClick()

        placeClickCount shouldBe 0
        addClickCount shouldBe 0
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
        composeRule.onNodeWithText(OFFICE_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-009 노출하는 장소가 없거나 여러 개여도 장소 추가 항목이 표시된다`() {
        var uiState by mutableStateOf(placeCardUiState())
        composeRule.setMemoPlaceCard(uiStateProvider = { uiState })

        listOf(
            emptyList(),
            listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE)),
        ).forEach { placeList ->
            uiState = placeCardUiState(selectedPlaceList = placeList)

            composeRule.onNodeWithText(DEFAULT_PLACE_SELECT_LABEL).assertExists()
        }
    }

    /**
     * 노출하는 장소 확인이 끝나면 지도가 표시되는 나머지 절반은 지도 제공자의 실제 표시 요소가 필요해
     * 호스트 테스트에서 자동화할 수 없다. (Robolectric에서 지도 SDK 뷰 생성 시 VerifyError)
     */
    @Test
    fun `TC-MEMO-PLACE-CARD-DOMAIN-013 기본 지도가 확인되어도 노출하는 장소를 확인하기 전에는 지도를 표시하지 않는다`() {
        val uiState =
            MemoPlaceCardUiState(
                mapUiState =
                    MemoPlaceMapUiState.Loaded(
                        provider = MapProvider.NAVER,
                        currentCoordinate = null,
                    ),
                placeUiState = MemoPlaceInputUiState(),
            )
        composeRule.setMemoPlaceCard(uiStateProvider = { uiState })

        // 지도가 표시되면 지도 위에 제공자 전환 컨트롤이 함께 나타난다.
        composeRule.onNodeWithText(NAVER_PROVIDER_NAME).assertDoesNotExist()
        composeRule.onNodeWithText(GOOGLE_PROVIDER_NAME).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_MAP_DESCRIPTION).assertExists()
    }

    @Test
    fun `장소가 생겨 칩이 칩 영역 안에 들어가면 장소 카드의 높이가 바뀌지 않는다`() {
        var uiState by mutableStateOf(placeCardUiState())
        composeRule.setMemoPlaceCard(uiStateProvider = { uiState })
        val emptyHeight = composeRule.memoPlaceCardHeight()

        val place = testPlace(title = HOME_PLACE_TITLE)
        uiState = placeCardUiState(selectedPlaceList = listOf(place))

        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
        composeRule.memoPlaceCardHeight() shouldBe emptyHeight
    }

    @Test
    fun `장소 칩이 칩 영역보다 많으면 장소 카드가 그만큼 높아진다`() {
        var uiState by mutableStateOf(placeCardUiState())
        composeRule.setMemoPlaceCard(uiStateProvider = { uiState })
        val emptyHeight = composeRule.memoPlaceCardHeight()
        val placeList = scrollTestPlaceList()

        uiState = placeCardUiState(selectedPlaceList = placeList)

        composeRule.onNodeWithText(placeList.last().detail.title).assertExists()
        composeRule.memoPlaceCardHeight() shouldBeGreaterThan emptyHeight
    }

    @Test
    fun `카드 높이가 정해지지 않으면 칩이 많아도 칩 영역은 세로 스크롤을 가져가지 않는다`() {
        val placeList = scrollTestPlaceList()

        composeRule.setMemoPlaceCard(uiStateProvider = { placeCardUiState(selectedPlaceList = placeList) })

        composeRule.onNode(hasScrollAction()).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun `높이를 지정받은 장소 카드는 칩이 많아도 카드 높이를 지키고 칩 영역 안에서 스크롤한다`() {
        val placeList = scrollTestPlaceList()
        composeRule.setFillHeightMemoPlaceCard(height = FILL_HEIGHT_CARD_HEIGHT) { placeCardUiState(selectedPlaceList = placeList) }
        val lastPlaceTitle = placeList.last().detail.title

        composeRule.memoPlaceCardHeight() shouldBe FILL_HEIGHT_CARD_HEIGHT
        composeRule.onNodeWithText(lastPlaceTitle).assertIsNotDisplayed()
        composeRule.onNodeWithText(lastPlaceTitle).performScrollTo()

        composeRule.onNodeWithText(lastPlaceTitle).assertIsDisplayed()
    }

    private companion object {
        // 지도 영역과 칩 영역이 함께 들어가는 높이다.
        private val FILL_HEIGHT_CARD_HEIGHT = 400.dp
    }
}
