package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SliderState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.compose.ui.color.toHexString
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ColorPickerStateTest : FunSpec() {
    init {
        test("컬러는 R, G, B 슬라이더 값으로 계산된다") {
            val initialColor = randomColor()

            val state = colorPickerState(initialColor = initialColor)

            state.color shouldBe initialColor
        }

        test("소수점 슬라이더 값은 반올림한 채널 값으로 계산된다") {
            val state = colorPickerState(initialColor = Color.Black)

            state.redSliderState.value = 127.4F
            state.greenSliderState.value = 127.5F

            state.color shouldBe Color(red = 127, green = 128, blue = 0)
        }
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
