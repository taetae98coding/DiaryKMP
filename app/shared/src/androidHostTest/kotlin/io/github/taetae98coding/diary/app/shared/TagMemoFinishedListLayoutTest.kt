@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.ui.memoEntry
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.ui.tagEntry
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// 완료된 메모 목록과 메모 상세가 어떻게 놓이는지는 두 기능이 앱에 등록한 배치 정보가 정한다.
// 두 기능의 실제 배치 정보와 선택 전 상세를 그대로 쓰고, 두 화면의 본문만 이름이 보이는 대역으로 바꿔 창 크기별로 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagMemoFinishedListLayoutTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-003 넓은 창에서 메모를 선택하면 목록을 유지한 채 상세 영역에 그 메모의 상세가 놓인다`() {
        val tagId = fixtureMonkey.giveMeOne<Uuid>()
        val backStack = finishedListBackStack(tagId = tagId)
        setAppNavDisplay(backStack = backStack)
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertIsDisplayed()

        composeRule.runOnIdle { backStack.add(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun `TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-005 한 영역만 쓰는 창에서는 현재 화면만 단독으로 표시한다`() {
        val tagId = fixtureMonkey.giveMeOne<Uuid>()
        val backStack = finishedListBackStack(tagId = tagId)
        setAppNavDisplay(backStack = backStack)
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertDoesNotExist()

        composeRule.runOnIdle { backStack.add(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-009 넓은 창에서 메모 상세를 연 뒤 뒤로가면 목록을 유지하고 선택 전 상태로 돌아간다`() {
        val tagId = fixtureMonkey.giveMeOne<Uuid>()
        val backStack = finishedListBackStack(tagId = tagId)
        setAppNavDisplay(backStack = backStack)
        composeRule.runOnIdle { backStack.add(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()

        pressBack()

        backStack.toList() shouldBe listOf(TagHomeNavKey, TagDetailNavKey(id = tagId), TagMemoFinishedListNavKey(tagId = tagId))
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertDoesNotExist()
    }

    // 다른 메모를 고르면 앞 상세를 쌓지 않고 바꾸는 것은 feature:tag:ui의 전환 테스트가 다루므로, 여기서는 바뀐 뒤의 전환 이력에서 뒤로가기 결과만 본다.
    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-009 넓은 창에서 다른 메모의 상세로 바꾼 뒤 뒤로가면 이전 메모가 아닌 선택 전 상태로 돌아간다`() {
        val tagId = fixtureMonkey.giveMeOne<Uuid>()
        val backStack = finishedListBackStack(tagId = tagId)
        setAppNavDisplay(backStack = backStack)
        composeRule.runOnIdle { backStack.add(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            backStack.removeLastOrNull()
            backStack.add(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()

        pressBack()

        backStack.toList() shouldBe listOf(TagHomeNavKey, TagDetailNavKey(id = tagId), TagMemoFinishedListNavKey(tagId = tagId))
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-018 넓은 창에서 상세가 선택 전 상태일 때 뒤로가면 완료된 메모 목록 전체를 떠나 TagDetail로 돌아간다`() {
        val tagId = fixtureMonkey.giveMeOne<Uuid>()
        val backStack = finishedListBackStack(tagId = tagId)
        setAppNavDisplay(backStack = backStack)

        pressBack()

        backStack.toList() shouldBe listOf(TagHomeNavKey, TagDetailNavKey(id = tagId))
        composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun finishedListBackStack(tagId: Uuid): NavBackStack<ScreenNavKey> = NavBackStack(TagHomeNavKey, TagDetailNavKey(id = tagId), TagMemoFinishedListNavKey(tagId = tagId))

    private fun setAppNavDisplay(backStack: NavBackStack<ScreenNavKey>) {
        composeRule.setContent {
            val appEntryProvider =
                remember {
                    entryProvider<ScreenNavKey> {
                        memoEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                        tagEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                    }
                }

            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider = { key ->
                        NavEntry(key = key, metadata = appEntryProvider(key).metadata) { Text(text = contentOf(key)) }
                    },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun contentOf(key: ScreenNavKey): String =
        when (key) {
            is TagMemoFinishedListNavKey -> LIST_CONTENT
            is MemoDetailNavKey -> DETAIL_CONTENT
            else -> OTHER_CONTENT
        }

    private companion object {
        const val LIST_CONTENT = "TagMemoFinishedListContent"
        const val DETAIL_CONTENT = "MemoDetailContent"
        const val OTHER_CONTENT = "OtherContent"
        const val DEFAULT_PLACEHOLDER_TEXT = "Choose a memo"
    }
}
