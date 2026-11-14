package io.github.taetae98coding.diary.compose.routine

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.util.Locale

class RoutineAmountTextTest {
    @Test
    fun `TC-8568139b-d0aa-4158-aa86-a0b144f787b2 양은 숫자 바로 뒤에 단위를 붙여 쓰고 자릿수 기호와 단위 변환을 쓰지 않는다`() {
        AMOUNT_ROW_LIST.forEach { row ->
            (row to routineAmountText(amount = row.amount, unit = row.unit)) shouldBe (row to row.text)
        }
    }

    @Test
    fun `TC-e38d198b-7085-4c49-97bd-389f79e9e502 단위가 비어 있으면 양의 문구는 숫자만 쓴다`() {
        listOf(0L to "0", 3L to "3", 1500L to "1500", 9_999_999L to "9999999").forEach { (amount, text) ->
            routineAmountText(amount = amount, unit = "") shouldBe text
        }
    }

    @Test
    fun `TC-2063445d-1360-40ee-9cea-019fa22beb43 단위는 언어 환경과 관계없이 적은 글자 그대로 붙인다`() {
        val defaultLocale = Locale.getDefault()

        try {
            listOf(
                Triple(Locale.KOREAN, "회", "3회"),
                Triple(Locale.ENGLISH, "회", "3회"),
                Triple(Locale.JAPANESE, "회", "3회"),
                Triple(Locale.KOREAN, "times", "3times"),
                Triple(Locale.ENGLISH, "", "3"),
            ).forEach { (locale, unit, text) ->
                Locale.setDefault(locale)

                routineAmountText(amount = 3, unit = unit) shouldBe text
            }
        } finally {
            Locale.setDefault(defaultLocale)
        }
    }

    @Test
    fun `자릿수를 나누는 기호를 쓰는 언어 환경에서도 숫자에 기호를 넣지 않는다`() {
        val defaultLocale = Locale.getDefault()

        try {
            listOf(Locale.US, Locale.GERMANY, Locale.FRANCE, Locale.forLanguageTag("ar-EG"), Locale.forLanguageTag("hi-IN")).forEach { locale ->
                Locale.setDefault(locale)

                routineAmountText(amount = 19_999_998, unit = "ml") shouldBe "19999998ml"
            }
        } finally {
            Locale.setDefault(defaultLocale)
        }
    }

    private data class AmountRow(
        val amount: Long,
        val unit: String,
        val text: String,
    )

    private companion object {
        val AMOUNT_ROW_LIST =
            listOf(
                AmountRow(amount = 3, unit = "회", text = "3회"),
                AmountRow(amount = 1, unit = "회", text = "1회"),
                AmountRow(amount = 0, unit = "회", text = "0회"),
                AmountRow(amount = 1500, unit = "ml", text = "1500ml"),
                AmountRow(amount = 1000, unit = "ml", text = "1000ml"),
                AmountRow(amount = 5000, unit = "m", text = "5000m"),
                AmountRow(amount = 20, unit = "쪽", text = "20쪽"),
                AmountRow(amount = 1500, unit = "ML", text = "1500ML"),
                AmountRow(amount = 30, unit = "분 걷기", text = "30분 걷기"),
                AmountRow(amount = 9_999_999, unit = "ml", text = "9999999ml"),
                AmountRow(amount = 19_999_998, unit = "회", text = "19999998회"),
            )
    }
}
