package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.paging.PagingData
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.AddWebUseCase
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.web.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class WebAddTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-9e1b1cea-5b20-4c59-b931-ad3dd58e4bdb 추가에 성공해도 고른 태그는 유지한다`() {
        val tag = webTestTag(title = SELECTED_TAG_TITLE)
        composeRule.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    addedResultRequestKey = null,
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    componentVisibleProvider = { WebAddScaffoldComponentVisible() },
                    addViewModel = effectViewModel(effect = WebAddEffect.AddSucceeded(id = Uuid.random())),
                    navigateToTagDetail = {},
                    tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)),
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.fillAllInput()

        composeRule.clickAdd()

        composeRule.titleInput().assert(hasText(""))
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.urlInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-479957d3-8ce6-49fd-9ead-e1d414718bf1 태그를 골라도 저장된 웹 항목의 연결은 바뀌지 않는다`() {
        val tag = webTestTag(title = SELECTED_TAG_TITLE)
        val addWebUseCase = mockk<AddWebUseCase>()
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(listOf(tag))))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            val idSet = firstArg<Set<Uuid>>()
            flowOf(Result.success(listOf(tag).filter { value -> value.id in idSet }))
        }
        composeRule.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    addedResultRequestKey = null,
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    componentVisibleProvider = { WebAddScaffoldComponentVisible() },
                    addViewModel = remember { WebAddViewModel(addWebUseCase = addWebUseCase) },
                    navigateToTagDetail = {},
                    tagViewModel =
                        remember {
                            EntityAddTagViewModel(
                                initialTagId = null,
                                pageTagUseCase = pageTagUseCase,
                                getSelectedTagUseCase = getSelectedTagUseCase,
                            )
                        },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        awaitTagTitle(count = 1)

        composeRule.onNodeWithText(SELECTED_TAG_TITLE).performClick()

        // 고른 태그는 선택 목록의 항목과 태그 입력의 연결 대상 표시에 함께 나타난다.
        awaitTagTitle(count = 2)
        coVerify(exactly = 0) { addWebUseCase(any()) }
    }

    // 선택 목록은 페이지 단위로 준비되므로 항목이 나타날 때까지 프레임과 실제 시간을 함께 진행시킨다.
    private fun awaitTagTitle(count: Int) {
        repeat(PICKER_WAIT_ATTEMPT_COUNT) {
            composeRule.waitForIdle()
            if (composeRule.onAllNodesWithText(SELECTED_TAG_TITLE).fetchSemanticsNodes().size == count) return
            composeRule.mainClock.advanceTimeByFrame()
            @Suppress("ForbiddenMethodCall")
            Thread.sleep(PICKER_WAIT_INTERVAL_MILLIS)
        }

        error("$SELECTED_TAG_TITLE 표시가 $count 개가 되지 않았다")
    }

    @Test
    fun `TC-a4b7ffe1-642f-4161-8a83-6e01ed06611a 화면이 재생성되어도 고른 태그가 유지된다`() {
        val tag = webTestTag(title = SELECTED_TAG_TITLE)
        val tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    addedResultRequestKey = null,
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    componentVisibleProvider = { WebAddScaffoldComponentVisible() },
                    addViewModel = screenTestViewModel(),
                    navigateToTagDetail = {},
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.urlInput().performTextInput(TYPED_URL)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.urlInput().assert(hasText(TYPED_URL))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    private companion object {
        const val SELECTED_TAG_TITLE = "WebAddSelectedTag"
        const val DEFAULT_ENTITY_TAG_LABEL = "Select tag"
        const val PICKER_WAIT_ATTEMPT_COUNT = 500
        const val PICKER_WAIT_INTERVAL_MILLIS = 10L
    }
}
