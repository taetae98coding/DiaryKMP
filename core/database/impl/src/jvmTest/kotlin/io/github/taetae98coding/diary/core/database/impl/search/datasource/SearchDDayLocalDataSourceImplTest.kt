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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class SearchDDayLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: SearchDDayLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = SearchDDayLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun searchedIdList(
            accountId: Uuid,
            query: String,
            sort: ListSortLocalEntity,
        ): List<Uuid> =
            dataSource
                .page(accountId = accountId, query = query, sort = sort)
                .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, DDayLocalEntity>>()
                .data
                .map { dday -> dday.id }

        test("TC-da410978-7f10-4491-aa7f-5c86cbb2c28c 연결된 태그의 제목은 디데이 검색 일치 판정에 쓰이지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagTitle = "TagOnlyTitle"
            val dday =
                Arb.localDDay(baseDate = LocalDate(2026, 1, 1), title = "DDayTitle").next().let { value ->
                    value.copy(detail = value.detail.copy(description = "DDayDescription"))
                }
            val tag = Arb.tagLocalEntity().next().let { value -> value.copy(detail = value.detail.copy(title = tagTitle), isFinished = false, isDeleted = false) }
            AccountDDayTransactionImpl(database = database).upsert(accountId = accountId, dday = dday, ddayTagList = emptyList())
            AccountTagTransactionImpl(database = database).upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())

            AccountEntityTagTransactionImpl(database = database).upsert(
                accountId = accountId,
                kind = EntityTagKindLocalEntity.DDAY,
                entityId = dday.id,
                tagId = tag.id,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )

            ListSortLocalEntity.entries.forEach { sort ->
                searchedIdList(accountId = accountId, query = tagTitle, sort = sort).shouldBeEmpty()
                searchedIdList(accountId = accountId, query = "DDayTitle", sort = sort) shouldBe listOf(dday.id)
            }
        }
    })
