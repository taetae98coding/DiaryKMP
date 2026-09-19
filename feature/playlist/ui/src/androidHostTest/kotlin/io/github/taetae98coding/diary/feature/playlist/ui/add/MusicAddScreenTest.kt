package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MusicAddScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MUSIC-ADD-FEATURE-001 화면에 처음 진입하면 입력이 모두 비어 있다`() {
        setMusicAddScreen(viewModel = screenTestViewModel())

        composeRule.linkInput().assert(hasText(""))
        composeRule.titleInput().assert(hasText(""))
        composeRule.artistInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT
        composeRule.thumbnailPreviewCount() shouldBe 0
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-002 링크, 제목과 가수를 입력할 수 있다`() {
        setMusicAddScreen(viewModel = screenTestViewModel())

        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.artistInput().performTextInput(TYPED_ARTIST)
        composeRule.waitForIdle()

        composeRule.linkInput().assert(hasText(TYPED_LINK))
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.artistInput().assert(hasText(TYPED_ARTIST))
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-004 추가에 성공하면 다음 곡을 작성할 수 있는 상태로 초기화한다`() {
        setMusicAddScreen(viewModel = effectViewModel(effect = MusicAddEffect.AddSucceeded))
        composeRule.fillAllInput()

        composeRule.clickAdd()

        composeRule.linkInput().assert(hasText(""))
        composeRule.titleInput().assert(hasText(""))
        composeRule.artistInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT
        composeRule.thumbnailPreviewCount() shouldBe 0
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-009 링크가 공백이면 작성 내용은 유지된다`() {
        assertInvalidInputRetainsInput(effect = MusicAddEffect.LinkBlank)
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-009 링크가 YouTube 주소가 아니면 작성 내용은 유지된다`() {
        assertInvalidInputRetainsInput(effect = MusicAddEffect.LinkNotYoutube)
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-009 제목이 공백이면 작성 내용은 유지된다`() {
        assertInvalidInputRetainsInput(effect = MusicAddEffect.TitleBlank)
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-009 가수가 공백이면 작성 내용은 유지된다`() {
        assertInvalidInputRetainsInput(effect = MusicAddEffect.ArtistBlank)
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-015 뒤로가면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setMusicAddScreen(
            viewModel = screenTestViewModel(),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    private fun assertInvalidInputRetainsInput(effect: MusicAddEffect) {
        setMusicAddScreen(viewModel = effectViewModel(effect = effect))
        composeRule.fillAllInput()

        composeRule.clickAdd()

        composeRule.inputCount() shouldBe INPUT_COUNT
        composeRule.linkInput().assert(hasText(TYPED_LINK))
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.artistInput().assert(hasText(TYPED_ARTIST))
    }

    private fun setMusicAddScreen(
        viewModel: MusicAddViewModel,
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MusicAddScreen(
                    navigateUp = navigateUp,
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }
}
