package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.testing.place.coordinateInFormPrecision
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceHomePlaceListUiState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-999268d9-64bd-45cb-a8f7-301751f4dd80 기본 지도를 확인하기 전에는 본문에 지도를 표시하지 않는다`() {
        setPlaceHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_PROVIDER_DESCRIPTION).assertDoesNotExist()
        displayedTexts() shouldBe listOf(DEFAULT_TITLE)
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-999268d9-64bd-45cb-a8f7-301751f4dd80 기본 지도를 확인하기 전에는 뒤로가기와 검색, 보기 모드 전환, 장소 추가만 조작할 수 있다`() {
        setPlaceHomeScaffold()

        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe TOP_LEVEL_ACTION_COUNT
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-0846968a-d00a-4664-a1a7-aece9a04f4a5 장소 추가 버튼을 누르면 추가 이벤트를 한 번 전달한다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceHomeScaffold(onEvent = { event -> eventList.add(event) })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()

        eventList.filterNot { event -> event is PlaceHomeScaffoldEvent.MoveMap } shouldBe
            listOf(PlaceHomeScaffoldEvent.ClickAdd(coordinate = null))
    }

    @Test
    fun `TC-dbb61a95-0934-4d89-ab50-f2905f184905 지도가 표시되지 않아도 장소 추가 버튼을 표시한다`() {
        setPlaceHomeScaffold(uiStateProvider = { PlaceHomeUiState.Loading })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-b6807ccb-d4d6-4776-89c6-88c98b70d542 뒤로가기 버튼을 누르면 뒤로가기 이벤트를 한 번 전달한다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceHomeScaffold(onEvent = { event -> eventList.add(event) })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList.filterNot { event -> event is PlaceHomeScaffoldEvent.MoveMap } shouldBe
            listOf(PlaceHomeScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `TC-85250a22-544b-45f6-b5df-044013a86a44 지도가 표시되지 않으면 지도 위치를 넘기지 않는다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceHomeScaffold(
            uiStateProvider = { PlaceHomeUiState.Loading },
            onEvent = { event -> eventList.add(event) },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()

        eventList.filterNot { event -> event is PlaceHomeScaffoldEvent.MoveMap } shouldBe
            listOf(PlaceHomeScaffoldEvent.ClickAdd(coordinate = null))
    }

    @Test
    fun `TC-415d2502-9395-4180-bdeb-eed7c2f15235 지도가 표시되지 않으면 장소 카드를 표시하지 않는다`() {
        setPlaceHomeScaffold(
            uiStateProvider = { PlaceHomeUiState.Loading },
            placeListUiStateProvider = { PlaceHomePlaceListUiState(isLoaded = true, placeList = List(PLACE_COUNT) { place() }) },
        )

        composeRule.onAllNodesWithTag(PLACE_CARD_TEST_TAG).fetchSemanticsNodes().size shouldBe 0
    }

    @Test
    fun `지도가 표시되기 전에는 보이는 영역 없음을 알리는 지도 이동 이벤트만 전달한다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceHomeScaffold(onEvent = { event -> eventList.add(event) })

        composeRule.waitForIdle()

        eventList shouldBe listOf(PlaceHomeScaffoldEvent.MoveMap(bounds = null))
    }

    @Test
    fun `TC-be17e8d9-736b-40c8-9704-01490fc3c0cf 검색 버튼을 누르면 검색 이벤트를 한 번 전달한다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceHomeScaffold(onEvent = { event -> eventList.add(event) })

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_DESCRIPTION).performClick()

        eventList.filterNot { event -> event is PlaceHomeScaffoldEvent.MoveMap } shouldBe
            listOf(PlaceHomeScaffoldEvent.ClickSearch)
    }

    @Test
    fun `TC-41fc41e1-dd6c-4625-924f-7c992bc83387 지도가 표시되지 않아도 검색 버튼을 표시한다`() {
        setPlaceHomeScaffold(uiStateProvider = { PlaceHomeUiState.Loading })

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `상단 바의 검색 버튼을 길게 누르면 접근성 이름과 같은 설명을 표시한다`() {
        setPlaceHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_DESCRIPTION).performTouchInput { longClick() }

        composeRule.onNodeWithText(DEFAULT_SEARCH_DESCRIPTION).assertExists()
    }

    @Test
    fun `상단 바의 보기 모드 전환 버튼을 길게 누르면 접근성 이름과 같은 설명을 표시한다`() {
        setPlaceHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).performTouchInput { longClick() }

        composeRule.onNodeWithText(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-c58529bf-774f-4c95-b18b-e1bf636ac0db 지도를 옮기지 않았으면 장소 추가와 추가 단축키가 진입 시 시작한 위치를 넘긴다`() {
        val initialCoordinate = fixtureMonkey.coordinateInFormPrecision()
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceHomeScaffold(
            uiStateProvider = { PlaceHomeUiState.Content(defaultProvider = MapProvider.GOOGLE, initialCoordinate = initialCoordinate) },
            onEvent = { event -> eventList.add(event) },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        performAddShortcut()

        eventList.filterIsInstance<PlaceHomeScaffoldEvent.ClickAdd>() shouldBe List(ADD_ACTION_COUNT) { PlaceHomeScaffoldEvent.ClickAdd(coordinate = initialCoordinate) }
    }

    private fun performAddShortcut() {
        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.A)
            keyUp(Key.A)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()
    }

    private fun setPlaceHomeScaffold(
        uiStateProvider: () -> PlaceHomeUiState = { PlaceHomeUiState.Loading },
        placeListUiStateProvider: () -> PlaceHomePlaceListUiState = { PlaceHomePlaceListUiState() },
        onEvent: (PlaceHomeScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                PlaceHomeScaffold(
                    uiStateProvider = uiStateProvider,
                    placeListUiStateProvider = placeListUiStateProvider,
                    onEvent = onEvent,
                )
            }
        }
    }

    private fun displayedTexts(): List<String> =
        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.Text] }
            .map { text -> text.text }

    public companion object {
        private const val DEFAULT_TITLE = "Place"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_ADD_DESCRIPTION = "Add place"
        private const val DEFAULT_MAP_PROVIDER_DESCRIPTION = "Map provider"
        private const val DEFAULT_LIST_VIEW_MODE_DESCRIPTION = "Show list"
        private const val DEFAULT_SEARCH_DESCRIPTION = "Search"
        private const val PLACE_COUNT = 3
        private const val TOP_LEVEL_ACTION_COUNT = 4

        // 장소 추가 버튼과 추가 단축키
        private const val ADD_ACTION_COUNT = 2

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun place(): Place =
            Place(
                id = Uuid.random(),
                detail = fixtureMonkey.giveMeOne<PlaceDetail>(),
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                createdAt = fixtureMonkey.giveMeOne<Instant>(),
            )
    }
}
