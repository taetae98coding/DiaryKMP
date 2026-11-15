package io.github.taetae98coding.diary.core.database.impl.search.datasource

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinefilter.entity.RoutineViewScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutine
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineRecord
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineTestTag
import io.github.taetae98coding.diary.core.database.impl.routine.linkRoutineTag
import io.github.taetae98coding.diary.core.database.impl.routine.loadAll
import io.github.taetae98coding.diary.core.database.impl.routine.routineCurrentCycleOf
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.todayRoutineTitleList
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinefilter.datasource.AccountRoutineFilterLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction.AccountRoutineRecordTransactionImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.localRoutineRecord
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SearchRoutineLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: SearchRoutineLocalDataSourceImpl

        beforeTest {
            database = routineTestDatabase()
            dataSource = SearchRoutineLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun searched(
            accountId: Uuid,
            query: String,
            today: LocalDate = TODAY,
        ): List<RoutineSummaryLocalEntity> = dataSource.page(accountId = accountId, query = query, currentCycle = routineCurrentCycleOf(today = today)).loadAll()

        suspend fun searchedTitleList(
            accountId: Uuid,
            query: String,
            today: LocalDate = TODAY,
        ): List<String> = searched(accountId = accountId, query = query, today = today).map { summary -> summary.routine.detail.title }

        fun routine(
            title: String,
            description: String = Arb.alphanumericString(prefix = "RoutineDescription").next(),
            startDate: LocalDate = LocalDate(2026, 1, 1),
            unit: String? = null,
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
            cycle: RoutineCycleLocalEntity = RoutineCycleLocalEntity.DAILY,
        ): RoutineLocalEntity =
            Arb.localRoutine(startDate = startDate, cycle = cycle, title = title, isFinished = isFinished, isDeleted = isDeleted, unit = unit).next().let { routine ->
                routine.copy(detail = routine.detail.copy(description = description))
            }

        test("루틴은 제목이나 설명에 질의를 포함하면 대소문자와 관계없이 결과가 되고 삭제된 루틴과 다른 계정의 루틴은 결과가 되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                routine(title = "Trip 준비"),
                routine(title = "운동", description = "주말 trip 전에"),
                routine(title = "독서", description = "소설"),
                routine(title = "trip 삭제됨", isDeleted = true),
            )
            database.insertRoutine(Arb.kotlinUuid().next(), routine(title = "trip 다른 계정"))

            searchedTitleList(accountId = accountId, query = "TRIP") shouldContainExactlyInAnyOrder listOf("Trip 준비", "운동")
        }

        test("TC-0bd03d13-47b1-4971-bdba-1265c7e1a02a 루틴 결과는 완료 여부와 관계없이 진행 중, 시작 전 순으로 놓고 같은 상태끼리는 제목 오름차순으로 놓는다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                routine(title = "여행 가", startDate = LocalDate(2026, 4, 1), isFinished = false),
                routine(title = "여행 나", startDate = LocalDate(2026, 3, 21), isFinished = true),
                routine(title = "여행 다", startDate = LocalDate(2026, 3, 20), isFinished = true),
                routine(title = "여행 라", startDate = LocalDate(2026, 1, 1), isFinished = false),
            )

            searchedTitleList(accountId = accountId, query = "여행") shouldBe listOf("여행 다", "여행 라", "여행 가", "여행 나")
        }

        test("TC-aa27e2d8-f9ab-4912-899f-4ecf37922b0e 완료된 루틴도 결과가 된다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                routine(title = "여행 진행 중", startDate = LocalDate(2026, 3, 1), isFinished = true),
                routine(title = "여행 시작 전", startDate = LocalDate(2026, 3, 21), isFinished = true),
            )

            searchedTitleList(accountId = accountId, query = "여행") shouldBe listOf("여행 진행 중", "여행 시작 전")
        }

        test("TC-4df6ec01-4cb7-4c97-a909-eaf94a996459 완료 여부와 완료한 시점은 함께 보여 주는 목록의 순서에 영향을 주지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 11, 13)
            val ga = routine(title = "여행 가", startDate = LocalDate(2026, 11, 1))
            val na = routine(title = "여행 나", startDate = LocalDate(2026, 11, 1))
            val da = routine(title = "여행 다", startDate = LocalDate(2026, 11, 20))
            val ra = routine(title = "여행 라", startDate = LocalDate(2026, 11, 20))
            val ma = routine(title = "여행 마", startDate = LocalDate(2026, 11, 20))
            database.insertRoutine(accountId, ga, na, da, ra, ma)
            listOf(ma, na, da).forEachIndexed { index, finished ->
                AccountRoutineTransactionImpl(database = database).updateFinished(
                    accountId = accountId,
                    routineId = finished.id,
                    isFinished = true,
                    updatedAt = Instant.fromEpochMilliseconds(finished.updatedAt.toEpochMilliseconds() + (index + 1) * 1_000L),
                ) shouldBe 1
            }

            searchedTitleList(accountId = accountId, query = "여행", today = today) shouldBe listOf("여행 가", "여행 나", "여행 다", "여행 라", "여행 마")
        }

        test("TC-5c21059e-13d6-4b25-847b-3779589e1d48 TC-8e911426-d51b-4734-9768-d5ed7d137fb1 루틴의 판정에 쓰지 않는 값만 질의를 포함하면 결과가 되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false, title = "여행").next()
            database.insertRoutineTestTag(accountId, tag)
            val routine = routine(title = "RoutineTitle", description = "RoutineDescription", startDate = LocalDate(2026, 3, 21), unit = "ml")
            database.insertRoutine(accountId, routine)
            database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tag.id)

            val typedUnit = routine(title = "RoutineTyped", description = "RoutineTypedDescription", unit = "스쿼트")
            database.insertRoutine(accountId, typedUnit)

            listOf("시작", "ml", "스쿼트", "여행").forEach { query ->
                searched(accountId = accountId, query = query).shouldBeEmpty()
            }
            searchedTitleList(accountId = accountId, query = "RoutineTitle") shouldBe listOf("RoutineTitle")
        }

        test("TC-bcaa4bbe-80cc-4e6b-9dcd-93a2913f3908 오늘의 요일이 하는 요일에 들지 않는 루틴도 결과가 되고 보기 범위는 검색에 적용하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val saturday = LocalDate(2026, 3, 21)
            database.insertRoutine(accountId, routine(title = "여행 준비", startDate = LocalDate(2026, 3, 1), cycle = RoutineCycleLocalEntity.WEEKDAYS))
            AccountRoutineFilterLocalDataSourceImpl(database = database).upsertViewScope(accountId = accountId, viewScope = RoutineViewScopeLocalEntity.TODAY)

            database.todayRoutineTitleList(accountId = accountId, today = saturday).shouldBeEmpty()
            searchedTitleList(accountId = accountId, query = "여행", today = saturday) shouldBe listOf("여행 준비")
        }

        test("TC-35d8cf9f-0777-44e6-bc77-a3fe5b228ba3 시작 전인 루틴도 결과가 된다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(accountId, routine(title = "여행 시작 전", startDate = LocalDate(2026, 3, 21)))

            searchedTitleList(accountId = accountId, query = "여행") shouldBe listOf("여행 시작 전")
        }

        test("TC-fb0e7fba-331b-4e48-a5b3-b0797711bf95 바뀐 오늘로 조회하면 루틴 결과의 합계와 순서가 바뀐 오늘을 기준으로 정해진다") {
            val accountId = Arb.kotlinUuid().next()
            val ga = routine(title = "여행 가", startDate = LocalDate(2026, 3, 20))
            val na = routine(title = "여행 나", startDate = LocalDate(2026, 3, 1))
            database.insertRoutine(accountId, ga, na)
            database.insertRoutineRecord(accountId = accountId, routineId = na.id, date = LocalDate(2026, 3, 19), amount = 2)
            searched(accountId = accountId, query = "여행", today = LocalDate(2026, 3, 19)).map { summary -> summary.routine.detail.title to summary.cycleAmount } shouldBe
                listOf("여행 나" to 2L, "여행 가" to 0L)

            searched(accountId = accountId, query = "여행").map { summary -> summary.routine.detail.title to summary.cycleAmount } shouldBe listOf("여행 가" to 0L, "여행 나" to 0L)
        }

        test("TC-fca70df8-6764-4a01-88ad-698663679928 루틴의 합계와 순서를 정하는 값이나 기록이 바뀌면 바뀐 합계와 순서로 다시 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val ga =
                routine(title = "여행 가", startDate = LocalDate(2026, 3, 1), unit = "ml").let { routine ->
                    routine.copy(detail = routine.detail.copy(goalAmount = 2000))
                }
            val na = routine(title = "여행 나")
            database.insertRoutine(accountId, ga, na)
            database.insertRoutineRecord(accountId = accountId, routineId = ga.id, date = TODAY, amount = 1000)
            val half = database.insertRoutineRecord(accountId = accountId, routineId = ga.id, date = TODAY, amount = 500)
            database.insertRoutineRecord(accountId = accountId, routineId = ga.id, date = LocalDate(2026, 3, 5), amount = 300)
            val recordTransaction = AccountRoutineRecordTransactionImpl(database = database)

            suspend fun titleAmountList(): List<Pair<String, Long>> = searched(accountId = accountId, query = "여행").map { summary -> summary.routine.detail.title to summary.cycleAmount }

            suspend fun updateDetail(detail: RoutineDetailLocalEntity) {
                AccountRoutineTransactionImpl(database = database).updateDetail(
                    accountId = accountId,
                    routineId = ga.id,
                    detail = detail,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                ) shouldBe 1
            }

            titleAmountList() shouldBe listOf("여행 가" to 1500L, "여행 나" to 0L)

            val added = Arb.localRoutineRecord(routineId = ga.id, date = TODAY, amount = 250).next()
            recordTransaction.upsert(accountId = accountId, record = added) shouldBe 1
            titleAmountList() shouldBe listOf("여행 가" to 1750L, "여행 나" to 0L)

            recordTransaction.updateDeleted(accountId = accountId, recordId = added.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
            recordTransaction.updateDeleted(accountId = accountId, recordId = half.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
            titleAmountList() shouldBe listOf("여행 가" to 1000L, "여행 나" to 0L)

            val goalChanged = ga.detail.copy(goalAmount = 3000)
            updateDetail(goalChanged)
            searched(accountId = accountId, query = "여행").map { summary -> summary.routine.detail to summary.cycleAmount } shouldBe listOf(goalChanged to 1000L, na.detail to 0L)

            val unitChanged = ga.detail.copy(unit = "count")
            updateDetail(unitChanged)
            searched(accountId = accountId, query = "여행").map { summary -> summary.routine.detail to summary.cycleAmount } shouldBe listOf(unitChanged to 1000L, na.detail to 0L)

            val cycleChanged = ga.detail.copy(cycle = RoutineCycleLocalEntity.MONTHLY)
            updateDetail(cycleChanged)
            searched(accountId = accountId, query = "여행").map { summary -> summary.routine.detail to summary.cycleAmount } shouldBe listOf(cycleChanged to 1300L, na.detail to 0L)

            updateDetail(ga.detail.copy(startDate = LocalDate(2026, 3, 21)))
            searchedTitleList(accountId = accountId, query = "여행") shouldBe listOf("여행 나", "여행 가")

            updateDetail(ga.detail)
            searchedTitleList(accountId = accountId, query = "여행") shouldBe listOf("여행 가", "여행 나")

            AccountRoutineTransactionImpl(database = database).updateFinished(accountId = accountId, routineId = ga.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
            searched(accountId = accountId, query = "여행").map { summary -> summary.routine.detail.title to summary.routine.isFinished } shouldBe listOf("여행 가" to true, "여행 나" to false)
        }
    }) {
    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 3, 20)
    }
}
