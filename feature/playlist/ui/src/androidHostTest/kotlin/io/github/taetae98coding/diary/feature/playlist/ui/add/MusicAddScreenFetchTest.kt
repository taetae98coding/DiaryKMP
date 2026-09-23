package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MusicAddScreenFetchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MUSIC-ADD-FEATURE-016 불러오기에 성공하면 제목과 가수를 채우고 링크와 썸네일은 그대로 둔다`() {
        setMusicAddScreen(viewModel = fetchEffectViewModel(fetchedEffect()))
        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.waitForIdle()

        composeRule.thumbnailPreviewCount() shouldBe 1

        composeRule.clickFetch()

        composeRule.linkInput().assert(hasText(TYPED_LINK))
        composeRule.titleInput().assert(hasText(FETCHED_TITLE))
        composeRule.artistInput().assert(hasText(FETCHED_ARTIST))
        composeRule.thumbnailPreviewCount() shouldBe 1
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-024 불러오기에 성공하면 이미 채워진 제목도 덮어쓴다`() {
        setMusicAddScreen(viewModel = fetchEffectViewModel(fetchedEffect()))
        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.waitForIdle()

        composeRule.clickFetch()

        composeRule.titleInput().assert(hasText(FETCHED_TITLE))
        composeRule.artistInput().assert(hasText(FETCHED_ARTIST))
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-024 불러오기에 성공하면 이미 채워진 가수도 덮어쓴다`() {
        setMusicAddScreen(viewModel = fetchEffectViewModel(fetchedEffect()))
        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.artistInput().performTextInput(TYPED_ARTIST)
        composeRule.waitForIdle()

        composeRule.clickFetch()

        composeRule.titleInput().assert(hasText(FETCHED_TITLE))
        composeRule.artistInput().assert(hasText(FETCHED_ARTIST))
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-024 제목과 가수가 모두 채워져 있어도 불러온 값으로 바꾼다`() {
        setMusicAddScreen(viewModel = fetchEffectViewModel(fetchedEffect()))
        composeRule.fillAllInput()

        composeRule.clickFetch()

        composeRule.titleInput().assert(hasText(FETCHED_TITLE))
        composeRule.artistInput().assert(hasText(FETCHED_ARTIST))
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-024 공백 문자뿐인 제목과 가수는 불러온 값으로 채운다`() {
        setMusicAddScreen(viewModel = fetchEffectViewModel(fetchedEffect()))
        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.titleInput().performTextInput("   ")
        composeRule.artistInput().performTextInput("   ")
        composeRule.waitForIdle()

        composeRule.clickFetch()

        composeRule.titleInput().assert(hasText(FETCHED_TITLE))
        composeRule.artistInput().assert(hasText(FETCHED_ARTIST))
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-023 썸네일만 바꾸거나 지우는 조작을 두지 않는다`() {
        setMusicAddScreen(viewModel = screenTestViewModel())
        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.waitForIdle()

        composeRule.thumbnailPreview().assertHasNoClickAction()
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-020 영상 정보를 가져오지 못해도 작성 내용은 유지된다`() {
        setMusicAddScreen(viewModel = fetchEffectViewModel(MusicAddEffect.LinkFetchFailed))
        composeRule.fillAllInput()

        composeRule.clickFetch()

        composeRule.inputCount() shouldBe INPUT_COUNT
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.artistInput().assert(hasText(TYPED_ARTIST))
        composeRule.linkInput().assert(hasText(TYPED_LINK))
        composeRule.thumbnailPreviewCount() shouldBe 1
    }

    @Test
    fun `TC-MUSIC-ADD-DATA-010 화면에 진입하거나 링크를 입력하는 것만으로는 조회하지 않는다`() {
        val viewModel = screenTestViewModel()
        setMusicAddScreen(viewModel = viewModel)

        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.waitForIdle()

        verify(exactly = 0) { viewModel.fetchLink(any()) }
    }

    @Test
    fun `TC-MUSIC-ADD-DATA-008 불러오기를 선택하면 입력한 링크로 조회를 요청한다`() {
        val viewModel = screenTestViewModel()
        setMusicAddScreen(viewModel = viewModel)
        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.waitForIdle()

        composeRule.clickFetch()

        verify(exactly = 1) { viewModel.fetchLink(TYPED_LINK) }
    }

    private fun setMusicAddScreen(viewModel: MusicAddViewModel) {
        composeRule.setContent {
            DiaryTheme {
                MusicAddScreen(
                    navigateUp = {},
                    componentVisibleProvider = { MusicAddScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }
}
