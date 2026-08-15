package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class WebAddScreenFocusTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-ADD-FEATURE-013 화면에 처음 진입하면 제목 입력에 초점이 있다`() {
        setWebAddScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule.titleInput().assertIsFocused()
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-014 헤더 항목을 추가해도 초점을 옮기지 않는다`() {
        setWebAddScreen(viewModel = screenTestViewModel())
        composeRule.addHeaderRow()
        composeRule.titleInput().performTextInput(fixtureMonkey.giveMeOne<String>())

        composeRule.onNodeWithContentDescription(DEFAULT_HEADER_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.titleInput().assertIsFocused()
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-014 헤더 항목을 삭제해도 초점을 옮기지 않는다`() {
        setWebAddScreen(viewModel = screenTestViewModel())
        composeRule.addHeaderRow()
        composeRule.titleInput().performTextInput(fixtureMonkey.giveMeOne<String>())

        composeRule.onNodeWithContentDescription(DEFAULT_HEADER_REMOVE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.titleInput().assertIsFocused()
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-015 추가에 성공하면 제목 입력으로 초점을 옮긴다`() {
        setWebAddScreen(viewModel = effectViewModel(effect = WebAddEffect.AddSucceeded(id = Uuid.random())))

        composeRule.titleInput().performTextInput(fixtureMonkey.giveMeOne<String>())
        composeRule.urlInput().performTextInput(fixtureMonkey.giveMeOne<String>())
        composeRule.clickAdd()

        composeRule.titleInput().assertIsFocused()
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-016 제목이 비어 있으면 제목 입력으로 초점을 옮긴다`() {
        setWebAddScreen(viewModel = effectViewModel(effect = WebAddEffect.TitleBlank))

        composeRule.urlInput().performTextInput(fixtureMonkey.giveMeOne<String>())
        composeRule.clickAdd()

        composeRule.titleInput().assertIsFocused()
        composeRule.urlInput().assertIsNotFocused()
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-016 URL이 비어 있으면 URL 입력으로 초점을 옮긴다`() {
        setWebAddScreen(viewModel = effectViewModel(effect = WebAddEffect.UrlBlank))

        composeRule.titleInput().performTextInput(fixtureMonkey.giveMeOne<String>())
        composeRule.clickAdd()

        composeRule.urlInput().assertIsFocused()
        composeRule.titleInput().assertIsNotFocused()
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-016 헤더 이름이 비어 있으면 초점을 옮기지 않는다`() {
        setWebAddScreen(viewModel = effectViewModel(effect = WebAddEffect.HeaderNameBlank))

        composeRule.titleInput().performTextInput(fixtureMonkey.giveMeOne<String>())
        composeRule.urlInput().performTextInput(fixtureMonkey.giveMeOne<String>())
        composeRule.addHeaderRow()
        composeRule.headerValueInput().performTextInput(fixtureMonkey.giveMeOne<String>())

        composeRule.headerValueInput().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        composeRule.headerValueInput().assertIsFocused()
        composeRule.titleInput().assertIsNotFocused()
        composeRule.urlInput().assertIsNotFocused()
        composeRule.headerNameInput().assertIsNotFocused()
    }

    private fun setWebAddScreen(viewModel: WebAddViewModel) {
        composeRule.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    componentVisibleProvider = { WebAddScaffoldComponentVisible() },
                    addViewModel = viewModel,
                    navigateToTagDetail = {},
                    tagViewModel = addTagScreenTestViewModel(),
                )
            }
        }
    }
}
