package io.github.taetae98coding.diary.core.database.impl.routinefilter.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.calendarfilter.entity.CalendarFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddayfilter.entity.DDayFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memofilter.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinefilter.datasource.AccountRoutineFilterLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.AccountDataTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
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

class RoutineHomeRoutineFilterTagDaoTest :
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

        fun dataSource(): AccountRoutineFilterLocalDataSourceImpl = AccountRoutineFilterLocalDataSourceImpl(database = database)

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

        suspend fun ddaySelectedTagIdList(accountId: Uuid): List<Uuid> =
            database
                .ddayFilterTagDao()
                .getTagList(accountId = accountId)
                .first()
                .map { tag -> tag.id }

        suspend fun selectDDay(
            accountId: Uuid,
            tagId: Uuid,
        ) {
            database.ddayFilterTagDao().upsert(entity = DDayFilterTagLocalEntity(accountId = accountId, tagId = tagId))
        }

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

        test("TC-65a5f4df-ef5f-483c-961b-a8fdbe5541fc 선택할 수 있는 태그만 제목 오름차순으로 제공한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val charlie = Arb.localTag(isFinished = false, isDeleted = false, title = "Charlie").next()
            val alpha = Arb.localTag(isFinished = false, isDeleted = false, title = "Alpha").next()
            val finished = Arb.localTag(isFinished = true, isDeleted = false, title = "Bravo").next()
            val deleted = Arb.localTag(isFinished = false, isDeleted = true, title = "Bravo2").next()
            val otherAccountTag = Arb.localTag(isFinished = false, isDeleted = false, title = "Bravo3").next()
            insertTag(accountId, charlie, alpha, finished, deleted)
            insertTag(otherAccountId, otherAccountTag)

            val result =
                database
                    .accountTagDao()
                    .page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT.queryValue)
                    .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))

            result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, TagLocalEntity>>().data.map { tag -> tag.id } shouldBe listOf(alpha.id, charlie.id)
        }

        test("TC-b44f19f0-b15a-4e76-8115-63ae5013a07f 필터 선택을 현재 사용자 계정의 선택 하나로 즉시 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)
            selectedTagIdList(accountId).shouldBeEmpty()

            dataSource().upsert(accountId = accountId, tagId = tag.id)

            selectedTagIdList(accountId) shouldBe listOf(tag.id)
            dataSource().getTagIdList(accountId = accountId).first() shouldBe listOf(tag.id)
        }

        context("TC-e472bb1e-c379-44d9-83ad-27799ade41e6 계정마다 서로 다른 필터 선택을 유지한다") {
            test("첫 번째에서 두 번째 계정으로 바뀌면 두 번째 계정의 선택만 적용된다") {
                val firstAccountId = Arb.kotlinUuid().next()
                val secondAccountId = Arb.kotlinUuid().next()
                val firstTag = Arb.localTag(isFinished = false, isDeleted = false, title = "Alpha").next()
                val secondTag = Arb.localTag(isFinished = false, isDeleted = false, title = "Bravo").next()
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
                val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
                insertTag(firstAccountId, tag)
                insertTag(thirdAccountId, tag)
                dataSource().upsert(accountId = firstAccountId, tagId = tag.id)
                selectedTagIdList(firstAccountId) shouldBe listOf(tag.id)

                selectedTagIdList(thirdAccountId).shouldBeEmpty()
            }

            test("두 번째에서 로그아웃 뒤 같은 두 번째 계정으로 다시 로그인하면 두 번째 계정의 선택이 다시 적용된다") {
                val secondAccountId = Arb.kotlinUuid().next()
                val guestAccountId = Uuid.NIL
                val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
                insertTag(secondAccountId, tag)
                dataSource().upsert(accountId = secondAccountId, tagId = tag.id)
                selectedTagIdList(secondAccountId) shouldBe listOf(tag.id)

                selectedTagIdList(guestAccountId).shouldBeEmpty()

                selectedTagIdList(secondAccountId) shouldBe listOf(tag.id)
            }
        }

        test("TC-6a5ce6d3-563e-4a8f-a08a-ae52ebc26078 필터 선택과 해제는 서버 동기화 대상으로 기록되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)

            dataSource().upsert(accountId = accountId, tagId = tag.id)
            dataSource().delete(accountId = accountId, tagId = tag.id)
            dataSource().upsert(accountId = accountId, tagId = tag.id)

            database.accountRoutineSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountTagSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountRoutineTagSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountRoutineRecordSyncDao().findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-1e0ead9c-3a07-4d75-aeea-e9f1e17b33fa 선택 전체 해제는 무시되고 있던 선택까지 지운다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            val tagB = Arb.localTag(isFinished = false, isDeleted = true).next()
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

        test("TC-bf5875b4-c174-403e-82ed-7a1dcdc04dd6 선택 전체 해제는 다른 계정과 다른 화면의 태그 필터 선택을 지우지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)
            insertTag(otherAccountId, tag)
            dataSource().upsert(accountId = accountId, tagId = tag.id)
            selectMemo(accountId = accountId, tagId = tag.id)
            selectCalendar(accountId = accountId, tagId = tag.id)
            selectDDay(accountId = accountId, tagId = tag.id)
            dataSource().upsert(accountId = otherAccountId, tagId = tag.id)

            dataSource().deleteAll(accountId = accountId)

            selectedTagIdList(accountId).shouldBeEmpty()
            memoSelectedTagIdList(accountId) shouldBe listOf(tag.id)
            calendarSelectedTagIdList(accountId) shouldBe listOf(tag.id)
            ddaySelectedTagIdList(accountId) shouldBe listOf(tag.id)
            selectedTagIdList(otherAccountId) shouldBe listOf(tag.id)
        }

        test("TC-517f6bc4-dfa1-4d4b-bb5c-ff875cf0f8bb RoutineHome의 필터 선택은 MemoHome, CalendarHome, DDayHome의 태그 필터 선택과 독립이다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false, title = "Alpha").next()
            val tagB = Arb.localTag(isFinished = false, isDeleted = false, title = "Bravo").next()
            val tagC = Arb.localTag(isFinished = false, isDeleted = false, title = "Charlie").next()
            val tagD = Arb.localTag(isFinished = false, isDeleted = false, title = "Delta").next()
            insertTag(accountId, tagA, tagB, tagC, tagD)
            selectMemo(accountId = accountId, tagId = tagA.id)
            selectCalendar(accountId = accountId, tagId = tagB.id)
            selectDDay(accountId = accountId, tagId = tagC.id)
            selectedTagIdList(accountId).shouldBeEmpty()

            dataSource().upsert(accountId = accountId, tagId = tagD.id)

            selectedTagIdList(accountId) shouldBe listOf(tagD.id)
            memoSelectedTagIdList(accountId) shouldBe listOf(tagA.id)
            calendarSelectedTagIdList(accountId) shouldBe listOf(tagB.id)
            ddaySelectedTagIdList(accountId) shouldBe listOf(tagC.id)
        }

        test("TC-8b42734d-c494-4ae6-8a02-37c771734b62 강제 전체 재동기화로 계정 데이터를 지우면 그 계정의 RoutineHome 필터 선택이 처음 상태로 돌아간다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
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
    })
