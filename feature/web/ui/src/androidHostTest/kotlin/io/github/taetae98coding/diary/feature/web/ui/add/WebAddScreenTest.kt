package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class WebAddScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-ADD-FEATURE-001 화면에 처음 진입하면 입력이 모두 비어 있다`() {
        setWebAddScreen(viewModel = screenTestViewModel())

        composeRule.titleInput().assert(hasText(""))
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.urlInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-002 제목, 설명, URL을 입력할 수 있다`() {
        setWebAddScreen(viewModel = screenTestViewModel())

        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        composeRule.urlInput().performTextInput(TYPED_URL)
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.urlInput().assert(hasText(TYPED_URL))
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-003 요청 헤더 항목을 추가해 이름과 값을 입력할 수 있다`() {
        setWebAddScreen(viewModel = screenTestViewModel())

        composeRule.addHeaderRow()
        composeRule.headerNameInput().performTextInput(TYPED_FIRST_HEADER_NAME)
        composeRule.headerValueInput().performTextInput(TYPED_FIRST_HEADER_VALUE)
        composeRule.addHeaderRow()
        composeRule.headerNameInput(row = 1).performTextInput(TYPED_SECOND_HEADER_NAME)
        composeRule.headerValueInput(row = 1).performTextInput(TYPED_SECOND_HEADER_VALUE)
        composeRule.waitForIdle()

        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER + INPUT_COUNT_PER_HEADER * 2
        composeRule.headerNameInput().assert(hasText(TYPED_FIRST_HEADER_NAME))
        composeRule.headerValueInput().assert(hasText(TYPED_FIRST_HEADER_VALUE))
        composeRule.headerNameInput(row = 1).assert(hasText(TYPED_SECOND_HEADER_NAME))
        composeRule.headerValueInput(row = 1).assert(hasText(TYPED_SECOND_HEADER_VALUE))
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-004 헤더 항목을 삭제해도 다른 입력은 유지된다`() {
        setWebAddScreen(viewModel = screenTestViewModel())
        composeRule.fillAllInput()

        composeRule.onAllNodesWithContentDescription(DEFAULT_HEADER_REMOVE_BUTTON_DESCRIPTION)[0].performClick()
        composeRule.waitForIdle()

        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER + INPUT_COUNT_PER_HEADER
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.urlInput().assert(hasText(TYPED_URL))
        composeRule.headerNameInput().assert(hasText(TYPED_SECOND_HEADER_NAME))
        composeRule.headerValueInput().assert(hasText(TYPED_SECOND_HEADER_VALUE))
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-005 추가에 성공하면 다음 웹 항목을 작성할 수 있는 상태로 초기화한다`() {
        setWebAddScreen(viewModel = effectViewModel(effect = WebAddEffect.AddSucceeded(id = Uuid.random())))
        composeRule.fillAllInput()

        composeRule.clickAdd()

        composeRule.titleInput().assert(hasText(""))
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.urlInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-009 제목이 공백이면 작성 내용은 유지된다`() {
        assertInvalidInputRetainsInput(effect = WebAddEffect.TitleBlank)
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-009 URL이 공백이면 작성 내용은 유지된다`() {
        assertInvalidInputRetainsInput(effect = WebAddEffect.UrlBlank)
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-009 헤더 이름이 공백이면 작성 내용은 유지된다`() {
        assertInvalidInputRetainsInput(effect = WebAddEffect.HeaderNameBlank)
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-012 뒤로가면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setWebAddScreen(
            viewModel = screenTestViewModel(),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    private fun assertInvalidInputRetainsInput(effect: WebAddEffect) {
        setWebAddScreen(viewModel = effectViewModel(effect = effect))
        composeRule.fillAllInput()

        composeRule.clickAdd()

        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER + INPUT_COUNT_PER_HEADER * 2
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.urlInput().assert(hasText(TYPED_URL))
        composeRule.headerNameInput().assert(hasText(TYPED_FIRST_HEADER_NAME))
        composeRule.headerValueInput().assert(hasText(TYPED_FIRST_HEADER_VALUE))
        composeRule.headerNameInput(row = 1).assert(hasText(TYPED_SECOND_HEADER_NAME))
        composeRule.headerValueInput(row = 1).assert(hasText(TYPED_SECOND_HEADER_VALUE))
    }

    @Test
    fun `TC-WEB-LIST-DETAIL-FEATURE-006 단독으로 표시되면 뒤로가기 버튼이 표시된다`() {
        setWebAddScreen(
            viewModel = screenTestViewModel(),
            componentVisible = WebAddScaffoldComponentVisible(isNavigateUpButtonVisible = true),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-WEB-LIST-DETAIL-FEATURE-005 목록과 함께 표시되면 뒤로가기 버튼이 표시되지 않는다`() {
        setWebAddScreen(
            viewModel = screenTestViewModel(),
            componentVisible = WebAddScaffoldComponentVisible(isNavigateUpButtonVisible = false),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertDoesNotExist()
    }

    private fun setWebAddScreen(
        viewModel: WebAddViewModel,
        navigateUp: () -> Unit = {},
        componentVisible: WebAddScaffoldComponentVisible = WebAddScaffoldComponentVisible(),
    ) {
        composeRule.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = navigateUp,
                    componentVisibleProvider = { componentVisible },
                    addViewModel = viewModel,
                    navigateToTagDetail = {},
                    tagViewModel = addTagScreenTestViewModel(),
                )
            }
        }
        composeRule.waitForIdle()
    }
}
