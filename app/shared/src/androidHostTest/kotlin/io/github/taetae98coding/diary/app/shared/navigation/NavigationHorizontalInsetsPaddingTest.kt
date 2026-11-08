@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared.navigation

import android.view.View
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 가로 시스템 영역을 내비게이션 루트가 한 번만 피하는지는 두 영역이 나란히 놓이는 목록·상세 배치로 확인한다.
// 각 영역의 화면이 가로 inset을 다시 피하면 시작 쪽이 아닌 영역의 본문이 그만큼 밀리기 때문이다.
// AppNavigation은 Koin이 등록한 실제 화면과 ViewModel을 모두 띄워야 해서 여기서 구성할 수 없으므로,
// 같은 배치 정보를 쓰는 NavDisplay에 AppNavigation이 붙이는 modifier를 직접 붙여 확인한다. AppNavigation이 그 modifier를 붙이는지는 이 테스트가 확인하지 않는다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h800dp")
class NavigationHorizontalInsetsPaddingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `시작 쪽 시스템 영역만큼 목록 영역을 밀고 상세 영역은 창 끝에 맞춘다`() {
        setListDetailNavDisplay()

        val (listPane, detailPane) = paneBoundsList()
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot

        listPane.left shouldBe (rootBounds.left + startInsetPx() plusOrMinus PIXEL_TOLERANCE)
        detailPane.right shouldBe (rootBounds.right plusOrMinus PIXEL_TOLERANCE)
    }

    @Test
    fun `두 영역의 화면은 가로 시스템 영역을 다시 피하지 않아 본문이 영역 시작에 놓인다`() {
        setListDetailNavDisplay()

        val (listPane, detailPane) = paneBoundsList()

        contentBounds(LIST_CONTENT).left shouldBe (listPane.left plusOrMinus PIXEL_TOLERANCE)
        contentBounds(DETAIL_CONTENT).left shouldBe (detailPane.left plusOrMinus PIXEL_TOLERANCE)
    }

    private fun setListDetailNavDisplay() {
        val backStack = NavBackStack(MemoHomeNavKey, MemoFinishedListNavKey, MemoDetailNavKey(id = Arb.kotlinUuid().next()))
        lateinit var view: View

        composeRule.setContent {
            val appEntryProvider = remember { appEntryProvider(backStack = backStack) }
            view = LocalView.current

            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.navigationHorizontalInsetsPadding(),
                    sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider = { key ->
                        NavEntry(key = key, metadata = appEntryProvider(key).metadata) { PaneContent(text = contentOf(key)) }
                    },
                )
            }
        }
        composeRule.runOnIdle {
            val insets =
                WindowInsetsCompat
                    .Builder()
                    .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(startInsetPx().toInt(), 0, 0, 0))
                    .build()

            ViewCompat.dispatchApplyWindowInsets(view.rootView, insets)
        }
        composeRule.waitForIdle()
    }

    // 실제 화면처럼 Scaffold가 시스템 영역을 피한 본문을 둔다.
    @Composable
    private fun PaneContent(text: String) {
        Scaffold(contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets) { paddingValues ->
            Text(
                text = text,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }

    private fun contentOf(key: ScreenNavKey): String =
        when (key) {
            is MemoFinishedListNavKey -> LIST_CONTENT
            is MemoDetailNavKey -> DETAIL_CONTENT
            else -> key.toString()
        }

    private fun startInsetPx(): Float = with(composeRule.density) { START_INSET.roundToPx().toFloat() }

    private fun contentBounds(text: String): Rect = composeRule.onNodeWithText(text).fetchSemanticsNode().boundsInRoot

    // 목록과 상세 영역은 pane 제목 시맨틱스를 가지며 시작 쪽부터 목록, 상세 순으로 놓인다.
    private fun paneBoundsList(): List<Rect> {
        val boundsList =
            composeRule
                .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.PaneTitle))
                .fetchSemanticsNodes()
                .map { node -> node.boundsInRoot }
                .sortedBy { bounds -> bounds.left }

        boundsList.size shouldBe PANE_COUNT

        return boundsList
    }

    private companion object {
        const val LIST_CONTENT = "MemoFinishedListContent"
        const val DETAIL_CONTENT = "MemoDetailContent"
        const val PANE_COUNT = 2
        const val PIXEL_TOLERANCE = 1F

        val START_INSET = 200.dp
    }
}
