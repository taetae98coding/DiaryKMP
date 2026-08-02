package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagPlace
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailPlaceScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-006 대상 태그의 상태와 무관하게 장소 추가를 시작할 수 있다`() {
        val uiStateFlow = MutableStateFlow<TagDetailUiState>(tagDetailUiState(detail = tagDetail(TAG_TITLE)))
        composeRule.setTagDetailScreen(viewModel = screenTestViewModel(uiStateFlow))
        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        listOf(
            tagDetailUiState(detail = tagDetail(TAG_TITLE)),
            tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = true),
            TagDetailUiState.Loading,
        ).forEach { uiState ->
            composeRule.runOnIdle { uiStateFlow.value = uiState }
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-007 장소 추가를 선택하면 PlaceAdd 이동을 요청한다`() {
        var navigateCount = 0
        setScreenOnPlaceTab(navigateToPlaceAdd = { navigateCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-DETAIL-PLACE-FEATURE-007 한국어 환경 장소 추가 버튼의 접근성 이름은 장소 추가다`() {
        setScreenOnPlaceTab(tabDescription = KOREAN_PLACE_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-012 대상 태그를 조회하지 못해도 빈 상태 안내를 표시한다`() {
        setScreenOnPlaceTab(uiState = TagDetailUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-013 빈 상태에서도 장소 추가를 실행할 수 있다`() {
        var navigateCount = 0
        setScreenOnPlaceTab(navigateToPlaceAdd = { navigateCount += 1 })
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-014 장소 추가는 장소 탭에서만 제공된다`() {
        setScreen()

        listOf(
            DEFAULT_DETAIL_TAB_DESCRIPTION,
            DEFAULT_MEMO_TAB_DESCRIPTION,
            DEFAULT_WEB_TAB_DESCRIPTION,
        ).forEach { tabDescription ->
            composeRule.selectTagDetailTab(tabDescription)

            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        }

        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-015 TC-TAG-DETAIL-PLACE-DATA-005 대상 태그를 조회하지 못해도 장소 목록과 동작을 제공한다`() {
        setScreenOnPlaceTab(
            uiState = TagDetailUiState.Loading,
            placePagingData = tagEntityPagingData(itemList = listOf(tagPlace(title = PLACE_TITLE))),
        )

        composeRule.onNodeWithText(PLACE_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-019 목록 모드에서 장소 추가를 선택하면 지도 위치 없이 이동을 요청한다`() {
        val coordinateList = mutableListOf<Coordinate?>()
        setScreenOnPlaceTab(navigateToPlaceAdd = coordinateList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        coordinateList shouldBe listOf(null)
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-005 장소을 선택하면 그 장소의 상세 이동을 요청한다`() {
        val place = tagPlace(title = PLACE_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setScreenOnPlaceTab(
            placePagingData = tagEntityPagingData(itemList = listOf(place)),
            navigateToPlaceDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(PLACE_TITLE).performClick()

        navigatedIdList shouldBe listOf(place.id)
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-006 다른 탭에 다녀와도 바꿔 둔 보기 모드를 유지한다`() {
        setScreenOnPlaceTab(placePagingData = tagEntityPagingData(itemList = listOf(tagPlace(title = PLACE_TITLE))))
        composeRule.onNodeWithText(PLACE_TITLE).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        composeRule.onNodeWithText(PLACE_TITLE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_DESCRIPTION).assert(hasClickAction())
    }

    private fun setScreenOnPlaceTab(
        uiState: TagDetailUiState = tagDetailUiState(detail = tagDetail(TAG_TITLE)),
        placePagingData: PagingData<Place> = tagEntityPagingData(itemList = emptyList()),
        navigateToPlaceAdd: (Coordinate?) -> Unit = {},
        navigateToPlaceDetail: (Uuid) -> Unit = {},
        tabDescription: String = DEFAULT_PLACE_TAB_DESCRIPTION,
    ) {
        setScreen(
            uiState = uiState,
            placePagingData = placePagingData,
            navigateToPlaceAdd = navigateToPlaceAdd,
            navigateToPlaceDetail = navigateToPlaceDetail,
        )
        composeRule.selectTagDetailTab(tabDescription)
    }

    private fun setScreen(
        uiState: TagDetailUiState = tagDetailUiState(detail = tagDetail(TAG_TITLE)),
        placePagingData: PagingData<Place> = tagEntityPagingData(itemList = emptyList()),
        navigateToPlaceAdd: (Coordinate?) -> Unit = {},
        navigateToPlaceDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(uiState)),
            placePagingData = placePagingData,
            navigateToPlaceAdd = navigateToPlaceAdd,
            navigateToPlaceDetail = navigateToPlaceDetail,
        )
    }

    private companion object {
        const val PLACE_TITLE = "TagDetailScreenPlace"
        const val DEFAULT_EMPTY_TITLE = "No places linked to this tag"
        const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add place"
        const val KOREAN_ADD_BUTTON_DESCRIPTION = "장소 추가"
    }
}
