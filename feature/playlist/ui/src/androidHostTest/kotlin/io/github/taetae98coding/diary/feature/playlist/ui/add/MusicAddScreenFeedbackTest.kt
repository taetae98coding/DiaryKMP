package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.feature.playlist.ui.MusicTagTestTheme
import io.github.taetae98coding.diary.feature.playlist.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.playlist.ui.addTagScreenTestViewModel
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MusicAddScreenFeedbackTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-4e3078a1-33db-496b-acbe-4605d36cde4f 추가에 성공하면 곡이 추가되었다는 안내를 표시한다`() {
        setMusicAddScreen(viewModel = effectViewModel(effect = MusicAddEffect.AddSucceeded))
        composeRule.titleInput().performTextInput(TYPED_TITLE)

        composeRule.clickAdd()

        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED_MESSAGE).assertExists()
    }

    @Test
    fun `TC-98531eb1-f6cd-4691-a115-f61ad55c3b3d 가수가 비어 있어도 추가를 실행해 성공 안내를 표시한다`() {
        assertAddSucceeded(artist = "", link = TYPED_LINK)
    }

    @Test
    fun `TC-98531eb1-f6cd-4691-a115-f61ad55c3b3d 링크가 비어 있어도 추가를 실행해 성공 안내를 표시한다`() {
        assertAddSucceeded(artist = TYPED_ARTIST, link = "")
    }

    @Test
    fun `TC-98531eb1-f6cd-4691-a115-f61ad55c3b3d 가수와 링크가 비어 있어도 추가를 실행해 성공 안내를 표시한다`() {
        assertAddSucceeded(artist = "", link = "")
    }

    @Test
    fun `TC-054310e3-a1cf-42f4-8119-3c37ef3f751e 제목이 성립하지 않으면 제목 입력이 필요하다는 안내를 표시한다`() {
        setMusicAddScreen(viewModel = effectViewModel(effect = MusicAddEffect.TitleBlank))

        composeRule.clickAdd()

        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK_MESSAGE).assertExists()
    }

    @Test
    fun `TC-054310e3-a1cf-42f4-8119-3c37ef3f751e 링크가 YouTube 주소가 아니면 추가할 때 영상 링크가 필요하다는 안내를 표시한다`() {
        setMusicAddScreen(viewModel = effectViewModel(effect = MusicAddEffect.LinkNotYoutube))
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.linkInput().performTextInput(NOT_YOUTUBE_LINK)

        composeRule.clickAdd()

        composeRule.onNodeWithText(DEFAULT_LINK_NOT_YOUTUBE_MESSAGE).assertExists()
    }

    @Test
    fun `TC-0b094f6b-7d0a-4647-93bb-40303a7f2b1f 링크가 비어 있으면 불러오지 않고 링크 입력이 필요하다는 안내를 표시한다`() {
        setMusicAddScreen(viewModel = fetchEffectViewModel(MusicAddEffect.LinkBlank))
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.artistInput().performTextInput(TYPED_ARTIST)

        composeRule.clickFetch()

        composeRule.onNodeWithText(DEFAULT_LINK_BLANK_MESSAGE).assertExists()
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.artistInput().assert(hasText(TYPED_ARTIST))
    }

    @Test
    fun `TC-0b094f6b-7d0a-4647-93bb-40303a7f2b1f 링크가 YouTube 주소가 아니면 불러오지 않고 영상 링크가 필요하다는 안내를 표시한다`() {
        setMusicAddScreen(viewModel = fetchEffectViewModel(MusicAddEffect.LinkNotYoutube))
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.artistInput().performTextInput(TYPED_ARTIST)
        composeRule.linkInput().performTextInput(NOT_YOUTUBE_LINK)

        composeRule.clickFetch()

        composeRule.onNodeWithText(DEFAULT_LINK_NOT_YOUTUBE_MESSAGE).assertExists()
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.artistInput().assert(hasText(TYPED_ARTIST))
    }

    @Test
    fun `TC-16709a4a-2bf6-4296-83c6-d143b4b60fa7 영상 정보를 가져오지 못하면 불러오지 못했다는 안내를 표시한다`() {
        setMusicAddScreen(viewModel = fetchEffectViewModel(MusicAddEffect.LinkFetchFailed))
        composeRule.linkInput().performTextInput(TYPED_LINK)

        composeRule.clickFetch()

        composeRule.onNodeWithText(DEFAULT_LINK_FETCH_FAILED_MESSAGE).assertExists()
    }

    private fun assertAddSucceeded(
        artist: String,
        link: String,
    ) {
        val viewModel = effectViewModel(effect = MusicAddEffect.AddSucceeded)
        setMusicAddScreen(viewModel = viewModel)
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        if (artist.isNotEmpty()) composeRule.artistInput().performTextInput(artist)
        if (link.isNotEmpty()) composeRule.linkInput().performTextInput(link)
        composeRule.waitForIdle()

        composeRule.clickAdd()

        verify(exactly = 1) { viewModel.add(detail = MusicDetail(title = TYPED_TITLE, artist = artist, link = link), tagIdSet = emptySet()) }
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED_MESSAGE).assertExists()
    }

    private fun setMusicAddScreen(viewModel: MusicAddViewModel) {
        composeRule.setContent {
            MusicTagTestTheme {
                MusicAddScreen(
                    navigateUp = {},
                    componentVisibleProvider = { MusicAddScaffoldComponentVisible() },
                    addViewModel = viewModel,
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    tagViewModel = addTagScreenTestViewModel(),
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val DEFAULT_ADD_SUCCEEDED_MESSAGE = "Music added."
        const val DEFAULT_TITLE_BLANK_MESSAGE = "Please enter a title."
        const val DEFAULT_LINK_BLANK_MESSAGE = "Please enter a link."
        const val DEFAULT_LINK_NOT_YOUTUBE_MESSAGE = "Please enter a YouTube video link."
        const val DEFAULT_LINK_FETCH_FAILED_MESSAGE = "Could not fetch music info from the link."
    }
}
