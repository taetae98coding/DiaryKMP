package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.MusicTagTestTheme
import io.github.taetae98coding.diary.feature.playlist.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.playlist.ui.addTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.addTagViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.clickEntityTagInput
import io.github.taetae98coding.diary.feature.playlist.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.playlist.ui.sendTagAddedResult
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MusicAddTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-37995e86-eb74-4953-b52d-764706eaa1cd 추가에 성공해도 고른 태그는 유지한다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        setMusicAddScreen(
            addViewModel = effectViewModel(effect = MusicAddEffect.AddSucceeded),
            tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)),
        )
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.waitForIdle()

        composeRule.clickAdd()

        composeRule.titleInput().assert(hasText(""))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-22b0c5f3-6b9d-402d-a599-8140be1226e2 추가할 때 고른 태그를 함께 넘긴다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        val addViewModel = effectViewModel(effect = MusicAddEffect.AddSucceeded)
        setMusicAddScreen(
            addViewModel = addViewModel,
            tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)),
        )
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.waitForIdle()

        composeRule.clickAdd()

        verify(exactly = 1) { addViewModel.add(detail = any(), tagIdSet = setOf(tag.id)) }
    }

    @Test
    fun `TC-4e1242a7-fd2a-4f4f-8491-07c739a5a3e8 화면이 재생성되어도 고른 태그가 유지된다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        val tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            MusicTagTestTheme {
                MusicAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    componentVisibleProvider = { MusicAddScaffoldComponentVisible() },
                    addViewModel = screenTestViewModel(),
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-2a0869ef-91b6-4384-b231-7e0a9405855c TagDetail 곡 탭에서 진입했으면 복원 뒤 입력은 남고 태그는 그 탭의 태그만 선택된다`() {
        assertMemoryRestore(
            initialTag = Arb.tag(title = INITIAL_TAG_TITLE).next(),
            expectedTitleListAfterRestore = listOf(INITIAL_TAG_TITLE),
        )
    }

    @Test
    fun `TC-2a0869ef-91b6-4384-b231-7e0a9405855c 그 밖의 경로로 진입했으면 복원 뒤 입력은 남고 선택된 태그가 없다`() {
        assertMemoryRestore(
            initialTag = null,
            expectedTitleListAfterRestore = emptyList(),
        )
    }

    @Test
    fun `TagAdd 화면에서 추가한 태그가 돌아오면 고른 태그로 더한다`() {
        val addedTag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        val tagViewModel = addTagScreenTestViewModel()
        val resultEventBus = ResultEventBus()
        setMusicAddScreen(resultEventBus = resultEventBus, tagViewModel = tagViewModel)

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        verify(exactly = 1) { tagViewModel.add(tagId = addedTag.id) }
    }

    @Test
    fun `고를 수 있는 태그가 없으면 태그 추가 칩이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setMusicAddScreen(navigateToTagAdd = { tagAddCount += 1 })

        composeRule.clickEntityTagInput()

        tagAddCount shouldBe 1
    }

    private fun setMusicAddScreen(
        addViewModel: MusicAddViewModel = screenTestViewModel(),
        tagViewModel: EntityAddTagViewModel = addTagScreenTestViewModel(),
        navigateToTagAdd: () -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        composeRule.setContent {
            MusicTagTestTheme(resultEventBus = resultEventBus) {
                MusicAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    componentVisibleProvider = { MusicAddScaffoldComponentVisible() },
                    addViewModel = addViewModel,
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun assertMemoryRestore(
        initialTag: Tag?,
        expectedTitleListAfterRestore: List<String>,
    ) {
        val otherTag = Arb.tag(title = OTHER_TAG_TITLE).next()
        val allTagList = listOfNotNull(initialTag, otherTag)
        val tagViewModelList = mutableListOf<EntityAddTagViewModel>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 메모리 정리 뒤에는 태그 선택을 들고 있던 객체도 새로 만들어지므로 복원할 때마다 진입 경로의 초기값으로 새로 만든다.
            val tagViewModel =
                remember {
                    addTagViewModel(initialTagId = initialTag?.id, allTagList = allTagList).also(tagViewModelList::add)
                }

            MusicTagTestTheme {
                MusicAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    componentVisibleProvider = { MusicAddScaffoldComponentVisible() },
                    addViewModel = remember { screenTestViewModel() },
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.fillAllInput()
        composeRule.runOnIdle { tagViewModelList.last().add(tagId = otherTag.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        tagViewModelList.size shouldBe 2
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.artistInput().assert(hasText(TYPED_ARTIST))
        composeRule.linkInput().assert(hasText(TYPED_LINK))
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertDoesNotExist()
        expectedTitleListAfterRestore.forEach { title -> composeRule.onNodeWithText(title).assertExists() }
        composeRule.runOnIdle {
            tagViewModelList
                .last()
                .selectionUiState.value.tagIdSet shouldBe setOfNotNull(initialTag?.id)
        }
    }

    private companion object {
        const val SELECTED_TAG_TITLE = "MusicAddSelectedTag"
        const val INITIAL_TAG_TITLE = "MusicAddRestoreInitialTag"
        const val OTHER_TAG_TITLE = "MusicAddRestoreOtherTag"
    }
}
