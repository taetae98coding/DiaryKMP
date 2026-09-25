package io.github.taetae98coding.diary.feature.tag.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.EDIT_SUFFIX
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScreenTestHost
import io.github.taetae98coding.diary.feature.tag.ui.detail.prepareTagDetailTabViewModels
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.selectTagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.titleInput
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.viewmodel.koinViewModel
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 화면을 떠났다 돌아올 때의 상태는 전환 이력의 항목마다 저장되므로, 앱과 같은 방식으로 항목을 그리는 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-011 완료된 메모 목록에서 뒤로가면 메모 탭이 선택된 같은 태그의 TagDetail로 돌아간다`() {
        val tagId = fixtureId()
        val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagDetailNavKey(id = tagId))
        setTagNavDisplay(backStack = backStack, titleMap = mapOf(tagId to fixtureText(prefix = "Tag")))
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()
        composeRule.onNodeWithText(FINISHED_LIST_CONTENT).assertIsDisplayed()

        composeRule.runOnIdle { backStack.navigateUpFromTagMemoFinishedList() }
        composeRule.waitForIdle()

        backStack shouldContainExactly listOf(TagHomeNavKey, TagDetailNavKey(id = tagId))
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-010 완료된 메모가 없어도 메모 탭의 완료된 메모 확인으로 같은 태그의 TagMemoFinishedList 화면으로 이동한다`() {
        val tagId = fixtureId()
        val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagDetailNavKey(id = tagId))
        setTagNavDisplay(backStack = backStack, titleMap = mapOf(tagId to fixtureText(prefix = "Tag")))
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()
        composeRule.waitForIdle()

        backStack shouldContainExactly listOf(TagHomeNavKey, TagDetailNavKey(id = tagId), TagMemoFinishedListNavKey(tagId = tagId))
        composeRule.onNodeWithText(FINISHED_LIST_CONTENT).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-058 연결한 태그의 상세에서 뒤로 돌아오면 이전 태그의 상세가 고치던 제목을 유지한다`() {
        val firstId = fixtureId()
        val secondId = fixtureId()
        val titleMap = mapOf(firstId to fixtureText(prefix = "FirstTag"), secondId to fixtureText(prefix = "SecondTag"))
        val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagDetailNavKey(id = firstId))
        setTagNavDisplay(backStack = backStack, titleMap = titleMap)
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.runOnIdle { backStack.add(TagDetailNavKey(id = secondId)) }
        composeRule.waitForIdle()
        composeRule.titleInput().assert(hasText(titleMap.getValue(secondId)))

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(titleMap.getValue(firstId) + EDIT_SUFFIX))
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-059 연결이 순환해 같은 태그의 상세가 다시 열려도 새 화면이고 뒤로가면 거쳐 온 상세로 차례로 돌아간다`() {
        val firstId = fixtureId()
        val secondId = fixtureId()
        val titleMap = mapOf(firstId to fixtureText(prefix = "FirstTag"), secondId to fixtureText(prefix = "SecondTag"))
        val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagDetailNavKey(id = firstId))
        setTagNavDisplay(backStack = backStack, titleMap = titleMap)
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.runOnIdle { backStack.add(TagDetailNavKey(id = secondId)) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.add(TagDetailNavKey(id = firstId)) }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(titleMap.getValue(firstId)))

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(titleMap.getValue(secondId)))

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        backStack shouldContainExactly listOf(TagHomeNavKey, TagDetailNavKey(id = firstId))
        composeRule.titleInput().assert(hasText(titleMap.getValue(firstId) + EDIT_SUFFIX))
    }

    private fun setTagNavDisplay(
        backStack: NavBackStack<ScreenNavKey>,
        titleMap: Map<Uuid, String>,
    ) {
        prepareTagDetailTabViewModels()
        composeRule.setContent {
            TagDetailScreenTestHost {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<TagHomeNavKey> { Text(text = TAG_HOME_CONTENT) }
                            entry<TagDetailNavKey>(
                                clazzContentKey = { key -> backStack.tagDetailContentKey(key) },
                            ) { navKey ->
                                TagDetailScreen(
                                    navigateUp = { backStack.removeLastOrNull() },
                                    navigateToTagAdd = {},
                                    navigateToDetail = { id -> backStack.add(TagDetailNavKey(id)) },
                                    navigateToMemoAdd = {},
                                    navigateToMemoDetail = {},
                                    navigateToMemoFinishedList = { backStack.add(TagMemoFinishedListNavKey(tagId = navKey.id)) },
                                    navigateToWebAdd = {},
                                    navigateToWebDetail = {},
                                    navigateToPlaceAdd = {},
                                    navigateToPlaceDetail = {},
                                    id = navKey.id,
                                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                                    componentVisibleProvider = { TagDetailScaffoldComponentVisible() },
                                    detailViewModel =
                                        remember(navKey.id) {
                                            screenTestViewModel(
                                                MutableStateFlow(tagDetailUiState(id = navKey.id, detail = tagDetail(title = titleMap.getValue(navKey.id)))),
                                            )
                                        },
                                    placeMapViewModel = koinViewModel(),
                                )
                            }
                            entry<TagMemoFinishedListNavKey> { Text(text = FINISHED_LIST_CONTENT) }
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val TAG_HOME_CONTENT = "TagHomeContent"
        const val FINISHED_LIST_CONTENT = "TagMemoFinishedListContent"
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
    }
}
