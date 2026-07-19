package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AccountMemoDaoTest :
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

        suspend fun insert(
            accountId: Uuid,
            vararg memoList: MemoLocalEntity,
        ) {
            database.withWriteTransaction {
                database.memoDao().upsert(memoList.toList())
                database.accountMemoDao().upsert(
                    memoList.map { memo ->
                        AccountMemoLocalEntity(
                            accountId = accountId,
                            memoId = memo.id,
                            isDirty = true,
                        )
                    },
                )
            }
        }

        suspend fun PagingSource<Int, MemoLocalEntity>.pagedIds(): List<Uuid> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = 100,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, MemoLocalEntity>>().data.map { memo -> memo.id }
        }

        suspend fun pagedIds(accountId: Uuid): List<Uuid> = database.accountMemoDao().page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue).pagedIds()

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
                            isDirty = true,
                        )
                    },
                )
            }
        }

        suspend fun linkTag(
            accountId: Uuid,
            memoId: Uuid,
            tagId: Uuid,
            isDeleted: Boolean = false,
        ) {
            database.withWriteTransaction {
                database.memoTagDao().upsert(
                    MemoTagLocalEntity(
                        memoId = memoId,
                        tagId = tagId,
                        isDeleted = isDeleted,
                        updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                        createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                    ),
                )
                database.accountMemoTagDao().upsert(
                    AccountMemoTagLocalEntity(
                        accountId = accountId,
                        memoId = memoId,
                        tagId = tagId,
                        isDirty = true,
                    ),
                )
            }
        }

        suspend fun selectFilterTag(
            accountId: Uuid,
            tagId: Uuid,
        ) {
            database.memoFilterTagDao().upsert(
                entity =
                    MemoFilterTagLocalEntity(
                        accountId = accountId,
                        tagId = tagId,
                    ),
            )
        }

        test("TC-MEMO-HOME-DATA-001 완료되었거나 삭제된 메모는 목록 조회에서 제외된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val activeMemo = memo(isFinished = false, isDeleted = false)
            val finishedMemo = memo(isFinished = true, isDeleted = false)
            val deletedMemo = memo(isFinished = false, isDeleted = true)
            insert(accountId, activeMemo, finishedMemo, deletedMemo)

            pagedIds(accountId) shouldBe listOf(activeMemo.id)
        }

        test("TC-MEMO-HOME-DATA-004 목록은 기간, 시작 시점, 종료 시점, 제목 순으로 정렬해 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val noDateTimeBravoMemo =
                memo(
                    detail =
                        detail(
                            title = "Bravo",
                            isAllDay = null,
                            start = null,
                            endInclusive = null,
                        ),
                )
            val noDateTimeAlphaMemo =
                memo(
                    detail =
                        detail(
                            title = "Alpha",
                            isAllDay = null,
                            start = null,
                            endInclusive = null,
                        ),
                )
            val allDayMemo =
                memo(
                    detail =
                        detail(
                            title = "Charlie",
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                        ),
                )
            val sameDayEarlyEndBravoMemo =
                memo(
                    detail =
                        detail(
                            title = "Bravo",
                            isAllDay = false,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 9, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 10, minute = 0),
                        ),
                )
            val sameDayEarlyEndAlphaMemo =
                memo(
                    detail =
                        detail(
                            title = "Alpha",
                            isAllDay = false,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 9, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 10, minute = 0),
                        ),
                )
            val sameDayLateEndMemo =
                memo(
                    detail =
                        detail(
                            title = "Delta",
                            isAllDay = false,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 9, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 10, minute = 0),
                        ),
                )
            val nextDayMemo =
                memo(
                    detail =
                        detail(
                            title = "Echo",
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                        ),
                )
            insert(
                accountId,
                nextDayMemo,
                sameDayLateEndMemo,
                sameDayEarlyEndBravoMemo,
                sameDayEarlyEndAlphaMemo,
                allDayMemo,
                noDateTimeBravoMemo,
                noDateTimeAlphaMemo,
            )

            pagedIds(accountId) shouldBe
                listOf(
                    noDateTimeAlphaMemo.id,
                    noDateTimeBravoMemo.id,
                    allDayMemo.id,
                    sameDayEarlyEndAlphaMemo.id,
                    sameDayEarlyEndBravoMemo.id,
                    sameDayLateEndMemo.id,
                    nextDayMemo.id,
                )
        }

        test("TC-MEMO-HOME-DATA-005 수정 시각은 목록 순서에 영향을 주지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val earlyStartMemo =
                memo(
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                        ),
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            val lateStartMemo =
                memo(
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                        ),
                    updatedAt = Instant.fromEpochMilliseconds(2_000),
                )
            insert(accountId, earlyStartMemo, lateStartMemo)

            pagedIds(accountId) shouldBe listOf(earlyStartMemo.id, lateStartMemo.id)
        }

        test("TC-MEMO-HOME-DATA-003 현재 계정과 연결된 메모만 목록에서 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val accountMemo = memo()
            val otherAccountMemo = memo()
            insert(accountId, accountMemo)
            insert(otherAccountId, otherAccountMemo)

            pagedIds(accountId) shouldBe listOf(accountMemo.id)
        }

        test("단일 메모 조회는 해당 계정과 연결된 메모만 반환한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insert(accountId, memo)

            database.accountMemoDao().find(accountId = accountId, memoId = memo.id).first() shouldBe memo
            database
                .accountMemoDao()
                .find(accountId = otherAccountId, memoId = memo.id)
                .first()
                .shouldBeNull()
            database
                .accountMemoDao()
                .find(accountId = accountId, memoId = fixtureMonkey.giveMeOne<Uuid>())
                .first()
                .shouldBeNull()
        }

        test("완료 갱신은 해당 계정과 연결된 메모에만 적용된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo(isFinished = false)
            insert(accountId, memo)

            database.accountMemoDao().updateFinished(
                accountId = otherAccountId,
                memoId = memo.id,
                isFinished = true,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 0

            pagedIds(accountId) shouldBe listOf(memo.id)

            database.accountMemoDao().updateFinished(
                accountId = accountId,
                memoId = memo.id,
                isFinished = true,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 1

            pagedIds(accountId).shouldBeEmpty()
        }

        test("삭제 갱신은 해당 계정과 연결된 메모에만 적용된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo(isDeleted = false)
            insert(accountId, memo)

            database.accountMemoDao().updateDeleted(
                accountId = otherAccountId,
                memoId = memo.id,
                isDeleted = true,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 0

            pagedIds(accountId) shouldBe listOf(memo.id)

            database.accountMemoDao().updateDeleted(
                accountId = accountId,
                memoId = memo.id,
                isDeleted = true,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 1

            pagedIds(accountId).shouldBeEmpty()
        }

        test("상세 갱신은 해당 계정의 메모 제목, 설명, 컬러, 기간, 수정 시각만 바꾸고 나머지 속성은 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo(updatedAt = Instant.fromEpochMilliseconds(1_000))
            insert(accountId, memo)
            val newDetail = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>()

            database.accountMemoDao().updateDetail(
                accountId = otherAccountId,
                memoId = memo.id,
                title = newDetail.title,
                description = newDetail.description,
                color = newDetail.color,
                isAllDay = newDetail.isAllDay,
                start = newDetail.start,
                endInclusive = newDetail.endInclusive,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 0

            database.accountMemoDao().find(accountId = accountId, memoId = memo.id).first() shouldBe memo

            database.accountMemoDao().updateDetail(
                accountId = accountId,
                memoId = memo.id,
                title = newDetail.title,
                description = newDetail.description,
                color = newDetail.color,
                isAllDay = newDetail.isAllDay,
                start = newDetail.start,
                endInclusive = newDetail.endInclusive,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 1

            val updated =
                database
                    .accountMemoDao()
                    .find(accountId = accountId, memoId = memo.id)
                    .first()
                    .shouldNotBeNull()
            updated.detail shouldBe newDetail
            updated.updatedAt shouldBe Instant.fromEpochMilliseconds(2_000)
            updated.id shouldBe memo.id
            updated.isFinished shouldBe memo.isFinished
            updated.isDeleted shouldBe memo.isDeleted
            updated.createdAt shouldBe memo.createdAt
        }

        test("여러 계정과 연결된 메모는 같은 내용을 공유한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo(updatedAt = Instant.fromEpochMilliseconds(1_000))
            insert(accountId, memo)
            insert(otherAccountId, memo)
            val newDetail = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>()
            val updatedAt = Instant.fromEpochMilliseconds(2_000)

            database.accountMemoDao().updateDetail(
                accountId = accountId,
                memoId = memo.id,
                title = newDetail.title,
                description = newDetail.description,
                color = newDetail.color,
                isAllDay = newDetail.isAllDay,
                start = newDetail.start,
                endInclusive = newDetail.endInclusive,
                updatedAt = updatedAt,
            ) shouldBe 1

            val expected = memo.copy(detail = newDetail, updatedAt = updatedAt)
            database.accountMemoDao().find(accountId = accountId, memoId = memo.id).first() shouldBe expected
            database.accountMemoDao().find(accountId = otherAccountId, memoId = memo.id).first() shouldBe expected
        }

        test("완료를 되돌린 메모는 자신의 시작 시점에 해당하는 자리에 다시 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstMemo =
                memo(
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                        ),
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            val secondMemo =
                memo(
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                        ),
                    isFinished = true,
                    updatedAt = Instant.fromEpochMilliseconds(2_000),
                )
            insert(accountId, firstMemo, secondMemo)

            database.accountMemoDao().updateFinished(
                accountId = accountId,
                memoId = secondMemo.id,
                isFinished = false,
                updatedAt = Instant.fromEpochMilliseconds(3_000),
            )

            pagedIds(accountId) shouldBe listOf(firstMemo.id, secondMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-021 태그를 필터에 선택하면 그 태그와 연결된 메모만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag()
            val secondTag = tag()
            val firstTagMemo = memo()
            val secondTagMemo = memo()
            insert(accountId, firstTagMemo, secondTagMemo)
            insertTag(accountId, firstTag, secondTag)
            linkTag(accountId = accountId, memoId = firstTagMemo.id, tagId = firstTag.id)
            linkTag(accountId = accountId, memoId = secondTagMemo.id, tagId = secondTag.id)

            selectFilterTag(accountId = accountId, tagId = firstTag.id)

            pagedIds(accountId) shouldBe listOf(firstTagMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-022 여러 태그를 선택하면 하나 이상과 연결된 메모만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag()
            val secondTag = tag()
            val firstTagMemo = memo()
            val secondTagMemo = memo()
            val noTagMemo = memo()
            insert(accountId, firstTagMemo, secondTagMemo, noTagMemo)
            insertTag(accountId, firstTag, secondTag)
            linkTag(accountId = accountId, memoId = firstTagMemo.id, tagId = firstTag.id)
            linkTag(accountId = accountId, memoId = secondTagMemo.id, tagId = secondTag.id)

            selectFilterTag(accountId = accountId, tagId = firstTag.id)
            selectFilterTag(accountId = accountId, tagId = secondTag.id)

            pagedIds(accountId) shouldContainExactlyInAnyOrder listOf(firstTagMemo.id, secondTagMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-023 마지막 태그의 선택을 해제하면 모든 메모가 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val tagMemo = memo()
            val noTagMemo = memo()
            insert(accountId, tagMemo, noTagMemo)
            insertTag(accountId, tag)
            linkTag(accountId = accountId, memoId = tagMemo.id, tagId = tag.id)
            selectFilterTag(accountId = accountId, tagId = tag.id)
            pagedIds(accountId) shouldBe listOf(tagMemo.id)

            database.memoFilterTagDao().delete(accountId = accountId, tagId = tag.id)

            pagedIds(accountId) shouldContainExactlyInAnyOrder listOf(tagMemo.id, noTagMemo.id)
        }

        test("TC-MEMO-HOME-DOMAIN-006 필터와 일치해도 완료되거나 삭제된 메모는 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val activeMemo = memo(isFinished = false, isDeleted = false)
            val finishedMemo = memo(isFinished = true, isDeleted = false)
            val deletedMemo = memo(isFinished = false, isDeleted = true)
            insert(accountId, activeMemo, finishedMemo, deletedMemo)
            insertTag(accountId, tag)
            listOf(activeMemo, finishedMemo, deletedMemo).forEach { memo ->
                linkTag(accountId = accountId, memoId = memo.id, tagId = tag.id)
            }

            selectFilterTag(accountId = accountId, tagId = tag.id)

            pagedIds(accountId) shouldBe listOf(activeMemo.id)
        }

        test("TC-MEMO-HOME-DOMAIN-009 선택된 유일한 태그가 선택할 수 없게 되면 모든 메모가 조회된다") {
            suspend fun unselectableCase(makeUnselectable: suspend (accountId: Uuid, tagId: Uuid) -> Unit) {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val tag = tag()
                val tagMemo = memo()
                val noTagMemo = memo()
                insert(accountId, tagMemo, noTagMemo)
                insertTag(accountId, tag)
                linkTag(accountId = accountId, memoId = tagMemo.id, tagId = tag.id)
                selectFilterTag(accountId = accountId, tagId = tag.id)
                pagedIds(accountId) shouldBe listOf(tagMemo.id)

                makeUnselectable(accountId, tag.id)

                pagedIds(accountId) shouldContainExactlyInAnyOrder listOf(tagMemo.id, noTagMemo.id)
            }

            unselectableCase { accountId, tagId ->
                database.accountTagDao().updateFinished(
                    accountId = accountId,
                    tagId = tagId,
                    isFinished = true,
                    updatedAt = Instant.fromEpochMilliseconds(2_000),
                )
            }
            unselectableCase { accountId, tagId ->
                database.accountTagDao().updateDeleted(
                    accountId = accountId,
                    tagId = tagId,
                    isDeleted = true,
                    updatedAt = Instant.fromEpochMilliseconds(2_000),
                )
            }
        }

        test("TC-MEMO-HOME-DOMAIN-009 선택된 유일한 태그가 현재 계정과 연결되어 있지 않으면 모든 메모가 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val tagMemo = memo()
            val noTagMemo = memo()
            insert(accountId, tagMemo, noTagMemo)
            insertTag(otherAccountId, tag)
            linkTag(accountId = accountId, memoId = tagMemo.id, tagId = tag.id)

            selectFilterTag(accountId = accountId, tagId = tag.id)

            pagedIds(accountId) shouldContainExactlyInAnyOrder listOf(tagMemo.id, noTagMemo.id)
        }

        test("TC-MEMO-HOME-DOMAIN-010 태그가 다시 선택할 수 있게 되면 목록이 이전 선택으로 다시 필터링된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val tagMemo = memo()
            val noTagMemo = memo()
            insert(accountId, tagMemo, noTagMemo)
            insertTag(accountId, tag)
            linkTag(accountId = accountId, memoId = tagMemo.id, tagId = tag.id)
            selectFilterTag(accountId = accountId, tagId = tag.id)
            database.accountTagDao().updateFinished(
                accountId = accountId,
                tagId = tag.id,
                isFinished = true,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            )
            pagedIds(accountId) shouldContainExactlyInAnyOrder listOf(tagMemo.id, noTagMemo.id)

            database.accountTagDao().updateFinished(
                accountId = accountId,
                tagId = tag.id,
                isFinished = false,
                updatedAt = Instant.fromEpochMilliseconds(3_000),
            )

            pagedIds(accountId) shouldBe listOf(tagMemo.id)
        }

        test("해제된 메모 태그 연결은 필터 일치로 보지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val linkedMemo = memo()
            val unlinkedMemo = memo()
            insert(accountId, linkedMemo, unlinkedMemo)
            insertTag(accountId, tag)
            linkTag(accountId = accountId, memoId = linkedMemo.id, tagId = tag.id)
            linkTag(accountId = accountId, memoId = unlinkedMemo.id, tagId = tag.id, isDeleted = true)

            selectFilterTag(accountId = accountId, tagId = tag.id)

            pagedIds(accountId) shouldBe listOf(linkedMemo.id)
        }

        test("다른 계정의 필터 선택은 현재 계정의 목록 조회에 영향을 주지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val tagMemo = memo()
            val noTagMemo = memo()
            insert(accountId, tagMemo, noTagMemo)
            insertTag(accountId, tag)
            insertTag(otherAccountId, tag)
            linkTag(accountId = accountId, memoId = tagMemo.id, tagId = tag.id)

            selectFilterTag(accountId = otherAccountId, tagId = tag.id)

            pagedIds(accountId) shouldContainExactlyInAnyOrder listOf(tagMemo.id, noTagMemo.id)
        }

        test("TC-MEMO-HOME-DATA-013 제목순은 제목 오름차순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTitleMemo = sortMemo(title = "Alpha", updatedAt = 1_000, startDay = 21)
            val lastTitleMemo = sortMemo(title = "Bravo", updatedAt = 3_000, startDay = 19)
            insert(accountId, lastTitleMemo, firstTitleMemo)

            database
                .accountMemoDao()
                .page(accountId = accountId, sort = ListSortLocalEntity.TITLE.queryValue)
                .pagedIds() shouldBe listOf(firstTitleMemo.id, lastTitleMemo.id)
        }

        test("TC-MEMO-HOME-DATA-013 최근 수정순은 수정 시각 내림차순으로 조회하고 같으면 제목 오름차순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val latestMemo = sortMemo(title = "Zebra", updatedAt = 3_000, startDay = 19)
            val sameUpdatedFirstMemo = sortMemo(title = "Alpha", updatedAt = 1_000, startDay = 21)
            val sameUpdatedLastMemo = sortMemo(title = "Bravo", updatedAt = 1_000, startDay = 20)
            insert(accountId, sameUpdatedLastMemo, latestMemo, sameUpdatedFirstMemo)

            database
                .accountMemoDao()
                .page(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED.queryValue)
                .pagedIds() shouldBe listOf(latestMemo.id, sameUpdatedFirstMemo.id, sameUpdatedLastMemo.id)
        }

        test("TC-MEMO-HOME-DATA-004 기본순은 기간을 기준으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val noPeriodMemo = sortMemo(title = "Zebra", updatedAt = 1_000, startDay = null)
            val earlyMemo = sortMemo(title = "Bravo", updatedAt = 2_000, startDay = 19)
            val lateMemo = sortMemo(title = "Alpha", updatedAt = 3_000, startDay = 21)
            insert(accountId, lateMemo, earlyMemo, noPeriodMemo)

            pagedIds(accountId) shouldBe listOf(noPeriodMemo.id, earlyMemo.id, lateMemo.id)
        }

        test("TC-MEMO-HOME-DATA-014 정렬을 바꿔도 노출하는 메모는 달라지지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = sortMemo(title = "Alpha", updatedAt = 1_000, startDay = 19)
            val finishedMemo = memo(isFinished = true)
            val deletedMemo = memo(isDeleted = true)
            insert(accountId, memo, finishedMemo, deletedMemo)

            listOf(ListSortLocalEntity.DEFAULT, ListSortLocalEntity.TITLE, ListSortLocalEntity.RECENTLY_UPDATED).forEach { sort ->
                database
                    .accountMemoDao()
                    .page(accountId = accountId, sort = sort.queryValue)
                    .pagedIds() shouldBe listOf(memo.id)
            }
        }
    }) {
    public companion object {
        private fun memo(
            id: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
            updatedAt: Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            detail: MemoDetailLocalEntity = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>(),
        ): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::id, id)
                .setExp(MemoLocalEntity::detail, detail)
                .setExp(MemoLocalEntity::isFinished, isFinished)
                .setExp(MemoLocalEntity::isDeleted, isDeleted)
                .setExp(MemoLocalEntity::updatedAt, updatedAt)
                .setExp(MemoLocalEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun sortMemo(
            title: String,
            updatedAt: Long,
            startDay: Int?,
        ): MemoLocalEntity =
            memo(
                updatedAt = Instant.fromEpochMilliseconds(updatedAt),
                detail =
                    detail(
                        title = title,
                        isAllDay = startDay?.let { true },
                        start = startDay?.let { day -> LocalDateTime(year = 2026, month = 7, day = day, hour = 0, minute = 0) },
                        endInclusive = startDay?.let { day -> LocalDateTime(year = 2026, month = 7, day = day, hour = 23, minute = 59) },
                    ),
            )

        private fun tag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::id, fixtureMonkey.giveMeOne<Uuid>())
                .setExp(TagLocalEntity::detail, fixtureMonkey.giveMeOne<TagDetailLocalEntity>())
                .setExp(TagLocalEntity::isFinished, false)
                .setExp(TagLocalEntity::isDeleted, false)
                .setExp(TagLocalEntity::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(TagLocalEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun detail(
            title: String = fixtureMonkey.giveMeOne<String>(),
            isAllDay: Boolean?,
            start: LocalDateTime?,
            endInclusive: LocalDateTime?,
        ): MemoDetailLocalEntity =
            fixtureMonkey
                .giveMeOne<MemoDetailLocalEntity>()
                .copy(
                    title = title,
                    isAllDay = isAllDay,
                    start = start,
                    endInclusive = endInclusive,
                )
    }
}
