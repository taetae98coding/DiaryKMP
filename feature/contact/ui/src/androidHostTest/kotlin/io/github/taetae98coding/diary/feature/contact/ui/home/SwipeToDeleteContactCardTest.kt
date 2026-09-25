package io.github.taetae98coding.diary.feature.contact.ui.home

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
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
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
class SwipeToDeleteContactCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-HOME-FEATURE-026 연락처 카드를 우에서 좌로 밀면 삭제를 한 번 전달한다`() {
        var deleteCount = 0
        var clickCount = 0
        setCard(contact = contact(), onClick = { clickCount += 1 }, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 1
        clickCount shouldBe 0
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-028 연락처 카드를 좌에서 우로 밀면 아무것도 전달하지 않는다`() {
        var deleteCount = 0
        var clickCount = 0
        val contact = contact()
        setCard(contact = contact, onClick = { clickCount += 1 }, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        clickCount shouldBe 0
        composeRule.onNodeWithText(contact.detail.name).assertIsDisplayed()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-029 자리 표시 카드는 밀어도 삭제를 전달하지 않는다`() {
        var deleteCount = 0
        setCard(contact = null, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).assertIsDisplayed()
    }

    private fun setCard(
        contact: Contact?,
        onClick: () -> Unit = {},
        onDelete: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SwipeToDeleteContactCard(
                    onClick = onClick,
                    onDelete = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    contact = contact,
                )
            }
        }
    }

    private fun contact(): Contact = testContact(name = "이름-${fixtureMonkey.giveMeOne<String>()}", isFavorite = fixtureMonkey.giveMeOne<Boolean>())
}
