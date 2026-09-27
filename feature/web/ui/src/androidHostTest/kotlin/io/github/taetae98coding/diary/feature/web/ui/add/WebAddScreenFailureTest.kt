package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.domain.web.usecase.AddWebUseCase
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
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
class WebAddScreenFailureTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-ADD-FEATURE-021 저장에 실패하면 안내 없이 작성 내용과 고른 태그를 유지하고 같은 내용으로 다시 추가할 수 있다`() {
        val tag = webTestTag(title = SELECTED_TAG_TITLE)
        val useCase = mockk<AddWebUseCase>()
        coEvery { useCase(any()) } returns Result.failure(IllegalStateException("저장 실패"))
        composeRule.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    componentVisibleProvider = { WebAddScaffoldComponentVisible() },
                    addViewModel = WebAddViewModel(addWebUseCase = useCase),
                    navigateToTagDetail = {},
                    tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)),
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.fillAllInput()

        composeRule.clickAdd()

        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_URL_BLANK_MESSAGE).assertDoesNotExist()
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.urlInput().assert(hasText(TYPED_URL))
        composeRule.headerNameInput().assert(hasText(TYPED_FIRST_HEADER_NAME))
        composeRule.headerValueInput().assert(hasText(TYPED_FIRST_HEADER_VALUE))
        composeRule.headerNameInput(row = 1).assert(hasText(TYPED_SECOND_HEADER_NAME))
        composeRule.headerValueInput(row = 1).assert(hasText(TYPED_SECOND_HEADER_VALUE))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()

        composeRule.clickAdd()

        val expected =
            AddWebUseCase.Parameter(
                detail =
                    WebDetail(
                        title = TYPED_TITLE,
                        description = TYPED_DESCRIPTION,
                        url = TYPED_URL,
                        headerList =
                            listOf(
                                WebHeader(name = TYPED_FIRST_HEADER_NAME, value = TYPED_FIRST_HEADER_VALUE),
                                WebHeader(name = TYPED_SECOND_HEADER_NAME, value = TYPED_SECOND_HEADER_VALUE),
                            ),
                    ),
                tagIdSet = setOf(tag.id),
            )
        coVerify(exactly = 2) { useCase(expected) }
    }

    private companion object {
        const val SELECTED_TAG_TITLE = "WebAddFailureTag"
        const val DEFAULT_ADD_SUCCEEDED_MESSAGE = "Web added."
        const val DEFAULT_TITLE_BLANK_MESSAGE = "Please enter a title."
        const val DEFAULT_URL_BLANK_MESSAGE = "Please enter a URL."
    }
}
