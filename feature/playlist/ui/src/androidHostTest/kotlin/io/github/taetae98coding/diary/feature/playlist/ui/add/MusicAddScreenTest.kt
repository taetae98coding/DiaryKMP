package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
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

        composeRule.titleInput().assert(hasText(""))
        composeRule.artistInput().assert(hasText(""))
        composeRule.linkInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT
        composeRule.thumbnailPreviewCount() shouldBe 0
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-002 제목, 가수와 링크를 입력할 수 있다`() {
        setMusicAddScreen(viewModel = screenTestViewModel())

        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.artistInput().performTextInput(TYPED_ARTIST)
        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.artistInput().assert(hasText(TYPED_ARTIST))
        composeRule.linkInput().assert(hasText(TYPED_LINK))
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-028 영상 링크를 입력하면 불러오기 없이 썸네일 미리보기가 표시된다`() {
        setMusicAddScreen(viewModel = screenTestViewModel())

        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.waitForIdle()

        composeRule.thumbnailPreviewCount() shouldBe 1
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-028 영상을 가리키지 않는 링크는 썸네일 미리보기를 표시하지 않는다`() {
        setMusicAddScreen(viewModel = screenTestViewModel())

        listOf(YOUTUBE_CHANNEL_LINK, NOT_YOUTUBE_LINK).forEach { link ->
            composeRule.linkInput().performTextReplacement(link)
            composeRule.waitForIdle()

            composeRule.thumbnailPreviewCount() shouldBe 0
        }
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-030 링크를 지우면 썸네일 미리보기가 사라진다`() {
        setMusicAddScreen(viewModel = screenTestViewModel())
        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.waitForIdle()

        composeRule.linkInput().performTextReplacement("")
        composeRule.waitForIdle()

        composeRule.thumbnailPreviewCount() shouldBe 0
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-030 링크를 채널 주소로 바꾸면 썸네일 미리보기가 사라진다`() {
        setMusicAddScreen(viewModel = screenTestViewModel())
        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.waitForIdle()

        composeRule.linkInput().performTextReplacement(YOUTUBE_CHANNEL_LINK)
        composeRule.waitForIdle()

        composeRule.thumbnailPreviewCount() shouldBe 0
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-030 링크를 다른 영상으로 바꾸어도 썸네일 미리보기가 유지된다`() {
        setMusicAddScreen(viewModel = screenTestViewModel())
        composeRule.linkInput().performTextInput(TYPED_LINK)
        composeRule.waitForIdle()

        composeRule.linkInput().performTextReplacement(OTHER_TYPED_LINK)
        composeRule.waitForIdle()

        composeRule.thumbnailPreviewCount() shouldBe 1
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-004 추가에 성공하면 다음 곡을 작성할 수 있는 상태로 초기화한다`() {
        setMusicAddScreen(viewModel = effectViewModel(effect = MusicAddEffect.AddSucceeded))
        composeRule.fillAllInput()
        composeRule.thumbnailPreviewCount() shouldBe 1

        composeRule.clickAdd()

        composeRule.titleInput().assert(hasText(""))
        composeRule.artistInput().assert(hasText(""))
        composeRule.linkInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT
        composeRule.thumbnailPreviewCount() shouldBe 0
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-009 제목이 공백이면 작성 내용은 유지된다`() {
        assertInvalidInputRetainsInput(effect = MusicAddEffect.TitleBlank)
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-009 링크가 YouTube 주소가 아니면 작성 내용은 유지된다`() {
        assertInvalidInputRetainsInput(effect = MusicAddEffect.LinkNotYoutube)
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-015 TC-PLAYLIST-LIST-DETAIL-FEATURE-006 단독으로 표시되면 뒤로가기로 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setMusicAddScreen(
            viewModel = screenTestViewModel(),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-005 목록과 함께 표시되면 뒤로가기 버튼이 표시되지 않는다`() {
        setMusicAddScreen(
            viewModel = screenTestViewModel(),
            componentVisible = MusicAddScaffoldComponentVisible(isNavigateUpButtonVisible = false),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertDoesNotExist()
    }

    private fun assertInvalidInputRetainsInput(effect: MusicAddEffect) {
        setMusicAddScreen(viewModel = effectViewModel(effect = effect))
        composeRule.fillAllInput()

        composeRule.clickAdd()

        composeRule.inputCount() shouldBe INPUT_COUNT
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.artistInput().assert(hasText(TYPED_ARTIST))
        composeRule.linkInput().assert(hasText(TYPED_LINK))
    }

    private fun setMusicAddScreen(
        viewModel: MusicAddViewModel,
        navigateUp: () -> Unit = {},
        componentVisible: MusicAddScaffoldComponentVisible = MusicAddScaffoldComponentVisible(),
    ) {
        composeRule.setContent {
            DiaryTheme {
                MusicAddScreen(
                    navigateUp = navigateUp,
                    componentVisibleProvider = { componentVisible },
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }
}
