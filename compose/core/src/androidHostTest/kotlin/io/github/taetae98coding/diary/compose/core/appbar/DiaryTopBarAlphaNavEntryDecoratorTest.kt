package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.compose.core.animation.DiaryFadeContentTransform
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.floats.shouldBeGreaterThan
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryTopBarAlphaNavEntryDecoratorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `화면이 바뀌는 동안 들어오는 화면의 상단 바 불투명도는 화면 전환과 함께 0에서 1로 바뀐다`() {
        val backStack = mutableStateListOf(FIRST)
        val alphaProviderByKey = mutableMapOf<String, () -> Float>()
        composeRule.setContent {
            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryDecorators = listOf(rememberDiaryTopBarAlphaNavEntryDecorator()),
                    transitionSpec = { DiaryFadeContentTransform },
                    popTransitionSpec = { DiaryFadeContentTransform },
                    predictivePopTransitionSpec = { DiaryFadeContentTransform },
                    entryProvider = { key ->
                        NavEntry(key) {
                            alphaProviderByKey[key] = LocalDiaryTopBarAlpha.current
                        }
                    },
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.mainClock.autoAdvance = false
        composeRule.runOnUiThread {
            backStack.add(SECOND)
            Snapshot.sendApplyNotifications()
        }
        repeat(TRANSITION_START_FRAME_COUNT) { composeRule.mainClock.advanceTimeByFrame() }

        val secondAlpha = alphaProviderByKey.getValue(SECOND)()
        val firstAlpha = alphaProviderByKey.getValue(FIRST)()
        secondAlpha shouldBeLessThan 1F
        firstAlpha shouldBeGreaterThan 0F

        composeRule.mainClock.advanceTimeBy(TRANSITION_MILLIS)

        alphaProviderByKey.getValue(SECOND)() shouldBe 1F
    }

    @Test
    fun `전환 정보를 주는 쪽이 없으면 상단 바는 불투명하다`() {
        var alpha = 0F
        composeRule.setContent {
            alpha = LocalDiaryTopBarAlpha.current()
        }

        composeRule.runOnIdle { alpha shouldBe 1F }
    }

    private companion object {
        const val FIRST = "first"
        const val SECOND = "second"
        const val TRANSITION_MILLIS = 2_000L
        const val TRANSITION_START_FRAME_COUNT = 5
    }
}
