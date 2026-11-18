package io.github.taetae98coding.diary.core.database.impl.memo.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.data.testing.memo.localMemo
import io.github.taetae98coding.diary.core.data.testing.tag.localTag
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountMemoLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountTagMemoLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.search.datasource.SearchMemoLocalDataSourceImpl
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

class AccountMemoDaoPrimaryTagColorTest :
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

        listOf<Pair<String, (DiaryDatabase, Uuid, Uuid, String) -> PagingSource<Int, MemoLocalEntity>>>(
            "MemoHome 목록" to { database, accountId, _, _ ->
                AccountMemoLocalDataSourceImpl(database = database).page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT)
            },
            "TagDetail 메모 탭" to { database, accountId, tagId, _ ->
                AccountTagMemoLocalDataSourceImpl(database = database).page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF, sort = ListSortLocalEntity.DEFAULT)
            },
            "SearchHome 메모 검색 결과" to { database, accountId, _, title ->
                SearchMemoLocalDataSourceImpl(database = database).page(accountId = accountId, query = title, sort = ListSortLocalEntity.DEFAULT)
            },
        ).forEach { (label, page) ->
            test("TC-f8720167-661e-4e49-9e04-a6ae8a8f5c63 $label: 대표 태그가 아닌 메모 자신의 컬러로 조회한다") {
                val accountId = Arb.kotlinUuid().next()
                val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
                val base = Arb.localMemo(isFinished = false, isDeleted = false, primaryTagId = tag.id).next()
                val memo = base.copy(detail = base.detail.copy(title = Arb.alphanumericString(prefix = "Memo").next(), color = tag.detail.color.inv()))
                AccountTagTransactionImpl(database = database).upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
                AccountMemoTransactionImpl(database = database).upsert(
                    accountId = accountId,
                    memoList = listOf(memo),
                    memoTagList = listOf(MemoTagLocalEntity(memoId = memo.id, tagId = tag.id, isDeleted = false, updatedAt = memo.updatedAt, createdAt = memo.createdAt)),
                    memoPlaceList = emptyList(),
                    memoWebList = emptyList(),
                    memoContactList = emptyList(),
                )

                val paged =
                    page(database, accountId, tag.id, memo.detail.title)
                        .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
                        .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, MemoLocalEntity>>()
                        .data
                        .single()

                paged.id shouldBe memo.id
                paged.detail.color shouldBe memo.detail.color
                paged.detail.color shouldNotBe tag.detail.color
            }
        }
    })
