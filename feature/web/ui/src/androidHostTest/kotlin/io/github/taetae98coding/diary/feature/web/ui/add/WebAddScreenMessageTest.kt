package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class WebAddScreenMessageTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-ADD-FEATURE-006 기본 환경 추가 성공 안내`() {
        assertMessage(effect = WebAddEffect.AddSucceeded(id = fixtureMonkey.giveMeOne<Uuid>()), expectedMessage = DEFAULT_ADD_SUCCEEDED_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-WEB-ADD-FEATURE-006 한국어 추가 성공 안내`() {
        assertMessage(effect = WebAddEffect.AddSucceeded(id = fixtureMonkey.giveMeOne<Uuid>()), expectedMessage = KOREAN_ADD_SUCCEEDED_MESSAGE)
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-008 기본 환경 제목 미입력 안내`() {
        assertMessage(effect = WebAddEffect.TitleBlank, expectedMessage = DEFAULT_TITLE_BLANK_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-WEB-ADD-FEATURE-008 한국어 제목 미입력 안내`() {
        assertMessage(effect = WebAddEffect.TitleBlank, expectedMessage = KOREAN_TITLE_BLANK_MESSAGE)
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-008 기본 환경 URL 미입력 안내`() {
        assertMessage(effect = WebAddEffect.UrlBlank, expectedMessage = DEFAULT_URL_BLANK_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-WEB-ADD-FEATURE-008 한국어 URL 미입력 안내`() {
        assertMessage(effect = WebAddEffect.UrlBlank, expectedMessage = KOREAN_URL_BLANK_MESSAGE)
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-008 기본 환경 헤더 이름 미입력 안내`() {
        assertMessage(effect = WebAddEffect.HeaderNameBlank, expectedMessage = DEFAULT_HEADER_NAME_BLANK_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-WEB-ADD-FEATURE-008 한국어 헤더 이름 미입력 안내`() {
        assertMessage(effect = WebAddEffect.HeaderNameBlank, expectedMessage = KOREAN_HEADER_NAME_BLANK_MESSAGE)
    }

    private fun assertMessage(
        effect: WebAddEffect,
        expectedMessage: String,
    ) {
        composeRule.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    componentVisibleProvider = { WebAddScaffoldComponentVisible() },
                    addViewModel = effectViewModel(effect = effect),
                    navigateToTagDetail = {},
                    tagViewModel = addTagScreenTestViewModel(),
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.urlInput().performTextInput(TYPED_URL)
        // 한국어 환경에서는 추가 버튼의 접근성 이름이 달라지므로 단축키로 추가를 실행한다.
        composeRule.urlInput().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(expectedMessage).assertExists()
    }

    private companion object {
        const val DEFAULT_ADD_SUCCEEDED_MESSAGE = "Web added."
        const val KOREAN_ADD_SUCCEEDED_MESSAGE = "웹이 추가되었습니다."
        const val DEFAULT_TITLE_BLANK_MESSAGE = "Please enter a title."
        const val KOREAN_TITLE_BLANK_MESSAGE = "제목을 입력해 주세요."
        const val DEFAULT_URL_BLANK_MESSAGE = "Please enter a URL."
        const val KOREAN_URL_BLANK_MESSAGE = "URL을 입력해 주세요."
        const val DEFAULT_HEADER_NAME_BLANK_MESSAGE = "Please enter a header name."
        const val KOREAN_HEADER_NAME_BLANK_MESSAGE = "헤더 이름을 입력해 주세요."
    }
}
