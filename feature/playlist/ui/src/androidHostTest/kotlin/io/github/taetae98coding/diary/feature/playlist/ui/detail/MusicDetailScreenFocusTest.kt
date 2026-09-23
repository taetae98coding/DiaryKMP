package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MusicDetailScreenFocusTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MUSIC-DETAIL-FEATURE-011 링크 형식이 성립하지 않으면 입력을 유지한 채 링크 입력으로 초점을 옮긴다`() {
        val effect = MutableStateFlow<MusicDetailEffect?>(null)
        val uiState = MutableStateFlow<MusicDetailUiState>(MusicDetailUiState.Content(id = Uuid.random(), detail = testMusicDetail()))

        composeRule.setContent {
            DiaryTheme {
                MusicDetailScreen(
                    navigateUp = {},
                    componentVisibleProvider = { MusicDetailScaffoldComponentVisible() },
                    viewModel = detailViewModel(uiState = uiState, effect = effect.filterNotNull()),
                )
            }
        }
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.linkInput().performTextReplacement(NOT_YOUTUBE_LINK)
        composeRule.waitForIdle()

        composeRule.runOnIdle { effect.value = MusicDetailEffect.LinkNotYoutube }
        composeRule.waitForIdle()

        composeRule.linkInput().assertIsFocused()
        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.artistInput().assert(hasText(STORED_ARTIST))
        composeRule.linkInput().assert(hasText(NOT_YOUTUBE_LINK))
        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 0
    }

    private companion object {
        private const val NOT_YOUTUBE_LINK = "https://vimeo.com/76979871"
    }
}
