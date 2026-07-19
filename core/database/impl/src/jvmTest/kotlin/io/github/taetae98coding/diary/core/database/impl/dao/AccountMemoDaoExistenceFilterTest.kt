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
import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AccountMemoDaoExistenceFilterTest :
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

        suspend fun insertMemo(
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

        suspend fun insertPlace(
            accountId: Uuid,
            vararg placeList: PlaceLocalEntity,
        ) {
            database.withWriteTransaction {
                database.placeDao().upsert(placeList.toList())
                database.accountPlaceDao().upsert(
                    placeList.map { place ->
                        AccountPlaceLocalEntity(
                            accountId = accountId,
                            placeId = place.id,
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

        suspend fun linkPlace(
            accountId: Uuid,
            memoId: Uuid,
            placeId: Uuid,
            isDeleted: Boolean = false,
        ) {
            database.withWriteTransaction {
                database.memoPlaceDao().upsert(
                    MemoPlaceLocalEntity(
                        memoId = memoId,
                        placeId = placeId,
                        isDeleted = isDeleted,
                        updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                        createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                    ),
                )
                database.accountMemoPlaceDao().upsert(
                    AccountMemoPlaceLocalEntity(
                        accountId = accountId,
                        memoId = memoId,
                        placeId = placeId,
                        isDirty = true,
                    ),
                )
            }
        }

        suspend fun setExistence(
            hasDate: Boolean? = null,
            hasTag: Boolean? = null,
            hasPlace: Boolean? = null,
        ) {
            database.memoExistenceFilterDao().upsert(
                entity =
                    MemoExistenceFilterLocalEntity(
                        hasDate = hasDate,
                        hasTag = hasTag,
                        hasPlace = hasPlace,
                    ),
            )
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

        suspend fun pagedIds(accountId: Uuid): List<Uuid> {
            val result =
                database.accountMemoDao().page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue).load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = 100,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, MemoLocalEntity>>().data.map { memo -> memo.id }
        }

        test("TC-MEMO-HOME-FEATURE-033 유무 필터를 한 번도 고르지 않으면 모든 메모가 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val place = place()
            val fullMemo = memo(detail = detail(isAllDay = true))
            val emptyMemo = memo(detail = detail(isAllDay = null))
            insertMemo(accountId, fullMemo, emptyMemo)
            insertTag(accountId, tag)
            insertPlace(accountId, place)
            linkTag(accountId = accountId, memoId = fullMemo.id, tagId = tag.id)
            linkPlace(accountId = accountId, memoId = fullMemo.id, placeId = place.id)

            pagedIds(accountId) shouldContainExactlyInAnyOrder listOf(fullMemo.id, emptyMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-034 날짜 축을 있음으로 두면 기간이 있는 메모만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val dateMemo = memo(detail = detail(isAllDay = true))
            val noDateMemo = memo(detail = detail(isAllDay = null))
            insertMemo(accountId, dateMemo, noDateMemo)

            setExistence(hasDate = true)

            pagedIds(accountId) shouldBe listOf(dateMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-034 태그 축을 있음으로 두면 태그와 연결된 메모만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val tagMemo = memo()
            val noTagMemo = memo()
            insertMemo(accountId, tagMemo, noTagMemo)
            insertTag(accountId, tag)
            linkTag(accountId = accountId, memoId = tagMemo.id, tagId = tag.id)

            setExistence(hasTag = true)

            pagedIds(accountId) shouldBe listOf(tagMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-034 장소 축을 있음으로 두면 장소와 연결된 메모만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val placeMemo = memo()
            val noPlaceMemo = memo()
            insertMemo(accountId, placeMemo, noPlaceMemo)
            insertPlace(accountId, place)
            linkPlace(accountId = accountId, memoId = placeMemo.id, placeId = place.id)

            setExistence(hasPlace = true)

            pagedIds(accountId) shouldBe listOf(placeMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-035 날짜 축을 없음으로 두면 기간이 없는 메모만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val dateMemo = memo(detail = detail(isAllDay = true))
            val noDateMemo = memo(detail = detail(isAllDay = null))
            insertMemo(accountId, dateMemo, noDateMemo)

            setExistence(hasDate = false)

            pagedIds(accountId) shouldBe listOf(noDateMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-035 태그 축을 없음으로 두면 태그와 연결되지 않은 메모만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val tagMemo = memo()
            val noTagMemo = memo()
            insertMemo(accountId, tagMemo, noTagMemo)
            insertTag(accountId, tag)
            linkTag(accountId = accountId, memoId = tagMemo.id, tagId = tag.id)

            setExistence(hasTag = false)

            pagedIds(accountId) shouldBe listOf(noTagMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-035 장소 축을 없음으로 두면 장소와 연결되지 않은 메모만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val placeMemo = memo()
            val noPlaceMemo = memo()
            insertMemo(accountId, placeMemo, noPlaceMemo)
            insertPlace(accountId, place)
            linkPlace(accountId = accountId, memoId = placeMemo.id, placeId = place.id)

            setExistence(hasPlace = false)

            pagedIds(accountId) shouldBe listOf(noPlaceMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-036 축을 전체로 되돌리면 그 축으로 거르지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val dateMemo = memo(detail = detail(isAllDay = true))
            val noDateMemo = memo(detail = detail(isAllDay = null))
            insertMemo(accountId, dateMemo, noDateMemo)
            setExistence(hasDate = true)
            pagedIds(accountId) shouldBe listOf(dateMemo.id)

            setExistence(hasDate = null)

            pagedIds(accountId) shouldContainExactlyInAnyOrder listOf(dateMemo.id, noDateMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-038 서로 다른 축을 함께 고르면 모두 만족하는 메모만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val datePlaceMemo = memo(detail = detail(isAllDay = true))
            val dateOnlyMemo = memo(detail = detail(isAllDay = true))
            val placeOnlyMemo = memo(detail = detail(isAllDay = null))
            val emptyMemo = memo(detail = detail(isAllDay = null))
            insertMemo(accountId, datePlaceMemo, dateOnlyMemo, placeOnlyMemo, emptyMemo)
            insertPlace(accountId, place)
            linkPlace(accountId = accountId, memoId = datePlaceMemo.id, placeId = place.id)
            linkPlace(accountId = accountId, memoId = placeOnlyMemo.id, placeId = place.id)

            setExistence(hasDate = true, hasPlace = true)

            pagedIds(accountId) shouldBe listOf(datePlaceMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-053 태그 축이 없음이면 선택한 태그를 무시하고 태그 없는 메모가 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val tagMemo = memo()
            val noTagMemo = memo()
            insertMemo(accountId, tagMemo, noTagMemo)
            insertTag(accountId, tag)
            linkTag(accountId = accountId, memoId = tagMemo.id, tagId = tag.id)

            setExistence(hasTag = false)
            selectFilterTag(accountId = accountId, tagId = tag.id)

            pagedIds(accountId) shouldBe listOf(noTagMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-054 태그 축을 되돌리면 유지된 태그 선택이 다시 적용된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val tagMemo = memo()
            val noTagMemo = memo()
            insertMemo(accountId, tagMemo, noTagMemo)
            insertTag(accountId, tag)
            linkTag(accountId = accountId, memoId = tagMemo.id, tagId = tag.id)
            selectFilterTag(accountId = accountId, tagId = tag.id)
            setExistence(hasTag = false)
            pagedIds(accountId) shouldBe listOf(noTagMemo.id)

            setExistence(hasTag = null)

            pagedIds(accountId) shouldBe listOf(tagMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-053 태그 축이 있음이면 선택한 태그를 함께 적용한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val selectedTag = tag()
            val otherTag = tag()
            val selectedTagMemo = memo()
            val otherTagMemo = memo()
            val noTagMemo = memo()
            insertMemo(accountId, selectedTagMemo, otherTagMemo, noTagMemo)
            insertTag(accountId, selectedTag, otherTag)
            linkTag(accountId = accountId, memoId = selectedTagMemo.id, tagId = selectedTag.id)
            linkTag(accountId = accountId, memoId = otherTagMemo.id, tagId = otherTag.id)

            setExistence(hasTag = true)
            selectFilterTag(accountId = accountId, tagId = selectedTag.id)

            pagedIds(accountId) shouldBe listOf(selectedTagMemo.id)
        }

        test("TC-MEMO-HOME-FEATURE-040 유무 필터와 태그 필터를 함께 만족하는 메모만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val selectedTag = tag()
            val otherTag = tag()
            val selectedTagDateMemo = memo(detail = detail(isAllDay = true))
            val selectedTagNoDateMemo = memo(detail = detail(isAllDay = null))
            val otherTagDateMemo = memo(detail = detail(isAllDay = true))
            insertMemo(accountId, selectedTagDateMemo, selectedTagNoDateMemo, otherTagDateMemo)
            insertTag(accountId, selectedTag, otherTag)
            linkTag(accountId = accountId, memoId = selectedTagDateMemo.id, tagId = selectedTag.id)
            linkTag(accountId = accountId, memoId = selectedTagNoDateMemo.id, tagId = selectedTag.id)
            linkTag(accountId = accountId, memoId = otherTagDateMemo.id, tagId = otherTag.id)

            selectFilterTag(accountId = accountId, tagId = selectedTag.id)
            setExistence(hasDate = true)

            pagedIds(accountId) shouldBe listOf(selectedTagDateMemo.id)
        }

        test("TC-MEMO-HOME-DOMAIN-011 날짜 축은 종일 여부와 시각을 판정에 쓰지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val allDayMemo =
                memo(
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                        ),
                )
            val dateTimeMemo =
                memo(
                    detail =
                        detail(
                            isAllDay = false,
                            start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 9, minute = 30),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 10, minute = 0),
                        ),
                )
            val pastMemo =
                memo(
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 1999, month = 1, day = 1, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 1999, month = 1, day = 1, hour = 0, minute = 0),
                        ),
                )
            val noDateMemo = memo(detail = detail(isAllDay = null))
            insertMemo(accountId, allDayMemo, dateTimeMemo, pastMemo, noDateMemo)

            setExistence(hasDate = true)

            pagedIds(accountId) shouldContainExactlyInAnyOrder listOf(allDayMemo.id, dateTimeMemo.id, pastMemo.id)
        }

        test("종일 여부, 시작, 종료 중 하나라도 없는 메모는 기간이 없는 것으로 본다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fullMemo = memo(detail = detail(isAllDay = true))
            val noEndMemo = memo(detail = detail(isAllDay = true, endInclusive = null))
            val noStartMemo = memo(detail = detail(isAllDay = true, start = null))
            val noAllDayMemo =
                memo(
                    detail =
                        detail(
                            isAllDay = null,
                            start = defaultDateTime,
                            endInclusive = defaultDateTime,
                        ),
                )
            insertMemo(accountId, fullMemo, noEndMemo, noStartMemo, noAllDayMemo)

            setExistence(hasDate = true)
            pagedIds(accountId) shouldBe listOf(fullMemo.id)

            setExistence(hasDate = false)
            pagedIds(accountId) shouldContainExactlyInAnyOrder listOf(noEndMemo.id, noStartMemo.id, noAllDayMemo.id)
        }

        test("TC-MEMO-HOME-DOMAIN-012 태그 축은 해제된 연결과 삭제된 태그를 세지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val activeTag = tag()
            val finishedTag = tag(isFinished = true)
            val deletedTag = tag(isDeleted = true)
            val otherAccountTag = tag()
            val activeTagMemo = memo()
            val finishedTagMemo = memo()
            val deletedTagMemo = memo()
            val unlinkedMemo = memo()
            val otherAccountTagMemo = memo()
            insertMemo(accountId, activeTagMemo, finishedTagMemo, deletedTagMemo, unlinkedMemo, otherAccountTagMemo)
            insertTag(accountId, activeTag, finishedTag, deletedTag)
            insertTag(otherAccountId, otherAccountTag)
            linkTag(accountId = accountId, memoId = activeTagMemo.id, tagId = activeTag.id)
            linkTag(accountId = accountId, memoId = finishedTagMemo.id, tagId = finishedTag.id)
            linkTag(accountId = accountId, memoId = deletedTagMemo.id, tagId = deletedTag.id)
            linkTag(accountId = accountId, memoId = unlinkedMemo.id, tagId = activeTag.id, isDeleted = true)
            linkTag(accountId = accountId, memoId = otherAccountTagMemo.id, tagId = otherAccountTag.id)

            setExistence(hasTag = true)

            pagedIds(accountId) shouldContainExactlyInAnyOrder listOf(activeTagMemo.id, finishedTagMemo.id)
        }

        test("TC-MEMO-HOME-DOMAIN-013 장소 축은 해제된 연결과 삭제된 장소를 세지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val activePlace = place()
            val deletedPlace = place(isDeleted = true)
            val otherAccountPlace = place()
            val activePlaceMemo = memo()
            val deletedPlaceMemo = memo()
            val unlinkedMemo = memo()
            val otherAccountPlaceMemo = memo()
            insertMemo(accountId, activePlaceMemo, deletedPlaceMemo, unlinkedMemo, otherAccountPlaceMemo)
            insertPlace(accountId, activePlace, deletedPlace)
            insertPlace(otherAccountId, otherAccountPlace)
            linkPlace(accountId = accountId, memoId = activePlaceMemo.id, placeId = activePlace.id)
            linkPlace(accountId = accountId, memoId = deletedPlaceMemo.id, placeId = deletedPlace.id)
            linkPlace(accountId = accountId, memoId = unlinkedMemo.id, placeId = activePlace.id, isDeleted = true)
            linkPlace(accountId = accountId, memoId = otherAccountPlaceMemo.id, placeId = otherAccountPlace.id)

            setExistence(hasPlace = true)

            pagedIds(accountId) shouldBe listOf(activePlaceMemo.id)
        }

        test("TC-MEMO-HOME-DOMAIN-014 유무 필터를 만족해도 완료되거나 삭제된 메모는 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val activeMemo = memo(detail = detail(isAllDay = true))
            val finishedMemo = memo(detail = detail(isAllDay = true), isFinished = true)
            val deletedMemo = memo(detail = detail(isAllDay = true), isDeleted = true)
            insertMemo(accountId, activeMemo, finishedMemo, deletedMemo)

            setExistence(hasDate = true)

            pagedIds(accountId) shouldBe listOf(activeMemo.id)
        }

        test("TC-MEMO-HOME-DOMAIN-015 메모에 기간이 더해지면 날짜 축으로 다시 판정한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo(detail = detail(isAllDay = null))
            insertMemo(accountId, memo)
            setExistence(hasDate = true)
            pagedIds(accountId).shouldBeEmpty()

            database.accountMemoDao().updateDetail(
                accountId = accountId,
                memoId = memo.id,
                title = memo.detail.title,
                description = memo.detail.description,
                color = memo.detail.color,
                isAllDay = true,
                start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            )

            pagedIds(accountId) shouldBe listOf(memo.id)
        }

        test("TC-MEMO-HOME-DOMAIN-015 메모에 태그 연결이 더해지면 태그 축으로 다시 판정한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val memo = memo()
            insertMemo(accountId, memo)
            insertTag(accountId, tag)
            setExistence(hasTag = true)
            pagedIds(accountId).shouldBeEmpty()

            linkTag(accountId = accountId, memoId = memo.id, tagId = tag.id)

            pagedIds(accountId) shouldBe listOf(memo.id)
        }

        test("TC-MEMO-HOME-DOMAIN-015 메모에 장소 연결이 더해지면 장소 축으로 다시 판정한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val memo = memo()
            insertMemo(accountId, memo)
            insertPlace(accountId, place)
            setExistence(hasPlace = true)
            pagedIds(accountId).shouldBeEmpty()

            linkPlace(accountId = accountId, memoId = memo.id, placeId = place.id)

            pagedIds(accountId) shouldBe listOf(memo.id)
        }

        test("TC-MEMO-HOME-DATA-012 유무 필터는 계정과 무관하게 모든 계정의 목록 조회에 적용된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val dateMemo = memo(detail = detail(isAllDay = true))
            val noDateMemo = memo(detail = detail(isAllDay = null))
            val otherAccountDateMemo = memo(detail = detail(isAllDay = true))
            val otherAccountNoDateMemo = memo(detail = detail(isAllDay = null))
            insertMemo(accountId, dateMemo, noDateMemo)
            insertMemo(otherAccountId, otherAccountDateMemo, otherAccountNoDateMemo)

            setExistence(hasDate = true)

            pagedIds(accountId) shouldBe listOf(dateMemo.id)
            pagedIds(otherAccountId) shouldBe listOf(otherAccountDateMemo.id)
        }
    }) {
    public companion object {
        private val defaultDateTime = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0)

        private fun memo(
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
            detail: MemoDetailLocalEntity = detail(isAllDay = null),
        ): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::id, fixtureMonkey.giveMeOne<Uuid>())
                .setExp(MemoLocalEntity::detail, detail)
                .setExp(MemoLocalEntity::isFinished, isFinished)
                .setExp(MemoLocalEntity::isDeleted, isDeleted)
                .setExp(MemoLocalEntity::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(MemoLocalEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun detail(
            isAllDay: Boolean?,
            start: LocalDateTime? = defaultDateTime.takeIf { isAllDay != null },
            endInclusive: LocalDateTime? = defaultDateTime.takeIf { isAllDay != null },
        ): MemoDetailLocalEntity =
            fixtureMonkey
                .giveMeOne<MemoDetailLocalEntity>()
                .copy(
                    isAllDay = isAllDay,
                    start = start,
                    endInclusive = endInclusive,
                )

        private fun tag(
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
        ): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::id, fixtureMonkey.giveMeOne<Uuid>())
                .setExp(TagLocalEntity::detail, fixtureMonkey.giveMeOne<TagDetailLocalEntity>())
                .setExp(TagLocalEntity::isFinished, isFinished)
                .setExp(TagLocalEntity::isDeleted, isDeleted)
                .setExp(TagLocalEntity::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(TagLocalEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun place(isDeleted: Boolean = false): PlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceLocalEntity>()
                .setExp(PlaceLocalEntity::id, fixtureMonkey.giveMeOne<Uuid>())
                .setExp(PlaceLocalEntity::detail, fixtureMonkey.giveMeOne<PlaceDetailLocalEntity>())
                .setExp(PlaceLocalEntity::isDeleted, isDeleted)
                .setExp(PlaceLocalEntity::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(PlaceLocalEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
