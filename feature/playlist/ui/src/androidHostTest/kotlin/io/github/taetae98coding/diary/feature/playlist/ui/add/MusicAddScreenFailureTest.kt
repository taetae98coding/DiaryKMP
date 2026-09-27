package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.playlist.usecase.AddMusicUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MusicAddScreenFailureTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MUSIC-ADD-FEATURE-031 저장에 실패하면 안내 없이 작성 내용을 유지하고 같은 내용으로 다시 추가할 수 있다`() {
        val useCase = mockk<AddMusicUseCase>()
        coEvery { useCase(any()) } returns Result.failure(IllegalStateException("저장 실패"))
        val viewModel = MusicAddViewModel(addMusicUseCase = useCase, fetchYoutubeVideoUseCase = mockk(relaxed = true))
        composeRule.setContent {
            DiaryTheme {
                MusicAddScreen(
                    navigateUp = {},
                    componentVisibleProvider = { MusicAddScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
        composeRule.fillAllInput()

        composeRule.clickAdd()

        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_LINK_BLANK_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_LINK_NOT_YOUTUBE_MESSAGE).assertDoesNotExist()
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.artistInput().assert(hasText(TYPED_ARTIST))
        composeRule.linkInput().assert(hasText(TYPED_LINK))

        composeRule.clickAdd()

        coVerify(exactly = 2) { useCase(MusicDetail(title = TYPED_TITLE, artist = TYPED_ARTIST, link = TYPED_LINK)) }
    }

    private companion object {
        const val DEFAULT_ADD_SUCCEEDED_MESSAGE = "Music added."
        const val DEFAULT_TITLE_BLANK_MESSAGE = "Please enter a title."
        const val DEFAULT_LINK_BLANK_MESSAGE = "Please enter a link."
        const val DEFAULT_LINK_NOT_YOUTUBE_MESSAGE = "Please enter a YouTube video link."
    }
}
