package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
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
    fun `TC-PLACE-HOME-FEATURE-011 기본 지도를 확인하기 전에는 본문에 지도를 표시하지 않는다`() {
        setPlaceHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_PROVIDER_DESCRIPTION).assertDoesNotExist()
        displayedTexts() shouldBe listOf(DEFAULT_TITLE)
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-011 기본 지도를 확인하기 전에는 뒤로가기와 검색, 보기 모드 전환, 장소 추가만 조작할 수 있다`() {
        setPlaceHomeScaffold()

        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe TOP_LEVEL_ACTION_COUNT
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-014 장소 추가 버튼을 누르면 추가 이벤트를 한 번 전달한다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceHomeScaffold(onEvent = { event -> eventList.add(event) })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()

        eventList.filterNot { event -> event is PlaceHomeScaffoldEvent.MoveMap } shouldBe
            listOf(PlaceHomeScaffoldEvent.ClickAdd(coordinate = null))
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-015 지도가 표시되지 않아도 장소 추가 버튼을 표시한다`() {
        setPlaceHomeScaffold(uiStateProvider = { PlaceHomeUiState.Loading })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-003 뒤로가기 버튼을 누르면 뒤로가기 이벤트를 한 번 전달한다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceHomeScaffold(onEvent = { event -> eventList.add(event) })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList.filterNot { event -> event is PlaceHomeScaffoldEvent.MoveMap } shouldBe
            listOf(PlaceHomeScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-008 지도가 표시되지 않으면 지도 위치를 넘기지 않는다`() {
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
    fun `TC-PLACE-HOME-FEATURE-021 지도가 표시되지 않으면 장소 카드를 표시하지 않는다`() {
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
    fun `TC-PLACE-HOME-FEATURE-052 검색 버튼을 누르면 검색 이벤트를 한 번 전달한다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceHomeScaffold(onEvent = { event -> eventList.add(event) })

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_DESCRIPTION).performClick()

        eventList.filterNot { event -> event is PlaceHomeScaffoldEvent.MoveMap } shouldBe
            listOf(PlaceHomeScaffoldEvent.ClickSearch)
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-053 지도가 표시되지 않아도 검색 버튼을 표시한다`() {
        setPlaceHomeScaffold(uiStateProvider = { PlaceHomeUiState.Loading })

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_DESCRIPTION).assert(hasClickAction())
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

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        // FixtureMonkey가 Instant를 생성하지 못하므로 장소는 직접 만든다.
        private fun place(): Place =
            Place(
                id = Uuid.random(),
                detail = fixtureMonkey.giveMeOne<PlaceDetail>(),
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            )
    }
}
