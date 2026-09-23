package io.github.taetae98coding.diary.app.shared.scaffold

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.floats.shouldBeGreaterThan
import io.kotest.matchers.floats.shouldBeLessThan
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `compact 창은 하단 바를 표시한다`() {
        setAppScaffold(
            mockWindowInfo(mutableStateOf(windowSize(width = 400.dp, height = 800.dp))),
        )

        assertHorizontalNavigationItems()
    }

    @Test
    fun `넓은 창은 내비게이션 레일을 표시한다`() {
        setAppScaffold(
            mockWindowInfo(mutableStateOf(windowSize(width = 800.dp, height = 800.dp))),
        )

        assertVerticalNavigationItems()
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-FEATURE-002 현재 목적지만 선택 상태로 표시한다`() {
        setAppScaffold(
            mockWindowInfo(mutableStateOf(windowSize(width = 400.dp, height = 800.dp))),
        )

        composeRule.onNode(navigationItem(DEFAULT_MEMO_LABEL), useUnmergedTree = true).assertIsSelected()
        DEFAULT_UNSELECTED_LABELS.forEach { label ->
            composeRule.onNode(navigationItem(label), useUnmergedTree = true).assertIsNotSelected()
        }
    }

    @Test
    fun `선택 여부와 관계없이 모든 목적지 이름을 표시한다`() {
        setAppScaffold(
            mockWindowInfo(mutableStateOf(windowSize(width = 400.dp, height = 800.dp))),
        )

        DEFAULT_LABELS.forEach { label ->
            composeRule.onNode(navigationLabel(label), useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    fun `기본 언어 목적지 이름을 제공한다`() {
        setAppScaffold(
            mockWindowInfo(mutableStateOf(windowSize(width = 400.dp, height = 800.dp))),
        )

        DEFAULT_LABELS.forEach { label ->
            composeRule.onNodeWithContentDescription(label, useUnmergedTree = true).assertExists()
        }
        composeRule.onNode(navigationLabel(DEFAULT_MEMO_LABEL), useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 목적지 이름을 제공한다`() {
        setAppScaffold(
            mockWindowInfo(mutableStateOf(windowSize(width = 400.dp, height = 800.dp))),
        )

        KOREAN_LABELS.forEach { label ->
            composeRule.onNodeWithContentDescription(label, useUnmergedTree = true).assertExists()
        }
        composeRule.onNode(navigationLabel(KOREAN_MEMO_LABEL), useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `실행 중 창 환경에 맞춰 형태를 변경한다`() {
        val containerSize = mutableStateOf(windowSize(width = 400.dp, height = 800.dp))
        setAppScaffold(mockWindowInfo(containerSize))
        assertHorizontalNavigationItems()

        composeRule.runOnIdle {
            containerSize.value = windowSize(width = 800.dp, height = 800.dp)
        }

        assertVerticalNavigationItems()
    }

    private fun setAppScaffold(windowInfo: WindowInfo) {
        composeRule.setContent {
            CompositionLocalProvider(LocalWindowInfo provides windowInfo) {
                DiaryTheme {
                    AppScaffold(
                        appState =
                            AppState(
                                backStack = NavBackStack(TopLevelNavigation.Memo.key),
                                scaffoldState = rememberNavigationSuiteScaffoldState(),
                                paneScaffoldDirectiveProvider = { PaneScaffoldDirective.Default },
                            ),
                        content = {},
                    )
                }
            }
        }
    }

    private fun windowSize(
        width: Dp,
        height: Dp,
    ): IntSize =
        with(composeRule.density) {
            IntSize(width = width.roundToPx(), height = height.roundToPx())
        }

    private fun assertHorizontalNavigationItems() {
        val memo = navigationItemCenter(DEFAULT_MEMO_LABEL)
        val tag = navigationItemCenter(DEFAULT_TAG_LABEL)

        abs(memo.y - tag.y) shouldBeLessThan POSITION_TOLERANCE
        abs(memo.x - tag.x) shouldBeGreaterThan POSITION_TOLERANCE
    }

    private fun assertVerticalNavigationItems() {
        val memo = navigationItemCenter(DEFAULT_MEMO_LABEL)
        val tag = navigationItemCenter(DEFAULT_TAG_LABEL)

        abs(memo.x - tag.x) shouldBeLessThan POSITION_TOLERANCE
        abs(memo.y - tag.y) shouldBeGreaterThan POSITION_TOLERANCE
    }

    private fun navigationItemCenter(contentDescription: String): Offset =
        composeRule
            .onNode(navigationItem(contentDescription), useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
            .center

    public companion object {
        private const val POSITION_TOLERANCE = 1F
        private const val DEFAULT_TAG_LABEL = "Tag"
        private const val KOREAN_MEMO_LABEL = "메모"
        private const val DEFAULT_MEMO_LABEL = "Memo"
        private val DEFAULT_LABELS = listOf("Memo", "Tag", "Calendar", "Routine", "More")
        private val DEFAULT_UNSELECTED_LABELS = DEFAULT_LABELS - DEFAULT_MEMO_LABEL
        private val KOREAN_LABELS = listOf("메모", "태그", "캘린더", "루틴", "더보기")

        private fun navigationItem(label: String): SemanticsMatcher = hasClickAction() and hasAnyDescendant(hasContentDescription(label))

        private fun navigationLabel(label: String): SemanticsMatcher = hasText(label) and hasAnyAncestor(navigationItem(label))
    }
}

private fun mockWindowInfo(containerSize: State<IntSize>): WindowInfo {
    val windowInfo = mockk<WindowInfo>()
    every { windowInfo.containerSize } answers { containerSize.value }
    every { windowInfo.isWindowFocused } returns true
    return windowInfo
}
