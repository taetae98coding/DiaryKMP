package io.github.taetae98coding.diary.compose.place

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-HOME-FEATURE-018 장소 카드에 제목과 주소를 표시한다`() {
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
    fun `TC-PLACE-HOME-FEATURE-042 자리 표시 카드는 제목과 주소를 비워 표시한다`() {
        val place = place(title = title(), address = address())

        setPlaceCard(place = null)

        composeRule.onNodeWithTag(PLACE_CARD_TEST_TAG).assertExists()
        composeRule.onNodeWithText(place.detail.title).assertDoesNotExist()
        composeRule.onNodeWithText(place.detail.address).assertDoesNotExist()
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
    fun `TC-PLACE-HOME-FEATURE-043 TC-SEARCH-HOME-FEATURE-013 TC-TAG-DETAIL-PLACE-FEATURE-003 자리 표시 카드는 선택할 수 없다`() {
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

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        // FixtureMonkey는 빈 문자열도 생성하므로 표시 여부 검증에 쓰는 값은 비어 있지 않게 접두사를 붙인다.
        private fun title(): String = "제목-${fixtureMonkey.giveMeOne<String>()}"

        private fun address(): String = "주소-${fixtureMonkey.giveMeOne<String>()}"

        private fun place(
            title: String,
            address: String,
        ): Place {
            val detail =
                fixtureMonkey
                    .giveMeKotlinBuilder<PlaceDetail>()
                    .setExp(PlaceDetail::title, title)
                    .setExp(PlaceDetail::address, address)
                    .sample()

            return Place(
                id = Uuid.random(),
                detail = detail,
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                createdAt = fixtureMonkey.giveMeOne<Instant>(),
            )
        }
    }
}
