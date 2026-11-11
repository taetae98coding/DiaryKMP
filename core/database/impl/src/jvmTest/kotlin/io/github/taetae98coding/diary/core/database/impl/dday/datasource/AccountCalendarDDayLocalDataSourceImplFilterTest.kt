package io.github.taetae98coding.diary.core.database.impl.dday.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.calendarfilter.entity.CalendarFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDayTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountCalendarMemoLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.dday.localDDay
import io.github.taetae98coding.diary.core.testing.memo.memoLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.uuid.Uuid

// 캘린더 홈의 태그 필터가 디데이 표시에 반영되는 규칙을 2026년 7월을 표시할 때의 조회 기간으로 확인한다.
class AccountCalendarDDayLocalDataSourceImplFilterTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountCalendarDDayLocalDataSourceImpl
        lateinit var ddayTransaction: AccountDDayTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var entityTagTransaction: AccountEntityTagTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountCalendarDDayLocalDataSourceImpl(database = database)
            ddayTransaction = AccountDDayTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
            entityTagTransaction = AccountEntityTagTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        fun ddayFlow(accountId: Uuid) = dataSource.get(accountId = accountId, dateRange = JULY_FETCH_START..JULY_FETCH_END_INCLUSIVE)

        suspend fun ddayIdSet(accountId: Uuid): Set<Uuid> = ddayFlow(accountId = accountId).first().map { calendarDDay -> calendarDDay.ddayId }.toSet()

        suspend fun insertTag(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
        ) {
            tagTransaction.upsert(accountId = accountId, tagList = tagList.toList(), tagLinkList = emptyList())
        }

        suspend fun insertDDay(
            accountId: Uuid,
            dday: DDayLocalEntity,
            vararg tagIdList: Uuid,
            isLinkDeleted: Boolean = false,
        ) {
            ddayTransaction.upsert(
                accountId = accountId,
                dday = dday,
                ddayTagList = tagIdList.map { tagId -> entityTag(entityId = dday.id, tagId = tagId, isDeleted = isLinkDeleted) },
            )
        }

        suspend fun selectFilterTag(
            accountId: Uuid,
            vararg tagIdList: Uuid,
        ) {
            tagIdList.forEach { tagId -> database.calendarFilterTagDao().upsert(entity = CalendarFilterTagLocalEntity(accountId = accountId, tagId = tagId)) }
        }

        suspend fun updateDDayLink(
            accountId: Uuid,
            ddayId: Uuid,
            tagId: Uuid,
            isDeleted: Boolean,
        ) {
            entityTagTransaction.upsert(
                accountId = accountId,
                kind = EntityTagKindLocalEntity.DDAY,
                entityId = ddayId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )
        }

        test("TC-1e1576f0-f71d-4a3a-8125-903b5163e166 태그를 선택하면 그 태그와 연결된 디데이만 기준일 당일과 기념일을 모두 표시한다") {
            data class Case(
                val selectedTagList: List<String>,
                val expectedTitleList: List<String>,
            )

            listOf(
                Case(selectedTagList = emptyList(), expectedTitleList = listOf(BABY, PROJECT, EXAM, TRIP)),
                Case(selectedTagList = listOf(FAMILY), expectedTitleList = listOf(BABY)),
                Case(selectedTagList = listOf(FAMILY, WORK), expectedTitleList = listOf(BABY, PROJECT)),
            ).forEach { case ->
                val accountId = Arb.kotlinUuid().next()
                val tagByName = mapOf(FAMILY to selectableTag(), WORK to selectableTag())
                insertTag(accountId, *tagByName.values.toTypedArray())
                val ddayByTitle = listOf(BABY, PROJECT, EXAM, TRIP).associateWith { title -> monthlyDDay(title = title) }
                insertDDay(accountId, ddayByTitle.getValue(BABY), tagByName.getValue(FAMILY).id)
                insertDDay(accountId, ddayByTitle.getValue(PROJECT), tagByName.getValue(WORK).id)
                insertDDay(accountId, ddayByTitle.getValue(EXAM))
                insertDDay(accountId, ddayByTitle.getValue(TRIP), tagByName.getValue(FAMILY).id, isLinkDeleted = true)
                selectFilterTag(accountId, *case.selectedTagList.map { name -> tagByName.getValue(name).id }.toTypedArray())

                val calendarList = ddayFlow(accountId = accountId).first()

                calendarList.map { calendarDDay -> calendarDDay.title }.toSet() shouldBe case.expectedTitleList.toSet()
                case.expectedTitleList.forEach { title ->
                    calendarList.dateListOf(ddayId = ddayByTitle.getValue(title).id).shouldContainAll(JULY_1, AUGUST_1)
                }
            }
        }

        test("TC-1531ef69-9719-4ef0-b218-ce35d5198a3d 메모와 디데이는 같은 필터 선택으로 함께 좁혀진다") {
            val accountId = Arb.kotlinUuid().next()
            val work = selectableTag()
            insertTag(accountId, work)
            val meeting = julyMemo()
            val walk = julyMemo()
            AccountMemoTransactionImpl(database = database).upsert(
                accountId = accountId,
                memoList = listOf(meeting, walk),
                memoTagList =
                    listOf(
                        MemoTagLocalEntity(
                            memoId = meeting.id,
                            tagId = work.id,
                            isDeleted = false,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                            createdAt = Arb.secondPrecisionInstant().next(),
                        ),
                    ),
            )
            val project = monthlyDDay(title = PROJECT)
            val exam = monthlyDDay(title = EXAM)
            insertDDay(accountId, project, work.id)
            insertDDay(accountId, exam)
            val memoDataSource = AccountCalendarMemoLocalDataSourceImpl(database = database)
            val memoFlow = memoDataSource.get(accountId = accountId, dateRange = JULY_FETCH_START..JULY_FETCH_END_INCLUSIVE)

            memoFlow.test {
                val memoTurbine = this
                ddayFlow(accountId = accountId).test {
                    memoTurbine.awaitItem().map { memo -> memo.id } shouldContainExactlyInAnyOrder listOf(meeting.id, walk.id)
                    awaitItem().map { calendarDDay -> calendarDDay.ddayId }.toSet() shouldBe setOf(project.id, exam.id)

                    selectFilterTag(accountId, work.id)

                    memoTurbine.awaitItem().map { memo -> memo.id } shouldBe listOf(meeting.id)
                    awaitItem().map { calendarDDay -> calendarDDay.ddayId }.toSet() shouldBe setOf(project.id)
                    cancelAndIgnoreRemainingEvents()
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-e37be6a1-9bcd-4e2b-80b1-80da0ac5ca89 선택한 태그가 완료되거나 삭제되면 디데이 판정에서도 그 선택을 무시한다") {
            suspend fun unselectableCase(makeUnselectable: suspend (accountId: Uuid, tagId: Uuid) -> Unit) {
                val accountId = Arb.kotlinUuid().next()
                val work = selectableTag()
                insertTag(accountId, work)
                val project = monthlyDDay(title = PROJECT)
                val exam = monthlyDDay(title = EXAM)
                insertDDay(accountId, project, work.id)
                insertDDay(accountId, exam)
                selectFilterTag(accountId, work.id)
                ddayIdSet(accountId = accountId) shouldBe setOf(project.id)

                makeUnselectable(accountId, work.id)

                ddayIdSet(accountId = accountId) shouldBe setOf(project.id, exam.id)
            }

            unselectableCase { accountId, tagId ->
                database.accountTagDao().updateFinished(accountId = accountId, tagId = tagId, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())
            }
            unselectableCase { accountId, tagId ->
                database.accountTagDao().updateDeleted(accountId = accountId, tagId = tagId, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
            }
        }

        test("TC-e37be6a1-9bcd-4e2b-80b1-80da0ac5ca89 선택한 태그가 현재 계정과 연결되지 않게 되면 디데이 판정에서 그 선택을 무시한다") {
            val accountId = Arb.kotlinUuid().next()
            val work = selectableTag()
            insertTag(accountId, work)
            val project = monthlyDDay(title = PROJECT)
            val exam = monthlyDDay(title = EXAM)
            insertDDay(accountId, project, work.id)
            insertDDay(accountId, exam)
            selectFilterTag(accountId, work.id)
            ddayIdSet(accountId = accountId) shouldBe setOf(project.id)

            database.accountDataDao().deleteAccountTag(accountId = accountId)

            ddayIdSet(accountId = accountId) shouldBe setOf(project.id, exam.id)
        }

        test("TC-da5e5935-8998-4da0-a2c8-80b01933f355 필터를 적용한 동안 디데이와 태그의 연결이 바뀌면 조작 없이 반영된다") {
            listOf(
                Triple(EXAM, false, setOf(PROJECT, EXAM)),
                Triple(PROJECT, true, emptySet()),
            ).forEach { (changedTitle, isDeleted, expectedTitleSet) ->
                val accountId = Arb.kotlinUuid().next()
                val work = selectableTag()
                insertTag(accountId, work)
                val ddayByTitle = mapOf(PROJECT to monthlyDDay(title = PROJECT), EXAM to monthlyDDay(title = EXAM))
                insertDDay(accountId, ddayByTitle.getValue(PROJECT), work.id)
                insertDDay(accountId, ddayByTitle.getValue(EXAM))
                selectFilterTag(accountId, work.id)

                ddayFlow(accountId = accountId).test {
                    awaitItem().map { calendarDDay -> calendarDDay.title }.toSet() shouldBe setOf(PROJECT)

                    updateDDayLink(accountId = accountId, ddayId = ddayByTitle.getValue(changedTitle).id, tagId = work.id, isDeleted = isDeleted)

                    awaitItem().map { calendarDDay -> calendarDDay.title }.toSet() shouldBe expectedTitleSet
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }) {
    private companion object {
        val JULY_FETCH_START: LocalDate = LocalDate(2026, 5, 1)
        val JULY_FETCH_END_INCLUSIVE: LocalDate = LocalDate(2026, 9, 30)
        val JULY_1: LocalDate = LocalDate(2026, 7, 1)
        val AUGUST_1: LocalDate = LocalDate(2026, 8, 1)
        const val FAMILY = "가족"
        const val WORK = "업무"
        const val BABY = "아기"
        const val PROJECT = "프로젝트"
        const val EXAM = "수능"
        const val TRIP = "여행"

        fun monthlyDDay(title: String): DDayLocalEntity = Arb.localDDay(baseDate = JULY_1, repeats = setOf(DateTimeUnit.MonthBased(months = 1)), title = title).next()

        fun selectableTag(): TagLocalEntity = Arb.tagLocalEntity().next().copy(isFinished = false, isDeleted = false)

        fun julyMemo(): MemoLocalEntity =
            Arb.memoLocalEntity().next().let { memo ->
                memo.copy(
                    detail = memo.detail.copy(isAllDay = true, start = LocalDateTime(date = LocalDate(2026, 7, 6), time = LocalTime(0, 0)), endInclusive = LocalDateTime(date = LocalDate(2026, 7, 7), time = LocalTime(0, 0))),
                    isFinished = false,
                    isDeleted = false,
                )
            }

        fun entityTag(
            entityId: Uuid,
            tagId: Uuid,
            isDeleted: Boolean,
        ): EntityTagLocalEntity =
            EntityTagLocalEntity(
                entityId = entityId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = Arb.secondPrecisionInstant().next(),
                createdAt = Arb.secondPrecisionInstant().next(),
            )

        fun List<CalendarDDayLocalEntity>.dateListOf(ddayId: Uuid): List<LocalDate> = filter { calendarDDay -> calendarDDay.ddayId == ddayId }.map { calendarDDay -> calendarDDay.date }
    }
}
