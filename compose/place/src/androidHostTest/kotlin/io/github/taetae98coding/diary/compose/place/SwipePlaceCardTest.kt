package io.github.taetae98coding.diary.compose.place

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SwipePlaceCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-bab09598-8421-4424-b1f4-2a18af1b800e TC-92903379-bc52-4bad-8c9a-fe9055f0e396 장소 카드를 우에서 좌로 밀면 삭제를 한 번 전달한다`() {
        var deleteCount = 0
        var clickCount = 0
        setCard(place = place(), onClick = { clickCount += 1 }, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(PLACE_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 1
        clickCount shouldBe 0
    }

    @Test
    fun `TC-685fc501-e0dc-4174-983b-c7e06f0f56c5 TC-676c9da7-92ca-4a7f-82a9-0bbc569cdb30 장소 카드를 좌에서 우로 밀면 아무것도 전달하지 않는다`() {
        var deleteCount = 0
        var clickCount = 0
        val place = place()
        setCard(place = place, onClick = { clickCount += 1 }, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(PLACE_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        clickCount shouldBe 0
        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-fa6c94e6-20ce-45ca-a586-6e1ed80c1e18 TC-99822615-2c00-4b29-b322-694e75992bd8 자리 표시 카드는 밀어도 삭제를 전달하지 않는다`() {
        var deleteCount = 0
        setCard(place = null, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(PLACE_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        composeRule.onNodeWithTag(PLACE_CARD_TEST_TAG).assertIsDisplayed()
    }

    private fun setCard(
        place: Place?,
        onClick: () -> Unit = {},
        onDelete: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SwipePlaceCard(
                    onEvent = { event ->
                        when (event) {
                            is PlaceListEvent.ClickPlace -> onClick()
                            is PlaceListEvent.SwipeDelete -> onDelete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    place = place,
                )
            }
        }
    }

    private fun place(): Place {
        val detail =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceDetail>()
                .setExp(PlaceDetail::title, "제목-${fixtureMonkey.giveMeOne<String>()}")
                .sample()

        return fixtureMonkey
            .giveMeKotlinBuilder<Place>()
            .setExp(Place::detail, detail)
            .sample()
    }
}
