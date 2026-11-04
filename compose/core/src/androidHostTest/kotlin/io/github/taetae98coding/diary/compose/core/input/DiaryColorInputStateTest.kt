package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryColorInputStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `animateTo가 완료되면 목표 컬러가 된다`() {
        val state = DiaryColorInputState(initialColor = randomColor())
        val newColor = randomColor()

        composeRule.setContent {
            LaunchedEffect(state) {
                state.animateTo(color = newColor)
            }
        }
        composeRule.waitForIdle()

        state.color.toArgb() shouldBe newColor.toArgb()
    }

    public companion object {
        private fun randomColor(): Color = Color(color = 0xFF000000.toInt() or (Arb.int().next() and 0xFFFFFF))
    }
}
