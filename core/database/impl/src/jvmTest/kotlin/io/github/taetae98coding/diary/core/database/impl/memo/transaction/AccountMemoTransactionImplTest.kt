package io.github.taetae98coding.diary.core.database.impl.memo.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountMemoSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.datasource.AccountMemoTagSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memotag.transaction.AccountMemoTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.atTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class AccountMemoTagTestException : RuntimeException()

class AccountMemoTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountMemoTransactionImpl
        lateinit var memoTagTransaction: AccountMemoTagTransactionImpl
        lateinit var syncDataSource: AccountMemoSyncLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountMemoTransactionImpl(database = database)
            memoTagTransaction = AccountMemoTagTransactionImpl(database = database)
            syncDataSource = AccountMemoSyncLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            memo: MemoLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.memoDao().upsert(listOf(memo))
                database.accountMemoDao().upsert(
                    listOf(
                        AccountMemoLocalEntity(
                            accountId = accountId,
                            memoId = memo.id,
                            isDirty = isDirty,
                        ),
                    ),
                )
            }
        }

        suspend fun findMemo(
            accountId: Uuid,
            memoId: Uuid,
        ): MemoLocalEntity? = database.accountMemoDao().find(accountId = accountId, memoId = memoId).first()

        suspend fun findMemoTagIdList(memoId: Uuid): List<Uuid> =
            database
                .memoTagDao()
                .findByMemoIdList(listOf(memoId))
                .filterNot { memoTag -> memoTag.isDeleted }
                .map { memoTag -> memoTag.tagId }

        suspend fun assertPending(
            accountId: Uuid,
            memoId: Uuid,
        ) {
            syncDataSource
                .readPendingList(accountId = accountId)
                .any { memo -> memo.id == memoId } shouldBe true
        }

        test("TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 TC-0ad50339-a92e-40c2-b05a-07ecf700ee28 메모 생성·수정·복사·이동·완료·다시 시작·삭제·실행 취소·대표 태그 지정과 해제는 업로드 대기 상태가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val updatedAt = instant()

            transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            assertPending(accountId = accountId, memoId = memo.id)

            insertWithSyncState(accountId, memo, isDirty = false)
            transaction.updateDetail(
                accountId = accountId,
                memoId = memo.id,
                detail = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>(),
                updatedAt = updatedAt,
            )
            assertPending(accountId = accountId, memoId = memo.id)

            // 복사는 원본의 내용을 담은 새 메모를 저장한다.
            val copiedMemo = memo().copy(detail = memo.detail)
            transaction.upsert(accountId = accountId, memoList = listOf(copiedMemo), memoTagList = emptyList())
            assertPending(accountId = accountId, memoId = copiedMemo.id)

            // 이동은 기간만 바꾼 내용을 수정으로 저장한다.
            val movedDateRange = fixtureMonkey.giveMeOne<LocalDateRange>()
            insertWithSyncState(accountId, memo, isDirty = false)
            transaction.updateDetail(
                accountId = accountId,
                memoId = memo.id,
                detail =
                    memo.detail.copy(
                        isAllDay = true,
                        start = movedDateRange.start.atTime(hour = 0, minute = 0),
                        endInclusive = movedDateRange.endInclusive.atTime(hour = 0, minute = 0),
                    ),
                updatedAt = updatedAt,
            )
            assertPending(accountId = accountId, memoId = memo.id)

            insertWithSyncState(accountId, memo.copy(isFinished = false), isDirty = false)
            transaction.updateFinished(accountId, memo.id, isFinished = true, updatedAt = updatedAt)
            assertPending(accountId = accountId, memoId = memo.id)

            insertWithSyncState(accountId, memo.copy(isDeleted = false), isDirty = false)
            transaction.updateDeleted(accountId, memo.id, isDeleted = true, updatedAt = updatedAt)
            assertPending(accountId = accountId, memoId = memo.id)

            insertWithSyncState(accountId, memo.copy(isFinished = true), isDirty = false)
            transaction.updateFinished(accountId, memo.id, isFinished = false, updatedAt = updatedAt)
            assertPending(accountId = accountId, memoId = memo.id)

            insertWithSyncState(accountId, memo.copy(isDeleted = true), isDirty = false)
            transaction.updateDeleted(accountId, memo.id, isDeleted = false, updatedAt = updatedAt)
            assertPending(accountId = accountId, memoId = memo.id)

            insertWithSyncState(accountId, memo.copy(primaryTagId = null), isDirty = false)
            memoTagTransaction.updatePrimaryTagId(
                accountId = accountId,
                memoId = memo.id,
                primaryTagId = fixtureMonkey.giveMeOne<Uuid>(),
                updatedAt = updatedAt,
            )
            assertPending(accountId = accountId, memoId = memo.id)

            insertWithSyncState(accountId, memo.copy(primaryTagId = fixtureMonkey.giveMeOne<Uuid>()), isDirty = false)
            memoTagTransaction.updatePrimaryTagId(accountId = accountId, memoId = memo.id, primaryTagId = null, updatedAt = updatedAt)
            assertPending(accountId = accountId, memoId = memo.id)
        }

        test("TC-8da41335-53f4-4825-a1cd-5fcda367cda2 같은 메모를 반복 변경해도 업로드 대상은 한 건이다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val changedMemo = memo.copy(detail = fixtureMonkey.giveMeOne())

            transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            transaction.upsert(accountId = accountId, memoList = listOf(changedMemo), memoTagList = emptyList())

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(changedMemo)
        }

        listOf(
            "대표 태그가 없으면" to null,
            "대표 태그가 있으면" to fixtureMonkey.giveMeOne<Uuid>(),
        ).forEach { (label, primaryTagId) ->
            test("TC-e390ae1d-cb22-4975-9a59-3c3640ce056e $label 저장한 대표 태그가 그대로 조회된다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo().copy(primaryTagId = primaryTagId)

                transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

                findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe primaryTagId
            }
        }

        test("TC-feb101ed-a288-41ed-9753-ecf704b13bd7 대표 태그로 지정한 태그가 기기에 없어도 메모가 저장되고 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val unknownTagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(primaryTagId = unknownTagId)

            transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            database.tagDao().findUpdatedAt(listOf(unknownTagId)).shouldBeEmpty()
            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
        }

        test("TC-722c3857-c84d-4d3c-b093-2b02d423b38b 대표 태그를 다시 지정하면 이전 대표 태그를 대체한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(primaryTagId = fixtureMonkey.giveMeOne<Uuid>())
            val newPrimaryTagId = fixtureMonkey.giveMeOne<Uuid>()
            transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            transaction.upsert(
                accountId = accountId,
                memoList = listOf(memo.copy(primaryTagId = newPrimaryTagId)),
                memoTagList = emptyList(),
            )

            findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe newPrimaryTagId
        }

        test("TC-02c2e084-60aa-49d7-807a-d754c9f79926 태그를 삭제하거나 되살려도 대표 태그 지정이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val memo = memo().copy(primaryTagId = tag.id)
            val tagTransaction = AccountTagTransactionImpl(database = database)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            tagTransaction.updateDeleted(
                accountId = accountId,
                tagId = tag.id,
                isDeleted = true,
                updatedAt = instant(),
            )
            findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe tag.id

            tagTransaction.updateDeleted(
                accountId = accountId,
                tagId = tag.id,
                isDeleted = false,
                updatedAt = instant(),
            )
            findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe tag.id
        }

        test("TC-e945777c-6c14-4d7c-957f-02db8bb12237 메모를 완료하거나 다시 시작하거나 삭제해도 대표 태그 지정이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val primaryTagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(primaryTagId = primaryTagId, isFinished = false, isDeleted = false)
            transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            transaction.updateFinished(accountId, memo.id, isFinished = true, updatedAt = instant())
            findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe primaryTagId

            transaction.updateFinished(accountId, memo.id, isFinished = false, updatedAt = instant())
            findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe primaryTagId

            transaction.updateDeleted(accountId, memo.id, isDeleted = true, updatedAt = instant())
            findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe primaryTagId
        }

        test("TC-a45b76f8-3752-4eb0-9863-b9aab9c84938 제목·설명·컬러·기간을 수정해도 대표 태그가 바뀌지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val primaryTagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(primaryTagId = primaryTagId)
            val newDetail = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>()
            val updatedAt = instant()
            transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            transaction.updateDetail(
                accountId = accountId,
                memoId = memo.id,
                detail = newDetail,
                updatedAt = updatedAt,
            )

            findMemo(accountId = accountId, memoId = memo.id) shouldBe
                memo.copy(detail = newDetail, updatedAt = updatedAt)
        }

        listOf(
            "완료된" to { memo: MemoLocalEntity -> memo.copy(isFinished = true, isDeleted = false) },
            "삭제된" to { memo: MemoLocalEntity -> memo.copy(isFinished = false, isDeleted = true) },
        ).forEach { (label, change) ->
            test("TC-1c7f4ce4-c44d-434e-a440-692a70a9033f $label 메모의 대표 태그도 다른 태그로 바꿀 수 있다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val newPrimaryTagId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = change(memo()).copy(primaryTagId = fixtureMonkey.giveMeOne<Uuid>())
                insertWithSyncState(accountId, memo, isDirty = false)

                memoTagTransaction.updatePrimaryTagId(
                    accountId = accountId,
                    memoId = memo.id,
                    primaryTagId = newPrimaryTagId,
                    updatedAt = instant(),
                )

                findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe newPrimaryTagId
            }
        }

        listOf<Pair<String, (Uuid) -> Uuid?>>(
            "다른 태그로 지정하면" to { newTagId -> newTagId },
            "지정을 해제하면" to { _ -> null },
        ).forEach { (label, nextPrimaryTagId) ->
            test("TC-e221e2b1-eeef-4d6d-ad5b-9308db106f80 대표 태그를 $label 메모의 내용과 완료·삭제 여부는 바뀌지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo().copy(primaryTagId = fixtureMonkey.giveMeOne<Uuid>())
                insertWithSyncState(accountId, memo, isDirty = false)

                memoTagTransaction.updatePrimaryTagId(
                    accountId = accountId,
                    memoId = memo.id,
                    primaryTagId = nextPrimaryTagId(fixtureMonkey.giveMeOne<Uuid>()),
                    updatedAt = instant(),
                )

                val stored = findMemo(accountId = accountId, memoId = memo.id)
                stored?.detail shouldBe memo.detail
                stored?.isFinished shouldBe memo.isFinished
                stored?.isDeleted shouldBe memo.isDeleted
            }
        }

        test("TC-fd2e66f0-833a-4e6b-b73e-a60bfd66f9cf 하나의 메모에 여러 태그를 연결해 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tagIdList = listOf(fixtureMonkey.giveMeOne<Uuid>(), fixtureMonkey.giveMeOne<Uuid>())

            transaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = tagIdList.map { tagId -> memoTag(memoId = memo.id, tagId = tagId, memo = memo) },
            )

            findMemoTagIdList(memoId = memo.id) shouldContainExactlyInAnyOrder tagIdList
        }

        test("TC-35e06d72-7688-469c-8b79-9c1a3cb4c2e3 같은 태그를 여러 메모에 연결해도 서로 대체되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val firstMemo = memo()
            val secondMemo = memo()

            listOf(firstMemo, secondMemo).forEach { memo ->
                transaction.upsert(
                    accountId = accountId,
                    memoList = listOf(memo),
                    memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
                )
            }

            findMemoTagIdList(memoId = firstMemo.id) shouldBe listOf(tagId)
            findMemoTagIdList(memoId = secondMemo.id) shouldBe listOf(tagId)
        }

        test("TC-3c6aa8ab-aac3-4e3b-aa0c-3ab987ae4ee1 같은 메모와 태그의 연결을 다시 저장해도 한 건으로 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()

            repeat(2) {
                transaction.upsert(
                    accountId = accountId,
                    memoList = listOf(memo),
                    memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
                )
            }

            database.memoTagDao().findByMemoIdList(listOf(memo.id)) shouldHaveSize 1
        }

        test("TC-ba01a631-dae3-45e1-b94e-cca2189d2d6f 연결할 태그가 없으면 태그 연결 없이 메모만 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()

            transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
            database.memoTagDao().findByMemoIdList(listOf(memo.id)).shouldBeEmpty()
        }

        test("TC-42c2218a-d872-40aa-a7b8-0d6451657847 TC-e4c546a3-8757-4f7f-8fdb-33d8897e29dc 태그를 완료·다시 시작·삭제해도 메모와의 연결이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val memo = memo()
            val tagTransaction = AccountTagTransactionImpl(database = database)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            transaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = listOf(memoTag(memoId = memo.id, tagId = tag.id, memo = memo)),
            )

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = instant())
            findMemoTagIdList(memoId = memo.id) shouldBe listOf(tag.id)

            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = instant())
            findMemoTagIdList(memoId = memo.id) shouldBe listOf(tag.id)
        }

        test("TC-2fe0d41a-efdf-4140-85b6-66a45c973776 TC-973a1bfe-4947-4c27-bb75-436e758d9a77 TC-32425cb7-28af-489f-8318-b0e61c7d4b5c TC-d3c48f13-735f-4f1e-b249-ce002a85e1fe 메모 상태를 변경해도 내용과 태그 연결, 대표 태그가 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(primaryTagId = tagId, isFinished = true, isDeleted = false)
            transaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
            )

            suspend fun assertContentKept() {
                val stored = findMemo(accountId = accountId, memoId = memo.id).shouldNotBeNull()
                stored.detail shouldBe memo.detail
                stored.primaryTagId shouldBe tagId
                findMemoTagIdList(memoId = memo.id) shouldBe listOf(tagId)
            }

            transaction.updateFinished(accountId = accountId, memoId = memo.id, isFinished = false, updatedAt = instant())
            assertContentKept()

            transaction.updateFinished(accountId = accountId, memoId = memo.id, isFinished = true, updatedAt = instant())
            assertContentKept()

            transaction.updateDeleted(accountId = accountId, memoId = memo.id, isDeleted = true, updatedAt = instant())
            assertContentKept()

            transaction.updateDeleted(accountId = accountId, memoId = memo.id, isDeleted = false, updatedAt = instant())
            assertContentKept()
        }

        test("TC-903020e3-9b26-4c21-bdf6-44741c9f2dc2 제목·설명·컬러·기간을 수정해도 태그 연결이 바뀌지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            transaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
            )

            transaction.updateDetail(
                accountId = accountId,
                memoId = memo.id,
                detail = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>(),
                updatedAt = instant(),
            )

            findMemoTagIdList(memoId = memo.id) shouldBe listOf(tagId)
        }

        test("TC-c8323bc1-422c-44ee-8acb-94bc76216682 해제된 연결을 다시 만들면 기존 연결이 되살아난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val deletedMemoTag =
                memoTag(memoId = memo.id, tagId = tagId, memo = memo).copy(
                    isDeleted = true,
                    updatedAt = instant(),
                )
            database.memoTagDao().upsert(listOf(deletedMemoTag))
            val restoredMemoTag = memoTag(memoId = memo.id, tagId = tagId, memo = memo)

            transaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = listOf(restoredMemoTag),
            )

            database.memoTagDao().findByMemoIdList(listOf(memo.id)) shouldBe listOf(restoredMemoTag)
        }

        test("TC-cf31ec56-1107-480f-98f0-73d83cdb95bc 저장된 연결의 생성 시각과 수정 시각은 메모와 같다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()

            transaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
            )

            database.memoTagDao().findByMemoIdList(listOf(memo.id)) shouldBe
                listOf(
                    MemoTagLocalEntity(
                        memoId = memo.id,
                        tagId = tagId,
                        isDeleted = false,
                        updatedAt = memo.updatedAt,
                        createdAt = memo.createdAt,
                    ),
                )
        }

        test("TC-a6632462-02fa-45a7-87d6-0ede996e55f7 같은 연결을 다른 수정 시각으로 저장하면 마지막 내용으로 덮어쓴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val changedMemo = memo.copy(updatedAt = instant())

            transaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
            )
            transaction.upsert(
                accountId = accountId,
                memoList = listOf(changedMemo),
                memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = changedMemo)),
            )

            database.memoTagDao().findByMemoIdList(listOf(memo.id)) shouldBe
                listOf(
                    MemoTagLocalEntity(
                        memoId = memo.id,
                        tagId = tagId,
                        isDeleted = false,
                        updatedAt = changedMemo.updatedAt,
                        createdAt = changedMemo.createdAt,
                    ),
                )
        }

        test("TC-b696fa61-04cc-4a7f-94b1-d8d7277ce50f 저장이 실패하면 메모와 계정 연결, 태그 연결이 모두 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val failingDatabase = spyk(database)
            every { failingDatabase.accountMemoTagDao() } throws AccountMemoTagTestException()
            val failingTransaction = AccountMemoTransactionImpl(database = failingDatabase)

            shouldThrowExactly<AccountMemoTagTestException> {
                failingTransaction.upsert(
                    accountId = accountId,
                    memoList = listOf(memo),
                    memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
                )
            }

            findMemo(accountId = accountId, memoId = memo.id).shouldBeNull()
            database.memoTagDao().findByMemoIdList(listOf(memo.id)).shouldBeEmpty()
            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-57535117-d865-477f-a7d0-2dc0ba8e0404 저장된 연결은 업로드 대기 상태가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val memoTagSyncDataSource = AccountMemoTagSyncLocalDataSourceImpl(database = database)

            transaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
            )

            memoTagSyncDataSource
                .readPendingList(accountId = accountId)
                .map { memoTag -> memoTag.tagId } shouldBe listOf(tagId)
        }

        test("TC-130270d5-4801-4215-b84d-029a6f8d8b96 태그를 추가하면 연결이 저장되고 메모의 다른 값은 바뀌지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(primaryTagId = null)
            insertWithSyncState(accountId, memo, isDirty = false)
            val updatedAt = instant()

            memoTagTransaction.upsert(accountId = accountId, memoId = memo.id, tagId = tagId, isDeleted = false, updatedAt = updatedAt)

            database.memoTagDao().findByMemoIdList(listOf(memo.id)) shouldBe
                listOf(
                    MemoTagLocalEntity(
                        memoId = memo.id,
                        tagId = tagId,
                        isDeleted = false,
                        updatedAt = updatedAt,
                        createdAt = updatedAt,
                    ),
                )
            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
        }

        test("TC-130270d5-4801-4215-b84d-029a6f8d8b96 추가한 연결은 업로드 대기 상태가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertWithSyncState(accountId, memo, isDirty = false)
            val memoTagSyncDataSource = AccountMemoTagSyncLocalDataSourceImpl(database = database)

            memoTagTransaction.upsert(accountId = accountId, memoId = memo.id, tagId = tagId, isDeleted = false, updatedAt = instant())

            memoTagSyncDataSource
                .readPendingList(accountId = accountId)
                .map { memoTag -> memoTag.tagId } shouldBe listOf(tagId)
        }

        test("TC-c8323bc1-422c-44ee-8acb-94bc76216682 해제된 연결의 태그를 다시 추가하면 생성 시각을 유지한 채 되살아난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val createdAt = instant()
            insertWithSyncState(accountId, memo, isDirty = false)
            database.memoTagDao().upsert(
                listOf(
                    MemoTagLocalEntity(
                        memoId = memo.id,
                        tagId = tagId,
                        isDeleted = true,
                        updatedAt = instant(),
                        createdAt = createdAt,
                    ),
                ),
            )
            val restoredAt = instant()

            memoTagTransaction.upsert(accountId = accountId, memoId = memo.id, tagId = tagId, isDeleted = false, updatedAt = restoredAt)

            database.memoTagDao().findByMemoIdList(listOf(memo.id)) shouldBe
                listOf(
                    MemoTagLocalEntity(
                        memoId = memo.id,
                        tagId = tagId,
                        isDeleted = false,
                        updatedAt = restoredAt,
                        createdAt = createdAt,
                    ),
                )
        }

        test("TC-278d2a9d-3386-4cee-a542-56f2577f0050 대표 태그가 아닌 태그를 제거하면 연결만 해제 상태로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val primaryTagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(primaryTagId = primaryTagId)
            insertWithSyncState(accountId, memo, isDirty = false)
            memoTagTransaction.upsert(accountId = accountId, memoId = memo.id, tagId = tagId, isDeleted = false, updatedAt = instant())
            val removedAt = instant()

            memoTagTransaction.upsert(accountId = accountId, memoId = memo.id, tagId = tagId, isDeleted = true, updatedAt = removedAt)

            findMemoTagIdList(memoId = memo.id).shouldBeEmpty()
            database.memoTagDao().findByMemoIdList(listOf(memo.id)).single().let { memoTag ->
                memoTag.isDeleted shouldBe true
                memoTag.updatedAt shouldBe removedAt
            }
            findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe primaryTagId
        }

        test("TC-e24e3b75-903d-4aa6-bdf9-01d1b57956b4 대표 태그를 제거하면 연결 해제와 대표 태그 해제가 함께 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertWithSyncState(accountId, memo, isDirty = false)
            memoTagTransaction.updatePrimaryTagId(accountId = accountId, memoId = memo.id, primaryTagId = tagId, updatedAt = instant())
            val removedAt = instant()

            memoTagTransaction.upsert(accountId = accountId, memoId = memo.id, tagId = tagId, isDeleted = true, updatedAt = removedAt)

            findMemoTagIdList(memoId = memo.id).shouldBeEmpty()
            database.memoTagDao().findByMemoIdList(listOf(memo.id)).single().let { memoTag ->
                memoTag.tagId shouldBe tagId
                memoTag.isDeleted shouldBe true
                memoTag.updatedAt shouldBe removedAt
            }
            findMemo(accountId = accountId, memoId = memo.id).shouldNotBeNull().let { stored ->
                stored.primaryTagId.shouldBeNull()
                stored.updatedAt shouldBe removedAt
            }
        }

        test("TC-46c90377-814a-4f6d-b005-798b0ce42a7d TC-f7c80d8e-b121-4beb-b675-e230c0842efd 제거 저장이 실패하면 연결과 대표 태그 지정이 모두 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertWithSyncState(accountId, memo, isDirty = false)
            memoTagTransaction.updatePrimaryTagId(accountId = accountId, memoId = memo.id, primaryTagId = tagId, updatedAt = instant())
            val failingDatabase = spyk(database)
            every { failingDatabase.accountMemoDao() } throws AccountMemoTagTestException()
            val failingTagTransaction = AccountMemoTagTransactionImpl(database = failingDatabase)

            shouldThrowExactly<AccountMemoTagTestException> {
                failingTagTransaction.upsert(
                    accountId = accountId,
                    memoId = memo.id,
                    tagId = tagId,
                    isDeleted = true,
                    updatedAt = instant(),
                )
            }

            findMemoTagIdList(memoId = memo.id) shouldBe listOf(tagId)
            findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe tagId
        }

        test("TC-121409a2-4451-472a-bd3b-c6986bf6ff27 연결되지 않은 태그를 대표 태그로 지정하면 연결도 함께 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(primaryTagId = null)
            insertWithSyncState(accountId, memo, isDirty = false)
            val updatedAt = instant()

            memoTagTransaction.updatePrimaryTagId(accountId = accountId, memoId = memo.id, primaryTagId = tagId, updatedAt = updatedAt)

            findMemoTagIdList(memoId = memo.id) shouldBe listOf(tagId)
            findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe tagId
        }

        test("TC-121409a2-4451-472a-bd3b-c6986bf6ff27 TC-b7d27335-e85f-49d3-b8c3-1032a8666ebf 대표 태그 지정 저장이 실패하면 연결도 만들어지지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(primaryTagId = null)
            insertWithSyncState(accountId, memo, isDirty = false)
            val failingDatabase = spyk(database)
            every { failingDatabase.accountMemoDao() } throws AccountMemoTagTestException()
            val failingTagTransaction = AccountMemoTagTransactionImpl(database = failingDatabase)

            shouldThrowExactly<AccountMemoTagTestException> {
                failingTagTransaction.updatePrimaryTagId(
                    accountId = accountId,
                    memoId = memo.id,
                    primaryTagId = tagId,
                    updatedAt = instant(),
                )
            }

            database.memoTagDao().findByMemoIdList(listOf(memo.id)).shouldBeEmpty()
            findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId.shouldBeNull()
        }

        test("TC-8f163fe3-4081-49db-adc8-22e85df13732 대표 태그 지정을 해제하면 대표 태그만 비워지고 연결은 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertWithSyncState(accountId, memo, isDirty = false)
            memoTagTransaction.updatePrimaryTagId(accountId = accountId, memoId = memo.id, primaryTagId = tagId, updatedAt = instant())
            val unsetAt = instant()

            memoTagTransaction.updatePrimaryTagId(accountId = accountId, memoId = memo.id, primaryTagId = null, updatedAt = unsetAt)

            findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId.shouldBeNull()
            findMemoTagIdList(memoId = memo.id) shouldBe listOf(tagId)
        }

        listOf<Triple<String, suspend (Uuid, Uuid, Uuid, Instant) -> Unit, Boolean>>(
            Triple("대표 태그를 지정하면", { accountId, memoId, tagId, updatedAt ->
                memoTagTransaction.updatePrimaryTagId(accountId = accountId, memoId = memoId, primaryTagId = tagId, updatedAt = updatedAt)
            }, false),
            Triple("대표 태그 지정을 해제하면", { accountId, memoId, _, updatedAt ->
                memoTagTransaction.updatePrimaryTagId(accountId = accountId, memoId = memoId, primaryTagId = null, updatedAt = updatedAt)
            }, true),
            Triple("대표 태그의 연결을 해제하면", { accountId, memoId, tagId, updatedAt ->
                memoTagTransaction.upsert(accountId = accountId, memoId = memoId, tagId = tagId, isDeleted = true, updatedAt = updatedAt)
            }, true),
        ).forEach { (label, action, needsPrimaryTag) ->
            test("TC-4a035170-0609-46fa-a688-811b27479294 $label 메모의 수정 시각이 갱신되고 업로드 대기 상태가 된다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo().copy(primaryTagId = null)
                insertWithSyncState(accountId, memo, isDirty = false)
                if (needsPrimaryTag) {
                    memoTagTransaction.updatePrimaryTagId(accountId = accountId, memoId = memo.id, primaryTagId = tagId, updatedAt = instant())
                    insertWithSyncState(accountId, findMemo(accountId, memo.id)!!, isDirty = false)
                }
                val updatedAt = instant()

                action(accountId, memo.id, tagId, updatedAt)

                findMemo(accountId = accountId, memoId = memo.id)?.updatedAt shouldBe updatedAt
                assertPending(accountId = accountId, memoId = memo.id)
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::updatedAt, instant())
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()

        private fun tag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::isDeleted, false)
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()

        private fun memoTag(
            memoId: Uuid,
            tagId: Uuid,
            memo: MemoLocalEntity,
        ): MemoTagLocalEntity =
            MemoTagLocalEntity(
                memoId = memoId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = memo.updatedAt,
                createdAt = memo.createdAt,
            )
    }
}
