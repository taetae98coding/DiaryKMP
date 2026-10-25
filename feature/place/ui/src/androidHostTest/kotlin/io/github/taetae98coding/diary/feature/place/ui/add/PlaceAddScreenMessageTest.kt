package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import io.mockk.every
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceAddScreenMessageTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-f3fccf17-885e-4620-9f20-482189daef9e 기본 환경 추가 성공 안내`() {
        assertMessage(effect = PlaceAddEffect.AddSucceeded(id = Uuid.random()), expectedMessage = DEFAULT_ADD_SUCCEEDED_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-f3fccf17-885e-4620-9f20-482189daef9e 한국어 추가 성공 안내`() {
        assertMessage(effect = PlaceAddEffect.AddSucceeded(id = Uuid.random()), expectedMessage = KOREAN_ADD_SUCCEEDED_MESSAGE)
    }

    @Test
    fun `TC-276fe0b6-4c7a-420e-825c-dc7a061e21a7 기본 환경 제목 미입력 안내`() {
        assertMessage(effect = PlaceAddEffect.TitleBlank, expectedMessage = DEFAULT_TITLE_BLANK_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-276fe0b6-4c7a-420e-825c-dc7a061e21a7 한국어 제목 미입력 안내`() {
        assertMessage(effect = PlaceAddEffect.TitleBlank, expectedMessage = KOREAN_TITLE_BLANK_MESSAGE)
    }

    @Test
    fun `TC-839f33f5-6c41-4342-8bbb-203f3dd09e38 기본 환경 좌표 오류 안내`() {
        assertMessage(effect = PlaceAddEffect.CoordinateInvalid, expectedMessage = DEFAULT_COORDINATE_INVALID_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-839f33f5-6c41-4342-8bbb-203f3dd09e38 한국어 좌표 오류 안내`() {
        assertMessage(effect = PlaceAddEffect.CoordinateInvalid, expectedMessage = KOREAN_COORDINATE_INVALID_MESSAGE)
    }

    private fun assertMessage(
        effect: PlaceAddEffect,
        expectedMessage: String,
    ) {
        val effectChannel = Channel<PlaceAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effectChannel.receiveAsFlow())
        every { viewModel.add(any(), tagIdSet = any()) } answers { effectChannel.trySend(effect).getOrThrow() }
        composeRule.setPlaceAddScreen(viewModel = viewModel)

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(expectedMessage).assertExists()
    }

    public companion object {
        private const val DEFAULT_ADD_SUCCEEDED_MESSAGE = "Place added."
        private const val KOREAN_ADD_SUCCEEDED_MESSAGE = "장소가 추가되었습니다."
        private const val DEFAULT_TITLE_BLANK_MESSAGE = "Please enter a title."
        private const val KOREAN_TITLE_BLANK_MESSAGE = "제목을 입력해 주세요."
        private const val DEFAULT_COORDINATE_INVALID_MESSAGE =
            "Select a location on the map, or enter a latitude between -90 and 90 and a longitude between -180 and 180."
        private const val KOREAN_COORDINATE_INVALID_MESSAGE = "지도에서 위치를 선택하거나, 위도는 -90에서 90, 경도는 -180에서 180 사이의 숫자로 입력해 주세요."
    }
}
