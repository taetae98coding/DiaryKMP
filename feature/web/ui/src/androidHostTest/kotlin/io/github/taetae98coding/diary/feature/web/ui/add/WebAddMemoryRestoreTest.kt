package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class WebAddMemoryRestoreTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-ADD-FEATURE-022 TagDetail 웹 탭에서 진입했으면 복원 뒤 입력은 남고 태그는 그 탭의 태그만 선택된다`() {
        val initialTag = webTestTag(title = INITIAL_TAG_TITLE)
        val otherTag = webTestTag(title = OTHER_TAG_TITLE)

        assertMemoryRestore(
            initialTag = initialTag,
            otherTag = otherTag,
            expectedTitleListAfterRestore = listOf(INITIAL_TAG_TITLE),
        )
    }

    @Test
    fun `TC-WEB-ADD-FEATURE-022 그 밖의 경로로 진입했으면 복원 뒤 입력은 남고 선택된 태그가 없다`() {
        val otherTag = webTestTag(title = OTHER_TAG_TITLE)

        assertMemoryRestore(
            initialTag = null,
            otherTag = otherTag,
            expectedTitleListAfterRestore = emptyList(),
        )
    }

    private fun assertMemoryRestore(
        initialTag: Tag?,
        otherTag: Tag,
        expectedTitleListAfterRestore: List<String>,
    ) {
        val allTagList = listOfNotNull(initialTag, otherTag)
        val tagViewModelList = mutableListOf<WebAddTagViewModel>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 메모리 정리 뒤에는 태그 선택을 들고 있던 객체도 새로 만들어지므로 복원할 때마다 진입 경로의 초기값으로 새로 만든다.
            val tagViewModel =
                remember {
                    tagViewModel(initialTagId = initialTag?.id, allTagList = allTagList).also(tagViewModelList::add)
                }

            WebAddScreenTestTheme {
                WebAddScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    componentVisibleProvider = { WebAddScaffoldComponentVisible() },
                    addViewModel = remember { screenTestViewModel() },
                    navigateToTagDetail = {},
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.fillAllInput()
        composeRule.runOnIdle { tagViewModelList.last().add(id = otherTag.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        tagViewModelList.size shouldBe 2
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.urlInput().assert(hasText(TYPED_URL))
        composeRule.headerNameInput().assert(hasText(TYPED_FIRST_HEADER_NAME))
        composeRule.headerValueInput().assert(hasText(TYPED_FIRST_HEADER_VALUE))
        composeRule.headerNameInput(row = 1).assert(hasText(TYPED_SECOND_HEADER_NAME))
        composeRule.headerValueInput(row = 1).assert(hasText(TYPED_SECOND_HEADER_VALUE))
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertDoesNotExist()
        expectedTitleListAfterRestore.forEach { title -> composeRule.onNodeWithText(title).assertExists() }
        composeRule.runOnIdle {
            tagViewModelList.last().tagIdSet.value shouldBe setOfNotNull(initialTag?.id)
        }
    }

    private fun tagViewModel(
        initialTagId: Uuid?,
        allTagList: List<Tag>,
    ): WebAddTagViewModel {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(allTagList)))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            val idSet = firstArg<Set<Uuid>>()
            flowOf(Result.success(allTagList.filter { tag -> tag.id in idSet }))
        }

        return WebAddTagViewModel(
            initialTagId = initialTagId,
            pageTagUseCase = pageTagUseCase,
            getSelectedTagUseCase = getSelectedTagUseCase,
        )
    }

    private companion object {
        const val INITIAL_TAG_TITLE = "WebAddRestoreInitialTag"
        const val OTHER_TAG_TITLE = "WebAddRestoreOtherTag"
    }
}
