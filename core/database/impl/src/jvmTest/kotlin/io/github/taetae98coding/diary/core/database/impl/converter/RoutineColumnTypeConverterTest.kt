package io.github.taetae98coding.diary.core.database.impl.converter

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinefilter.entity.RoutineViewScopeLocalEntity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class RoutineColumnTypeConverterTest :
    FunSpec({
        test("TC-6f442d59-63b6-4f58-b781-7060329ecdce 주기는 정해진 다섯 값으로 기록하고 그 값을 다시 같은 주기로 읽는다") {
            val converter = RoutineCycleColumnTypeConverter()

            listOf(
                RoutineCycleLocalEntity.DAILY to "FREQ=DAILY",
                RoutineCycleLocalEntity.WEEKDAYS to "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR",
                RoutineCycleLocalEntity.WEEKENDS to "FREQ=DAILY;BYDAY=SA,SU",
                RoutineCycleLocalEntity.WEEKLY to "FREQ=WEEKLY;WKST=SU",
                RoutineCycleLocalEntity.MONTHLY to "FREQ=MONTHLY",
            ).forEach { (cycle, value) ->
                converter.cycleToText(cycle = cycle) shouldBe value
                converter.textToCycle(value = value) shouldBe cycle
            }
            RoutineCycleLocalEntity.entries.size shouldBe 5
        }

        test("TC-b0f6d809-7328-44d8-8bf4-1d62c8eb366c 정해진 다섯 값이 아닌 반복 규칙은 루틴의 주기로 읽지 않는다") {
            listOf(
                "freq=daily",
                "Freq=Monthly",
                "WKST=SU;FREQ=WEEKLY",
                "FREQ=WEEKLY",
                "FREQ=WEEKLY;WKST=MO",
                "FREQ=WEEKLY;INTERVAL=2;WKST=SU",
                "FREQ=WEEKLY;WKST=SU;BYDAY=MO,WE",
                "FREQ=DAILY;BYDAY=MO,WE,FR",
                "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR,SA",
                "FREQ=DAILY;BYDAY=SU,SA",
                "FREQ=DAILY;BYDAY=FR,TH,WE,TU,MO",
                "BYDAY=SA,SU;FREQ=DAILY",
                "FREQ=DAILY;BYDAY=sa,su",
                "FREQ=DAILY;BYDAY=MO, TU, WE, TH, FR",
                "FREQ=WEEKLY;WKST=SU;BYDAY=SA,SU",
                "FREQ=DAILY;INTERVAL=2",
                "FREQ=YEARLY",
                "",
            ).forEach { value ->
                RoutineCycleLocalEntity.fromPersistentValue(persistentValue = value).shouldBeNull()
                shouldThrow<IllegalArgumentException> { RoutineCycleColumnTypeConverter().textToCycle(value = value) }
            }
        }

        test("보기 범위는 정해진 두 값으로 기록하고 그 값을 다시 같은 보기 범위로 읽는다") {
            val converter = RoutineViewScopeColumnTypeConverter()

            listOf(
                RoutineViewScopeLocalEntity.TODAY to "today",
                RoutineViewScopeLocalEntity.ALL to "all",
            ).forEach { (viewScope, value) ->
                converter.viewScopeToText(viewScope = viewScope) shouldBe value
                converter.textToViewScope(value = value) shouldBe viewScope
            }
            RoutineViewScopeLocalEntity.entries.size shouldBe 2
        }

        test("정해진 두 값이 아닌 글자는 보기 범위로 읽지 않는다") {
            listOf("TODAY", "All", "", "week").forEach { value ->
                RoutineViewScopeLocalEntity.fromPersistentValue(persistentValue = value).shouldBeNull()
                shouldThrow<IllegalArgumentException> { RoutineViewScopeColumnTypeConverter().textToViewScope(value = value) }
            }
        }
    })
