package io.github.taetae98coding.diary.core.database.impl.search.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDayTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.dday.localDDay
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

// 디데이 결과가 어떤 값으로 판정되고, 카드 문구를 정하는 값이 바뀌면 다시 전달되는지를 확인한다.
class SearchDDayLocalDataSourceImplMatchTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: SearchDDayLocalDataSourceImpl
        lateinit var ddayTransaction: AccountDDayTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = SearchDDayLocalDataSourceImpl(database = database)
            ddayTransaction = AccountDDayTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insert(
            accountId: Uuid,
            vararg ddayList: DDayLocalEntity,
        ) {
            ddayList.forEach { dday -> ddayTransaction.upsert(accountId = accountId, dday = dday, ddayTagList = emptyList()) }
        }

        suspend fun PagingSource<Int, DDayLocalEntity>.loadPage(): List<DDayLocalEntity> =
            load(PagingSource.LoadParams.Refresh(key = null, loadSize = LOAD_SIZE, placeholdersEnabled = false))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, DDayLocalEntity>>()
                .data

        suspend fun searchedList(
            accountId: Uuid,
            query: String,
        ): List<DDayLocalEntity> = dataSource.page(accountId = accountId, query = query, sort = ListSortLocalEntity.DEFAULT).loadPage()

        fun dday(
            title: String,
            baseDate: LocalDate,
            repeats: Set<DateTimeUnit.DateBased> = emptySet(),
            endDate: LocalDate? = null,
        ): DDayLocalEntity =
            Arb.localDDay(baseDate = baseDate, repeats = repeats, endDate = endDate, title = title).next().let { value ->
                value.copy(detail = value.detail.copy(description = DESCRIPTION))
            }

        test("TC-be404453-1c58-49ca-9b26-6746e410cec5 카드 문구에만 질의가 있는 디데이는 결과가 되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            // 오늘이 기준일이면 카드 문구가 D-Day가 되지만, 제목과 설명에는 그 문구가 없다.
            insert(accountId, dday(title = "수능", baseDate = LocalDate(2026, 11, 8)))

            searchedList(accountId = accountId, query = "D-Day").shouldBeEmpty()
        }

        test("TC-be404453-1c58-49ca-9b26-6746e410cec5 연결된 태그의 제목에만 질의가 있는 디데이는 결과가 되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val dday = dday(title = "수능", baseDate = LocalDate(2026, 11, 8))
            val tag = Arb.tagLocalEntity().next().let { value -> value.copy(detail = value.detail.copy(title = "여행"), isFinished = false, isDeleted = false) }
            insert(accountId, dday)
            AccountTagTransactionImpl(database = database).upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            AccountEntityTagTransactionImpl(database = database).upsert(
                accountId = accountId,
                kind = EntityTagKindLocalEntity.DDAY,
                entityId = dday.id,
                tagId = tag.id,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )

            searchedList(accountId = accountId, query = "여행").shouldBeEmpty()
        }

        test("TC-71a7676b-8320-441f-a45e-56d8507c3c5f 종료일이 지났거나 다가오는 날짜가 없는 디데이도 결과가 된다") {
            val accountId = Arb.kotlinUuid().next()
            val endedDDay =
                dday(
                    title = "여행 끝",
                    baseDate = LocalDate(2026, 1, 1),
                    repeats = setOf(DateTimeUnit.MonthBased(months = 1)),
                    endDate = LocalDate(2026, 3, 31),
                )
            val passedDDay = dday(title = "여행 지난", baseDate = LocalDate(2026, 1, 1))
            insert(accountId, endedDDay, passedDDay)

            searchedList(accountId = accountId, query = "여행").map { dday -> dday.id } shouldContainExactlyInAnyOrder listOf(endedDDay.id, passedDDay.id)
        }

        test("TC-d80c53cc-1252-47d8-aa6f-c2c794d61614 카드 문구를 정하는 기준일이 바뀌면 바뀐 기준일로 결과가 다시 전달된다") {
            val accountId = Arb.kotlinUuid().next()
            val exam = dday(title = "시험 수능", baseDate = LocalDate(2026, 3, 20))
            insert(accountId, exam)
            val pagingSource = dataSource.page(accountId = accountId, query = "시험", sort = ListSortLocalEntity.DEFAULT)
            pagingSource.loadPage().map { dday -> dday.detail.baseDate } shouldBe listOf(LocalDate(2026, 3, 20))

            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }
            ddayTransaction.updateDetail(
                accountId = accountId,
                ddayId = exam.id,
                detail = exam.detail.copy(baseDate = LocalDate(2026, 3, 21)),
                updatedAt = Arb.secondPrecisionInstant().next(),
            )

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
            searchedList(accountId = accountId, query = "시험").map { dday -> dday.id to dday.detail.baseDate } shouldBe
                listOf(exam.id to LocalDate(2026, 3, 21))
        }
    }) {
    private companion object {
        const val DESCRIPTION = "설명"
        const val LOAD_SIZE = 100
        const val INVALIDATION_TIMEOUT_MILLIS = 5_000L
    }
}
