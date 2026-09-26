package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MemoPlaceCardColorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-004 연결된 장소가 저장된 컬러와 제목으로 구분되어 표시된다`() {
        val placeList =
            listOf(
                testPlace(title = HOME_PLACE_TITLE).withColor(opaqueColor()),
                testPlace(title = OFFICE_PLACE_TITLE).withColor(opaqueColor()),
            )
        composeRule.setMemoPlaceCard(uiStateProvider = { placeCardUiState(selectedPlaceList = placeList) })

        placeList.forEach { place -> assertChipShows(place) }
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-041 선택한 장소의 제목이 바뀌면 카드의 장소 목록에 반영된다`() {
        val place = testPlace(title = HOME_PLACE_TITLE).withColor(opaqueColor())
        var uiState by mutableStateOf(placeCardUiState(selectedPlaceList = listOf(place)))
        composeRule.setMemoPlaceCard(uiStateProvider = { uiState })
        val renamedPlace = place.copy(detail = place.detail.copy(title = OFFICE_PLACE_TITLE))

        uiState = placeCardUiState(selectedPlaceList = listOf(renamedPlace))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertDoesNotExist()
        assertChipShows(renamedPlace)
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-041 선택한 장소의 컬러가 바뀌면 카드의 장소 목록에 반영된다`() {
        val place = testPlace(title = HOME_PLACE_TITLE).withColor(opaqueColor())
        var uiState by mutableStateOf(placeCardUiState(selectedPlaceList = listOf(place)))
        composeRule.setMemoPlaceCard(uiStateProvider = { uiState })
        val recoloredPlace = place.withColor(opaqueColor(except = place.detail.color))

        uiState = placeCardUiState(selectedPlaceList = listOf(recoloredPlace))
        composeRule.waitForIdle()

        assertChipShows(recoloredPlace)
    }

    /**
     * 칩의 컬러 표시는 이름 앞에 놓이므로, 칩 안에서 이름보다 앞쪽 구간에 그 장소의 컬러가 칠해져 있는지 확인한다.
     */
    private fun assertChipShows(place: Place) {
        val chipBounds = composeRule.onNodeWithText(place.detail.title).fetchSemanticsNode().boundsInRoot
        val titleBounds =
            composeRule
                .onNodeWithText(text = place.detail.title, useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot
        val pixelMap = composeRule.onRoot().captureToImage().toPixelMap()

        pixelMap.hasColorInRow(
            y = titleBounds.center.y.roundToInt(),
            fromX = chipBounds.left.roundToInt(),
            untilX = titleBounds.left.roundToInt(),
            color = Color(color = place.detail.color.toInt()),
        ) shouldBe true
    }

    private fun PixelMap.hasColorInRow(
        y: Int,
        fromX: Int,
        untilX: Int,
        color: Color,
    ): Boolean = (fromX until untilX).any { x -> this[x, y] == color }

    private fun Place.withColor(color: Long): Place = copy(detail = detail.copy(color = color))

    private fun opaqueColor(except: Long? = null): Long =
        generateSequence { fixtureMonkey.giveMeOne<Int>().toLong() and RGB_MASK or OPAQUE_ALPHA }
            .first { color -> color != except }

    private companion object {
        const val RGB_MASK: Long = 0x00FFFFFF
        const val OPAQUE_ALPHA: Long = 0xFF000000
    }
}
