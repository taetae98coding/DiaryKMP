package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MusicAddScreenFocusTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MUSIC-ADD-FEATURE-026 화면에 처음 진입하면 제목 입력에 초점이 있다`() {
        setMusicAddScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule.titleInput().assertIsFocused()
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-027 추가에 성공하면 제목 입력으로 초점을 옮긴다`() {
        setMusicAddScreen(viewModel = effectViewModel(effect = MusicAddEffect.AddSucceeded))
        composeRule.titleInput().performTextInput(fixtureMonkey.giveMeOne<String>())
        composeRule.linkInput().performClick()
        composeRule.waitForIdle()
        composeRule.linkInput().assertIsFocused()

        composeRule.clickAdd()

        composeRule.titleInput().assertIsFocused()
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-010 제목이 공백이면 제목 입력으로 초점을 옮긴다`() {
        setMusicAddScreen(viewModel = effectViewModel(effect = MusicAddEffect.TitleBlank))
        composeRule.artistInput().performTextInput(fixtureMonkey.giveMeOne<String>())

        composeRule.clickAdd()

        composeRule.titleInput().assertIsFocused()
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-010 링크가 YouTube 주소가 아니면 링크 입력으로 초점을 옮긴다`() {
        setMusicAddScreen(viewModel = effectViewModel(effect = MusicAddEffect.LinkNotYoutube))
        composeRule.titleInput().performTextInput(fixtureMonkey.giveMeOne<String>())

        composeRule.clickAdd()

        composeRule.linkInput().assertIsFocused()
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-019 링크가 공백이면 불러오기 뒤 링크 입력으로 초점을 옮긴다`() {
        setMusicAddScreen(viewModel = fetchEffectViewModel(MusicAddEffect.LinkBlank))
        composeRule.artistInput().performTextInput(fixtureMonkey.giveMeOne<String>())

        composeRule.clickFetch()

        composeRule.linkInput().assertIsFocused()
    }

    @Test
    fun `TC-MUSIC-ADD-FEATURE-019 링크가 YouTube 주소가 아니면 불러오기 뒤 링크 입력으로 초점을 옮긴다`() {
        setMusicAddScreen(viewModel = fetchEffectViewModel(MusicAddEffect.LinkNotYoutube))
        composeRule.artistInput().performTextInput(fixtureMonkey.giveMeOne<String>())

        composeRule.clickFetch()

        composeRule.linkInput().assertIsFocused()
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
