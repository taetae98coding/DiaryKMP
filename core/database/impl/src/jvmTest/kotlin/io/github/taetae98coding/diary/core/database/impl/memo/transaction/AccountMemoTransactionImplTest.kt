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
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
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
                .findPending(accountId = accountId)
                .any { memo -> memo.id == memoId } shouldBe true
        }

        test("TC-DATA-SYNC-DOMAIN-001 메모 생성·수정·완료·다시 시작·삭제·실행 취소·대표 태그 지정과 해제는 업로드 대기 상태가 된다") {
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

        test("TC-DATA-SYNC-DOMAIN-003 같은 메모를 반복 변경해도 업로드 대상은 한 건이다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val changedMemo = memo.copy(detail = fixtureMonkey.giveMeOne())

            transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            transaction.upsert(accountId = accountId, memoList = listOf(changedMemo), memoTagList = emptyList())

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changedMemo)
        }

        listOf(
            "대표 태그가 없으면" to null,
            "대표 태그가 있으면" to fixtureMonkey.giveMeOne<Uuid>(),
        ).forEach { (label, primaryTagId) ->
            test("TC-MEMO-PRIMARY-TAG-DATA-001 $label 저장한 대표 태그가 그대로 조회된다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo().copy(primaryTagId = primaryTagId)

                transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

                findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe primaryTagId
            }
        }

        test("TC-MEMO-PRIMARY-TAG-DATA-002 대표 태그로 지정한 태그가 기기에 없어도 메모가 저장되고 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val unknownTagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(primaryTagId = unknownTagId)

            transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            database.tagDao().findUpdatedAt(listOf(unknownTagId)).shouldBeEmpty()
            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
        }

        test("TC-MEMO-PRIMARY-TAG-DOMAIN-001 대표 태그를 다시 지정하면 이전 대표 태그를 대체한다") {
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

        test("TC-MEMO-PRIMARY-TAG-DOMAIN-002 태그를 삭제하거나 되살려도 대표 태그 지정이 유지된다") {
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

        test("TC-MEMO-PRIMARY-TAG-DOMAIN-003 메모를 완료하거나 다시 시작하거나 삭제해도 대표 태그 지정이 유지된다") {
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

        test("TC-MEMO-PRIMARY-TAG-DOMAIN-004 제목·설명·컬러·기간을 수정해도 대표 태그가 바뀌지 않는다") {
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
            test("TC-MEMO-PRIMARY-TAG-DOMAIN-007 $label 메모의 대표 태그도 다른 태그로 바꿀 수 있다") {
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
            test("TC-MEMO-PRIMARY-TAG-DOMAIN-008 대표 태그를 $label 메모의 내용과 완료·삭제 여부는 바뀌지 않는다") {
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

        test("TC-MEMO-TAG-DOMAIN-001 하나의 메모에 여러 태그를 연결해 저장한다") {
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

        test("TC-MEMO-TAG-DOMAIN-002 같은 태그를 여러 메모에 연결해도 서로 대체되지 않는다") {
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

        test("TC-MEMO-TAG-DOMAIN-003 같은 메모와 태그의 연결을 다시 저장해도 한 건으로 유지된다") {
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

        test("TC-MEMO-TAG-DOMAIN-004 연결할 태그가 없으면 태그 연결 없이 메모만 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()

            transaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
            database.memoTagDao().findByMemoIdList(listOf(memo.id)).shouldBeEmpty()
        }

        test("TC-MEMO-TAG-DOMAIN-006 TC-TAG-DETAIL-DOMAIN-005 태그를 완료·다시 시작·삭제해도 메모와의 연결이 유지된다") {
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

        test("TC-MEMO-TAG-DOMAIN-007 TC-TAG-DETAIL-MEMO-DATA-004 TC-MEMO-FINISHED-LIST-DOMAIN-006 TC-TAG-MEMO-FINISHED-LIST-DATA-004 메모 상태를 변경해도 태그와의 연결이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            transaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
            )

            transaction.updateFinished(accountId = accountId, memoId = memo.id, isFinished = true, updatedAt = instant())
            findMemoTagIdList(memoId = memo.id) shouldBe listOf(tagId)

            transaction.updateFinished(accountId = accountId, memoId = memo.id, isFinished = false, updatedAt = instant())
            findMemoTagIdList(memoId = memo.id) shouldBe listOf(tagId)

            transaction.updateDeleted(accountId = accountId, memoId = memo.id, isDeleted = true, updatedAt = instant())
            findMemoTagIdList(memoId = memo.id) shouldBe listOf(tagId)

            transaction.updateDeleted(accountId = accountId, memoId = memo.id, isDeleted = false, updatedAt = instant())
            findMemoTagIdList(memoId = memo.id) shouldBe listOf(tagId)
        }

        test("TC-MEMO-TAG-DOMAIN-008 제목·설명·컬러·기간을 수정해도 태그 연결이 바뀌지 않는다") {
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

        test("TC-MEMO-TAG-DOMAIN-010 해제된 연결을 다시 만들면 기존 연결이 되살아난다") {
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

        test("TC-MEMO-TAG-DATA-001 저장된 연결의 생성 시각과 수정 시각은 메모와 같다") {
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

        test("TC-MEMO-TAG-DATA-003 같은 연결을 다른 수정 시각으로 저장하면 마지막 내용으로 덮어쓴다") {
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

        test("TC-MEMO-TAG-DATA-002 저장이 실패하면 메모와 계정 연결, 태그 연결이 모두 남지 않는다") {
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
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-MEMO-TAG-DATA-004 저장된 연결은 업로드 대기 상태가 된다") {
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
                .findPending(accountId = accountId)
                .map { memoTag -> memoTag.tagId } shouldBe listOf(tagId)
        }

        test("TC-MEMO-DETAIL-DATA-004 태그를 추가하면 연결이 저장되고 메모의 다른 값은 바뀌지 않는다") {
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

        test("TC-MEMO-DETAIL-DATA-004 추가한 연결은 업로드 대기 상태가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertWithSyncState(accountId, memo, isDirty = false)
            val memoTagSyncDataSource = AccountMemoTagSyncLocalDataSourceImpl(database = database)

            memoTagTransaction.upsert(accountId = accountId, memoId = memo.id, tagId = tagId, isDeleted = false, updatedAt = instant())

            memoTagSyncDataSource
                .findPending(accountId = accountId)
                .map { memoTag -> memoTag.tagId } shouldBe listOf(tagId)
        }

        test("TC-MEMO-TAG-DOMAIN-010 해제된 연결의 태그를 다시 추가하면 생성 시각을 유지한 채 되살아난다") {
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

        test("TC-MEMO-DETAIL-DATA-005 대표 태그가 아닌 태그를 제거하면 연결만 해제 상태로 남는다") {
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

        test("TC-MEMO-TAG-DOMAIN-009 대표 태그를 제거하면 연결 해제와 대표 태그 해제가 함께 반영된다") {
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
            findMemo(accountId = accountId, memoId = memo.id)?.let { stored ->
                stored.primaryTagId.shouldBeNull()
                stored.updatedAt shouldBe removedAt
            }
        }

        test("TC-MEMO-DETAIL-DATA-007 TC-MEMO-TAG-DATA-011 제거 저장이 실패하면 연결과 대표 태그 지정이 모두 유지된다") {
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

        test("TC-MEMO-DETAIL-DATA-008 연결되지 않은 태그를 대표 태그로 지정하면 연결도 함께 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(primaryTagId = null)
            insertWithSyncState(accountId, memo, isDirty = false)
            val updatedAt = instant()

            memoTagTransaction.updatePrimaryTagId(accountId = accountId, memoId = memo.id, primaryTagId = tagId, updatedAt = updatedAt)

            findMemoTagIdList(memoId = memo.id) shouldBe listOf(tagId)
            findMemo(accountId = accountId, memoId = memo.id)?.primaryTagId shouldBe tagId
        }

        test("TC-MEMO-DETAIL-DATA-008 대표 태그 지정 저장이 실패하면 연결도 만들어지지 않는다") {
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

        test("TC-MEMO-DETAIL-DATA-009 대표 태그 지정을 해제하면 대표 태그만 비워지고 연결은 유지된다") {
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
            test("TC-MEMO-PRIMARY-TAG-DATA-006 $label 메모의 수정 시각이 갱신되고 업로드 대기 상태가 된다") {
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

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

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
