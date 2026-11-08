package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.saveable.SaverScope
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.domain.dday.preset.DDayPreset
import io.github.taetae98coding.diary.domain.dday.preset.value
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class DDayRuleInputStateTest : FunSpec() {
    init {
        test("TC-9e114b93-a844-4db8-8737-161a29947f0e 프리셋을 고르면 네 입력이 그 프리셋의 값으로 바뀐다") {
            listOf(
                DDayPreset.COUPLE to listOf(DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY), DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR)),
                DDayPreset.EXAM to emptyList(),
                DDayPreset.BABY to
                    listOf(
                        DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY),
                        DDayRepeatInput(number = 1, unit = DDayRepeatUnit.MONTH),
                        DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR),
                    ),
                DDayPreset.CUSTOM to emptyList(),
            ).forEach { (preset, repeatList) ->
                val state = editedState()

                state.applyPreset(value = preset.value())

                state.countsBaseDay shouldBe true
                state.repeatInputList shouldBe repeatList
                state.endDate shouldBe null
                state.overflowPolicy shouldBe DDayOverflowPolicy.CLAMP_TO_MONTH_END
            }
        }

        test("TC-e5628fbe-673f-4e85-872e-97fc8393757c 프리셋은 고쳐 둔 네 입력을 덮어쓰고 기준일은 바꾸지 않는다") {
            val baseDate = Arb.kotlinxLocalDate().next()
            val state = editedState(baseDate = baseDate)

            state.applyPreset(value = DDayPreset.COUPLE.value())

            state.baseDate shouldBe baseDate
            state.countsBaseDay shouldBe true
            state.repeatInputList shouldBe listOf(DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY), DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR))
            state.endDate shouldBe null
            state.overflowPolicy shouldBe DDayOverflowPolicy.CLAMP_TO_MONTH_END
        }

        test("TC-a14c44e4-089c-47f6-9251-4cadeb183a9a 프리셋으로 채운 값을 모두 고칠 수 있다") {
            val state = state()
            state.applyPreset(value = DDayPreset.BABY.value())
            val endDate = Arb.kotlinxLocalDate().next()

            state.updateCountsBaseDay(countsBaseDay = false)
            state.removeRepeat(key = state.repeatRowList[1].key)
            state.repeatRowList[0].numberState.setTextAndPlaceCursorAtEnd("200")
            state.selectEndDate(date = endDate)
            state.selectOverflowPolicy(overflowPolicy = DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY)

            state.countsBaseDay shouldBe false
            state.repeatInputList shouldBe listOf(DDayRepeatInput(number = 200, unit = DDayRepeatUnit.DAY), DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR))
            state.endDate shouldBe endDate
            state.overflowPolicy shouldBe DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY
        }

        test("TC-1ab9dbae-8463-4725-9531-12f55af8432d 기준일을 종료일보다 뒤로 고르면 종료일이 기준일과 같은 날짜로 맞춰진다") {
            val baseDate = Arb.kotlinxLocalDate().next()
            val previousEndDate = baseDate.plus(Arb.dayOffset().next(), DateTimeUnit.DAY)
            val afterEndDate = previousEndDate.plus(Arb.dayOffset().next(), DateTimeUnit.DAY)
            val beforeEndDate = previousEndDate.minus(Arb.dayOffset().next(), DateTimeUnit.DAY)
            listOf(
                Triple(previousEndDate, afterEndDate, afterEndDate),
                Triple(previousEndDate, previousEndDate, previousEndDate),
                Triple(previousEndDate, beforeEndDate, previousEndDate),
                Triple(null, afterEndDate, null),
            ).forEach { (endDate, selected, expected) ->
                val state = state(baseDate = baseDate, endDate = endDate)

                state.selectBaseDate(date = selected)

                state.baseDate shouldBe selected
                state.endDate shouldBe expected
            }
        }

        test("TC-414d134e-d848-44cc-88c0-adcf632c57d4 기준일을 1일로 세기를 켜거나 끌 수 있다") {
            val state = state(countsBaseDay = true)

            state.updateCountsBaseDay(countsBaseDay = false)
            state.countsBaseDay shouldBe false

            state.updateCountsBaseDay(countsBaseDay = true)
            state.countsBaseDay shouldBe true
        }

        test("TC-db083202-8779-4ebe-b816-c4e7c7d42742 간격을 추가하면 마지막 자리에 단위 일의 빈 간격이 놓인다") {
            val state = state(repeatList = listOf(DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY), DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR)))

            state.addRepeat()

            state.repeatInputList shouldBe
                listOf(
                    DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY),
                    DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR),
                    DDayRepeatInput(number = null, unit = DDayRepeatUnit.DAY),
                )
            state.repeatRowList.last().number shouldBe ""
        }

        test("TC-1a8fe5c1-5688-42c5-93a1-684cda3ec7f1 간격을 삭제하면 그 간격만 사라진다") {
            val state =
                state(
                    repeatList =
                        listOf(
                            DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY),
                            DDayRepeatInput(number = 1, unit = DDayRepeatUnit.MONTH),
                            DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR),
                        ),
                )

            state.removeRepeat(key = state.repeatRowList[1].key)

            state.repeatInputList shouldBe listOf(DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY), DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR))
        }

        test("TC-0dd93d8f-5bba-44a8-b150-5cc18aeb8d89 단위를 바꿔도 입력한 숫자는 그대로다") {
            val state = state(repeatList = listOf(DDayRepeatInput(number = 3, unit = DDayRepeatUnit.DAY)))
            val row = state.repeatRowList.single()

            row.selectUnit(unit = DDayRepeatUnit.MONTH)
            row.number shouldBe "3"
            row.unit shouldBe DDayRepeatUnit.MONTH

            row.selectUnit(unit = DDayRepeatUnit.YEAR)
            row.number shouldBe "3"
            row.unit shouldBe DDayRepeatUnit.YEAR
        }

        test("TC-cdd10340-9a47-434a-8831-44dcbbc19ce4 간격의 개수에는 제한이 없다") {
            val state = state()

            (1..REPEAT_COUNT).forEach { number ->
                state.addRepeat().numberState.setTextAndPlaceCursorAtEnd(number.toString())
            }

            state.repeatInputList shouldBe (1..REPEAT_COUNT).map { number -> DDayRepeatInput(number = number, unit = DDayRepeatUnit.DAY) }
        }

        test("TC-c4c30a10-b7f6-4a9a-8492-21153722abc1 같은 간격을 여러 번 입력해도 막지 않는다") {
            val state = state(repeatList = listOf(DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY)))

            state.addRepeat().numberState.setTextAndPlaceCursorAtEnd("100")
            state.addRepeat().apply {
                numberState.setTextAndPlaceCursorAtEnd("12")
                selectUnit(unit = DDayRepeatUnit.MONTH)
            }
            state.addRepeat().apply {
                numberState.setTextAndPlaceCursorAtEnd("1")
                selectUnit(unit = DDayRepeatUnit.YEAR)
            }

            state.repeatInputList shouldBe
                listOf(
                    DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY),
                    DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY),
                    DDayRepeatInput(number = 12, unit = DDayRepeatUnit.MONTH),
                    DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR),
                )
        }

        test("TC-7b2c569d-3cbc-4081-9fd2-883820002800 종료일을 고르면 고른 날짜가 종료일이 된다") {
            val baseDate = Arb.kotlinxLocalDate().next()
            val selected = baseDate.plus(Arb.dayOffset().next(), DateTimeUnit.DAY)
            listOf(null, selected.plus(Arb.dayOffset().next(), DateTimeUnit.DAY)).forEach { endDate ->
                val state = state(baseDate = baseDate, endDate = endDate)

                state.selectEndDate(date = selected)

                state.endDate shouldBe selected
            }
        }

        test("TC-b0e57a45-6ac8-4893-a1b4-5d0e2b4a3a08 고른 종료일을 지우면 종료일이 없는 상태로 돌아간다") {
            val baseDate = Arb.kotlinxLocalDate().next()
            val state = state(baseDate = baseDate, endDate = baseDate.plus(Arb.dayOffset().next(), DateTimeUnit.DAY))

            state.clearEndDate()

            state.endDate shouldBe null
        }

        test("TC-36f0fa0c-fa01-4990-b74d-cc351fe03ccd 달력에 없는 날짜 처리는 개월이나 년 간격이 없어도 세 값 중 하나를 고를 수 있다") {
            listOf(
                emptyList(),
                listOf(DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY)),
                listOf(DDayRepeatInput(number = 1, unit = DDayRepeatUnit.MONTH)),
            ).forEach { repeatList ->
                val state = state(repeatList = repeatList)

                listOf(DDayOverflowPolicy.SKIP, DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY, DDayOverflowPolicy.CLAMP_TO_MONTH_END).forEach { policy ->
                    state.selectOverflowPolicy(overflowPolicy = policy)

                    state.overflowPolicy shouldBe policy
                }
            }
        }

        test("TC-7111288b-02d3-460e-9537-e6d873d9a098 처음 상태로 되돌리면 기준일은 주어진 날짜, 나머지 네 입력은 직접 입력 값이 된다") {
            val state = editedState()
            val today = Arb.kotlinxLocalDate().next()

            state.reset(baseDate = today)

            state.baseDate shouldBe today
            state.countsBaseDay shouldBe true
            state.repeatInputList.shouldBeEmpty()
            state.endDate shouldBe null
            state.overflowPolicy shouldBe DDayOverflowPolicy.CLAMP_TO_MONTH_END
        }
        test("저장했다가 복원하면 기준일, 네 입력과 간격의 순서·숫자·단위를 그대로 되살린다") {
            val state =
                state(
                    endDate = Arb.kotlinxLocalDate().next(),
                    repeatList =
                        listOf(
                            DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR),
                            DDayRepeatInput(number = null, unit = DDayRepeatUnit.MONTH),
                        ),
                )
            state.addRepeat().numberState.setTextAndPlaceCursorAtEnd("0100")

            val saved = with(DDayRuleInputState.Saver) { SaverScope { true }.save(state) }
            val restored = DDayRuleInputState.Saver.restore(requireNotNull(saved)).shouldNotBeNull()

            restored.baseDate shouldBe state.baseDate
            restored.countsBaseDay shouldBe state.countsBaseDay
            restored.endDate shouldBe state.endDate
            restored.overflowPolicy shouldBe state.overflowPolicy
            restored.repeatRowList.map { row -> Triple(row.key, row.number, row.unit) } shouldBe
                state.repeatRowList.map { row -> Triple(row.key, row.number, row.unit) }
            restored.repeatRowList.map { row -> row.number } shouldBe listOf("1", "", "0100")
        }

        test("종료일이 없어도 저장했다가 복원하면 종료일이 없다") {
            val state = state(endDate = null)

            val saved = with(DDayRuleInputState.Saver) { SaverScope { true }.save(state) }
            val restored = DDayRuleInputState.Saver.restore(requireNotNull(saved)).shouldNotBeNull()

            restored.endDate shouldBe null
        }
    }

    private companion object {
        const val REPEAT_COUNT = 30
        const val MAX_DAY_OFFSET = 3_650

        fun Arb.Companion.dayOffset(): Arb<Int> = Arb.int(1..MAX_DAY_OFFSET)

        fun state(
            baseDate: LocalDate = Arb.kotlinxLocalDate().next(),
            countsBaseDay: Boolean = Arb.boolean().next(),
            endDate: LocalDate? = Arb.kotlinxLocalDate().orNull().next(),
            overflowPolicy: DDayOverflowPolicy = Arb.enum<DDayOverflowPolicy>().next(),
            repeatList: List<DDayRepeatInput> = emptyList(),
        ): DDayRuleInputState =
            DDayRuleInputState(
                baseDate = baseDate,
                countsBaseDay = countsBaseDay,
                endDate = endDate,
                overflowPolicy = overflowPolicy,
                repeatRowList =
                    repeatList.map { input ->
                        DDayRepeatRowState(key = Arb.kotlinUuid().next().toString(), initialNumber = input.number?.toString().orEmpty(), initialUnit = input.unit)
                    },
            )

        // 기준일을 1일로 세기를 끄고, 반복 간격을 `3` `개월` 하나, 종료일과 `표시하지 않음`으로 고쳐 둔 상태다.
        fun editedState(baseDate: LocalDate = Arb.kotlinxLocalDate().next()): DDayRuleInputState =
            state(
                baseDate = baseDate,
                countsBaseDay = false,
                endDate = baseDate.plus(Arb.dayOffset().next(), DateTimeUnit.DAY),
                overflowPolicy = DDayOverflowPolicy.SKIP,
                repeatList = listOf(DDayRepeatInput(number = 3, unit = DDayRepeatUnit.MONTH)),
            )
    }
}
