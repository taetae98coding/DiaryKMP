package io.github.taetae98coding.diary.core.database.impl.dday.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.data.testing.dday.ddayLocalEntity
import io.github.taetae98coding.diary.core.data.testing.tag.localTag
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountTagDDayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDayTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.search.datasource.SearchDDayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.uuid.Uuid

class AccountDDayDaoPrimaryTagColorTest :
    FunSpec({
        lateinit var database: DiaryDatabase

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
        }

        afterTest {
            database.close()
        }

        listOf<Pair<String, (DiaryDatabase, Uuid, Uuid, String) -> PagingSource<Int, DDayLocalEntity>>>(
            "DDayHome 목록" to { database, accountId, _, _ -> AccountDDayLocalDataSourceImpl(database = database).page(accountId = accountId) },
            "TagDetail 디데이 탭" to { database, accountId, tagId, _ ->
                AccountTagDDayLocalDataSourceImpl(database = database).page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF)
            },
            "SearchHome 디데이 검색 결과" to { database, accountId, _, title ->
                SearchDDayLocalDataSourceImpl(database = database).page(accountId = accountId, query = title, sort = ListSortLocalEntity.DEFAULT)
            },
        ).forEach { (label, page) ->
            test("TC-e36fa6c4-24e6-40c2-a2e4-3cd59ddceb7e $label: 대표 태그가 아닌 디데이 자신의 컬러로 조회한다") {
                val accountId = Arb.kotlinUuid().next()
                val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
                val base = Arb.ddayLocalEntity().next()
                val dday =
                    base.copy(
                        detail = base.detail.copy(title = Arb.alphanumericString(prefix = "DDay").next(), color = tag.detail.color.inv()),
                        primaryTagId = tag.id,
                        isDeleted = false,
                    )
                AccountTagTransactionImpl(database = database).upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
                AccountDDayTransactionImpl(database = database).upsert(
                    accountId = accountId,
                    dday = dday,
                    ddayTagList = listOf(EntityTagLocalEntity(entityId = dday.id, tagId = tag.id, isDeleted = false, updatedAt = dday.updatedAt, createdAt = dday.createdAt)),
                )

                val paged =
                    page(database, accountId, tag.id, dday.detail.title)
                        .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
                        .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, DDayLocalEntity>>()
                        .data
                        .single()

                paged.id shouldBe dday.id
                paged.detail.color shouldBe dday.detail.color
                paged.detail.color shouldNotBe tag.detail.color
            }
        }
    })
