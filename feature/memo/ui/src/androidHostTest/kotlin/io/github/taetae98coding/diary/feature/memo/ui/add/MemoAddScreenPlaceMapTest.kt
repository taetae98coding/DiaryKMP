package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.map.DiaryMapBounds
import io.github.taetae98coding.diary.compose.map.DiaryMapContentOverrideScope
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapPin
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.testing.location.coordinate
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetSelectedPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceUseCase
import io.github.taetae98coding.diary.feature.memo.ui.ProvideTestDiaryMap
import io.github.taetae98coding.diary.feature.memo.ui.TEST_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.TestDiaryMap
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_PLACE_PICKER_PLACE_ADD
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_PLACE_PICKER_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_PLACE_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.place.GOOGLE_PROVIDER_NAME
import io.github.taetae98coding.diary.feature.memo.ui.place.HOME_PLACE_QUERY
import io.github.taetae98coding.diary.feature.memo.ui.place.HOME_PLACE_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceMapUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlacePickerMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.OFFICE_PLACE_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.placeDialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.place.placeDialogSearchField
import io.github.taetae98coding.diary.feature.memo.ui.place.placePagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.testPlace
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.memo.ui.sendPlaceAddedResult
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAddScreenPlaceMapTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val map = TestDiaryMap()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-ee257126-2dd0-48fd-8c3b-99cfc84577cd 지도를 눌러도 지점이 만들어지지 않는다`() {
        setMemoAddScreen(placeList = emptyList())

        composeRule.runOnIdle {
            val cardMap = cardMap()
            cardMap.onSpotClick?.invoke(Arb.coordinate().next().toDiaryMapCoordinate())
        }

        composeRule.runOnIdle { cardMap().state.spot.shouldBeNull() }
    }

    @Test
    fun `TC-b4820f67-50fa-4e61-a61e-f86dabc63a79 연결된 장소가 좌표 컬러 제목의 지도 핀으로 지정된다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val officePlace = testPlace(title = OFFICE_PLACE_TITLE)
        setMemoAddScreen(placeList = listOf(homePlace, officePlace))

        composeRule.selectPlaces(HOME_PLACE_TITLE, OFFICE_PLACE_TITLE)

        composeRule.runOnIdle {
            cardMap().state.pins shouldContainExactlyInAnyOrder listOf(homePlace.expectedPin(), officePlace.expectedPin())
        }
    }

    @Test
    fun `TC-c192eec0-c936-4129-9cd3-47ff0b80dcda 노출하는 장소가 바뀌면 핀도 함께 바뀐다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val officePlace = testPlace(title = OFFICE_PLACE_TITLE)
        setMemoAddScreen(placeList = listOf(homePlace, officePlace), initialPlaceId = homePlace.id)
        composeRule.runOnIdle { cardMap().state.pins.map(DiaryMapPin::id) shouldBe listOf(homePlace.id) }

        // 새 장소를 선택하고 처음 장소를 해제한다.
        composeRule.openPlacePicker()
        composeRule.awaitDialogNode(OFFICE_PLACE_TITLE)
        composeRule.placeDialogNodeWithText(OFFICE_PLACE_TITLE).performClick()
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle { cardMap().state.pins.map(DiaryMapPin::id) shouldBe listOf(officePlace.id) }
    }

    @Test
    fun `TC-6f355b95-4860-4c00-8be2-a1dd51acf036 노출하는 장소가 없으면 핀도 없다`() {
        setMemoAddScreen(placeList = listOf(testPlace(title = HOME_PLACE_TITLE)))

        composeRule.runOnIdle { cardMap().state.pins shouldBe emptyList() }
    }

    @Test
    fun `TC-ec74d700-0bdf-485f-9ce2-3a9a727cde70 삭제된 장소는 핀에서도 사라진다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val placeStore = MutableStateFlow(listOf(homePlace))
        setMemoAddScreen(placeStore = placeStore, initialPlaceId = homePlace.id)
        composeRule.runOnIdle { cardMap().state.pins.map(DiaryMapPin::id) shouldBe listOf(homePlace.id) }

        placeStore.value = listOf(homePlace.copy(isDeleted = true))
        composeRule.waitForIdle()

        composeRule.runOnIdle { cardMap().state.pins shouldBe emptyList() }
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-9abc5d57-6f0f-4359-9e90-c0c44ca76380 장소를 선택하거나 해제해도 지도가 보고 있는 위치와 확대 수준이 바뀌지 않는다`() {
        val reportedCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        setMemoAddScreen(placeList = listOf(testPlace(title = HOME_PLACE_TITLE)))
        val initialState = composeRule.runOnIdle { cardMap().state }
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = reportedCoordinate, zoom = TEST_ZOOM) }
        composeRule.openPlacePicker()
        composeRule.awaitDialogNode(HOME_PLACE_TITLE)

        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle { cardMap().state.pins shouldHaveSize 1 }
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            cardMap().state shouldBeSameInstanceAs initialState
            initialState.coordinate shouldBe reportedCoordinate
            cardMap().zoom shouldBe TEST_ZOOM
        }
    }

    @Test
    fun `TC-c1675e90-9734-4d23-95ab-19d56294c722 지도 핀을 누르면 그 장소의 상세로 이동하고 지도와 선택은 그대로다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val officePlace = testPlace(title = OFFICE_PLACE_TITLE)
        val reportedCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        val detailIdList = mutableListOf<Uuid>()
        setMemoAddScreen(placeList = listOf(homePlace, officePlace), navigateToPlaceDetail = detailIdList::add)
        composeRule.selectPlaces(HOME_PLACE_TITLE, OFFICE_PLACE_TITLE)
        composeRule.onNodeWithText(GOOGLE_PROVIDER_NAME).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = reportedCoordinate, zoom = TEST_ZOOM) }

        composeRule.runOnIdle { cardMap().onPinClick.shouldNotBeNull().invoke(officePlace.id) }
        composeRule.waitForIdle()

        detailIdList shouldBe listOf(officePlace.id)
        composeRule.runOnIdle {
            cardMap().state.coordinate shouldBe reportedCoordinate
            cardMap().zoom shouldBe TEST_ZOOM
            cardMap().state.provider shouldBe DiaryMapProvider.GOOGLE
        }
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
        composeRule.onNodeWithText(OFFICE_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-eb15cb67-d877-49f5-ae9a-deaa368576bf 장소 상세에서 돌아와도 선택과 지도 상태가 유지된다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val reportedCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        var isShown by mutableStateOf(true)
        setMemoAddScreen(
            placeList = listOf(homePlace),
            initialPlaceId = homePlace.id,
            navigateToPlaceDetail = { isShown = false },
            isShownProvider = { isShown },
        )
        composeRule.onNodeWithText(GOOGLE_PROVIDER_NAME).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = reportedCoordinate, zoom = TEST_ZOOM) }

        composeRule.runOnIdle { cardMap().onPinClick.shouldNotBeNull().invoke(homePlace.id) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { map.scopes shouldBe emptyList() }
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
        composeRule.runOnIdle {
            cardMap().state.coordinate shouldBe reportedCoordinate
            cardMap().zoom shouldBe TEST_ZOOM
            cardMap().state.provider shouldBe DiaryMapProvider.GOOGLE
            cardMap().state.pins.map(DiaryMapPin::id) shouldBe listOf(homePlace.id)
        }
    }

    @Test
    fun `TC-a62214d2-abbf-4897-83b5-7d6b0ef91589 검색어는 지도 핀과 지도가 보고 있는 위치와 확대 수준을 바꾸지 않는다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val officePlace = testPlace(title = OFFICE_PLACE_TITLE)
        val reportedCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        setMemoAddScreen(placeList = listOf(homePlace, officePlace))
        composeRule.openPlacePicker()
        composeRule.awaitDialogNode(OFFICE_PLACE_TITLE)
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.placeDialogNodeWithText(OFFICE_PLACE_TITLE).performClick()
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = reportedCoordinate, zoom = TEST_ZOOM) }

        composeRule.placeDialogSearchField().performTextInput(HOME_PLACE_QUERY)
        composeRule.awaitDialogNodeGone(OFFICE_PLACE_TITLE)

        composeRule.runOnIdle {
            cardMap().state.pins.map(DiaryMapPin::id) shouldContainExactlyInAnyOrder listOf(homePlace.id, officePlace.id)
            cardMap().state.coordinate shouldBe reportedCoordinate
            cardMap().zoom shouldBe TEST_ZOOM
        }
    }

    @Test
    fun `TC-eaa7dbd3-15c0-49d1-a893-3e24af03f68e 카드의 추가 항목으로 이동하면 지도가 알려온 위치를 넘긴다`() {
        val coordinateList = mutableListOf<Coordinate?>()
        setMemoAddScreen(placeList = emptyList(), navigateToPlaceAdd = coordinateList::add)
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = SOUTH_WEST_CITY, zoom = TEST_ZOOM) }

        composeRule.openPlacePicker()

        coordinateList shouldBe listOf(SOUTH_WEST_CITY.toTestCoordinate())
    }

    @Test
    fun `TC-eaa7dbd3-15c0-49d1-a893-3e24af03f68e 목록 보기의 장소 추가 항목으로 이동하면 지도가 알려온 위치를 넘긴다`() {
        val coordinateList = mutableListOf<Coordinate?>()
        setMemoAddScreen(placeList = listOf(testPlace(title = HOME_PLACE_TITLE)), navigateToPlaceAdd = coordinateList::add)
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = SEOUL, zoom = TEST_ZOOM) }

        composeRule.openPlacePicker()
        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()

        coordinateList shouldBe listOf(SEOUL.toTestCoordinate())
    }

    @Test
    fun `TC-3e9b991b-4aa1-4bde-bb0a-106c54fe1250 추가한 장소가 자동 선택되어도 카드의 지도 위치와 확대 수준이 바뀌지 않는다`() {
        val addedPlace = testPlace(title = OFFICE_PLACE_TITLE)
        val reportedCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        val resultEventBus = ResultEventBus()
        setMemoAddScreen(placeList = listOf(addedPlace), resultEventBus = resultEventBus)
        val initialState = composeRule.runOnIdle { cardMap().state }
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = reportedCoordinate, zoom = TEST_ZOOM) }
        composeRule.openPlacePicker()
        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle { cardMap().state.pins shouldBe emptyList() }

        resultEventBus.sendPlaceAddedResult(id = addedPlace.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(OFFICE_PLACE_TITLE).assertExists()
        composeRule.runOnIdle {
            cardMap().state.pins.map(DiaryMapPin::id) shouldBe listOf(addedPlace.id)
            cardMap().state shouldBeSameInstanceAs initialState
            initialState.coordinate shouldBe reportedCoordinate
            cardMap().zoom shouldBe TEST_ZOOM
        }
    }

    @Test
    fun `TC-586e80db-a779-454c-a6cf-409873b5f84f 돌아온 뒤 다시 장소 추가로 이동하면 그때 보고 있던 위치를 새로 넘긴다`() {
        val addedPlace = testPlace(title = OFFICE_PLACE_TITLE)
        val firstCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        val movedCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        val coordinateList = mutableListOf<Coordinate?>()
        val resultEventBus = ResultEventBus()
        setMemoAddScreen(placeList = listOf(addedPlace), navigateToPlaceAdd = coordinateList::add, resultEventBus = resultEventBus)
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = firstCoordinate, zoom = TEST_ZOOM) }
        composeRule.openPlacePicker()
        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()
        resultEventBus.sendPlaceAddedResult(id = addedPlace.id)
        composeRule.waitForIdle()
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = movedCoordinate, zoom = TEST_ZOOM) }

        composeRule.openPlacePicker()
        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()

        coordinateList shouldBe listOf(firstCoordinate.toTestCoordinate(), movedCoordinate.toTestCoordinate())
    }

    @Test
    fun `TC-ca9a2c3d-6156-4d6f-9fa2-17b71a953a42 지도 보기에서 장소 추가로 이동하면 지도 보기의 지도 위치를 넘긴다`() {
        val cardCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        val pickerCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        val coordinateList = mutableListOf<Coordinate?>()
        setMemoAddScreen(placeList = listOf(testPlace(title = HOME_PLACE_TITLE)), navigateToPlaceAdd = coordinateList::add)
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = cardCoordinate, zoom = TEST_ZOOM) }
        composeRule.openPlacePicker()
        composeRule.showMapView()
        composeRule.runOnIdle { pickerMap().reportCamera(coordinate = pickerCoordinate, zoom = TEST_ZOOM, bounds = VISIBLE_BOUNDS) }

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()

        coordinateList shouldBe listOf(pickerCoordinate.toTestCoordinate())
    }

    @Test
    fun `TC-6f7e7eb7-7b0f-4556-9d47-24e0d4fe9605 지도 보기에서 지도를 옮긴 뒤 목록 보기로 돌아와 이동하면 카드의 지도 위치를 넘긴다`() {
        val cardCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        val pickerCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        val coordinateList = mutableListOf<Coordinate?>()
        setMemoAddScreen(placeList = listOf(testPlace(title = HOME_PLACE_TITLE)), navigateToPlaceAdd = coordinateList::add)
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = cardCoordinate, zoom = TEST_ZOOM) }
        composeRule.openPlacePicker()
        composeRule.showMapView()
        composeRule.runOnIdle { pickerMap().reportCamera(coordinate = pickerCoordinate, zoom = TEST_ZOOM, bounds = VISIBLE_BOUNDS) }
        composeRule.showListView()

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()

        coordinateList shouldBe listOf(cardCoordinate.toTestCoordinate())
    }

    @Test
    fun `TC-8b1b6279-417e-4065-a933-a1adbd5525ee 지도 보기에서 장소 추가 항목을 누르면 목록이 닫히고 PlaceAdd 이동이 요청된다`() {
        var placeAddCount = 0
        setMemoAddScreen(placeList = listOf(testPlace(title = HOME_PLACE_TITLE)), navigateToPlaceAdd = { placeAddCount += 1 })
        composeRule.openPlacePicker()
        composeRule.showMapView()

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()

        placeAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PLACE_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-bea460d9-34ea-4387-9078-2ecd28186acc 지도 보기로 바꾸면 검색어 입력이 사라진다`() {
        setMemoAddScreen(placeList = listOf(testPlace(title = HOME_PLACE_TITLE)))
        composeRule.openPlacePicker()
        composeRule.placeDialogSearchField().assertExists()

        composeRule.showMapView()

        composeRule.placeDialogSearchField().assertDoesNotExist()
    }

    @Test
    fun `TC-e05705cc-ec14-4dc5-9376-c051a718ede3 지도 보기에 다녀와도 입력하던 검색어가 유지된다`() {
        setMemoAddScreen(placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE)))
        composeRule.openPlacePicker()
        composeRule.awaitDialogNode(OFFICE_PLACE_TITLE)
        composeRule.placeDialogSearchField().performTextInput(HOME_PLACE_QUERY)
        composeRule.awaitDialogNodeGone(OFFICE_PLACE_TITLE)

        composeRule.showMapView()
        composeRule.showListView()

        composeRule.placeDialogSearchField().assert(hasText(HOME_PLACE_QUERY))
        composeRule.awaitDialogNode(HOME_PLACE_TITLE)
        composeRule.placeDialogNodeWithText(OFFICE_PLACE_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-4d1f35eb-cebf-4eb5-8817-37694d310243 검색어는 지도 보기의 목록을 좁히지 않는다`() {
        setMemoAddScreen(placeList = listOf(testPlace(title = CAFE_PLACE_TITLE, coordinate = INSIDE_COORDINATE), testPlace(title = BOOKSTORE_PLACE_TITLE, coordinate = INSIDE_COORDINATE)))
        composeRule.openPlacePicker()
        composeRule.awaitDialogNode(BOOKSTORE_PLACE_TITLE)
        composeRule.placeDialogSearchField().performTextInput(CAFE_PLACE_TITLE)
        composeRule.awaitDialogNodeGone(BOOKSTORE_PLACE_TITLE)

        composeRule.showMapView()
        composeRule.runOnIdle { pickerMap().reportCamera(coordinate = INSIDE_COORDINATE.toDiaryMapCoordinate(), zoom = TEST_ZOOM, bounds = VISIBLE_BOUNDS) }

        composeRule.awaitDialogNode(BOOKSTORE_PLACE_TITLE)
        composeRule.placeDialogNodeWithText(CAFE_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-d2630269-a8cd-4514-b3ee-8a3523bc5d6a 지도 보기의 핀은 목록과 같은 장소를 장소의 컬러와 제목으로 표시한다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE, coordinate = INSIDE_COORDINATE)
        val officePlace = testPlace(title = OFFICE_PLACE_TITLE, coordinate = INSIDE_COORDINATE)
        setMemoAddScreen(placeList = listOf(homePlace, officePlace, testPlace(title = OUTSIDE_PLACE_TITLE, coordinate = OUTSIDE_COORDINATE)))
        composeRule.openPlacePicker()
        composeRule.showMapView()

        composeRule.runOnIdle { pickerMap().reportCamera(coordinate = INSIDE_COORDINATE.toDiaryMapCoordinate(), zoom = TEST_ZOOM, bounds = VISIBLE_BOUNDS) }
        composeRule.awaitDialogNode(OFFICE_PLACE_TITLE)

        composeRule.runOnIdle {
            pickerMap().state.pins shouldContainExactlyInAnyOrder listOf(homePlace.expectedPin(), officePlace.expectedPin())
        }
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertExists()
        composeRule.placeDialogNodeWithText(OUTSIDE_PLACE_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-391f9731-8b80-41cc-901e-7bf7af807ec4 지도 보기의 핀을 누르면 그 장소의 선택 여부가 바뀐다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE, coordinate = INSIDE_COORDINATE)
        val pickerCoordinate = INSIDE_COORDINATE.toDiaryMapCoordinate()
        val detailIdList = mutableListOf<Uuid>()
        setMemoAddScreen(placeList = listOf(homePlace), navigateToPlaceDetail = detailIdList::add)
        composeRule.openPlacePicker()
        composeRule.showMapView()
        composeRule.runOnIdle { pickerMap().reportCamera(coordinate = pickerCoordinate, zoom = TEST_ZOOM, bounds = VISIBLE_BOUNDS) }
        composeRule.awaitDialogNode(HOME_PLACE_TITLE)

        composeRule.runOnIdle { pickerMap().onPinClick.shouldNotBeNull().invoke(homePlace.id) }
        composeRule.waitForIdle()

        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertIsOn()
        composeRule.runOnIdle {
            pickerMap().state.pins.map(DiaryMapPin::id) shouldBe listOf(homePlace.id)
            pickerMap().state.coordinate shouldBe pickerCoordinate
        }
        detailIdList shouldBe emptyList()
    }

    @Test
    fun `TC-48e1e605-3ba0-47b8-abb4-3828a8cbf7a3 지도 보기 목록의 장소를 눌러도 지도가 옮겨지지 않고 선택 여부만 바뀐다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE, coordinate = INSIDE_COORDINATE)
        val pickerCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        val detailIdList = mutableListOf<Uuid>()
        setMemoAddScreen(placeList = listOf(homePlace), navigateToPlaceDetail = detailIdList::add)
        composeRule.openPlacePicker()
        composeRule.showMapView()
        composeRule.runOnIdle { pickerMap().reportCamera(coordinate = pickerCoordinate, zoom = TEST_ZOOM, bounds = VISIBLE_BOUNDS) }
        composeRule.awaitDialogNode(HOME_PLACE_TITLE)

        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertIsOn()
        composeRule.runOnIdle {
            pickerMap().state.coordinate shouldBe pickerCoordinate
            pickerMap().zoom shouldBe TEST_ZOOM
            pickerMap().state.pins.map(DiaryMapPin::id) shouldBe listOf(homePlace.id)
        }
        detailIdList shouldBe emptyList()
    }

    @Test
    fun `TC-49f993ee-983e-4813-a437-dbac9202b007 지도 보기의 지도에서 핀이 없는 곳을 누르면 아무 일도 일어나지 않는다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE, coordinate = INSIDE_COORDINATE)
        val pickerCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        val detailIdList = mutableListOf<Uuid>()
        setMemoAddScreen(placeList = listOf(homePlace), navigateToPlaceDetail = detailIdList::add)
        composeRule.openPlacePicker()
        composeRule.showMapView()
        composeRule.runOnIdle { pickerMap().reportCamera(coordinate = pickerCoordinate, zoom = TEST_ZOOM, bounds = VISIBLE_BOUNDS) }
        composeRule.awaitDialogNode(HOME_PLACE_TITLE)

        composeRule.runOnIdle { pickerMap().onSpotClick?.invoke(Arb.coordinate().next().toDiaryMapCoordinate()) }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            pickerMap().state.spot.shouldBeNull()
            pickerMap().state.coordinate shouldBe pickerCoordinate
            pickerMap().state.pins.map(DiaryMapPin::id) shouldBe listOf(homePlace.id)
        }
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertIsOff()
        detailIdList shouldBe emptyList()
    }

    @Test
    fun `TC-57e8c50e-d99a-4372-ad46-54f2c218e967 검색어를 입력하면 확정 동작 없이 검색어를 만족하는 장소만 목록에 남는다`() {
        setMemoAddScreen(placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE)))
        composeRule.openPlacePicker()
        composeRule.awaitDialogNode(OFFICE_PLACE_TITLE)

        composeRule.placeDialogSearchField().performTextInput(HOME_PLACE_QUERY)
        composeRule.awaitDialogNodeGone(OFFICE_PLACE_TITLE)

        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-b826a68e-6f4e-43f0-964d-5e51a2aad49b 검색어를 지우면 대상 장소가 모두 다시 나타난다`() {
        setMemoAddScreen(placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE)))
        composeRule.openPlacePicker()
        composeRule.awaitDialogNode(OFFICE_PLACE_TITLE)
        composeRule.placeDialogSearchField().performTextInput(HOME_PLACE_QUERY)
        composeRule.awaitDialogNodeGone(OFFICE_PLACE_TITLE)

        composeRule.placeDialogSearchField().performTextClearance()
        composeRule.awaitDialogNode(OFFICE_PLACE_TITLE)

        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-e8eae743-7a34-442f-8258-b9d5166a64a1 선택한 장소의 제목 컬러 좌표가 바뀌면 카드의 지도 핀에 반영된다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE, coordinate = OUTSIDE_COORDINATE)
        val reportedCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        val placeStore = MutableStateFlow(listOf(homePlace))
        setMemoAddScreen(placeStore = placeStore, initialPlaceId = homePlace.id)
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = reportedCoordinate, zoom = TEST_ZOOM) }
        composeRule.runOnIdle { cardMap().state.pins shouldBe listOf(homePlace.expectedPin()) }

        listOf(
            homePlace.copy(detail = homePlace.detail.copy(title = OFFICE_PLACE_TITLE)),
            homePlace.copy(detail = homePlace.detail.copy(color = homePlace.detail.color + 1)),
            homePlace.copy(detail = homePlace.detail.copy(coordinate = INSIDE_COORDINATE)),
        ).forEach { changedPlace ->
            placeStore.value = listOf(changedPlace)
            composeRule.waitForIdle()

            composeRule.runOnIdle {
                cardMap().state.pins shouldBe listOf(changedPlace.expectedPin())
                cardMap().state.coordinate shouldBe reportedCoordinate
                cardMap().zoom shouldBe TEST_ZOOM
            }
        }
    }

    @Test
    fun `TC-4ddfb50b-016b-4ef4-9323-70948299a75d 영역 밖으로 나간 장소의 선택은 유지된다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE, coordinate = INSIDE_COORDINATE)
        setMemoAddScreen(placeList = listOf(homePlace), initialPlaceId = homePlace.id)
        composeRule.openPlacePicker()
        composeRule.showMapView()
        composeRule.runOnIdle { pickerMap().reportCamera(coordinate = INSIDE_COORDINATE.toDiaryMapCoordinate(), zoom = TEST_ZOOM, bounds = VISIBLE_BOUNDS) }
        composeRule.awaitDialogNode(HOME_PLACE_TITLE)

        composeRule.runOnIdle { pickerMap().reportCamera(coordinate = OUTSIDE_COORDINATE.toDiaryMapCoordinate(), zoom = TEST_ZOOM, bounds = OUTSIDE_BOUNDS) }
        composeRule.awaitDialogNodeGone(HOME_PLACE_TITLE)

        composeRule.onNode(hasText(HOME_PLACE_TITLE) and !hasAnyAncestor(isDialog())).assertExists()
    }

    @Test
    fun `TC-d223d43e-a506-4214-8b18-6b75fcb23907 목록 보기에 다녀와도 지도 보기의 지도는 보던 위치와 제공자를 이어서 보여 준다`() {
        val pickerCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        setMemoAddScreen(placeList = listOf(testPlace(title = HOME_PLACE_TITLE)))
        composeRule.openPlacePicker()
        composeRule.showMapView()
        composeRule.runOnIdle { pickerMap().reportCamera(coordinate = pickerCoordinate, zoom = TEST_ZOOM, bounds = VISIBLE_BOUNDS) }
        composeRule.onNode(hasText(GOOGLE_PROVIDER_NAME) and hasAnyAncestor(isDialog())).performClick()
        composeRule.waitForIdle()

        composeRule.showListView()
        composeRule.showMapView()

        composeRule.runOnIdle {
            pickerMap().state.coordinate shouldBe pickerCoordinate
            pickerMap().zoom shouldBe TEST_ZOOM
            pickerMap().state.provider shouldBe DiaryMapProvider.GOOGLE
        }
    }

    @Test
    fun `TC-28ac4c7d-eebe-44f8-8ea1-b2fb84661c8d 지도 보기의 지도는 카드의 위치에서 초기 확대 수준으로 시작한다`() {
        val cardCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        setMemoAddScreen(placeList = listOf(testPlace(title = HOME_PLACE_TITLE)))
        val initialZoom = composeRule.runOnIdle { cardMap().zoom.shouldNotBeNull() }
        val cardZoom = initialZoom - ZOOM_DIFFERENCE
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = cardCoordinate, zoom = cardZoom) }
        composeRule.openPlacePicker()

        composeRule.showMapView()

        composeRule.runOnIdle {
            pickerMap().state.coordinate shouldBe cardCoordinate
            pickerMap().zoom shouldBe initialZoom
            cardMap().zoom shouldBe cardZoom
        }
    }

    @Test
    fun `TC-2eedbcd6-775d-48b4-a131-9a376c7a0e74 장소 추가로 이동할 때 보고 있던 지도 제공자는 넘기지 않는다`() {
        val reportedCoordinate = Arb.coordinate().next().toDiaryMapCoordinate()
        val coordinateList = mutableListOf<Coordinate?>()
        setMemoAddScreen(placeList = emptyList(), navigateToPlaceAdd = coordinateList::add)
        composeRule.onNodeWithText(GOOGLE_PROVIDER_NAME).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.runOnIdle { cardMap().reportCamera(coordinate = reportedCoordinate, zoom = TEST_ZOOM) }
        composeRule.runOnIdle { cardMap().state.provider shouldBe DiaryMapProvider.GOOGLE }

        composeRule.openPlacePicker()

        // 이동 요청에는 초기 지도 위치만 담기고 제공자를 담을 자리가 없다.
        coordinateList shouldBe listOf(reportedCoordinate.toTestCoordinate())
    }

    private fun cardMap(): DiaryMapContentOverrideScope = map.scopes.first()

    private fun pickerMap(): DiaryMapContentOverrideScope {
        map.scopes shouldHaveSize 2
        return map.scopes.last()
    }

    private fun setMemoAddScreen(
        placeList: List<Place> = emptyList(),
        placeStore: MutableStateFlow<List<Place>> = MutableStateFlow(placeList),
        initialPlaceId: Uuid? = null,
        navigateToPlaceAdd: (Coordinate?) -> Unit = {},
        navigateToPlaceDetail: (Uuid) -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
        isShownProvider: () -> Boolean = { true },
    ) {
        val viewModels = screenTestRealViewModel().copy(placeViewModel = placeViewModel(placeStore = placeStore, initialPlaceId = initialPlaceId))
        val placeMapViewModel =
            screenTestPlaceMapViewModel(
                uiState = MutableStateFlow(MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = true, currentCoordinate = Arb.coordinate().next())),
            )
        val placePickerMapViewModel = placePickerMapViewModel(placeStore = placeStore)

        composeRule.setContent {
            val saveableStateHolder = rememberSaveableStateHolder()

            ProvideTestDiaryMap(map = map) {
                MemoAddScreenTestTheme(resultEventBus = resultEventBus) {
                    if (isShownProvider()) {
                        saveableStateHolder.SaveableStateProvider(key = SCREEN_KEY) {
                            MemoAddScreen(
                                addRequestKey = TEST_ADD_REQUEST_KEY,
                                addViewModel = viewModels.viewModel,
                                tagViewModel = viewModels.tagViewModel,
                                webViewModel = viewModels.webViewModel,
                                contactViewModel = viewModels.contactViewModel,
                                placeViewModel = viewModels.placeViewModel,
                                placeMapViewModel = placeMapViewModel,
                                placePickerMapViewModel = placePickerMapViewModel,
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
                                navigateToGeminiSetting = {},
                                initialDateTime = null,
                                componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                                isStandalone = true,
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun ComposeContentTestRule.openPlacePicker() {
        onNodeWithText(DEFAULT_PLACE_SELECT_LABEL).performSemanticsAction(SemanticsActions.OnClick)
        waitForIdle()
    }

    private fun ComposeContentTestRule.selectPlaces(vararg titleList: String) {
        openPlacePicker()
        titleList.forEach { title ->
            awaitDialogNode(title)
            placeDialogNodeWithText(title).performClick()
        }
        closeDialogByBack()
    }

    private fun ComposeContentTestRule.showMapView() {
        onNode(hasContentDescription(DEFAULT_SHOW_MAP_ACTION) and hasAnyAncestor(isDialog())).performClick()
        waitForIdle()
    }

    private fun ComposeContentTestRule.showListView() {
        onNode(hasContentDescription(DEFAULT_SHOW_LIST_ACTION) and hasAnyAncestor(isDialog())).performClick()
        waitForIdle()
    }

    private fun ComposeContentTestRule.awaitDialogNode(title: String) {
        awaitCondition(description = "$title 표시") { onAllNodes(hasDialogText(title)).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun ComposeContentTestRule.awaitDialogNodeGone(title: String) {
        awaitCondition(description = "$title 사라짐") { onAllNodes(hasDialogText(title)).fetchSemanticsNodes().isEmpty() }
    }

    /**
     * 목록 조회와 검색어 반영은 ViewModel의 코루틴이 실제 시간으로 진행하므로 프레임과 실제 시간을 함께 진행시키며 기다린다.
     */
    private fun ComposeContentTestRule.awaitCondition(
        description: String,
        condition: () -> Boolean,
    ) {
        repeat(WAIT_ATTEMPT_COUNT) {
            waitForIdle()
            if (condition()) return
            mainClock.advanceTimeByFrame()
            @Suppress("ForbiddenMethodCall")
            Thread.sleep(WAIT_INTERVAL_MILLIS)
        }

        error("$description 을 기다리지 못했다")
    }

    private fun hasDialogText(text: String): SemanticsMatcher = hasText(text) and hasAnyAncestor(isDialog())

    // 장소 조회는 저장소와 같게 삭제된 장소를 돌려주지 않는다.
    private fun placeViewModel(
        placeStore: MutableStateFlow<List<Place>>,
        initialPlaceId: Uuid?,
    ): MemoAddPlaceViewModel {
        val pagePlaceUseCase = mockk<PagePlaceUseCase>()
        every { pagePlaceUseCase(parameter = any()) } answers {
            val query = firstArg<String>().trim()
            placeStore.map { list -> Result.success(placePagingDataOf(list.filter { place -> !place.isDeleted && place.detail.title.contains(query, ignoreCase = true) })) }
        }

        val getSelectedPlaceUseCase = mockk<GetSelectedPlaceUseCase>()
        every { getSelectedPlaceUseCase(parameter = any()) } answers {
            val idSet = firstArg<Set<Uuid>>()
            placeStore.map { list -> Result.success(list.filter { place -> !place.isDeleted && place.id in idSet }) }
        }

        return MemoAddPlaceViewModel(
            initialPlaceId = initialPlaceId,
            pagePlaceUseCase = pagePlaceUseCase,
            getSelectedPlaceUseCase = getSelectedPlaceUseCase,
        )
    }

    private fun placePickerMapViewModel(placeStore: MutableStateFlow<List<Place>>): MemoPlacePickerMapViewModel {
        val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
        every { getPlaceListUseCase(parameter = any()) } answers {
            val bounds = firstArg<GetPlaceListUseCase.Parameter>().bounds
            placeStore.map { list -> Result.success(list.filter { place -> !place.isDeleted && bounds.contains(place.detail.coordinate) }) }
        }

        return MemoPlacePickerMapViewModel(getPlaceListUseCase = getPlaceListUseCase)
    }

    private companion object {
        const val SCREEN_KEY = "MemoAdd"
        const val TEST_ZOOM = 12.0
        const val ZOOM_DIFFERENCE = 3.0
        const val DEFAULT_SHOW_MAP_ACTION = "Show map"
        const val DEFAULT_SHOW_LIST_ACTION = "Show list"
        const val CAFE_PLACE_TITLE = "카페"
        const val BOOKSTORE_PLACE_TITLE = "서점"
        const val OUTSIDE_PLACE_TITLE = "MemoPlaceOutside"
        const val WAIT_ATTEMPT_COUNT: Int = 500
        const val WAIT_INTERVAL_MILLIS: Long = 10

        val SOUTH_WEST_CITY = DiaryMapCoordinate(latitude = -34.6037, longitude = -58.3816)
        val SEOUL = DiaryMapCoordinate(latitude = 37.5665, longitude = 126.9780)
        val INSIDE_COORDINATE = Coordinate(latitude = 37.5, longitude = 126.5)
        val OUTSIDE_COORDINATE = Coordinate(latitude = 35.1, longitude = 129.0)
        val VISIBLE_BOUNDS = DiaryMapBounds(south = 37.0, north = 38.0, west = 126.0, east = 127.0)
        val OUTSIDE_BOUNDS = DiaryMapBounds(south = 35.0, north = 36.0, west = 128.5, east = 129.5)

        fun DiaryMapCoordinate.toTestCoordinate(): Coordinate = Coordinate(latitude = latitude, longitude = longitude)

        fun Coordinate.toDiaryMapCoordinate(): DiaryMapCoordinate = DiaryMapCoordinate(latitude = latitude, longitude = longitude)

        fun CoordinateBounds.contains(coordinate: Coordinate): Boolean = coordinate.latitude in south..north && coordinate.longitude in west..east

        fun Place.expectedPin(): DiaryMapPin =
            DiaryMapPin(
                id = id,
                coordinate = DiaryMapCoordinate(latitude = detail.coordinate.latitude, longitude = detail.coordinate.longitude),
                color = detail.color.toColor(),
                label = detail.title,
            )
    }
}
