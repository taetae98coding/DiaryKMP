package io.github.taetae98coding.diary.core.database.impl.ddayfilter.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.calendarfilter.entity.CalendarFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddayfilter.datasource.AccountDDayFilterLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memofilter.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.AccountDataTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.testing.tag.tagDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

class DDayHomeDDayFilterTagDaoTest :
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

        suspend fun insertTag(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
        ) {
            database.withWriteTransaction {
                database.tagDao().upsert(tagList.toList())
                database.accountTagDao().upsert(
                    tagList.map { tag ->
                        AccountTagLocalEntity(
                            accountId = accountId,
                            tagId = tag.id,
                            isDirty = false,
                        )
                    },
                )
            }
        }

        fun dataSource(): AccountDDayFilterLocalDataSourceImpl = AccountDDayFilterLocalDataSourceImpl(database = database)

        suspend fun selectedTagIdList(accountId: Uuid): List<Uuid> =
            dataSource()
                .getTagList(accountId = accountId)
                .first()
                .map { tag -> tag.id }

        suspend fun memoSelectedTagIdList(accountId: Uuid): List<Uuid> =
            database
                .memoFilterTagDao()
                .getTagList(accountId = accountId)
                .first()
                .map { tag -> tag.id }

        suspend fun calendarSelectedTagIdList(accountId: Uuid): List<Uuid> =
            database
                .calendarFilterTagDao()
                .getTagList(accountId = accountId)
                .first()
                .map { tag -> tag.id }

        suspend fun selectMemo(
            accountId: Uuid,
            tagId: Uuid,
        ) {
            database.memoFilterTagDao().upsert(entity = MemoFilterTagLocalEntity(accountId = accountId, tagId = tagId))
        }

        suspend fun selectCalendar(
            accountId: Uuid,
            tagId: Uuid,
        ) {
            database.calendarFilterTagDao().upsert(entity = CalendarFilterTagLocalEntity(accountId = accountId, tagId = tagId))
        }

        test("TC-46f30852-bdb4-4dd8-86c6-633e7c4a34bc 선택할 수 있는 태그만 제목 오름차순으로 제공한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val charlie = tag(title = "Charlie")
            val alpha = tag(title = "Alpha")
            val finished = tag(title = "Bravo", isFinished = true)
            val deleted = tag(title = "Bravo2", isDeleted = true)
            val otherAccountTag = tag(title = "Bravo3")
            insertTag(accountId, charlie, alpha, finished, deleted)
            insertTag(otherAccountId, otherAccountTag)

            val result =
                database
                    .accountTagDao()
                    .page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT.queryValue)
                    .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))

            result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, TagLocalEntity>>().data.map { tag -> tag.id } shouldBe listOf(alpha.id, charlie.id)
        }

        test("TC-4407c962-2b91-48d0-a808-15a53c159e25 필터 선택을 현재 사용자 계정에 즉시 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId, tag)
            selectedTagIdList(accountId).shouldBeEmpty()

            dataSource().upsert(accountId = accountId, tagId = tag.id)

            selectedTagIdList(accountId) shouldBe listOf(tag.id)
            dataSource().getTagIdList(accountId = accountId).first() shouldBe listOf(tag.id)
        }

        context("TC-6853f7a9-382b-4b08-8078-1e78067cedb8 계정마다 서로 다른 필터 선택을 유지한다") {
            test("첫 번째에서 두 번째 계정으로 바뀌면 두 번째 계정의 선택만 적용된다") {
                val firstAccountId = Arb.kotlinUuid().next()
                val secondAccountId = Arb.kotlinUuid().next()
                val firstTag = tag(title = "Alpha")
                val secondTag = tag(title = "Bravo")
                insertTag(firstAccountId, firstTag, secondTag)
                insertTag(secondAccountId, firstTag, secondTag)
                dataSource().upsert(accountId = firstAccountId, tagId = firstTag.id)
                dataSource().upsert(accountId = secondAccountId, tagId = secondTag.id)
                selectedTagIdList(firstAccountId) shouldBe listOf(firstTag.id)

                selectedTagIdList(secondAccountId) shouldBe listOf(secondTag.id)
            }

            test("첫 번째에서 세 번째 계정으로 바뀌면 선택한 태그가 없다") {
                val firstAccountId = Arb.kotlinUuid().next()
                val thirdAccountId = Arb.kotlinUuid().next()
                val tag = tag()
                insertTag(firstAccountId, tag)
                insertTag(thirdAccountId, tag)
                dataSource().upsert(accountId = firstAccountId, tagId = tag.id)
                selectedTagIdList(firstAccountId) shouldBe listOf(tag.id)

                selectedTagIdList(thirdAccountId).shouldBeEmpty()
            }

            test("두 번째에서 로그아웃 뒤 같은 두 번째 계정으로 다시 로그인하면 두 번째 계정의 선택이 다시 적용된다") {
                val secondAccountId = Arb.kotlinUuid().next()
                val guestAccountId = Uuid.NIL
                val tag = tag()
                insertTag(secondAccountId, tag)
                dataSource().upsert(accountId = secondAccountId, tagId = tag.id)
                selectedTagIdList(secondAccountId) shouldBe listOf(tag.id)

                selectedTagIdList(guestAccountId).shouldBeEmpty()

                selectedTagIdList(secondAccountId) shouldBe listOf(tag.id)
            }
        }

        test("TC-34742a39-186a-436b-b358-e7b3aeb9088b 필터 선택과 해제는 서버 동기화 대상으로 기록되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId, tag)

            dataSource().upsert(accountId = accountId, tagId = tag.id)
            dataSource().delete(accountId = accountId, tagId = tag.id)
            dataSource().upsert(accountId = accountId, tagId = tag.id)

            database.accountDDaySyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountTagSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountDDayTagSyncDao().findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-fa768c1e-19a6-4bf1-9773-caf996cdb89e 선택 전체 해제는 무시되고 있던 선택까지 지운다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val tagB = tag(isDeleted = true)
            insertTag(accountId, tagA, tagB)
            dataSource().upsert(accountId = accountId, tagId = tagA.id)
            dataSource().upsert(accountId = accountId, tagId = tagB.id)
            selectedTagIdList(accountId) shouldBe listOf(tagA.id)

            dataSource().deleteAll(accountId = accountId)
            AccountTagSyncTransactionImpl(database = database).upsert(
                accountId = accountId,
                tagList = listOf(tagB.copy(isDeleted = false, updatedAt = Instant.fromEpochMilliseconds(tagB.updatedAt.toEpochMilliseconds() + 1_000))),
                cursor = Arb.long().next(),
            )

            selectedTagIdList(accountId).shouldBeEmpty()
            dataSource().getTagIdList(accountId = accountId).first().shouldBeEmpty()
        }

        test("TC-f6aff045-a681-4be4-ac29-65dd36d1e16f 선택 전체 해제는 다른 계정과 다른 화면의 태그 필터 선택을 지우지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId, tag)
            insertTag(otherAccountId, tag)
            dataSource().upsert(accountId = accountId, tagId = tag.id)
            selectMemo(accountId = accountId, tagId = tag.id)
            selectCalendar(accountId = accountId, tagId = tag.id)
            dataSource().upsert(accountId = otherAccountId, tagId = tag.id)

            dataSource().deleteAll(accountId = accountId)

            selectedTagIdList(accountId).shouldBeEmpty()
            memoSelectedTagIdList(accountId) shouldBe listOf(tag.id)
            calendarSelectedTagIdList(accountId) shouldBe listOf(tag.id)
            selectedTagIdList(otherAccountId) shouldBe listOf(tag.id)
        }

        test("TC-be8dcfaa-a0c3-46a0-85c6-6e06cb1339b7 DDayHome의 필터 선택은 MemoHome과 CalendarHome의 태그 필터 선택과 독립이다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag(title = "Alpha")
            val tagB = tag(title = "Bravo")
            val tagC = tag(title = "Charlie")
            insertTag(accountId, tagA, tagB, tagC)
            selectMemo(accountId = accountId, tagId = tagA.id)
            selectCalendar(accountId = accountId, tagId = tagB.id)
            selectedTagIdList(accountId).shouldBeEmpty()

            dataSource().upsert(accountId = accountId, tagId = tagC.id)

            selectedTagIdList(accountId) shouldBe listOf(tagC.id)
            memoSelectedTagIdList(accountId) shouldBe listOf(tagA.id)
            calendarSelectedTagIdList(accountId) shouldBe listOf(tagB.id)
        }

        test("TC-b2059906-b648-4476-af01-26a7644a1b65 강제 전체 재동기화로 계정 데이터를 지우면 그 계정의 DDayHome 필터 선택이 처음 상태로 돌아간다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId, tag)
            insertTag(otherAccountId, tag)
            dataSource().upsert(accountId = accountId, tagId = tag.id)
            dataSource().upsert(accountId = otherAccountId, tagId = tag.id)

            AccountDataTransactionImpl(database = database).delete(accountId = accountId)
            insertTag(accountId, tag)

            selectedTagIdList(accountId).shouldBeEmpty()
            dataSource().getTagIdList(accountId = accountId).first().shouldBeEmpty()
            selectedTagIdList(otherAccountId) shouldBe listOf(tag.id)
        }
    }) {
    public companion object {
        private fun tag(
            title: String? = null,
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
        ): TagLocalEntity {
            val detail = Arb.tagDetailLocalEntity().next()

            return Arb
                .tagLocalEntity()
                .next()
                .copy(
                    id = Arb.kotlinUuid().next(),
                    detail = detail.copy(title = title ?: detail.title),
                    isFinished = isFinished,
                    isDeleted = isDeleted,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )
        }
    }
}
