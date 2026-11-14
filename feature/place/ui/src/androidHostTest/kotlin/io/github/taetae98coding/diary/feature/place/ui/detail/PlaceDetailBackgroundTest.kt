package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.place.ui.ProvideTestDiaryMap
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.TestDiaryMap
import io.github.taetae98coding.diary.feature.place.ui.add.placeTestTag
import io.github.taetae98coding.diary.feature.place.ui.diaryMapCoordinateInFormPrecision
import io.github.taetae98coding.diary.feature.place.ui.search.DEFAULT_QUERY_PLACEHOLDER
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 앱이 종료되지 않은 채 백그라운드에 갔다가 같은 화면으로 돌아오는 경계를 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class PlaceDetailBackgroundTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val map = TestDiaryMap()

    @Test
    fun `TC-f5ab6ac6-9dff-484f-ac24-6f052232c6f7 앱이 백그라운드에 다녀와도 수정 중인 내용과 태그와 지도를 유지한다`() {
        val changedSpot = DiaryMapCoordinate(latitude = CHANGED_LATITUDE.toDouble(), longitude = CHANGED_LONGITUDE.toDouble())
        val viewedCoordinate = generateSequence { Arb.diaryMapCoordinateInFormPrecision().next() }.first { coordinate -> coordinate != changedSpot }
        setPlaceDetailScreen(tagViewModel = detailTagScreenTestViewModel(tagList = listOf(placeTestTag(title = LINKED_TAG_TITLE))))
        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.input(DESCRIPTION_INDEX).performTextReplacement(CHANGED_DESCRIPTION)
        composeRule.input(ADDRESS_INDEX).performTextReplacement(CHANGED_ADDRESS)
        composeRule.input(LATITUDE_INDEX).performTextReplacement(CHANGED_LATITUDE)
        composeRule.input(LONGITUDE_INDEX).performTextReplacement(CHANGED_LONGITUDE)
        composeRule.changeColor()
        composeRule.mainClock.advanceTimeBy(REFLECT_WAIT_MILLIS)
        composeRule.runOnIdle { map.firstScope().state.select(DiaryMapProvider.GOOGLE) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { map.firstScope().reportCamera(coordinate = viewedCoordinate, zoom = CHANGED_ZOOM) }
        composeRule.waitForIdle()

        moveToBackgroundAndReturn()

        composeRule.input(TITLE_INDEX).assert(hasText(CHANGED_TITLE))
        composeRule.input(DESCRIPTION_INDEX).assert(hasText(CHANGED_DESCRIPTION))
        composeRule.input(ADDRESS_INDEX).assert(hasText(CHANGED_ADDRESS))
        composeRule.input(LATITUDE_INDEX).assert(hasText(CHANGED_LATITUDE))
        composeRule.input(LONGITUDE_INDEX).assert(hasText(CHANGED_LONGITUDE))
        composeRule.onNodeWithText(CHANGED_COLOR_HEX).assertExists()
        composeRule.onNodeWithText(LINKED_TAG_TITLE).assertExists()
        composeRule.runOnIdle {
            map.firstScope().state.spot shouldBe changedSpot
            map.firstScope().state.provider shouldBe DiaryMapProvider.GOOGLE
            map.firstScope().state.coordinate shouldBe viewedCoordinate
            map.firstScope().zoom shouldBe CHANGED_ZOOM
        }
    }

    @Test
    fun `TC-971b64fb-beb3-4d6b-92c8-4097f2f4b37d 앱이 백그라운드에 다녀와도 열려 있던 장소 검색이 남는다`() {
        setPlaceDetailScreen(tagViewModel = detailTagScreenTestViewModel())
        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(SEARCH_QUERY)
        composeRule.waitForIdle()

        moveToBackgroundAndReturn()

        composeRule.onNode(hasText(SEARCH_QUERY) and hasAnyAncestor(isDialog())).assertExists()
        composeRule.input(TITLE_INDEX).assert(hasText(CHANGED_TITLE))
    }

    private fun moveToBackgroundAndReturn() {
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()
    }

    private fun setPlaceDetailScreen(tagViewModel: EntityDetailTagViewModel) {
        val uiState = MutableStateFlow<PlaceDetailUiState>(content(id = FIRST_PLACE_ID).copy(defaultProvider = MapProvider.NAVER))
        preparePlaceDetailTabViewModels()

        composeRule.setContent {
            ProvideTestDiaryMap(map = map) {
                PlaceDetailScreenTestTheme {
                    PlaceDetailScreen(
                        navigateUp = {},
                        navigateToTagAdd = {},
                        navigateToTagDetail = {},
                        navigateToMemoAdd = {},
                        navigateToMemoDetail = {},
                        navigateToMemoFinishedList = {},
                        id = FIRST_PLACE_ID,
                        tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                        detailViewModel = remember { screenTestViewModel(uiState = uiState) },
                        searchViewModel = remember { searchScreenTestViewModel() },
                        tagViewModel = tagViewModel,
                        contentSharerFactory = contentSharerFactoryOf(),
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        private const val LINKED_TAG_TITLE = "PlaceDetailBackgroundTag"
        private const val SEARCH_QUERY = "PlaceDetailBackgroundQuery"

        // 지도가 처음 정하는 확대 수준과 구분되도록 다른 값으로 알려 온다.
        private const val CHANGED_ZOOM = 18.0

        // 입력 정지 대기 시간이 넉넉히 지나도록 기다린다.
        private const val REFLECT_WAIT_MILLIS = 400L
    }
}
