package io.github.taetae98coding.diary.domain.dday.repeat

import io.github.taetae98coding.diary.core.testing.dday.ddayRepeat
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.set
import kotlinx.datetime.DateTimeUnit

private val MONTHS_PER_YEAR: Int = DateTimeUnit.YEAR.months
private const val MAX_REPEAT_NUMBER: Int = 1000

// 추가 테스트: 입력 단위와 기록 단위 사이의 변환 계약을 확인한다.
class DDayRepeatRecordTest :
    FunSpec({
        test("년 입력은 12배 개월로 기록하고 12의 배수 개월은 년 입력으로 되돌린다") {
            listOf(
                DDayRepeatInput(number = 2, unit = DDayRepeatUnit.YEAR),
                DDayRepeatInput(number = 18, unit = DDayRepeatUnit.MONTH),
                DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY),
            ).toRepeatSetOrNull() shouldBe
                setOf(DateTimeUnit.MonthBased(months = 24), DateTimeUnit.MonthBased(months = 18), DateTimeUnit.DayBased(days = 100))

            setOf(DateTimeUnit.MonthBased(months = 24), DateTimeUnit.MonthBased(months = 18), DateTimeUnit.DayBased(days = 100)).toRepeatInputList() shouldBe
                listOf(
                    DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY),
                    DDayRepeatInput(number = 18, unit = DDayRepeatUnit.MONTH),
                    DDayRepeatInput(number = 2, unit = DDayRepeatUnit.YEAR),
                )
        }

        test("기록한 반복 간격을 입력으로 바꿨다가 다시 기록하면 같은 간격이 된다") {
            val repeats = Arb.set(Arb.ddayRepeat(), 0..MAX_COLLECTION_SIZE).next()

            repeats.toRepeatInputList().toRepeatSetOrNull() shouldBe repeats
        }

        test("비어 있거나 1보다 작은 수가 하나라도 있으면 기록하지 않는다") {
            listOf(null, 0, -1).forEach { number ->
                listOf(
                    DDayRepeatInput(number = 1, unit = DDayRepeatUnit.DAY),
                    DDayRepeatInput(number = number, unit = DDayRepeatUnit.MONTH),
                ).toRepeatSetOrNull().shouldBeNull()
            }
        }

        test("기록한 반복 간격을 입력으로 바꾸면 일, 개월, 년 순서로 둔다") {
            val repeats =
                linkedSetOf(
                    DDayRepeatInput(number = Arb.int(1..MAX_REPEAT_NUMBER).next(), unit = DDayRepeatUnit.YEAR),
                    DDayRepeatInput(number = Arb.int(1..MAX_REPEAT_NUMBER).filter { number -> number % MONTHS_PER_YEAR != 0 }.next(), unit = DDayRepeatUnit.MONTH),
                    DDayRepeatInput(number = Arb.int(1..MAX_REPEAT_NUMBER).next(), unit = DDayRepeatUnit.DAY),
                ).toList()
                    .toRepeatSetOrNull()
                    .shouldNotBeNull()

            repeats.toRepeatInputList().map { input -> input.unit } shouldContainExactly
                listOf(
                    DDayRepeatUnit.DAY,
                    DDayRepeatUnit.MONTH,
                    DDayRepeatUnit.YEAR,
                )
        }

        test("모든 반복 간격 단위를 한 번씩만 정렬 순서에 둔다") {
            val repeats =
                DDayRepeatUnit.entries
                    .reversed()
                    .map { unit -> DDayRepeatInput(number = Arb.int(1..MAX_REPEAT_NUMBER).filter { number -> number % MONTHS_PER_YEAR != 0 }.next(), unit = unit) }
                    .toRepeatSetOrNull()
                    .shouldNotBeNull()

            val units = repeats.toRepeatInputList().map { input -> input.unit }

            units shouldContainExactlyInAnyOrder DDayRepeatUnit.entries
            units shouldContainExactly listOf(DDayRepeatUnit.DAY, DDayRepeatUnit.MONTH, DDayRepeatUnit.YEAR)
        }

        test("같은 간격을 두 번 입력하면 하나로 기록한다") {
            listOf(
                DDayRepeatInput(number = 12, unit = DDayRepeatUnit.MONTH),
                DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR),
            ).toRepeatSetOrNull() shouldBe setOf(DateTimeUnit.MonthBased(months = 12))
        }
    })
