package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SliderState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.taetae98coding.diary.compose.core.opaqueColor
import io.github.taetae98coding.diary.library.compose.ui.color.toHexString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ColorPickerStateTest : FunSpec() {
    init {
        test("컬러는 R, G, B 슬라이더 값으로 계산된다") {
            val initialColor = Arb.opaqueColor().next()

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
        private fun colorPickerState(initialColor: Color): ColorPickerState {
            val argb = initialColor.toArgb()

            return ColorPickerState(
                redSliderState = SliderState(value = (argb shr 16 and 0xFF).toFloat(), valueRange = 0F..255F),
                greenSliderState = SliderState(value = (argb shr 8 and 0xFF).toFloat(), valueRange = 0F..255F),
                blueSliderState = SliderState(value = (argb and 0xFF).toFloat(), valueRange = 0F..255F),
                hexTextFieldState = TextFieldState(initialText = initialColor.toHexString()),
            )
        }
    }
}
