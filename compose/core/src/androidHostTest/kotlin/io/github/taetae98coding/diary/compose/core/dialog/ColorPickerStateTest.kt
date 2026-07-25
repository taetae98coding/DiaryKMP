package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SliderState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.compose.ui.color.toHexString
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ColorPickerStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `animateColorTo가 완료되면 슬라이더가 목표 컬러의 채널 값이 된다`() {
        val state = colorPickerState(initialColor = randomColor())
        val newColor = randomColor()

        composeRule.setContent {
            LaunchedEffect(state) {
                state.animateColorTo(value = newColor)
            }
        }
        composeRule.waitForIdle()

        state.color shouldBe newColor
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun colorPickerState(initialColor: Color): ColorPickerState {
            val argb = initialColor.toArgb()

            return ColorPickerState(
                redSliderState = SliderState(value = (argb shr 16 and 0xFF).toFloat(), valueRange = 0F..255F),
                greenSliderState = SliderState(value = (argb shr 8 and 0xFF).toFloat(), valueRange = 0F..255F),
                blueSliderState = SliderState(value = (argb and 0xFF).toFloat(), valueRange = 0F..255F),
                hexTextFieldState = TextFieldState(initialText = initialColor.toHexString()),
            )
        }

        private fun randomColor(): Color = Color(color = 0xFF000000.toInt() or (fixtureMonkey.giveMeOne<Int>() and 0xFFFFFF))
    }
}
