@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.app.shared.navigation.appEntryProvider
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.api.navigateToRoutineDetail
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagRoutineFinishedListNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 완료된 루틴 목록과 루틴 상세가 어떻게 놓이는지는 두 기능이 앱에 등록한 배치 정보가 정한다.
// 두 기능의 실제 배치 정보와 선택 전 상세를 그대로 쓰고, 두 화면의 본문만 이름이 보이는 대역으로 바꿔 창 크기별로 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagRoutineFinishedListLayoutTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-164dda64-b811-470e-9034-230fcc269454 TC-154ae2ec-9e89-4d9b-890e-ad4f6ea31964 넓은 창에서 완료된 루틴 목록은 선택 전 안내만 있는 상세 영역과 함께 표시한다`() {
        val backStack = finishedListBackStack(tagId = Arb.kotlinUuid().next())
        setAppNavDisplay(backStack = backStack)

        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertIsDisplayed()
        // 목록 본문은 누를 수 없는 대역이므로, 누를 수 있는 것이 없으면 선택 전 상세에도 실행할 동작이 없다.
        composeRule.onAllNodes(hasClickAction()).assertCountEquals(0)
        composeRule.onNodeWithText(RoutineAddNavKey().screenName).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko-w1280dp-h1600dp")
    fun `TC-154ae2ec-9e89-4d9b-890e-ad4f6ea31964 한국어 환경의 선택 전 안내는 루틴을 선택하세요다`() {
        setAppNavDisplay(backStack = finishedListBackStack(tagId = Arb.kotlinUuid().next()))

        composeRule.onNodeWithText(KOREAN_PLACEHOLDER_TEXT).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun `TC-8c529fc6-2dbd-4189-bd40-8f2893c67078 한 영역만 쓰는 창에서는 현재 화면만 단독으로 표시한다`() {
        val backStack = finishedListBackStack(tagId = Arb.kotlinUuid().next())
        setAppNavDisplay(backStack = backStack)
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertDoesNotExist()

        composeRule.runOnIdle { backStack.navigateToRoutineDetail(id = Arb.kotlinUuid().next()) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-68f75b15-fdba-4cbb-8763-32fcbdd68f97 TC-7f33f4ee-03f0-4610-acf1-16c2c7d2ec87 넓은 창에서 루틴을 이어 선택해도 목록을 유지한 채 상세 영역에 그 루틴의 상세가 놓인다`() {
        val tagId = Arb.kotlinUuid().next()
        val backStack = finishedListBackStack(tagId = tagId)
        setAppNavDisplay(backStack = backStack)

        List(2) { Arb.kotlinUuid().next() }.forEach { routineId ->
            composeRule.runOnIdle { backStack.navigateToRoutineDetail(id = routineId) }
            composeRule.waitForIdle()

            backStack.toList() shouldBe finishedListBackStack(tagId = tagId).toList() + RoutineDetailNavKey(id = routineId)
            composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
            composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
            composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertDoesNotExist()
        }
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-927bb652-dd2c-486a-848d-aedb5df309af 넓은 창에서 루틴 상세를 연 뒤 뒤로가면 목록을 유지하고 선택 전 상태로 돌아간다`() {
        assertBackToPlaceholder(detailCount = 1)
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-927bb652-dd2c-486a-848d-aedb5df309af 넓은 창에서 다른 루틴의 상세로 바꾼 뒤 뒤로가면 이전 루틴이 아닌 선택 전 상태로 돌아간다`() {
        assertBackToPlaceholder(detailCount = 2)
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-d1d859f0-ded0-4a98-9b1e-72de4ac09d74 넓은 창에서 상세가 선택 전 상태일 때 뒤로가면 완료된 루틴 목록 전체를 떠나 TagDetail로 돌아간다`() {
        val tagId = Arb.kotlinUuid().next()
        val backStack = finishedListBackStack(tagId = tagId)
        setAppNavDisplay(backStack = backStack)

        pressBack()

        backStack.toList() shouldBe listOf(TagHomeNavKey, TagDetailNavKey(id = tagId))
        composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-ddfbc029-f7d8-45ac-b473-726c5e0aee61 넓은 창에서 상세 영역의 루틴에서 연 태그 화면은 단독으로 표시하고 돌아오면 떠나기 전의 루틴이 놓인다`() {
        val backStack = finishedListBackStack(tagId = Arb.kotlinUuid().next())
        val openedKeyList: List<ScreenNavKey> =
            listOf(
                TagDetailNavKey(id = Arb.kotlinUuid().next()),
                TagAddNavKey(requestKey = Arb.kotlinUuid().next()),
            )
        setAppNavDisplay(backStack = backStack)
        composeRule.runOnIdle { backStack.navigateToRoutineDetail(id = Arb.kotlinUuid().next()) }
        composeRule.waitForIdle()
        val selectedDetailKey = backStack.last()

        openedKeyList.forEach { openedKey ->
            composeRule.runOnIdle { backStack.add(openedKey) }
            composeRule.waitForIdle()

            composeRule.onNodeWithText(contentOf(openedKey)).assertIsDisplayed()
            composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
            composeRule.onNodeWithText(DETAIL_CONTENT).assertDoesNotExist()

            pressBack()

            backStack.last() shouldBe selectedDetailKey
            composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
            composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        }
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-9aaaa735-3a42-4c94-907b-087dd192cc80 TagRoutineFinishedList에서 진입하지 않은 루틴 화면은 이 배치의 선택 전 상세나 목록과 함께 놓이지 않는다`() {
        val tagId = Arb.kotlinUuid().next()
        val tagDetailPath = listOf(TagHomeNavKey, TagDetailNavKey(id = tagId))
        val backStackCases: List<List<ScreenNavKey>> =
            listOf(
                tagDetailPath + RoutineDetailNavKey(id = Arb.kotlinUuid().next(), isStandalone = true),
                tagDetailPath + RoutineAddNavKey(initialTagId = tagId),
                listOf(RoutineHomeNavKey, RoutineDetailNavKey(id = Arb.kotlinUuid().next())),
                listOf(RoutineHomeNavKey, RoutineFinishedListNavKey, RoutineDetailNavKey(id = Arb.kotlinUuid().next())),
                listOf(SearchHomeNavKey(initialType = SearchHomeType.ROUTINE), RoutineDetailNavKey(id = Arb.kotlinUuid().next(), isStandalone = true)),
            )
        val backStack = NavBackStack(*backStackCases.first().toTypedArray())
        setAppNavDisplay(backStack = backStack)

        backStackCases.forEach { keyList ->
            composeRule.runOnIdle {
                backStack.clear()
                backStack.addAll(keyList)
            }
            composeRule.waitForIdle()

            composeRule.onNodeWithText(contentOf(keyList.last())).assertIsDisplayed()
            composeRule.onNodeWithText(LIST_CONTENT).assertDoesNotExist()
        }
    }

    private fun assertBackToPlaceholder(detailCount: Int) {
        val tagId = Arb.kotlinUuid().next()
        val backStack = finishedListBackStack(tagId = tagId)
        setAppNavDisplay(backStack = backStack)
        repeat(detailCount) {
            composeRule.runOnIdle { backStack.navigateToRoutineDetail(id = Arb.kotlinUuid().next()) }
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()

        pressBack()

        backStack.toList() shouldBe finishedListBackStack(tagId = tagId).toList()
        composeRule.onNodeWithText(LIST_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertDoesNotExist()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun finishedListBackStack(tagId: Uuid): NavBackStack<ScreenNavKey> = NavBackStack(TagHomeNavKey, TagDetailNavKey(id = tagId), TagRoutineFinishedListNavKey(tagId = tagId))

    private fun setAppNavDisplay(backStack: NavBackStack<ScreenNavKey>) {
        composeRule.setContent {
            val appEntryProvider =
                remember { appEntryProvider(backStack = backStack) }

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
            is TagRoutineFinishedListNavKey -> LIST_CONTENT
            is RoutineDetailNavKey -> DETAIL_CONTENT
            else -> key.screenName
        }

    private companion object {
        const val LIST_CONTENT = "TagRoutineFinishedListContent"
        const val DETAIL_CONTENT = "RoutineDetailContent"
        const val DEFAULT_PLACEHOLDER_TEXT = "Choose a routine"
        const val KOREAN_PLACEHOLDER_TEXT = "루틴을 선택하세요"
    }
}
