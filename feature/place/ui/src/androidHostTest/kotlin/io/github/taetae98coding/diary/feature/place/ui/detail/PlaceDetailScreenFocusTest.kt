package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.mockk.every
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailScreenFocusTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-030 진입해도 어느 입력에도 초점을 두지 않는다`() {
        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule.input(TITLE_INDEX).assertIsNotFocused()
        composeRule.input(DESCRIPTION_INDEX).assertIsNotFocused()
        composeRule.input(ADDRESS_INDEX).assertIsNotFocused()
        composeRule.input(LATITUDE_INDEX).assertIsNotFocused()
        composeRule.input(LONGITUDE_INDEX).assertIsNotFocused()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-031 좌표 오류로 수정해도 초점을 옮기지 않는다`() {
        val effectChannel = Channel<PlaceDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effectChannel.receiveAsFlow())
        every { viewModel.update(any()) } answers { effectChannel.trySend(PlaceDetailEffect.CoordinateInvalid).getOrThrow() }
        composeRule.setPlaceDetailScreen(viewModel = viewModel)

        composeRule.input(LATITUDE_INDEX).performTextReplacement(INVALID_LATITUDE)
        composeRule.input(ADDRESS_INDEX).performTextReplacement(CHANGED_ADDRESS)
        composeRule.input(ADDRESS_INDEX).assertIsFocused()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.input(ADDRESS_INDEX).assertIsFocused()
        composeRule.input(TITLE_INDEX).assertIsNotFocused()
        composeRule.input(LATITUDE_INDEX).assertIsNotFocused()
    }
}
