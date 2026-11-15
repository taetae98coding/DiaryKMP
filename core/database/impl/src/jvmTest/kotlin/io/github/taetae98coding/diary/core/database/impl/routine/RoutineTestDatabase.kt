package io.github.taetae98coding.diary.core.database.impl.routine

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import androidx.room3.withWriteTransaction
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.CalendarRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCurrentCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountCalendarRoutineRecordLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.entity.AccountRoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinefilter.entity.RoutineFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinerecord.entity.AccountRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutineRecord
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Instant
import kotlin.uuid.Uuid

private const val DAYS_PER_WEEK = 7
private const val WEEKDAY_COUNT = 5
private const val LOAD_SIZE = 100

internal val CALENDAR_RANGE_START: LocalDate = LocalDate(2026, 6, 28)
internal val CALENDAR_RANGE_END_INCLUSIVE: LocalDate = LocalDate(2026, 8, 8)

internal fun routineTestDatabase(): DiaryDatabase =
    Room
        .inMemoryDatabaseBuilder<DiaryDatabase>()
        .setDriver(BundledSQLiteDriver())
        .build()

// 오늘이 속한 일요일부터 토요일까지의 주와 1일부터 마지막 날까지의 달을 이번 주기로 넘긴다.
internal fun routineCurrentCycleOf(today: LocalDate): RoutineCurrentCycleLocalEntity {
    val weekStart = today.minus(today.dayOfWeek.isoDayNumber % DAYS_PER_WEEK, DateTimeUnit.DAY)
    val monthStart = LocalDate(year = today.year, month = today.month, day = 1)

    return RoutineCurrentCycleLocalEntity(
        today = today,
        week = weekStart..weekStart.plus(DAYS_PER_WEEK - 1, DateTimeUnit.DAY),
        month = monthStart..monthStart.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY),
    )
}

internal suspend fun <T : Any> PagingSource<Int, T>.loadAll(): List<T> =
    load(PagingSource.LoadParams.Refresh(key = null, loadSize = LOAD_SIZE, placeholdersEnabled = false))
        .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, T>>()
        .data

internal suspend fun DiaryDatabase.insertRoutine(
    accountId: Uuid,
    vararg routineList: RoutineLocalEntity,
) {
    routineList.forEach { routine -> AccountRoutineTransactionImpl(database = this).upsert(accountId = accountId, routine = routine, routineTagList = emptyList()) }
}

// 삭제된 루틴이나 다른 계정의 루틴에 속한 기록처럼 기록 넣기로는 만들 수 없는 상태도 준비할 수 있도록 테이블에 직접 넣는다.
internal suspend fun DiaryDatabase.insertRoutineRecord(
    accountId: Uuid,
    routineId: Uuid,
    date: LocalDate,
    amount: Int,
    isDeleted: Boolean = false,
    createdAt: Instant = Arb.secondPrecisionInstant().next(),
): RoutineRecordLocalEntity {
    val record = Arb.localRoutineRecord(routineId = routineId, date = date, amount = amount, isDeleted = isDeleted).next().copy(createdAt = createdAt)

    withWriteTransaction {
        routineRecordDao().upsert(record)
        accountRoutineRecordDao().upsert(AccountRoutineRecordLocalEntity(accountId = accountId, routineRecordId = record.id, isDirty = true))
    }

    return record
}

internal suspend fun DiaryDatabase.routineSummaryList(
    accountId: Uuid,
    today: LocalDate,
): List<RoutineSummaryLocalEntity> =
    AccountRoutineLocalDataSourceImpl(database = this)
        .page(accountId = accountId, currentCycle = routineCurrentCycleOf(today = today))
        .loadAll()

internal fun routineCycleListDoneOn(today: LocalDate): List<RoutineCycleLocalEntity> {
    val isWeekend = today.dayOfWeek.isoDayNumber > WEEKDAY_COUNT

    return RoutineCycleLocalEntity.entries.filter { cycle ->
        when (cycle) {
            RoutineCycleLocalEntity.WEEKDAYS -> !isWeekend
            RoutineCycleLocalEntity.WEEKENDS -> isWeekend
            RoutineCycleLocalEntity.DAILY, RoutineCycleLocalEntity.WEEKLY, RoutineCycleLocalEntity.MONTHLY -> true
        }
    }
}

internal suspend fun DiaryDatabase.todayRoutineSummaryList(
    accountId: Uuid,
    today: LocalDate,
): List<RoutineSummaryLocalEntity> =
    AccountRoutineLocalDataSourceImpl(database = this)
        .pageStarted(accountId = accountId, currentCycle = routineCurrentCycleOf(today = today), cycleList = routineCycleListDoneOn(today = today))
        .loadAll()

internal suspend fun DiaryDatabase.todayRoutineTitleList(
    accountId: Uuid,
    today: LocalDate,
): List<String> = todayRoutineSummaryList(accountId = accountId, today = today).map { summary -> summary.routine.detail.title }

internal suspend fun DiaryDatabase.finishedRoutineSummaryList(
    accountId: Uuid,
    today: LocalDate,
): List<RoutineSummaryLocalEntity> =
    AccountRoutineLocalDataSourceImpl(database = this)
        .pageFinished(accountId = accountId, currentCycle = routineCurrentCycleOf(today = today))
        .loadAll()

internal suspend fun DiaryDatabase.finishedRoutineTitleList(
    accountId: Uuid,
    today: LocalDate,
): List<String> = finishedRoutineSummaryList(accountId = accountId, today = today).map { summary -> summary.routine.detail.title }

internal suspend fun DiaryDatabase.routineTitleList(
    accountId: Uuid,
    today: LocalDate,
): List<String> = routineSummaryList(accountId = accountId, today = today).map { summary -> summary.routine.detail.title }

internal suspend fun DiaryDatabase.cycleAmount(
    accountId: Uuid,
    routineId: Uuid,
    today: LocalDate,
): Long = routineSummaryList(accountId = accountId, today = today).single { summary -> summary.routine.id == routineId }.cycleAmount

internal suspend fun DiaryDatabase.calendarRoutineRecordList(
    accountId: Uuid,
    start: LocalDate = CALENDAR_RANGE_START,
    endInclusive: LocalDate = CALENDAR_RANGE_END_INCLUSIVE,
): List<CalendarRoutineRecordLocalEntity> = AccountCalendarRoutineRecordLocalDataSourceImpl(database = this).get(accountId = accountId, dateRange = start..endInclusive).first()

internal suspend fun DiaryDatabase.insertRoutineTestTag(
    accountId: Uuid,
    vararg tagList: TagLocalEntity,
) {
    AccountTagTransactionImpl(database = this).upsert(accountId = accountId, tagList = tagList.toList(), tagLinkList = emptyList())
}

internal suspend fun DiaryDatabase.linkRoutineTag(
    accountId: Uuid,
    routineId: Uuid,
    tagId: Uuid,
    isDeleted: Boolean = false,
) {
    AccountEntityTagTransactionImpl(database = this).upsert(
        accountId = accountId,
        kind = EntityTagKindLocalEntity.ROUTINE,
        entityId = routineId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = Arb.secondPrecisionInstant().next(),
    )
}

internal suspend fun DiaryDatabase.selectRoutineFilterTag(
    accountId: Uuid,
    tagId: Uuid,
) {
    routineFilterTagDao().upsert(entity = RoutineFilterTagLocalEntity(accountId = accountId, tagId = tagId))
}

internal suspend fun DiaryDatabase.findRoutineRecordList(): List<RoutineRecordLocalEntity> =
    useReaderConnection { transactor ->
        transactor.usePrepared(
            """
            SELECT id, routine_id, date, amount, is_deleted, updated_at, created_at
            FROM routine_record
            ORDER BY id ASC
            """,
        ) { statement ->
            statement.readAll {
                RoutineRecordLocalEntity(
                    id = Uuid.parse(it.getText(0)),
                    routineId = Uuid.parse(it.getText(1)),
                    date = LocalDate.parse(it.getText(2)),
                    amount = it.getInt(3),
                    isDeleted = it.getBoolean(4),
                    updatedAt = Instant.fromEpochMilliseconds(it.getLong(5)),
                    createdAt = Instant.fromEpochMilliseconds(it.getLong(6)),
                )
            }
        }
    }

internal suspend fun DiaryDatabase.findAccountRoutineRecordList(): List<AccountRoutineRecordLocalEntity> =
    useReaderConnection { transactor ->
        transactor.usePrepared(
            """
            SELECT account_id, routine_record_id, is_dirty
            FROM account_routine_record
            ORDER BY account_id ASC, routine_record_id ASC
            """,
        ) { statement ->
            statement.readAll {
                AccountRoutineRecordLocalEntity(
                    accountId = Uuid.parse(it.getText(0)),
                    routineRecordId = Uuid.parse(it.getText(1)),
                    isDirty = it.getBoolean(2),
                )
            }
        }
    }

internal suspend fun DiaryDatabase.findAccountRoutineList(): List<AccountRoutineLocalEntity> =
    useReaderConnection { transactor ->
        transactor.usePrepared(
            """
            SELECT account_id, routine_id, is_dirty
            FROM account_routine
            ORDER BY account_id ASC, routine_id ASC
            """,
        ) { statement ->
            statement.readAll {
                AccountRoutineLocalEntity(
                    accountId = Uuid.parse(it.getText(0)),
                    routineId = Uuid.parse(it.getText(1)),
                    isDirty = it.getBoolean(2),
                )
            }
        }
    }

// 주기와 단위가 저장소에 기록된 글자 그대로의 값이다.
internal suspend fun DiaryDatabase.findRoutineCycleAndUnitText(routineId: Uuid): Pair<String, String> =
    useReaderConnection { transactor ->
        transactor.usePrepared("SELECT cycle, unit FROM routine WHERE id = ?") { statement ->
            statement.bindText(1, routineId.toString())
            statement.readAll { it.getText(0) to it.getText(1) }.single()
        }
    }

// 식별값만 정해 넣어, 따로 정하지 않은 값이 저장소의 기본값으로 채워지게 한다.
internal suspend fun DiaryDatabase.insertRoutineWithDefaults(routineId: Uuid) {
    useWriterConnection { transactor ->
        transactor.usePrepared("INSERT INTO routine (id) VALUES (?)") { statement ->
            statement.bindText(1, routineId.toString())
            statement.step()
        }
    }
}

// 변경이 업로드 대기를 다시 세우는지 보려면 저장 직후의 대기 상태를 먼저 지워야 한다.
internal suspend fun DiaryDatabase.markRoutineUploaded(accountId: Uuid) {
    useWriterConnection { transactor ->
        listOf("account_routine", "account_routine_record", "account_routine_tag").forEach { table ->
            transactor.usePrepared("UPDATE $table SET is_dirty = 0 WHERE account_id = ?") { statement ->
                statement.bindText(1, accountId.toString())
                statement.step()
            }
        }
    }
}

private fun <T> SQLiteStatement.readAll(read: (SQLiteStatement) -> T): List<T> =
    buildList {
        while (step()) {
            add(read(this@readAll))
        }
    }
