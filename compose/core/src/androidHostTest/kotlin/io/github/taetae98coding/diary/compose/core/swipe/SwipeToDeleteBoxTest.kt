package io.github.taetae98coding.diary.compose.core.swipe

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
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
class SwipeToDeleteBoxTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-FEATURE-007 삭제만 제공하는 카드는 좌에서 우로 밀어도 동작을 실행하지 않는다`() {
        var deleteCount = 0
        setSwipeToDeleteBox(onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        composeRule.onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DELETE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-FEATURE-008 삭제만 제공하는 카드는 우에서 좌로 밀면 삭제 동작을 한 번 실행한다`() {
        var deleteCount = 0
        setSwipeToDeleteBox(onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 1
    }

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-FEATURE-009 비활성화된 삭제 전용 카드는 우에서 좌로 밀어도 삭제하지 않는다`() {
        var deleteCount = 0
        setSwipeToDeleteBox(gesturesEnabled = false, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        composeRule.onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-DOMAIN-003 삭제 전용 카드가 다른 항목을 표시하면 스와이프 상태가 초기화된다`() {
        val firstKey = fixtureMonkey.giveMeOne<Long>()
        var key by mutableStateOf(firstKey)
        var deleteCount = 0
        composeRule.setContent {
            DiaryTheme {
                DeleteBox(key = key, onDelete = { deleteCount += 1 })
            }
        }

        composeRule.onNodeWithTag(CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        deleteCount shouldBe 1

        composeRule.runOnIdle { key = firstKey + 1 }
        composeRule.waitForIdle()

        deleteCount shouldBe 1
        composeRule.onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DELETE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-DOMAIN-004 실행 취소로 다시 나타난 삭제 전용 카드는 스와이프 전 모양으로 표시되고 삭제를 다시 실행하지 않는다`() {
        val key = fixtureMonkey.giveMeOne<Long>()
        var isShown by mutableStateOf(true)
        var deleteCount = 0
        composeRule.setContent {
            DiaryTheme {
                if (isShown) {
                    DeleteBox(key = key, onDelete = { deleteCount += 1 })
                }
            }
        }
        composeRule.onNodeWithTag(CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        deleteCount shouldBe 1

        // 삭제가 반영되면 카드가 목록에서 사라지고, 실행 취소하면 같은 항목의 카드가 다시 나타난다.
        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        deleteCount shouldBe 1
        composeRule.onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DELETE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-DOMAIN-006 삭제 전용 카드도 삭제 뒤에 남으면 원래 모양으로 돌아와 다시 실행할 수 있다`() {
        var deleteCount = 0
        setSwipeToDeleteBox(onDelete = { deleteCount += 1 })

        repeat(2) {
            composeRule.onNodeWithTag(CARD_TEST_TAG).performTouchInput { swipeLeft() }
            composeRule.waitForIdle()
            composeRule.mainClock.advanceTimeBy(RESET_WAIT_MILLIS)
            composeRule.waitForIdle()

            composeRule.onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
        }

        deleteCount shouldBe 2
    }

    @Composable
    private fun DeleteBox(
        key: Any?,
        onDelete: () -> Unit,
    ) {
        SwipeToDeleteBox(
            deleteContentDescription = DELETE_DESCRIPTION,
            onDelete = onDelete,
            modifier = Modifier.fillMaxWidth(),
            key = key,
        ) {
            Card(modifier = Modifier.fillMaxWidth().testTag(CARD_TEST_TAG)) {
                Text(
                    text = CONTENT_TEXT,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }

    private fun setSwipeToDeleteBox(
        gesturesEnabled: Boolean = true,
        onDelete: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SwipeToDeleteBox(
                    deleteContentDescription = DELETE_DESCRIPTION,
                    onDelete = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    gesturesEnabled = gesturesEnabled,
                ) {
                    // 실행 기준은 카드 폭에 비례하므로 카드가 상자의 폭을 모두 차지하게 두고 카드 전체를 민다.
                    Card(modifier = Modifier.fillMaxWidth().testTag(CARD_TEST_TAG)) {
                        Text(
                            text = CONTENT_TEXT,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val RESET_WAIT_MILLIS = 2_000L
        const val CONTENT_TEXT = "SwipeToDeleteBoxContent"
        const val DELETE_DESCRIPTION = "Delete item"
        const val CARD_TEST_TAG = "SwipeToDeleteBoxCard"
    }
}
