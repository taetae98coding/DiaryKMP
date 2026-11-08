package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit

@Stable
internal class DDayRepeatRowState(
    val key: String,
    initialNumber: String,
    initialUnit: DDayRepeatUnit,
) {
    val numberState: TextFieldState = TextFieldState(initialText = initialNumber)
    val bringIntoViewRequester: BringIntoViewRequester = BringIntoViewRequester()

    var unit: DDayRepeatUnit by mutableStateOf(initialUnit)
        private set

    val number: String
        get() = numberState.text.toString()

    // 입력이 0~9 숫자만 다섯 자리까지 남기므로 비어 있지 않은 값은 항상 정수로 읽을 수 있다.
    val input: DDayRepeatInput
        get() = DDayRepeatInput(number = number.toIntOrNull(), unit = unit)

    fun selectUnit(unit: DDayRepeatUnit) {
        this.unit = unit
    }
}

internal val ddayRepeatUnitList: List<DDayRepeatUnit> = listOf(DDayRepeatUnit.DAY, DDayRepeatUnit.MONTH, DDayRepeatUnit.YEAR)

internal object DDayRepeatNumberInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val text = asCharSequence()
        if (text.length > MAX_DIGIT_COUNT || text.any { char -> char !in DIGIT_RANGE }) {
            revertAllChanges()
        }
    }
}

private const val MAX_DIGIT_COUNT = 5

// Char.isDigit은 전각 숫자 같은 유니코드 숫자도 받지만 toIntOrNull은 0~9만 읽으므로 범위로 거른다.
private val DIGIT_RANGE = '0'..'9'
