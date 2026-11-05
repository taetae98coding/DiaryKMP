package io.github.taetae98coding.diary.compose.place

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.testing.place.placeDetail
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt
import kotlin.time.Instant
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PlaceCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-964e27af-7d54-4aa9-8b49-6b1108fc3f7c 장소 카드에 제목과 주소를 표시한다`() {
        val place = place(title = title(), address = address())

        setPlaceCard(place)

        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(place.detail.address).assertIsDisplayed()
    }

    @Test
    fun `주소가 비어 있는 장소 카드는 제목만 표시한다`() {
        val place = place(title = title(), address = "")

        setPlaceCard(place)

        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText("").assertDoesNotExist()
    }

    @Test
    fun `장소 카드를 누르면 선택이 전달된다`() {
        var clickCount = 0
        val place = place(title = title(), address = address())

        setPlaceCard(place = place, onClick = { clickCount++ })

        composeRule.onNodeWithText(place.detail.title).performClick()

        clickCount shouldBe 1
    }

    @Test
    fun `TC-886b7185-ca97-4b0c-9cec-193ad3d6846e 자리 표시 카드는 제목과 주소를 비워 표시한다`() {
        val place = place(title = title(), address = address())

        setPlaceCard(place = null)

        composeRule.onNodeWithTag(PLACE_CARD_TEST_TAG).assertExists()
        composeRule.onNodeWithText(place.detail.title).assertDoesNotExist()
        composeRule.onNodeWithText(place.detail.address).assertDoesNotExist()
    }

    @Test
    fun `장소 카드의 제목 왼쪽 원형 표시에 저장된 컬러를 사용한다`() {
        val place = place(title = title(), address = address(), color = opaqueColor())

        setPlaceCard(place)

        val indicator = boundsInRoot(PLACE_CARD_COLOR_INDICATOR_TEST_TAG)
        val title = textBoundsInRoot(place.detail.title)
        val pixelMap = composeRule.onRoot().captureToImage().toPixelMap()

        pixelMap[indicator.center.x.roundToInt(), indicator.center.y.roundToInt()] shouldBe place.detail.color.toColor()
        (indicator.right < title.left) shouldBe true
    }

    @Test
    fun `자리 표시 카드의 컬러 원형 표시는 카드 배경과 같은 색으로 비워 둔다`() {
        setPlaceCard(place = null)

        val indicator = boundsInRoot(PLACE_CARD_COLOR_INDICATOR_TEST_TAG)
        val pixelMap = composeRule.onRoot().captureToImage().toPixelMap()
        val indicatorColor = pixelMap[indicator.center.x.roundToInt(), indicator.center.y.roundToInt()]
        val cardBackgroundColor = pixelMap[(indicator.right + BESIDE_INDICATOR_OFFSET).roundToInt(), indicator.center.y.roundToInt()]

        indicatorColor shouldBe cardBackgroundColor
    }

    @Test
    fun `컬러 원형 표시는 제목과 주소를 합친 글 묶음의 세로 가운데에 놓인다`() {
        val place = place(title = title(), address = address())

        setPlaceCard(place)

        val indicator = boundsInRoot(PLACE_CARD_COLOR_INDICATOR_TEST_TAG)
        val title = textBoundsInRoot(place.detail.title)
        val address = textBoundsInRoot(place.detail.address)

        indicator.center.y shouldBe ((title.top + address.bottom) / 2 plusOrMinus CENTER_TOLERANCE)
    }

    @Test
    fun `TC-1be24c68-03eb-4258-90f7-71356643c87b TC-f37b3a3e-09fd-4ad0-bd7f-db2f0cb9e101 TC-9191b292-7609-48f2-aa21-6a461ee24c8e 자리 표시 카드는 선택할 수 없다`() {
        var clickCount = 0

        setPlaceCard(place = null, onClick = { clickCount++ })

        composeRule.onNodeWithTag(PLACE_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 0
    }

    private fun boundsInRoot(testTag: String): Rect =
        composeRule
            .onNodeWithTag(testTag, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot

    private fun textBoundsInRoot(text: String): Rect =
        composeRule
            .onNodeWithText(text, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot

    private fun setPlaceCard(
        place: Place?,
        onClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                PlaceCard(
                    onClick = onClick,
                    place = place,
                )
            }
        }
    }

    private companion object {
        private const val CENTER_TOLERANCE = 0.5f
        private const val BESIDE_INDICATOR_OFFSET = 4f

        // 임의로 만든 문자열은 비어 있을 수 있으므로 표시 여부 검증에 쓰는 값은 비어 있지 않게 접두사를 붙인다.
        private fun title(): String = "제목-${Arb.string().next()}"

        private fun address(): String = "주소-${Arb.string().next()}"

        // 배경과 섞이지 않고 화면에서 그대로 읽히도록 불투명한 컬러를 쓴다.
        private fun opaqueColor(): Long = (Arb.int().next() or 0xFF000000.toInt()).toLong() and 0xFFFFFFFFL

        private fun place(
            title: String,
            address: String,
            color: Long = Arb.placeDetail().next().color,
        ): Place {
            val detail =
                Arb
                    .placeDetail()
                    .next()
                    .copy(
                        title = title,
                        address = address,
                        color = color,
                    )

            return Place(
                id = Uuid.random(),
                detail = detail,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
                createdAt = Arb.secondPrecisionInstant().next(),
            )
        }
    }
}
