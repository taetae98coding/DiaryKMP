package io.github.taetae98coding.diary.core.database.impl.memo.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.localMemo
import io.github.taetae98coding.diary.core.testing.tag.localTag
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

        test("TC-f8720167-661e-4e49-9e04-a6ae8a8f5c63 메모 목록은 대표 태그와 관계없이 메모 자신의 컬러로 메모를 표시한다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            val baseMemo = Arb.localMemo(isFinished = false, isDeleted = false, primaryTagId = tag.id).next()
            val memo = baseMemo.copy(detail = baseMemo.detail.copy(color = tag.detail.color.inv()))
            database.withWriteTransaction {
                database.tagDao().upsert(listOf(tag))
                database.accountTagDao().upsert(listOf(AccountTagLocalEntity(accountId = accountId, tagId = tag.id, isDirty = true)))
                database.memoDao().upsert(listOf(memo))
                database.accountMemoDao().upsert(listOf(AccountMemoLocalEntity(accountId = accountId, memoId = memo.id, isDirty = true)))
            }

            val result =
                database
                    .accountMemoDao()
                    .page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue)
                    .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
            val pagedMemo = result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, MemoLocalEntity>>().data.single()

            pagedMemo.detail.color shouldBe memo.detail.color
            pagedMemo.detail.color shouldNotBe tag.detail.color
        }
    })
