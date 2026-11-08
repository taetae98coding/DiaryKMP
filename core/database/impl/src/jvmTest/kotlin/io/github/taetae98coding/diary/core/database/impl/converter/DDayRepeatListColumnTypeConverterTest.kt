package io.github.taetae98coding.diary.core.database.impl.converter

import io.github.taetae98coding.diary.core.testing.dday.ddayRepeat
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.set
import kotlinx.datetime.DateTimeUnit

class DDayRepeatListColumnTypeConverterTest :
    FunSpec({
        test("반복 간격 집합은 저장 문자열로 바꿨다가 되돌리면 같은 집합이 된다") {
            val converter = DDayRepeatListColumnTypeConverter()
            val repeats = Arb.set(Arb.ddayRepeat(), 0..MAX_COLLECTION_SIZE).next()

            converter.textToRepeatSet(converter.repeatSetToText(repeats)) shouldBe repeats
        }

        test("반복 간격은 일 간격, 개월 간격 순으로 작은 수부터 days나 months 키 하나만 가진 JSON으로 저장된다") {
            val converter = DDayRepeatListColumnTypeConverter()
            val repeats =
                setOf(
                    DateTimeUnit.MonthBased(months = 12),
                    DateTimeUnit.DayBased(days = 100),
                    DateTimeUnit.MonthBased(months = 1),
                    DateTimeUnit.DayBased(days = 10),
                )
            val storedText = """[{"days":10},{"days":100},{"months":1},{"months":12}]"""

            converter.repeatSetToText(repeats) shouldBe storedText
            converter.textToRepeatSet(storedText) shouldBe repeats
        }

        test("같은 반복 간격 집합은 넣은 순서와 관계없이 같은 문자열로 저장된다") {
            val converter = DDayRepeatListColumnTypeConverter()
            val repeats = Arb.set(Arb.ddayRepeat(), 0..MAX_COLLECTION_SIZE).next()

            converter.repeatSetToText(repeats.reversed().toSet()) shouldBe converter.repeatSetToText(repeats)
        }

        test("빈 반복 간격은 빈 JSON 배열로 저장되고 그대로 되돌아온다") {
            val converter = DDayRepeatListColumnTypeConverter()

            converter.repeatSetToText(emptySet()) shouldBe "[]"
            converter.textToRepeatSet("[]") shouldBe emptySet()
        }
    })
