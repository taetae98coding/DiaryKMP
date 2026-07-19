package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.CalendarFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import java.io.File
import kotlin.time.Instant
import kotlin.uuid.Uuid

class CalendarFilterTagDaoTest :
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

        suspend fun DiaryDatabase.insertTag(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
            isDirty: Boolean = false,
        ) {
            withWriteTransaction {
                tagDao().upsert(tagList.toList())
                accountTagDao().upsert(
                    tagList.map { tag ->
                        AccountTagLocalEntity(
                            accountId = accountId,
                            tagId = tag.id,
                            isDirty = isDirty,
                        )
                    },
                )
            }
        }

        suspend fun DiaryDatabase.select(
            accountId: Uuid,
            tagId: Uuid,
        ) {
            calendarFilterTagDao().upsert(
                entity =
                    CalendarFilterTagLocalEntity(
                        accountId = accountId,
                        tagId = tagId,
                    ),
            )
        }

        suspend fun DiaryDatabase.selectedTagIdList(accountId: Uuid): List<Uuid> =
            calendarFilterTagDao()
                .getTagList(accountId = accountId)
                .first()
                .map { tag -> tag.id }

        test("TC-CALENDAR-HOME-DATA-028 필터 선택을 현재 사용자 계정에 즉시 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            database.insertTag(accountId, tag)

            database.select(accountId = accountId, tagId = tag.id)

            database.selectedTagIdList(accountId = accountId) shouldBe listOf(tag.id)
        }

        test("TC-CALENDAR-HOME-DATA-029 계정마다 서로 다른 필터 선택을 유지한다") {
            val firstAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val secondAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag()
            val secondTag = tag()
            database.insertTag(firstAccountId, firstTag, secondTag)
            database.insertTag(secondAccountId, firstTag, secondTag)

            database.select(accountId = firstAccountId, tagId = firstTag.id)
            database.select(accountId = secondAccountId, tagId = secondTag.id)

            database.selectedTagIdList(accountId = firstAccountId) shouldBe listOf(firstTag.id)
            database.selectedTagIdList(accountId = secondAccountId) shouldBe listOf(secondTag.id)
        }

        test("TC-CALENDAR-HOME-FEATURE-052 저장된 필터 선택은 새로 조회를 시작해도 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag(title = "Alpha")
            val secondTag = tag(title = "Bravo")
            database.insertTag(accountId, firstTag, secondTag)
            database.select(accountId = accountId, tagId = firstTag.id)
            database.select(accountId = accountId, tagId = secondTag.id)

            database.selectedTagIdList(accountId = accountId) shouldBe listOf(firstTag.id, secondTag.id)
            database.selectedTagIdList(accountId = accountId) shouldBe listOf(firstTag.id, secondTag.id)
        }

        test("TC-CALENDAR-HOME-DATA-030 데이터베이스를 닫고 다시 열어도 필터 선택을 유지한다") {
            val file = File.createTempFile("calendar-filter-tag", ".db")

            try {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val tag = tag()
                val firstDatabase =
                    Room
                        .databaseBuilder<DiaryDatabase>(name = file.absolutePath)
                        .setDriver(BundledSQLiteDriver())
                        .build()
                firstDatabase.insertTag(accountId, tag)
                firstDatabase.select(accountId = accountId, tagId = tag.id)
                firstDatabase.close()

                val secondDatabase =
                    Room
                        .databaseBuilder<DiaryDatabase>(name = file.absolutePath)
                        .setDriver(BundledSQLiteDriver())
                        .build()

                try {
                    secondDatabase.selectedTagIdList(accountId = accountId) shouldBe listOf(tag.id)
                } finally {
                    secondDatabase.close()
                }
            } finally {
                file.delete()
            }
        }

        test("TC-CALENDAR-HOME-DOMAIN-004 선택할 수 없게 된 태그는 선택 목록 조회에서 제외된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val selectableTag = tag()
            val finishedTag = tag(isFinished = true)
            val deletedTag = tag(isDeleted = true)
            val unlinkedTag = tag()
            database.insertTag(accountId, selectableTag, finishedTag, deletedTag)
            database.tagDao().upsert(unlinkedTag)
            listOf(selectableTag, finishedTag, deletedTag, unlinkedTag).forEach { tag ->
                database.select(accountId = accountId, tagId = tag.id)
            }

            database.selectedTagIdList(accountId = accountId) shouldBe listOf(selectableTag.id)
        }

        test("TC-CALENDAR-HOME-DOMAIN-005 태그가 다시 선택할 수 있게 되면 이전 선택이 다시 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            database.insertTag(accountId, tag)
            database.select(accountId = accountId, tagId = tag.id)
            database.accountTagDao().updateFinished(
                accountId = accountId,
                tagId = tag.id,
                isFinished = true,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            )
            database.selectedTagIdList(accountId = accountId).shouldBeEmpty()

            database.accountTagDao().updateFinished(
                accountId = accountId,
                tagId = tag.id,
                isFinished = false,
                updatedAt = Instant.fromEpochMilliseconds(3_000),
            )

            database.selectedTagIdList(accountId = accountId) shouldBe listOf(tag.id)
        }

        test("TC-CALENDAR-HOME-DOMAIN-006 캘린더 필터 선택과 메모 목록 필터 선택은 서로 영향을 주지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val calendarTag = tag(title = "Calendar")
            val memoTag = tag(title = "Memo")
            database.insertTag(accountId, calendarTag, memoTag)

            database.select(accountId = accountId, tagId = calendarTag.id)
            database.memoFilterTagDao().upsert(
                entity =
                    MemoFilterTagLocalEntity(
                        accountId = accountId,
                        tagId = memoTag.id,
                    ),
            )

            database.selectedTagIdList(accountId = accountId) shouldBe listOf(calendarTag.id)
            database
                .memoFilterTagDao()
                .getTagList(accountId = accountId)
                .first()
                .map { tag -> tag.id } shouldBe listOf(memoTag.id)
        }

        test("TC-CALENDAR-HOME-DATA-031 필터 선택과 해제는 동기화 업로드 대기 목록에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            database.insertTag(accountId, tag, isDirty = false)

            database.select(accountId = accountId, tagId = tag.id)
            database.calendarFilterTagDao().delete(accountId = accountId, tagId = tag.id)
            database.select(accountId = accountId, tagId = tag.id)

            database.accountMemoSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountTagSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountMemoTagSyncDao().findPending(accountId = accountId).shouldBeEmpty()
        }

        test("선택 해제는 해당 계정의 해당 태그 선택만 제거한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag(title = "Alpha")
            val secondTag = tag(title = "Bravo")
            database.insertTag(accountId, firstTag, secondTag)
            database.insertTag(otherAccountId, firstTag)
            database.select(accountId = accountId, tagId = firstTag.id)
            database.select(accountId = accountId, tagId = secondTag.id)
            database.select(accountId = otherAccountId, tagId = firstTag.id)

            database.calendarFilterTagDao().delete(accountId = accountId, tagId = firstTag.id)

            database.selectedTagIdList(accountId = accountId) shouldBe listOf(secondTag.id)
            database.selectedTagIdList(accountId = otherAccountId) shouldBe listOf(firstTag.id)
        }

        test("TC-CALENDAR-HOME-DATA-035 선택 전체 해제는 무시되고 있던 선택까지 지운다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val selectableTag = tag()
            val finishedTag = tag(isFinished = true)
            val deletedTag = tag(isDeleted = true)
            database.insertTag(accountId, selectableTag, finishedTag, deletedTag)
            listOf(selectableTag, finishedTag, deletedTag).forEach { tag ->
                database.select(accountId = accountId, tagId = tag.id)
            }

            database.calendarFilterTagDao().deleteAll(accountId = accountId)

            database.selectedTagIdList(accountId = accountId).shouldBeEmpty()

            database.accountTagDao().updateFinished(
                accountId = accountId,
                tagId = finishedTag.id,
                isFinished = false,
                updatedAt = Instant.fromEpochMilliseconds(3_000),
            )

            database.selectedTagIdList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-CALENDAR-HOME-DATA-036 선택 전체 해제는 다른 계정의 선택을 지우지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            database.insertTag(accountId, tag)
            database.insertTag(otherAccountId, tag)
            database.select(accountId = accountId, tagId = tag.id)
            database.select(accountId = otherAccountId, tagId = tag.id)

            database.calendarFilterTagDao().deleteAll(accountId = accountId)

            database.selectedTagIdList(accountId = accountId).shouldBeEmpty()
            database.selectedTagIdList(accountId = otherAccountId) shouldBe listOf(tag.id)
        }

        test("선택한 태그가 없어도 선택 전체 해제는 실패하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            database.insertTag(accountId, tag)

            database.calendarFilterTagDao().deleteAll(accountId = accountId)

            database.selectedTagIdList(accountId = accountId).shouldBeEmpty()
        }

        test("같은 태그를 다시 선택해도 선택은 한 번만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            database.insertTag(accountId, tag)

            database.select(accountId = accountId, tagId = tag.id)
            database.select(accountId = accountId, tagId = tag.id)

            database.selectedTagIdList(accountId = accountId) shouldBe listOf(tag.id)
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(
            title: String = fixtureMonkey.giveMeOne<String>(),
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
        ): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::id, fixtureMonkey.giveMeOne<Uuid>())
                .setExp(TagLocalEntity::detail, fixtureMonkey.giveMeOne<TagDetailLocalEntity>().copy(title = title))
                .setExp(TagLocalEntity::isFinished, isFinished)
                .setExp(TagLocalEntity::isDeleted, isDeleted)
                .setExp(TagLocalEntity::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(TagLocalEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
