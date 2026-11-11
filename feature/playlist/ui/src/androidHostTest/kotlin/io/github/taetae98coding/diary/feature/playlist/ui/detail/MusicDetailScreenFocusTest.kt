package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.feature.playlist.ui.MusicTagTestTheme
import io.github.taetae98coding.diary.feature.playlist.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.playlist.ui.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MusicDetailScreenFocusTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-2d9e4d4d-0af3-4df2-9bc8-cb83bdba7cf5 링크 형식이 성립하지 않으면 영상 링크가 필요하다고 알린다`() {
        val effect = MutableStateFlow<MusicDetailEffect?>(null)
        val uiState = MutableStateFlow<MusicDetailUiState>(MusicDetailUiState.Content(id = Arb.kotlinUuid().next(), detail = testMusicDetail()))
        composeRule.setContent {
            MusicTagTestTheme {
                MusicDetailScreen(
                    navigateUp = {},
                    componentVisibleProvider = { MusicDetailScaffoldComponentVisible() },
                    musicViewModel = detailViewModel(uiState = uiState, effect = effect.filterNotNull()),
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    tagViewModel = detailTagScreenTestViewModel(),
                )
            }
        }
        composeRule.linkInput().performTextReplacement(NOT_YOUTUBE_LINK)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_LINK_NOT_YOUTUBE_MESSAGE).assertDoesNotExist()

        composeRule.runOnIdle { effect.value = MusicDetailEffect.LinkNotYoutube }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_LINK_NOT_YOUTUBE_MESSAGE).assertExists()
    }

    @Test
    fun `TC-bd285bf7-0a43-4f87-a5e4-6dbf44b712ae 링크 형식이 성립하지 않으면 입력을 유지한 채 링크 입력으로 초점을 옮긴다`() {
        val effect = MutableStateFlow<MusicDetailEffect?>(null)
        val uiState = MutableStateFlow<MusicDetailUiState>(MusicDetailUiState.Content(id = Arb.kotlinUuid().next(), detail = testMusicDetail()))

        composeRule.setContent {
            MusicTagTestTheme {
                MusicDetailScreen(
                    navigateUp = {},
                    componentVisibleProvider = { MusicDetailScaffoldComponentVisible() },
                    musicViewModel = detailViewModel(uiState = uiState, effect = effect.filterNotNull()),
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    tagViewModel = detailTagScreenTestViewModel(),
                )
            }
        }
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.artistInput().performTextReplacement(EDITING_ARTIST)
        composeRule.linkInput().performTextReplacement(NOT_YOUTUBE_LINK)
        composeRule.waitForIdle()

        composeRule.runOnIdle { effect.value = MusicDetailEffect.LinkNotYoutube }
        composeRule.waitForIdle()

        composeRule.linkInput().assertIsFocused()
        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.artistInput().assert(hasText(EDITING_ARTIST))
        composeRule.linkInput().assert(hasText(NOT_YOUTUBE_LINK))
        composeRule.nodeCount(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION) shouldBe 0
    }

    private companion object {
        private const val DEFAULT_LINK_NOT_YOUTUBE_MESSAGE = "Please enter a YouTube video link."
        private const val NOT_YOUTUBE_LINK = "https://vimeo.com/76979871"
        private const val EDITING_ARTIST = "MusicDetailFocusEditingArtist"
    }
}
