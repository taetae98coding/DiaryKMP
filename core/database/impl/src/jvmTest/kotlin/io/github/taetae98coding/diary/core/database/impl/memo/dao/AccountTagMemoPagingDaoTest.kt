package io.github.taetae98coding.diary.core.database.impl.memo.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.memoDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.memoLocalEntity
import io.github.taetae98coding.diary.core.testing.memotag.memoTagLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.Uuid

class AccountTagMemoPagingDaoTest :
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
            tag: TagLocalEntity,
        ) {
            database.withWriteTransaction {
                database.tagDao().upsert(listOf(tag))
                database.accountTagDao().upsert(
                    AccountTagLocalEntity(
                        accountId = accountId,
                        tagId = tag.id,
                        isDirty = true,
                    ),
                )
            }
        }

        suspend fun insertMemoTag(
            accountId: Uuid,
            memoTag: MemoTagLocalEntity,
        ) {
            database.withWriteTransaction {
                database.memoTagDao().upsert(listOf(memoTag))
                database.accountMemoTagDao().upsert(
                    AccountMemoTagLocalEntity(
                        accountId = accountId,
                        memoId = memoTag.memoId,
                        tagId = memoTag.tagId,
                        isDirty = true,
                    ),
                )
            }
        }

        suspend fun insertTagMemo(
            accountId: Uuid,
            tag: TagLocalEntity,
            memo: MemoLocalEntity,
            isConnectionDeleted: Boolean = false,
        ) {
            insertTag(accountId = accountId, tag = tag)
            insertMemo(accountId, memo)
            insertMemoTag(
                accountId = accountId,
                memoTag =
                    memoTag(
                        memoId = memo.id,
                        tagId = tag.id,
                        isDeleted = isConnectionDeleted,
                    ),
            )
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

        suspend fun tagPagedIds(
            accountId: Uuid,
            tagId: Uuid,
            scope: TagScopeLocalEntity = TagScopeLocalEntity.SELF,
        ): List<Uuid> =
            database
                .accountTagMemoDao()
                .page(
                    accountId = accountId,
                    tagId = tagId,
                    scope = scope.queryValue,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                ).pagedIds()

        suspend fun assertTagPageInvalidated(
            pagingSource: PagingSource<Int, MemoLocalEntity>,
            accountId: Uuid,
            tagId: Uuid,
            expectedIds: List<Uuid>,
            change: suspend () -> Unit,
        ) {
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(30_000) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
            tagPagedIds(accountId = accountId, tagId = tagId) shouldBe expectedIds
        }

        test("TC-bd3e718f-ecd8-4215-87ce-41cc846f6eec 현재 계정의 대상 태그와 활성 연결을 가진 미완료·미삭제 메모만 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val otherTag = tag()
            val activeMemo = memo(isFinished = false, isDeleted = false)
            val finishedMemo = memo(isFinished = true, isDeleted = false)
            val deletedMemo = memo(isFinished = false, isDeleted = true)
            val disconnectedMemo = memo(isFinished = false, isDeleted = false)
            val otherTagMemo = memo(isFinished = false, isDeleted = false)
            val otherAccountMemo = memo(isFinished = false, isDeleted = false)
            insertTagMemo(accountId, targetTag, activeMemo)
            insertTagMemo(accountId, targetTag, finishedMemo)
            insertTagMemo(accountId, targetTag, deletedMemo)
            insertTagMemo(accountId, targetTag, disconnectedMemo, isConnectionDeleted = true)
            insertTagMemo(accountId, otherTag, otherTagMemo)
            insertTagMemo(otherAccountId, targetTag, otherAccountMemo)

            tagPagedIds(accountId = accountId, tagId = targetTag.id) shouldBe listOf(activeMemo.id)
        }

        test("태그별 메모 조회는 현재 계정의 태그·메모·관계 연결이 모두 존재할 때만 메모를 반환한다") {
            val accountId = Arb.kotlinUuid().next()

            val tagWithoutAccount = tag()
            val memoOfTagWithoutAccount = memo()
            database.tagDao().upsert(listOf(tagWithoutAccount))
            insertMemo(accountId, memoOfTagWithoutAccount)
            insertMemoTag(
                accountId = accountId,
                memoTag = memoTag(memoId = memoOfTagWithoutAccount.id, tagId = tagWithoutAccount.id),
            )

            val tagOfMemoWithoutAccount = tag()
            val memoWithoutAccount = memo()
            insertTag(accountId = accountId, tag = tagOfMemoWithoutAccount)
            database.memoDao().upsert(listOf(memoWithoutAccount))
            insertMemoTag(
                accountId = accountId,
                memoTag = memoTag(memoId = memoWithoutAccount.id, tagId = tagOfMemoWithoutAccount.id),
            )

            val tagWithoutAccountMemoTag = tag()
            val memoWithoutAccountMemoTag = memo()
            insertTag(accountId = accountId, tag = tagWithoutAccountMemoTag)
            insertMemo(accountId, memoWithoutAccountMemoTag)
            database.memoTagDao().upsert(
                listOf(
                    memoTag(
                        memoId = memoWithoutAccountMemoTag.id,
                        tagId = tagWithoutAccountMemoTag.id,
                    ),
                ),
            )

            val tagWithoutMemoTag = tag()
            val memoWithoutMemoTag = memo()
            insertTag(accountId = accountId, tag = tagWithoutMemoTag)
            insertMemo(accountId, memoWithoutMemoTag)
            database.accountMemoTagDao().upsert(
                AccountMemoTagLocalEntity(
                    accountId = accountId,
                    memoId = memoWithoutMemoTag.id,
                    tagId = tagWithoutMemoTag.id,
                    isDirty = true,
                ),
            )

            listOf(
                tagWithoutAccount,
                tagOfMemoWithoutAccount,
                tagWithoutAccountMemoTag,
                tagWithoutMemoTag,
            ).forEach { targetTag ->
                tagPagedIds(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()
            }
        }

        test("TC-00f996ed-35b0-4273-9f53-c50069a76bbf 가리키는 메모나 태그가 기기에 없는 연결은 태그별 메모 조회에 나타나지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val existingTag = tag()
            val missingMemoId = Arb.kotlinUuid().next()
            val missingMemoRelation = memoTag(memoId = missingMemoId, tagId = existingTag.id)
            insertTag(accountId = accountId, tag = existingTag)
            insertMemoTag(accountId = accountId, memoTag = missingMemoRelation)

            val existingMemo = memo()
            val missingTagId = Arb.kotlinUuid().next()
            val missingTagRelation = memoTag(memoId = existingMemo.id, tagId = missingTagId)
            insertMemo(accountId, existingMemo)
            insertMemoTag(accountId = accountId, memoTag = missingTagRelation)

            database.memoTagDao().findByMemoIdList(listOf(missingMemoId)) shouldBe listOf(missingMemoRelation)
            database.memoTagDao().findByMemoIdList(listOf(existingMemo.id)) shouldBe listOf(missingTagRelation)
            tagPagedIds(accountId = accountId, tagId = existingTag.id).shouldBeEmpty()
            tagPagedIds(accountId = accountId, tagId = missingTagId).shouldBeEmpty()
        }

        test("TC-409bc9b2-72cc-4cad-944d-c56055d103f1 태그가 완료되거나 삭제되어도 연결된 메모는 계속 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val targetList =
                listOf(
                    tag(isFinished = true, isDeleted = false) to memo(),
                    tag(isFinished = false, isDeleted = true) to memo(),
                )
            targetList.forEach { (targetTag, targetMemo) ->
                insertTagMemo(accountId, targetTag, targetMemo)
            }

            targetList.forEach { (targetTag, targetMemo) ->
                tagPagedIds(accountId = accountId, tagId = targetTag.id) shouldBe listOf(targetMemo.id)
            }
        }

        test("TC-b836c29e-fd66-4c7f-9c50-2917c3cb292a 태그별 메모는 기간 없음, 시작 날짜, 종일 여부, 시작 시각, 종료 시점, 제목 순으로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
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
            val multiDayAllDayMemo =
                memo(
                    detail =
                        detail(
                            title = "Alpha",
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 21, hour = 0, minute = 0),
                        ),
                )
            val midnightMemo =
                memo(
                    detail =
                        detail(
                            title = "Alpha",
                            isAllDay = false,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 1, minute = 0),
                        ),
                )
            listOf(
                nextDayMemo,
                midnightMemo,
                multiDayAllDayMemo,
                sameDayLateEndMemo,
                sameDayEarlyEndBravoMemo,
                sameDayEarlyEndAlphaMemo,
                allDayMemo,
                noDateTimeBravoMemo,
                noDateTimeAlphaMemo,
            ).forEach { targetMemo ->
                insertTagMemo(accountId, targetTag, targetMemo)
            }

            tagPagedIds(accountId = accountId, tagId = targetTag.id) shouldBe
                listOf(
                    noDateTimeAlphaMemo.id,
                    noDateTimeBravoMemo.id,
                    allDayMemo.id,
                    multiDayAllDayMemo.id,
                    midnightMemo.id,
                    sameDayEarlyEndAlphaMemo.id,
                    sameDayEarlyEndBravoMemo.id,
                    sameDayLateEndMemo.id,
                    nextDayMemo.id,
                )
        }

        test("TC-7972e5be-3e38-494a-a4ad-3ef585ae9b6e 연결 해제는 페이지를 무효화하고 메모를 목록에서 제외한다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val targetMemo = memo()
            val relation = memoTag(memoId = targetMemo.id, tagId = targetTag.id)
            insertTagMemo(accountId, targetTag, targetMemo)
            val pagingSource =
                database.accountTagMemoDao().page(
                    accountId = accountId,
                    tagId = targetTag.id,
                    scope = TagScopeLocalEntity.SELF.queryValue,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                )
            pagingSource.pagedIds() shouldBe listOf(targetMemo.id)

            assertTagPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                tagId = targetTag.id,
                expectedIds = emptyList(),
            ) {
                database.memoTagDao().upsert(
                    listOf(
                        relation.copy(
                            isDeleted = true,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        ),
                    ),
                )
            }
        }

        test("TC-7972e5be-3e38-494a-a4ad-3ef585ae9b6e 연결 복구는 페이지를 무효화하고 메모를 목록에 다시 포함한다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val targetMemo = memo()
            val relation = memoTag(memoId = targetMemo.id, tagId = targetTag.id, isDeleted = true)
            insertTagMemo(accountId, targetTag, targetMemo, isConnectionDeleted = true)
            val pagingSource =
                database.accountTagMemoDao().page(
                    accountId = accountId,
                    tagId = targetTag.id,
                    scope = TagScopeLocalEntity.SELF.queryValue,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                )
            pagingSource.pagedIds().shouldBeEmpty()

            assertTagPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                tagId = targetTag.id,
                expectedIds = listOf(targetMemo.id),
            ) {
                database.memoTagDao().upsert(
                    listOf(
                        relation.copy(
                            isDeleted = false,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        ),
                    ),
                )
            }
        }

        listOf(
            "완료" to true,
            "삭제" to false,
        ).forEach { (label, isFinishChange) ->
            test("TC-7972e5be-3e38-494a-a4ad-3ef585ae9b6e 메모 $label 은 페이지를 무효화하고 메모를 목록에서 제외한다") {
                val accountId = Arb.kotlinUuid().next()
                val targetTag = tag()
                val targetMemo = memo()
                insertTagMemo(accountId, targetTag, targetMemo)
                val pagingSource =
                    database.accountTagMemoDao().page(
                        accountId = accountId,
                        tagId = targetTag.id,
                        scope = TagScopeLocalEntity.SELF.queryValue,
                        sort = ListSortLocalEntity.DEFAULT.queryValue,
                    )
                pagingSource.pagedIds() shouldBe listOf(targetMemo.id)

                assertTagPageInvalidated(
                    pagingSource = pagingSource,
                    accountId = accountId,
                    tagId = targetTag.id,
                    expectedIds = emptyList(),
                ) {
                    if (isFinishChange) {
                        database.accountMemoDao().updateFinished(
                            accountId = accountId,
                            memoId = targetMemo.id,
                            isFinished = true,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        )
                    } else {
                        database.accountMemoDao().updateDeleted(
                            accountId = accountId,
                            memoId = targetMemo.id,
                            isDeleted = true,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        )
                    }
                }
            }
        }

        test("TC-7972e5be-3e38-494a-a4ad-3ef585ae9b6e 기간 변경은 페이지를 무효화하고 메모를 변경된 정렬 위치로 옮긴다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = tag()
            val movingMemo =
                memo(
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                        ),
                )
            val anchorMemo =
                memo(
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                        ),
                )
            val movedDetail =
                detail(
                    isAllDay = true,
                    start = LocalDateTime(year = 2026, month = 7, day = 21, hour = 0, minute = 0),
                    endInclusive = LocalDateTime(year = 2026, month = 7, day = 21, hour = 0, minute = 0),
                )
            insertTagMemo(accountId, targetTag, movingMemo)
            insertTagMemo(accountId, targetTag, anchorMemo)
            val pagingSource =
                database.accountTagMemoDao().page(
                    accountId = accountId,
                    tagId = targetTag.id,
                    scope = TagScopeLocalEntity.SELF.queryValue,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                )
            pagingSource.pagedIds() shouldBe listOf(movingMemo.id, anchorMemo.id)

            assertTagPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                tagId = targetTag.id,
                expectedIds = listOf(anchorMemo.id, movingMemo.id),
            ) {
                database.accountMemoDao().updateDetail(
                    accountId = accountId,
                    memoId = movingMemo.id,
                    title = movedDetail.title,
                    description = movedDetail.description,
                    color = movedDetail.color,
                    isAllDay = movedDetail.isAllDay,
                    start = movedDetail.start,
                    endInclusive = movedDetail.endInclusive,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            }
        }

        listOf(
            "완료 실행 취소" to true,
            "삭제 실행 취소" to false,
        ).forEach { (label, isFinishUndo) ->
            test("TC-74efe09e-2852-412c-abaa-cd2b552bf626 메모 $label 는 페이지를 무효화하고 메모를 정렬 위치에 다시 포함한다") {
                val accountId = Arb.kotlinUuid().next()
                val targetTag = tag()
                val undoMemo =
                    memo(
                        isFinished = isFinishUndo,
                        isDeleted = !isFinishUndo,
                        detail =
                            detail(
                                isAllDay = true,
                                start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                                endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            ),
                    )
                val anchorMemo =
                    memo(
                        detail =
                            detail(
                                isAllDay = true,
                                start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                                endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                            ),
                    )
                insertTagMemo(accountId, targetTag, undoMemo)
                insertTagMemo(accountId, targetTag, anchorMemo)
                val pagingSource =
                    database.accountTagMemoDao().page(
                        accountId = accountId,
                        tagId = targetTag.id,
                        scope = TagScopeLocalEntity.SELF.queryValue,
                        sort = ListSortLocalEntity.DEFAULT.queryValue,
                    )
                pagingSource.pagedIds() shouldBe listOf(anchorMemo.id)

                assertTagPageInvalidated(
                    pagingSource = pagingSource,
                    accountId = accountId,
                    tagId = targetTag.id,
                    expectedIds = listOf(undoMemo.id, anchorMemo.id),
                ) {
                    if (isFinishUndo) {
                        database.accountMemoDao().updateFinished(
                            accountId = accountId,
                            memoId = undoMemo.id,
                            isFinished = false,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        )
                    } else {
                        database.accountMemoDao().updateDeleted(
                            accountId = accountId,
                            memoId = undoMemo.id,
                            isDeleted = false,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        )
                    }
                }
            }
        }
    }) {
    public companion object {
        private fun memo(
            id: Uuid = Arb.kotlinUuid().next(),
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
            detail: MemoDetailLocalEntity = Arb.memoDetailLocalEntity().next(),
        ): MemoLocalEntity =
            Arb
                .memoLocalEntity()
                .next()
                .copy(
                    id = id,
                    detail = detail,
                    isFinished = isFinished,
                    isDeleted = isDeleted,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun tag(
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
        ): TagLocalEntity =
            Arb
                .tagLocalEntity()
                .next()
                .copy(
                    isFinished = isFinished,
                    isDeleted = isDeleted,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun memoTag(
            memoId: Uuid,
            tagId: Uuid,
            isDeleted: Boolean = false,
        ): MemoTagLocalEntity =
            Arb
                .memoTagLocalEntity()
                .next()
                .copy(
                    memoId = memoId,
                    tagId = tagId,
                    isDeleted = isDeleted,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun detail(
            isAllDay: Boolean?,
            start: LocalDateTime?,
            endInclusive: LocalDateTime?,
            title: String = Arb.string().next(),
        ): MemoDetailLocalEntity =
            Arb
                .memoDetailLocalEntity()
                .next()
                .copy(
                    title = title,
                    isAllDay = isAllDay,
                    start = start,
                    endInclusive = endInclusive,
                )
    }
}
