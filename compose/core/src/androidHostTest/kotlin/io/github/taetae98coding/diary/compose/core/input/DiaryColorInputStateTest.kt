package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.core.opaqueColor
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
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
        val state = DiaryColorInputState(initialColor = Arb.opaqueColor().next())
        val newColor = Arb.opaqueColor().next()

        composeRule.setContent {
            LaunchedEffect(state) {
                state.animateTo(color = newColor)
            }
        }
        composeRule.waitForIdle()

        state.color.toArgb() shouldBe newColor.toArgb()
    }
}
