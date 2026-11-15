package io.github.taetae98coding.diary.core.database.impl.routine.datasource

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.calendarRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.cycleAmount
import io.github.taetae98coding.diary.core.database.impl.routine.entity.AccountRoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routine.findRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.finishedRoutineSummaryList
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutine
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineRecord
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineTestTag
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineWithDefaults
import io.github.taetae98coding.diary.core.database.impl.routine.linkRoutineTag
import io.github.taetae98coding.diary.core.database.impl.routine.routineCurrentCycleOf
import io.github.taetae98coding.diary.core.database.impl.routine.routineSummaryList
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.routineTitleList
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction.AccountRoutineRecordTransactionImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.localRoutineRecord
import io.github.taetae98coding.diary.core.testing.routine.routineAmount
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import java.util.TimeZone
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class AccountRoutineLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase

        beforeTest {
            database = routineTestDatabase()
        }

        afterTest {
            database.close()
        }

        // 따로 정하지 않으면 시작 날짜가 2026-01-01이고 목표량이 없는 루틴이다.
        suspend fun insertRoutine(
            accountId: Uuid,
            cycle: RoutineCycleLocalEntity,
            startDate: LocalDate = LocalDate(2026, 1, 1),
            recordList: List<Pair<LocalDate, Int>> = emptyList(),
        ): Uuid {
            val routine = Arb.localRoutine(startDate = startDate, cycle = cycle).next()
            database.insertRoutine(accountId, routine)
            recordList.forEach { (date, amount) -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = date, amount = amount) }

            return routine.id
        }

        test("TC-818e0b02-00ec-4b07-bb49-4ef5dea27df5 매일 루틴의 주기의 합계는 오늘 날짜의 기록만 더한다") {
            val accountId = Arb.kotlinUuid().next()
            val routineId =
                insertRoutine(
                    accountId = accountId,
                    cycle = RoutineCycleLocalEntity.DAILY,
                    recordList =
                        listOf(
                            LocalDate(2026, 11, 12) to 100,
                            LocalDate(2026, 11, 13) to 200,
                            LocalDate(2026, 11, 13) to 300,
                            LocalDate(2026, 11, 14) to 400,
                        ),
                )

            database.cycleAmount(accountId = accountId, routineId = routineId, today = LocalDate(2026, 11, 13)) shouldBe 500L
        }

        test("TC-48bc7ccc-4625-45cc-ba03-629f39e3380d 평일만과 주말만 루틴의 주기의 합계는 하는 요일이 아닌 날에도 오늘 날짜의 기록만 더한다") {
            listOf(
                Triple(RoutineCycleLocalEntity.WEEKDAYS, LocalDate(2026, 11, 13), 100L),
                Triple(RoutineCycleLocalEntity.WEEKDAYS, LocalDate(2026, 11, 14), 500L),
                Triple(RoutineCycleLocalEntity.WEEKENDS, LocalDate(2026, 11, 14), 500L),
                Triple(RoutineCycleLocalEntity.WEEKENDS, LocalDate(2026, 11, 13), 100L),
                Triple(RoutineCycleLocalEntity.WEEKENDS, LocalDate(2026, 11, 16), 0L),
            ).forEach { (cycle, today, expected) ->
                val accountId = Arb.kotlinUuid().next()
                val routineId =
                    insertRoutine(
                        accountId = accountId,
                        cycle = cycle,
                        recordList =
                            listOf(
                                LocalDate(2026, 11, 13) to 100,
                                LocalDate(2026, 11, 14) to 200,
                                LocalDate(2026, 11, 14) to 300,
                                LocalDate(2026, 11, 15) to 400,
                            ),
                    )

                database.cycleAmount(accountId = accountId, routineId = routineId, today = today) shouldBe expected
            }
        }

        test("TC-1f0d3b55-f104-4839-98ff-1994e4618260 매주 루틴의 주기의 합계는 그 주의 일요일부터 토요일까지의 기록만 더한다") {
            listOf(
                Triple(
                    LocalDate(2026, 11, 11),
                    listOf(LocalDate(2026, 11, 7) to 1, LocalDate(2026, 11, 8) to 2, LocalDate(2026, 11, 11) to 4, LocalDate(2026, 11, 14) to 8, LocalDate(2026, 11, 15) to 16),
                    14L,
                ),
                Triple(
                    LocalDate(2026, 12, 1),
                    listOf(LocalDate(2026, 11, 28) to 1, LocalDate(2026, 11, 29) to 2, LocalDate(2026, 12, 5) to 4, LocalDate(2026, 12, 6) to 8),
                    6L,
                ),
                Triple(
                    LocalDate(2027, 1, 1),
                    listOf(LocalDate(2026, 12, 26) to 1, LocalDate(2026, 12, 27) to 2, LocalDate(2027, 1, 2) to 4, LocalDate(2027, 1, 3) to 8),
                    6L,
                ),
            ).forEach { (today, recordList, expected) ->
                val accountId = Arb.kotlinUuid().next()
                val routineId = insertRoutine(accountId = accountId, cycle = RoutineCycleLocalEntity.WEEKLY, recordList = recordList)

                database.cycleAmount(accountId = accountId, routineId = routineId, today = today) shouldBe expected
            }
        }

        test("TC-be2e35ed-95fd-4386-8e61-2f1fc3d22e93 매월 루틴의 주기의 합계는 그 달의 1일부터 마지막 날까지의 기록만 더한다") {
            listOf(
                LocalDate(2026, 11, 13) to listOf(LocalDate(2026, 10, 31) to 1, LocalDate(2026, 11, 1) to 2, LocalDate(2026, 11, 30) to 4, LocalDate(2026, 12, 1) to 8),
                LocalDate(2026, 12, 13) to listOf(LocalDate(2026, 11, 30) to 1, LocalDate(2026, 12, 1) to 2, LocalDate(2026, 12, 31) to 4, LocalDate(2027, 1, 1) to 8),
                LocalDate(2026, 2, 10) to listOf(LocalDate(2026, 1, 31) to 1, LocalDate(2026, 2, 1) to 2, LocalDate(2026, 2, 28) to 4, LocalDate(2026, 3, 1) to 8),
                LocalDate(2028, 2, 10) to listOf(LocalDate(2028, 1, 31) to 1, LocalDate(2028, 2, 1) to 2, LocalDate(2028, 2, 29) to 4, LocalDate(2028, 3, 1) to 8),
            ).forEach { (today, recordList) ->
                val accountId = Arb.kotlinUuid().next()
                val routineId = insertRoutine(accountId = accountId, cycle = RoutineCycleLocalEntity.MONTHLY, recordList = recordList)

                database.cycleAmount(accountId = accountId, routineId = routineId, today = today) shouldBe 6L
            }
        }

        test("TC-7eeaa013-ba3c-4f85-b79c-6a36758b467a 이번 주기에 드는 기록이 없으면 주기의 합계는 0이다") {
            listOf(
                emptyList(),
                listOf(LocalDate(2026, 11, 12) to 100),
            ).forEach { recordList ->
                val accountId = Arb.kotlinUuid().next()
                val routineId = insertRoutine(accountId = accountId, cycle = RoutineCycleLocalEntity.DAILY, recordList = recordList)

                database.cycleAmount(accountId = accountId, routineId = routineId, today = LocalDate(2026, 11, 13)) shouldBe 0L
            }
        }

        test("TC-083ffdf1-88c3-45bd-ab8a-4b1585d5bf5d 삭제된 기록은 주기의 합계에 넣지 않는다") {
            val today = LocalDate(2026, 11, 13)

            listOf(false to 200L, true to 0L).forEach { (isFirstDeleted, expected) ->
                val accountId = Arb.kotlinUuid().next()
                val routineId = insertRoutine(accountId = accountId, cycle = RoutineCycleLocalEntity.DAILY)
                database.insertRoutineRecord(accountId = accountId, routineId = routineId, date = today, amount = 200, isDeleted = isFirstDeleted)
                database.insertRoutineRecord(accountId = accountId, routineId = routineId, date = today, amount = 300, isDeleted = true)

                database.cycleAmount(accountId = accountId, routineId = routineId, today = today) shouldBe expected
            }
        }

        test("TC-7cd036cd-c8f3-4b37-85cb-1c37daec9b5f 다른 루틴에 속한 기록은 주기의 합계에 넣지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 11, 13)
            val routineA = insertRoutine(accountId = accountId, cycle = RoutineCycleLocalEntity.DAILY, recordList = listOf(today to 200))
            val routineB = insertRoutine(accountId = accountId, cycle = RoutineCycleLocalEntity.DAILY, recordList = listOf(today to 300))

            database.cycleAmount(accountId = accountId, routineId = routineA, today = today) shouldBe 200L
            database.cycleAmount(accountId = accountId, routineId = routineB, today = today) shouldBe 300L
        }

        test("다른 계정과만 연결된 기록은 주기의 합계에 넣지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 11, 13)
            val routineId = insertRoutine(accountId = accountId, cycle = RoutineCycleLocalEntity.DAILY, recordList = listOf(today to 200))
            database.insertRoutineRecord(accountId = Arb.kotlinUuid().next(), routineId = routineId, date = today, amount = 300)

            database.cycleAmount(accountId = accountId, routineId = routineId, today = today) shouldBe 200L
        }

        test("TC-bcc1378c-3640-4c18-9175-55b704bb6a9a 오늘보다 뒤의 기록도 이번 주기에 들면 주기의 합계에 넣는다") {
            listOf(
                Triple(RoutineCycleLocalEntity.WEEKLY, listOf(LocalDate(2026, 11, 10) to 2, LocalDate(2026, 11, 13) to 3), 5L),
                Triple(RoutineCycleLocalEntity.MONTHLY, listOf(LocalDate(2026, 11, 10) to 2, LocalDate(2026, 11, 30) to 3), 5L),
                Triple(RoutineCycleLocalEntity.DAILY, listOf(LocalDate(2026, 11, 11) to 2, LocalDate(2026, 11, 12) to 3), 2L),
            ).forEach { (cycle, recordList, expected) ->
                val accountId = Arb.kotlinUuid().next()
                val routineId = insertRoutine(accountId = accountId, cycle = cycle, recordList = recordList)

                database.cycleAmount(accountId = accountId, routineId = routineId, today = LocalDate(2026, 11, 11)) shouldBe expected
            }
        }

        test("TC-7fbc5f29-45aa-47a3-ad23-43ceeb94f8c4 시작 날짜보다 앞에 놓인 기록도 이번 주기에 들면 주기의 합계에 넣는다") {
            val today = LocalDate(2026, 11, 12)
            val accountId = Arb.kotlinUuid().next()
            val weekly =
                insertRoutine(
                    accountId = accountId,
                    cycle = RoutineCycleLocalEntity.WEEKLY,
                    startDate = LocalDate(2026, 11, 11),
                    recordList = listOf(LocalDate(2026, 11, 9) to 2, LocalDate(2026, 11, 11) to 3),
                )
            val monthly =
                insertRoutine(
                    accountId = accountId,
                    cycle = RoutineCycleLocalEntity.MONTHLY,
                    startDate = LocalDate(2026, 11, 10),
                    recordList = listOf(LocalDate(2026, 11, 5) to 2, LocalDate(2026, 11, 12) to 3),
                )

            database.cycleAmount(accountId = accountId, routineId = weekly, today = today) shouldBe 5L
            database.cycleAmount(accountId = accountId, routineId = monthly, today = today) shouldBe 5L
        }

        test("TC-4210b8a5-ba71-4a36-b7dc-672982539931 목표량을 채운 뒤에도 기록을 넣으면 주기의 합계가 계속 늘어난다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 11, 13)
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), unit = "count", goalAmount = 3).next()
            database.insertRoutine(accountId, routine)
            database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = today, amount = 3)

            AccountRoutineRecordTransactionImpl(database = database).upsert(
                accountId = accountId,
                record = Arb.localRoutineRecord(routineId = routine.id, date = today, amount = 1).next(),
            ) shouldBe 1

            database.cycleAmount(accountId = accountId, routineId = routine.id, today = today) shouldBe 4L
        }

        test("주기의 합계는 양이 가장 큰 기록 둘을 더해도 넘치지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 11, 13)
            val routineId = insertRoutine(accountId = accountId, cycle = RoutineCycleLocalEntity.DAILY, recordList = listOf(today to 9_999_999, today to 9_999_999))

            database.cycleAmount(accountId = accountId, routineId = routineId, today = today) shouldBe 19_999_998L
        }

        test("TC-535c4071-5f2e-4ef7-ad46-72191b97513d 루틴의 단위가 바뀌어도 기록의 양은 그대로이고 바뀐 단위와 함께 조회된다") {
            listOf("회", "m", "L", "").forEach { changedUnit ->
                val accountId = Arb.kotlinUuid().next()
                val today = LocalDate(2026, 11, 13)
                val routine = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), unit = "ml").next()
                database.insertRoutine(accountId, routine)
                val record = database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = today, amount = 1500)

                AccountRoutineTransactionImpl(database = database).updateDetail(
                    accountId = accountId,
                    routineId = routine.id,
                    detail = routine.detail.copy(unit = changedUnit),
                    updatedAt = Arb.secondPrecisionInstant().next(),
                ) shouldBe 1

                database.findRoutineRecordList().filter { stored -> stored.routineId == routine.id } shouldBe listOf(record)
                val summary = database.routineSummaryList(accountId = accountId, today = today).single()
                summary.cycleAmount shouldBe 1500L
                summary.routine.detail.unit shouldBe changedUnit
                database.calendarRoutineRecordList(accountId = accountId, start = today, endInclusive = today).map { calendarRecord -> calendarRecord.amount to calendarRecord.unit } shouldBe
                    listOf(1500L to changedUnit)
            }
        }

        test("TC-3ba53fe9-94b9-495b-895b-c7ae224eec65 시작 날짜를 뒤로 옮겨 시작 날짜보다 앞에 놓인 기록도 지워지지 않고 합계에 그대로 쓰인다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 11, 13)
            val recordDate = LocalDate(2026, 11, 5)
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 11, 1), cycle = RoutineCycleLocalEntity.MONTHLY).next()
            database.insertRoutine(accountId, routine)
            val record = database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = recordDate, amount = 300)

            AccountRoutineTransactionImpl(database = database).updateDetail(
                accountId = accountId,
                routineId = routine.id,
                detail = routine.detail.copy(startDate = LocalDate(2026, 11, 10)),
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 1

            database.findRoutineRecordList() shouldBe listOf(record)
            database.cycleAmount(accountId = accountId, routineId = routine.id, today = today) shouldBe 300L
            database.calendarRoutineRecordList(accountId = accountId, start = recordDate, endInclusive = recordDate).map { calendarRecord -> calendarRecord.amount } shouldBe listOf(300L)
        }

        test("TC-97058f6c-8269-4b74-9e28-27cb5dfb0943 목록은 진행 중, 시작 전 순이고 같은 진행 상태 안에서는 제목 오름차순이다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                Arb.localRoutine(title = "나", startDate = LocalDate(2026, 11, 20)).next(),
                Arb.localRoutine(title = "다", startDate = LocalDate(2026, 11, 1)).next(),
                Arb.localRoutine(title = "라", startDate = LocalDate(2026, 11, 13)).next(),
                Arb.localRoutine(title = "마", startDate = LocalDate(2026, 11, 14)).next(),
            )

            database.routineTitleList(accountId = accountId, today = LocalDate(2026, 11, 13)) shouldBe listOf("다", "라", "나", "마")
        }

        test("TC-2d6a3601-5088-49c1-812f-be69a860e37d 오늘의 요일이 하는 요일에 드는지는 목록 순서에 영향을 주지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                Arb.localRoutine(title = "다", startDate = LocalDate(2026, 11, 1), cycle = RoutineCycleLocalEntity.WEEKENDS).next(),
                Arb.localRoutine(title = "가", startDate = LocalDate(2026, 11, 1), cycle = RoutineCycleLocalEntity.WEEKDAYS).next(),
                Arb.localRoutine(title = "라", startDate = LocalDate(2026, 11, 20), cycle = RoutineCycleLocalEntity.WEEKENDS).next(),
                Arb.localRoutine(title = "나", startDate = LocalDate(2026, 11, 1), cycle = RoutineCycleLocalEntity.DAILY).next(),
            )

            database.routineTitleList(accountId = accountId, today = LocalDate(2026, 11, 14)) shouldBe listOf("가", "나", "다", "라")
        }

        test("TC-3473ecbe-d720-47c8-a2f1-58f331bdd777 합계, 달성률, 주기와 단위는 목록 순서에 영향을 주지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 11, 13)
            val da = Arb.localRoutine(title = "다", startDate = LocalDate(2026, 1, 1), cycle = RoutineCycleLocalEntity.WEEKLY, unit = "ml").next()
            val ga =
                Arb
                    .localRoutine(title = "가", startDate = LocalDate(2026, 1, 1), cycle = RoutineCycleLocalEntity.MONTHLY, unit = "m", goalAmount = 20_000)
                    .next()
            val na = Arb.localRoutine(title = "나", startDate = LocalDate(2026, 1, 1), cycle = RoutineCycleLocalEntity.DAILY, unit = "count", goalAmount = 3).next()
            database.insertRoutine(accountId, da, ga, na)
            database.insertRoutineRecord(accountId = accountId, routineId = da.id, date = today, amount = 9000)
            database.insertRoutineRecord(accountId = accountId, routineId = na.id, date = today, amount = 3)

            database.routineSummaryList(accountId = accountId, today = today).map { summary -> summary.routine.detail.title to summary.cycleAmount } shouldBe
                listOf("가" to 0L, "나" to 3L, "다" to 9000L)
        }

        test("TC-55d85e67-90bc-486e-90b8-8059a0c17ee1 오늘이 바뀌어 진행 상태가 달라진 루틴은 바뀐 상태의 자리로 옮겨진다") {
            val accountId = Arb.kotlinUuid().next()
            database.insertRoutine(
                accountId,
                Arb.localRoutine(title = "가", startDate = LocalDate(2026, 11, 14)).next(),
                Arb.localRoutine(title = "나", startDate = LocalDate(2026, 11, 1)).next(),
                Arb.localRoutine(title = "다", startDate = LocalDate(2026, 11, 20)).next(),
            )
            database.routineTitleList(accountId = accountId, today = LocalDate(2026, 11, 13)) shouldBe listOf("나", "가", "다")

            database.routineTitleList(accountId = accountId, today = LocalDate(2026, 11, 14)) shouldBe listOf("가", "나", "다")
        }

        test("TC-ec2c4ee3-a73a-4e56-abd1-7ff0ec880d17 완료 여부는 주기의 합계와 날짜별 합계에 영향을 주지 않는다") {
            val today = LocalDate(2026, 11, 11)
            val date = LocalDate(2026, 11, 10)

            listOf(false, true).forEach { isFinished ->
                val accountId = Arb.kotlinUuid().next()
                val routine =
                    Arb
                        .localRoutine(startDate = LocalDate(2026, 11, 1), cycle = RoutineCycleLocalEntity.WEEKLY, isFinished = isFinished, unit = "count", goalAmount = 3)
                        .next()
                database.insertRoutine(accountId, routine)
                listOf(LocalDate(2026, 11, 7), date, date).forEach { recordDate -> database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = recordDate, amount = 1) }

                val summary =
                    AccountRoutineLocalDataSourceImpl(database = database)
                        .findSummary(accountId = accountId, routineId = routine.id, currentCycle = routineCurrentCycleOf(today = today))
                        .first()
                        .shouldNotBeNull()
                val listed = if (isFinished) database.finishedRoutineSummaryList(accountId = accountId, today = today) else database.routineSummaryList(accountId = accountId, today = today)

                summary.cycleAmount shouldBe 2L
                listed.map { item -> item.cycleAmount } shouldBe listOf(2L)
                database.calendarRoutineRecordList(accountId = accountId, start = date, endInclusive = date).map { record -> record.amount to record.unit } shouldBe
                    listOf(2L to "count")
            }
        }

        test("TC-4c8d139c-50a3-4d73-834f-2f6af23f7ffb TC-eaa7d7f7-8938-4679-986e-be6ce7fc7535 완료와 다시 시작은 완료 여부와 수정 시각만 바꾼다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(isDeleted = false, isFinished = false).next()
            database.insertRoutine(accountId, routine)
            val recordList =
                listOf(false, false, true).map { isDeleted ->
                    database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = Arb.kotlinxLocalDate().next(), amount = Arb.routineAmount().next(), isDeleted = isDeleted)
                }
            val transaction = AccountRoutineTransactionImpl(database = database)
            val dataSource = AccountRoutineLocalDataSourceImpl(database = database)
            val finishedAt = routine.updatedAt + 1.seconds
            val restartedAt = finishedAt + 1.seconds

            transaction.updateFinished(accountId = accountId, routineId = routine.id, isFinished = true, updatedAt = finishedAt) shouldBe 1
            dataSource.find(accountId = accountId, routineId = routine.id).first() shouldBe routine.copy(isFinished = true, updatedAt = finishedAt)
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList

            transaction.updateFinished(accountId = accountId, routineId = routine.id, isFinished = false, updatedAt = restartedAt) shouldBe 1
            dataSource.find(accountId = accountId, routineId = routine.id).first() shouldBe routine.copy(isFinished = false, updatedAt = restartedAt)
            database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
        }

        test("TC-d8b54831-56d1-44ff-8a5e-24ae97c4b065 완료 여부는 기록을 넣고 빼거나 목표량을 채워도 저절로 바뀌지 않는다") {
            val today = LocalDate(2026, 11, 13)

            listOf(false, true).forEach { isFinished ->
                val accountId = Arb.kotlinUuid().next()
                val routine =
                    Arb
                        .localRoutine(startDate = LocalDate(2026, 11, 10), cycle = RoutineCycleLocalEntity.DAILY, isFinished = isFinished, unit = "count", goalAmount = 3)
                        .next()
                database.insertRoutine(accountId, routine)
                val recordTransaction = AccountRoutineRecordTransactionImpl(database = database)
                val dataSource = AccountRoutineLocalDataSourceImpl(database = database)

                suspend fun found() = dataSource.find(accountId = accountId, routineId = routine.id).first()

                val recordList = listOf(1, 3).map { amount -> Arb.localRoutineRecord(routineId = routine.id, date = today, amount = amount).next() }
                recordList.forEach { record ->
                    recordTransaction.upsert(accountId = accountId, record = record) shouldBe 1
                    found() shouldBe routine
                }
                recordList.forEach { record ->
                    recordTransaction.updateDeleted(accountId = accountId, recordId = record.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                    found() shouldBe routine
                }
            }
        }

        test("TC-9850658b-1865-4a9c-8067-10b6675fced0 시작 날짜와 기록의 날짜는 기기의 시간대와 관계없이 같은 연·월·일이다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next()
            val defaultTimeZone = TimeZone.getDefault()
            try {
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
                database.insertRoutine(accountId, routine)
                database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = LocalDate(2026, 3, 1), amount = 1)

                // Etc/GMT-14는 협정 세계시보다 14시간 빠르고 Etc/GMT+12는 12시간 느리다.
                listOf("Etc/GMT-14", "UTC", "Etc/GMT+12").forEach { timeZoneId ->
                    TimeZone.setDefault(TimeZone.getTimeZone(timeZoneId))

                    val found =
                        AccountRoutineLocalDataSourceImpl(database = database)
                            .find(accountId = accountId, routineId = routine.id)
                            .first()
                            .shouldNotBeNull()

                    found.detail.startDate shouldBe LocalDate(2026, 3, 1)
                    database.findRoutineRecordList().map { record -> record.date } shouldBe listOf(LocalDate(2026, 3, 1))
                    database.calendarRoutineRecordList(accountId = accountId, start = LocalDate(2026, 3, 1), endInclusive = LocalDate(2026, 3, 1)).map { record -> record.date } shouldBe
                        listOf(LocalDate(2026, 3, 1))
                }
            } finally {
                TimeZone.setDefault(defaultTimeZone)
            }
        }

        test("TC-1af36da8-fc2d-4ba9-a485-75fd74bf43a6 태그 연결은 루틴의 정보와 주기의 합계를 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 11, 13)
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tag)
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), unit = "ml", goalAmount = 2000).next()
            database.insertRoutine(accountId, routine)
            database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = today, amount = 1500)
            val before = database.routineSummaryList(accountId = accountId, today = today)
            before.map { summary -> summary.cycleAmount } shouldBe listOf(1500L)

            database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tag.id)
            database.routineSummaryList(accountId = accountId, today = today) shouldBe before

            database.linkRoutineTag(accountId = accountId, routineId = routine.id, tagId = tag.id, isDeleted = true)
            database.routineSummaryList(accountId = accountId, today = today) shouldBe before
        }

        test("값을 정하지 않고 넣은 루틴 행은 매일, 빈 단위, 한 번에 기록할 양 1이고 목표량이 없으며 완료되지 않은 상태다") {
            val accountId = Arb.kotlinUuid().next()
            val routineId = Arb.kotlinUuid().next()
            database.insertRoutineWithDefaults(routineId = routineId)
            database.accountRoutineDao().upsert(AccountRoutineLocalEntity(accountId = accountId, routineId = routineId, isDirty = true))

            val routine =
                AccountRoutineLocalDataSourceImpl(database = database)
                    .find(accountId = accountId, routineId = routineId)
                    .first()
                    .shouldNotBeNull()
            val detail = routine.detail

            detail.cycle shouldBe RoutineCycleLocalEntity.DAILY
            detail.unit shouldBe ""
            detail.amountPerRecord shouldBe 1
            detail.goalAmount shouldBe null
            routine.isFinished shouldBe false
        }
    })
