package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.place.ui.ProvideTestDiaryMap
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.TestDiaryMap
import io.github.taetae98coding.diary.feature.place.ui.diaryMapCoordinateInFormPrecision
import io.github.taetae98coding.diary.feature.place.ui.search.DEFAULT_QUERY_PLACEHOLDER
import io.github.taetae98coding.diary.feature.place.ui.toCoordinateText
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 앱이 종료되지 않은 채 백그라운드에 갔다가 같은 화면으로 돌아오는 경계를 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class PlaceAddBackgroundTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val map = TestDiaryMap()

    @Test
    fun `TC-b1e5c814-cea6-45b7-8628-5cebcc7a75c1 앱이 백그라운드에 다녀와도 입력과 고른 태그와 지도를 유지한다`() {
        val selectedCoordinate = Arb.diaryMapCoordinateInFormPrecision().next()
        val viewedCoordinate = generateSequence { Arb.diaryMapCoordinateInFormPrecision().next() }.first { coordinate -> coordinate != selectedCoordinate }
        setPlaceAddScreen(tagViewModel = addTagScreenTestViewModel(tagList = listOf(placeTestTag(title = SELECTED_TAG_TITLE))))
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].performTextInput(TYPED_DESCRIPTION)
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].performTextInput(TYPED_ADDRESS)
        composeRule.runOnIdle { checkNotNull(map.firstScope().onSpotClick).invoke(selectedCoordinate) }
        composeRule.runOnIdle { map.firstScope().state.select(DiaryMapProvider.GOOGLE) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { map.firstScope().reportCamera(coordinate = viewedCoordinate, zoom = CHANGED_ZOOM) }
        composeRule.waitForIdle()
        val colorHex = composeRule.currentColorHex()

        moveToBackgroundAndReturn()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(TYPED_TITLE))
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].assert(hasText(TYPED_DESCRIPTION))
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].assert(hasText(TYPED_ADDRESS))
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].assert(hasText(selectedCoordinate.latitude.toCoordinateText()))
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].assert(hasText(selectedCoordinate.longitude.toCoordinateText()))
        composeRule.onNodeWithText(colorHex, substring = true).assertExists()
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
        composeRule.runOnIdle {
            map.firstScope().state.spot shouldBe selectedCoordinate
            map.firstScope().state.provider shouldBe DiaryMapProvider.GOOGLE
            map.firstScope().state.coordinate shouldBe viewedCoordinate
            map.firstScope().zoom shouldBe CHANGED_ZOOM
        }
    }

    @Test
    fun `TC-66fd3183-04ba-4a6f-807c-ceb5a7d9eea6 앱이 백그라운드에 다녀와도 열려 있던 장소 검색이 남는다`() {
        setPlaceAddScreen(tagViewModel = addTagScreenTestViewModel())
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(SEARCH_QUERY)
        composeRule.waitForIdle()

        moveToBackgroundAndReturn()

        composeRule.onNode(hasText(SEARCH_QUERY) and hasAnyAncestor(isDialog())).assertExists()
        composeRule.onNodeWithText(TYPED_TITLE).assertExists()
    }

    private fun moveToBackgroundAndReturn() {
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()
    }

    private fun setPlaceAddScreen(tagViewModel: EntityAddTagViewModel) {
        composeRule.setContent {
            ProvideTestDiaryMap(map = map) {
                PlaceAddScreenTestTheme {
                    PlaceAddScreen(
                        addedResultRequestKey = null,
                        navigateToTagAdd = {},
                        tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                        navigateUp = {},
                        initialCoordinate = null,
                        addViewModel = remember { screenTestViewModel(uiState = PlaceAddUiState(defaultProvider = MapProvider.NAVER)) },
                        searchViewModel = remember { searchScreenTestViewModel() },
                        navigateToTagDetail = {},
                        tagViewModel = tagViewModel,
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        private const val SELECTED_TAG_TITLE = "PlaceAddBackgroundTag"
        private const val SEARCH_QUERY = "PlaceAddBackgroundQuery"

        // 지도가 처음 정하는 확대 수준과 구분되도록 다른 값으로 알려 온다.
        private const val CHANGED_ZOOM = 18.0
    }
}
