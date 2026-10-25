package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailScreenMessageTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-45149e3f-8b71-4217-b619-2517667cd553 수정에 성공하면 성공 안내를 표시한다`() {
        val effect = Channel<PlaceDetailEffect>(Channel.BUFFERED)

        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel(effect = effect.receiveAsFlow()))

        effect.trySend(PlaceDetailEffect.UpdateSucceeded).getOrThrow()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertIsDisplayed()
    }

    @Test
    fun `TC-ef5fe2c4-8682-4d58-ad74-d964c1dba446 좌표가 성립하지 않으면 좌표 안내를 표시한다`() {
        val effect = Channel<PlaceDetailEffect>(Channel.BUFFERED)

        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel(effect = effect.receiveAsFlow()))

        effect.trySend(PlaceDetailEffect.CoordinateInvalid).getOrThrow()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_COORDINATE_INVALID_MESSAGE).assertIsDisplayed()
    }

    @Test
    fun `TC-b223dd7a-4b41-4381-8278-095cd1231671 삭제에 성공하면 화면에서 빠져나간다`() {
        val effect = Channel<PlaceDetailEffect>(Channel.BUFFERED)
        var navigateUpCount = 0

        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(effect = effect.receiveAsFlow()),
            navigateUp = { navigateUpCount++ },
        )

        effect.trySend(PlaceDetailEffect.DeleteSucceeded).getOrThrow()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }
}
