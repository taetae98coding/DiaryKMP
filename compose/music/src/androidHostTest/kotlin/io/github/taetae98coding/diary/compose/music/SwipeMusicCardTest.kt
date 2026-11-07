package io.github.taetae98coding.diary.compose.music

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SwipeMusicCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-9978aa69-9804-4ff3-bd5a-96492a93752c 곡 카드를 우에서 좌로 밀면 삭제를 한 번 전달한다`() {
        var deleteCount = 0
        var clickCount = 0
        setCard(music = music(), onClick = { clickCount += 1 }, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(MUSIC_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 1
        clickCount shouldBe 0
    }

    @Test
    fun `TC-c955e4b9-2e82-4477-b800-4320914c66a3 TC-756a123e-232e-45c9-9cde-d021fc248b8b 곡 카드를 좌에서 우로 밀면 아무것도 전달하지 않는다`() {
        var deleteCount = 0
        var clickCount = 0
        val music = music()
        setCard(music = music, onClick = { clickCount += 1 }, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(MUSIC_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        clickCount shouldBe 0
        composeRule.onNodeWithText(music.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-debc98e9-c75e-482c-9070-52f930294fcd TC-3fc7dede-f770-479e-a2e4-c84d1aac37ae 자리 표시 카드는 밀어도 삭제를 전달하지 않는다`() {
        var deleteCount = 0
        setCard(music = null, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(MUSIC_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        composeRule.onNodeWithTag(MUSIC_CARD_TEST_TAG).assertIsDisplayed()
    }

    private fun setCard(
        music: Music?,
        onClick: () -> Unit = {},
        onDelete: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SwipeMusicCard(
                    onEvent = { event ->
                        when (event) {
                            is MusicListEvent.ClickMusic -> onClick()
                            is MusicListEvent.SwipeDelete -> onDelete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    music = music,
                )
            }
        }
    }

    private fun music(): Music = testMusic(title = "제목-${Arb.string().next()}")
}
