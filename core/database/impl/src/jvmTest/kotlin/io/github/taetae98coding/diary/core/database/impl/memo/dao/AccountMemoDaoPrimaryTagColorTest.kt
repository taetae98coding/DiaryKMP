package io.github.taetae98coding.diary.core.database.impl.memo.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.localMemo
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

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

        test("TC-MEMO-PRIMARY-TAG-DOMAIN-010 메모 목록은 대표 태그와 관계없이 메모 자신의 컬러로 메모를 표시한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = fixtureMonkey.localTag(isFinished = false, isDeleted = false)
            val baseMemo = fixtureMonkey.localMemo(isFinished = false, isDeleted = false, primaryTagId = tag.id)
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
