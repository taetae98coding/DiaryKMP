package io.github.taetae98coding.diary.feature.playlist.ui.music

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.feature.playlist.ui.home.testMusic
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MusicCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-015 준비된 곡 카드는 눌러 선택할 수 있다`() {
        var clickCount = 0

        setMusicCard(music = testMusic(title = MUSIC_TITLE), onClick = { clickCount += 1 })
        composeRule.onNodeWithTag(MUSIC_CARD_TEST_TAG).assertIsEnabled()
        composeRule.onNodeWithTag(MUSIC_CARD_TEST_TAG).performClick()

        clickCount shouldBe 1
    }

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-016 아직 준비되지 않은 자리는 선택할 수 없다`() {
        var clickCount = 0

        setMusicCard(music = null, onClick = { clickCount += 1 })
        composeRule.onNodeWithTag(MUSIC_CARD_TEST_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(MUSIC_CARD_TEST_TAG).performClick()

        clickCount shouldBe 0
    }

    private fun setMusicCard(
        music: Music?,
        onClick: () -> Unit,
    ) {
        composeRule.setContent {
            DiaryTheme {
                MusicCard(
                    onClick = onClick,
                    music = music,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        private const val MUSIC_TITLE = "MusicCardTitle"
    }
}
