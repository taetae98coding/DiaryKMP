package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.slot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceAddCoordinateParseTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `입력한 십진수 좌표를 그대로 전달한다`() {
        val detailSlot = setPlaceAddScreenCapturingDetail()

        enterCoordinate(latitude = "37.5665", longitude = "126.9780")

        detailSlot.captured.coordinate shouldBe Coordinate(latitude = 37.5665, longitude = 126.9780)
    }

    @Test
    fun `음수 좌표를 그대로 전달한다`() {
        val detailSlot = setPlaceAddScreenCapturingDetail()

        enterCoordinate(latitude = "-33.8688", longitude = "-70.6693")

        detailSlot.captured.coordinate shouldBe Coordinate(latitude = -33.8688, longitude = -70.6693)
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-009 읽을 수 없는 자리는 숫자가 아닌 값으로 채워 UseCase 검증에 넘긴다`() {
        val detailSlot = setPlaceAddScreenCapturingDetail()

        val caseList =
            listOf(
                "" to "",
                VALID_LATITUDE to "",
                "" to VALID_LONGITUDE,
                "abc" to VALID_LONGITUDE,
                "-" to VALID_LONGITUDE,
                "1.2.3" to VALID_LONGITUDE,
                "37,5" to VALID_LONGITUDE,
                "NaN" to VALID_LONGITUDE,
                "Infinity" to VALID_LONGITUDE,
                "-Infinity" to VALID_LONGITUDE,
            )

        caseList.forEach { (latitude, longitude) ->
            enterCoordinate(latitude = latitude, longitude = longitude)

            withClue("위도 '$latitude', 경도 '$longitude'") {
                detailSlot.captured.coordinate
                    .isValid()
                    .shouldBeFalse()
            }
        }
    }

    private fun setPlaceAddScreenCapturingDetail(): CapturingSlot<PlaceDetail> {
        val detailSlot = slot<PlaceDetail>()
        val viewModel = screenTestViewModel()
        every { viewModel.add(capture(detailSlot), tagIdSet = any()) } returns Unit
        composeRule.setPlaceAddScreen(viewModel = viewModel)

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)

        return detailSlot
    }

    private fun enterCoordinate(
        latitude: String,
        longitude: String,
    ) {
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].performTextReplacement(latitude)
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].performTextReplacement(longitude)
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    public companion object {
        private const val VALID_LATITUDE = "37.5"
        private const val VALID_LONGITUDE = "127.0"

        private fun Coordinate.isValid(): Boolean = latitude.isFinite() && longitude.isFinite()
    }
}
