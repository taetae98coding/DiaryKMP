package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.kotest.matchers.shouldBe
import io.mockk.every
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
class PlaceDetailScreenUpdateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-006 수정을 실행하면 입력한 내용으로 수정을 요청한다`() {
        val detail = placeDetail()
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(content(detail = detail)))

        composeRule.setPlaceDetailScreen(viewModel = viewModel)

        composeRule.input(DESCRIPTION_INDEX).performTextReplacement(CHANGED_DESCRIPTION)
        composeRule.triggerUpdate()

        verify(exactly = 1) {
            viewModel.update(detail = detail.copy(description = CHANGED_DESCRIPTION))
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-007 수정에 성공하면 화면 제목을 새 제목으로 갱신한다`() {
        val detail = placeDetail()
        val id = Uuid.random()
        val uiState = MutableStateFlow(content(id = id, detail = detail))
        val viewModel = screenTestViewModel(uiState = uiState)
        every { viewModel.update(any()) } answers {
            uiState.value = content(id = id, detail = detail.copy(title = CHANGED_TITLE))
        }

        composeRule.setPlaceDetailScreen(viewModel = viewModel)

        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.triggerUpdate()

        composeRule
            .onAllNodes(hasText(CHANGED_TITLE))
            .fetchSemanticsNodes()
            .size shouldBe TITLE_DISPLAY_COUNT
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-009 제목을 비운 채 수정하면 비운 제목으로 요청하고 화면 제목은 기존 제목으로 남는다`() {
        assertBlankTitleUpdate(blankTitle = "")
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-009 제목을 공백만 남긴 채 수정하면 공백 제목으로 요청하고 화면 제목은 기존 제목으로 남는다`() {
        assertBlankTitleUpdate(blankTitle = BLANK_TITLE)
    }

    // 기존 제목을 쓰는 판단은 수정 규칙이 하므로(TC-PLACE-DETAIL-DOMAIN-005), 화면은 입력한 값을 그대로 넘기고 저장 결과를 표시한다.
    private fun assertBlankTitleUpdate(blankTitle: String) {
        val detail = placeDetail()
        val id = Uuid.random()
        val uiState = MutableStateFlow(content(id = id, detail = detail))
        val viewModel = screenTestViewModel(uiState = uiState)
        every { viewModel.update(any()) } answers {
            uiState.value = content(id = id, detail = detail.copy(description = CHANGED_DESCRIPTION, address = CHANGED_ADDRESS))
        }

        composeRule.setPlaceDetailScreen(viewModel = viewModel)

        composeRule.input(TITLE_INDEX).performTextReplacement(blankTitle)
        composeRule.input(DESCRIPTION_INDEX).performTextReplacement(CHANGED_DESCRIPTION)
        composeRule.input(ADDRESS_INDEX).performTextReplacement(CHANGED_ADDRESS)
        composeRule.triggerUpdate()

        verify(exactly = 1) {
            viewModel.update(detail = detail.copy(title = blankTitle, description = CHANGED_DESCRIPTION, address = CHANGED_ADDRESS))
        }
        composeRule
            .onAllNodes(hasText(detail.title))
            .fetchSemanticsNodes()
            .size shouldBe 1
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-010 좌표를 비운 채 수정을 실행하면 성립하지 않는 좌표로 요청한다`() {
        val detail = placeDetail()
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(content(detail = detail)))

        composeRule.setPlaceDetailScreen(viewModel = viewModel)

        composeRule.input(LATITUDE_INDEX).performTextClearance()
        composeRule.triggerUpdate()

        verify(exactly = 1) {
            viewModel.update(
                detail =
                    detail.copy(
                        coordinate = Coordinate(latitude = Double.NaN, longitude = detail.coordinate.longitude),
                    ),
            )
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-018 지도가 표시되지 않아도 좌표를 직접 입력해 수정한다`() {
        val detail = placeDetail()
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(content(detail = detail)))

        composeRule.setPlaceDetailScreen(viewModel = viewModel)

        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_MAP_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0

        composeRule.input(LATITUDE_INDEX).performTextReplacement(CHANGED_LATITUDE)
        composeRule.input(LONGITUDE_INDEX).performTextReplacement(CHANGED_LONGITUDE)
        composeRule.triggerUpdate()

        verify(exactly = 1) {
            viewModel.update(
                detail =
                    detail.copy(
                        coordinate =
                            Coordinate(
                                latitude = CHANGED_LATITUDE.toDouble(),
                                longitude = CHANGED_LONGITUDE.toDouble(),
                            ),
                    ),
            )
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-012 삭제 버튼을 누르면 삭제를 한 번 요청한다`() {
        val viewModel = screenTestViewModel()

        composeRule.setPlaceDetailScreen(viewModel = viewModel)

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.delete() }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-014 저장된 내용이 바뀌면 화면 제목만 갱신하고 수정 중인 내용은 유지한다`() {
        val detail = placeDetail()
        val id = Uuid.random()
        val uiState = MutableStateFlow(content(id = id, detail = detail))

        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel(uiState = uiState))

        composeRule.input(DESCRIPTION_INDEX).performTextReplacement(CHANGED_DESCRIPTION)
        composeRule.waitForIdle()

        uiState.value = content(id = id, detail = detail.copy(title = CHANGED_TITLE))
        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasText(CHANGED_TITLE))
            .fetchSemanticsNodes()
            .size shouldBe 1
        composeRule.input(TITLE_INDEX).assert(hasText(detail.title))
        composeRule.input(DESCRIPTION_INDEX).assert(hasText(CHANGED_DESCRIPTION))
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-054 수정 저장에 실패하면 안내 없이 입력을 유지하고 수정 반영을 다시 실행할 수 있다`() {
        val detail = placeDetail()
        val id = Uuid.random()
        val uiState = MutableStateFlow(content(id = id, detail = detail))
        val viewModel = screenTestViewModel(uiState = uiState)
        every { viewModel.update(any()) } answers {
            uiState.value = content(id = id, detail = detail, isUpdateInProgress = true)
            uiState.value = content(id = id, detail = detail, isUpdateInProgress = false)
        }
        var navigateUpCount = 0
        composeRule.setPlaceDetailScreen(viewModel = viewModel, navigateUp = { navigateUpCount++ })

        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.input(LATITUDE_INDEX).performTextReplacement(CHANGED_LATITUDE)
        composeRule.input(LONGITUDE_INDEX).performTextReplacement(CHANGED_LONGITUDE)
        composeRule.triggerUpdate()

        navigateUpCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertDoesNotExist()
        composeRule.input(TITLE_INDEX).assert(hasText(CHANGED_TITLE))
        composeRule.input(LATITUDE_INDEX).assert(hasText(CHANGED_LATITUDE))
        composeRule.input(LONGITUDE_INDEX).assert(hasText(CHANGED_LONGITUDE))
        composeRule
            .onAllNodes(hasText(detail.title))
            .fetchSemanticsNodes()
            .size shouldBe 1

        composeRule.triggerUpdate()

        val changedDetail =
            detail.copy(
                title = CHANGED_TITLE,
                coordinate = Coordinate(latitude = CHANGED_LATITUDE.toDouble(), longitude = CHANGED_LONGITUDE.toDouble()),
            )
        verify(exactly = 2) { viewModel.update(detail = changedDetail) }
    }

    private fun ComposeContentTestRule.triggerUpdate() {
        waitForIdle()
        onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).performClick()
        waitForIdle()
    }

    private companion object {
        private const val BLANK_TITLE = "   "
    }
}
