package io.github.taetae98coding.diary.core.database.impl.memoplace.transaction

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountMemoSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memoplace.datasource.AccountMemoPlaceLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memoplace.datasource.AccountMemoPlaceSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class MemoPlaceTestException : RuntimeException()

class AccountMemoPlaceTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var memoTransaction: AccountMemoTransactionImpl
        lateinit var placeTransaction: AccountPlaceTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var accountMemoPlaceTransaction: AccountMemoPlaceTransactionImpl
        lateinit var syncTransaction: AccountMemoPlaceSyncTransactionImpl
        lateinit var dataSource: AccountMemoPlaceLocalDataSourceImpl
        lateinit var syncDataSource: AccountMemoPlaceSyncLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            memoTransaction = AccountMemoTransactionImpl(database = database)
            placeTransaction = AccountPlaceTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
            accountMemoPlaceTransaction = AccountMemoPlaceTransactionImpl(database = database)
            syncTransaction = AccountMemoPlaceSyncTransactionImpl(database = database)
            dataSource = AccountMemoPlaceLocalDataSourceImpl(database = database)
            syncDataSource = AccountMemoPlaceSyncLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertMemoWithPlaceList(
            accountId: Uuid,
            memo: MemoLocalEntity,
            placeList: List<PlaceLocalEntity>,
        ) {
            placeTransaction.upsert(accountId = accountId, placeList = placeList, placeTagList = emptyList())
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoPlaceList = placeList.map { place -> memoPlace(memoId = memo.id, placeId = place.id, memo = memo) },
            )
        }

        suspend fun findMemo(
            accountId: Uuid,
            memoId: Uuid,
        ): MemoLocalEntity? = database.accountMemoDao().find(accountId = accountId, memoId = memoId).first()

        suspend fun findMemoPlaceList(memoId: Uuid): List<MemoPlaceLocalEntity> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT memo_id, place_id, is_deleted, updated_at, created_at
                    FROM memo_place
                    WHERE memo_id = '$memoId'
                    ORDER BY place_id ASC
                    """,
                ) { statement -> statement.readAll { it.toMemoPlace() } }
            }

        suspend fun getPlaceList(
            accountId: Uuid,
            memoId: Uuid,
        ): List<PlaceLocalEntity> = dataSource.getPlaceList(accountId = accountId, memoId = memoId).first()

        suspend fun findPendingPlaceIdList(accountId: Uuid): List<Uuid> =
            syncDataSource
                .findPending(accountId = accountId)
                .map { memoPlace -> memoPlace.placeId }

        suspend fun clearAllPending(accountId: Uuid) {
            syncTransaction.clearPending(
                accountId = accountId,
                memoPlaceList = syncDataSource.findPending(accountId = accountId),
            )
        }

        test("TC-MEMO-PLACE-DOMAIN-001 하나의 메모에 여러 장소를 연결해 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val placeList = List(2) { place() }

            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = placeList)

            getPlaceList(accountId = accountId, memoId = memo.id) shouldContainExactlyInAnyOrder placeList
        }

        test("TC-MEMO-PLACE-DOMAIN-002 같은 장소를 여러 메모에 연결해도 서로 대체되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val firstMemo = memo()
            val secondMemo = memo()

            listOf(firstMemo, secondMemo).forEach { memo ->
                insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            }

            getPlaceList(accountId = accountId, memoId = firstMemo.id) shouldBe listOf(place)
            getPlaceList(accountId = accountId, memoId = secondMemo.id) shouldBe listOf(place)
        }

        test("TC-MEMO-PLACE-DOMAIN-003 같은 메모와 장소의 연결을 다시 저장해도 한 건으로 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()

            repeat(2) {
                insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            }

            findMemoPlaceList(memoId = memo.id) shouldHaveSize 1
        }

        test("TC-MEMO-PLACE-DOMAIN-004 연결할 장소가 없으면 장소 연결 없이 메모만 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()

            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
            findMemoPlaceList(memoId = memo.id).shouldBeEmpty()
            getPlaceList(accountId = accountId, memoId = memo.id).shouldBeEmpty()
        }

        test("TC-MEMO-PLACE-DOMAIN-005 장소를 삭제해도 메모와의 연결이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))

            placeTransaction.upsert(accountId = accountId, placeList = listOf(place.copy(isDeleted = true)), placeTagList = emptyList())

            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(memoPlace(memoId = memo.id, placeId = place.id, memo = memo))
        }

        test("TC-MEMO-PLACE-DOMAIN-006 메모를 완료하거나 삭제해도 장소와의 연결이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(isFinished = false, isDeleted = false)
            val place = place()
            val memoPlace = memoPlace(memoId = memo.id, placeId = place.id, memo = memo)
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))

            memoTransaction.updateFinished(accountId = accountId, memoId = memo.id, isFinished = true, updatedAt = fixtureMonkey.giveMeOne<Instant>())
            findMemoPlaceList(memoId = memo.id) shouldBe listOf(memoPlace)

            memoTransaction.updateDeleted(accountId = accountId, memoId = memo.id, isDeleted = true, updatedAt = fixtureMonkey.giveMeOne<Instant>())
            findMemoPlaceList(memoId = memo.id) shouldBe listOf(memoPlace)
        }

        test("TC-MEMO-PLACE-DOMAIN-007 메모의 제목·설명·컬러·기간 수정은 장소 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            val newDetail = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>()
            val updatedAt = fixtureMonkey.giveMeOne<Instant>()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))

            memoTransaction.updateDetail(accountId = accountId, memoId = memo.id, detail = newDetail, updatedAt = updatedAt)

            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo.copy(detail = newDetail, updatedAt = updatedAt)
            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(memoPlace(memoId = memo.id, placeId = place.id, memo = memo))
            getPlaceList(accountId = accountId, memoId = memo.id) shouldBe listOf(place)
        }

        test("TC-MEMO-PLACE-DOMAIN-008 TC-MEMO-DETAIL-DATA-021 연결을 해제하면 조회에서 제외되고 해제 상태로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            val removedAt = fixtureMonkey.giveMeOne<Instant>()

            accountMemoPlaceTransaction.upsert(accountId = accountId, memoId = memo.id, placeId = place.id, isDeleted = true, updatedAt = removedAt)

            getPlaceList(accountId = accountId, memoId = memo.id).shouldBeEmpty()
            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(
                    MemoPlaceLocalEntity(
                        memoId = memo.id,
                        placeId = place.id,
                        isDeleted = true,
                        updatedAt = removedAt,
                        createdAt = memo.createdAt,
                    ),
                )
        }

        test("TC-MEMO-PLACE-DOMAIN-009 해제된 연결을 다시 만들면 생성 시각을 유지한 채 되살아난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            val createdAt = fixtureMonkey.giveMeOne<Instant>()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            database.memoPlaceDao().upsert(
                MemoPlaceLocalEntity(
                    memoId = memo.id,
                    placeId = place.id,
                    isDeleted = true,
                    updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                    createdAt = createdAt,
                ),
            )
            val restoredAt = fixtureMonkey.giveMeOne<Instant>()

            accountMemoPlaceTransaction.upsert(accountId = accountId, memoId = memo.id, placeId = place.id, isDeleted = false, updatedAt = restoredAt)

            getPlaceList(accountId = accountId, memoId = memo.id) shouldBe listOf(place)
            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(
                    MemoPlaceLocalEntity(
                        memoId = memo.id,
                        placeId = place.id,
                        isDeleted = false,
                        updatedAt = restoredAt,
                        createdAt = createdAt,
                    ),
                )
        }

        test("TC-MEMO-PLACE-DATA-010 TC-DATA-SYNC-DOMAIN-001 연결을 하나 해제하면 그 연결만 업로드 대기가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val removedPlace = place()
            val keptPlace = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(removedPlace, keptPlace))
            clearAllPending(accountId = accountId)

            accountMemoPlaceTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                placeId = removedPlace.id,
                isDeleted = true,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
            )

            findPendingPlaceIdList(accountId = accountId) shouldBe listOf(removedPlace.id)
        }

        test("TC-MEMO-PLACE-DATA-010 TC-DATA-SYNC-DOMAIN-001 연결을 하나 만들면 그 연결만 업로드 대기가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptPlace = place()
            val addedPlace = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(keptPlace))
            placeTransaction.upsert(accountId = accountId, placeList = listOf(addedPlace), placeTagList = emptyList())
            clearAllPending(accountId = accountId)

            accountMemoPlaceTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                placeId = addedPlace.id,
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
            )

            findPendingPlaceIdList(accountId = accountId) shouldBe listOf(addedPlace.id)
        }

        test("TC-MEMO-PLACE-DATA-001 저장된 연결의 생성 시각과 수정 시각은 저장 시점으로 서로 같고 해제되지 않은 상태다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val now = fixtureMonkey.giveMeOne<Instant>()
            val memo = memo().copy(updatedAt = now, createdAt = now)
            val place = place()

            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))

            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(
                    MemoPlaceLocalEntity(
                        memoId = memo.id,
                        placeId = place.id,
                        isDeleted = false,
                        updatedAt = now,
                        createdAt = now,
                    ),
                )
        }

        test("TC-MEMO-PLACE-DATA-002 저장이 실패하면 메모와 계정 연결, 태그 연결, 장소 연결이 모두 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            val syncDataSource = AccountMemoSyncLocalDataSourceImpl(database = database)
            val failingDatabase = spyk(database)
            every { failingDatabase.memoPlaceDao() } throws MemoPlaceTestException()
            val failingTransaction = AccountMemoTransactionImpl(database = failingDatabase)

            shouldThrowExactly<MemoPlaceTestException> {
                failingTransaction.upsert(
                    accountId = accountId,
                    memoList = listOf(memo),
                    memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
                    memoPlaceList = listOf(memoPlace(memoId = memo.id, placeId = place.id, memo = memo)),
                )
            }

            findMemo(accountId = accountId, memoId = memo.id).shouldBeNull()
            database.memoTagDao().findByMemoIdList(listOf(memo.id)).shouldBeEmpty()
            findMemoPlaceList(memoId = memo.id).shouldBeEmpty()
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-MEMO-PLACE-DATA-003 같은 연결을 다른 수정 시각으로 저장하면 마지막 내용으로 덮어쓴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val changedMemo = memo.copy(updatedAt = fixtureMonkey.giveMeOne<Instant>())
            val place = place()

            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            insertMemoWithPlaceList(accountId = accountId, memo = changedMemo, placeList = listOf(place))

            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(
                    MemoPlaceLocalEntity(
                        memoId = memo.id,
                        placeId = place.id,
                        isDeleted = false,
                        updatedAt = changedMemo.updatedAt,
                        createdAt = changedMemo.createdAt,
                    ),
                )
        }

        test("TC-MEMO-PLACE-DATA-004 해제되지 않은 연결의 장소만 메모의 장소로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptPlace = place()
            val removedPlace = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(keptPlace, removedPlace))

            accountMemoPlaceTransaction.upsert(accountId = accountId, memoId = memo.id, placeId = removedPlace.id, isDeleted = true, updatedAt = fixtureMonkey.giveMeOne<Instant>())

            getPlaceList(accountId = accountId, memoId = memo.id) shouldBe listOf(keptPlace)
        }

        test("TC-MEMO-DETAIL-DATA-020 장소를 선택하면 연결이 선택 시점으로 저장되고 메모의 다른 값은 바뀌지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            val updatedAt = fixtureMonkey.giveMeOne<Instant>()

            accountMemoPlaceTransaction.upsert(accountId = accountId, memoId = memo.id, placeId = place.id, isDeleted = false, updatedAt = updatedAt)

            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(
                    MemoPlaceLocalEntity(
                        memoId = memo.id,
                        placeId = place.id,
                        isDeleted = false,
                        updatedAt = updatedAt,
                        createdAt = updatedAt,
                    ),
                )
            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
            getPlaceList(accountId = accountId, memoId = memo.id) shouldBe listOf(place)
        }

        suspend fun copyMemoWithPlace(
            accountId: Uuid,
            sourceId: Uuid,
            copy: MemoLocalEntity,
        ): Set<Uuid> {
            val sourcePlaceIdSet =
                dataSource
                    .findPlaceIdList(accountId = accountId, memoId = sourceId)
                    .toSet()
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(copy),
                memoTagList = emptyList(),
                memoPlaceList = sourcePlaceIdSet.map { placeId -> memoPlace(memoId = copy.id, placeId = placeId, memo = copy) },
            )
            return sourcePlaceIdSet
        }

        test("TC-MEMO-DETAIL-DATA-039 복사본에 원본의 해제되지 않은 장소 연결만 복사 시점으로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val source = memo()
            val keptPlaceList = List(2) { place() }
            val removedPlace = place()
            insertMemoWithPlaceList(accountId = accountId, memo = source, placeList = keptPlaceList + removedPlace)
            accountMemoPlaceTransaction.upsert(accountId = accountId, memoId = source.id, placeId = removedPlace.id, isDeleted = true, updatedAt = fixtureMonkey.giveMeOne<Instant>())
            val copiedAt = fixtureMonkey.giveMeOne<Instant>()
            val copy = memo().copy(updatedAt = copiedAt, createdAt = copiedAt)

            val sourcePlaceIdSet = copyMemoWithPlace(accountId = accountId, sourceId = source.id, copy = copy)

            sourcePlaceIdSet shouldBe keptPlaceList.map { place -> place.id }.toSet()
            findMemoPlaceList(memoId = copy.id) shouldContainExactlyInAnyOrder
                keptPlaceList.map { place ->
                    MemoPlaceLocalEntity(
                        memoId = copy.id,
                        placeId = place.id,
                        isDeleted = false,
                        updatedAt = copiedAt,
                        createdAt = copiedAt,
                    )
                }
        }

        test("TC-MEMO-DETAIL-DATA-040 삭제된 장소를 가리키는 원본 연결도 복사본에 만들어진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val source = memo()
            val deletedPlace = place()
            insertMemoWithPlaceList(accountId = accountId, memo = source, placeList = listOf(deletedPlace))
            placeTransaction.upsert(accountId = accountId, placeList = listOf(deletedPlace.copy(isDeleted = true)), placeTagList = emptyList())
            val copy = memo()

            val sourcePlaceIdSet = copyMemoWithPlace(accountId = accountId, sourceId = source.id, copy = copy)

            sourcePlaceIdSet shouldBe setOf(deletedPlace.id)
            findMemoPlaceList(memoId = copy.id) shouldBe
                listOf(memoPlace(memoId = copy.id, placeId = deletedPlace.id, memo = copy))
        }

        test("TC-MEMO-DETAIL-DATA-041 복사는 원본의 장소 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val source = memo()
            val place = place()
            insertMemoWithPlaceList(accountId = accountId, memo = source, placeList = listOf(place))
            val sourceMemoPlaceList = findMemoPlaceList(memoId = source.id)

            copyMemoWithPlace(accountId = accountId, sourceId = source.id, copy = memo())

            findMemoPlaceList(memoId = source.id) shouldBe sourceMemoPlaceList
            getPlaceList(accountId = accountId, memoId = source.id) shouldBe listOf(place)
        }

        test("TC-MEMO-PLACE-DOMAIN-010 연결된 장소는 제목 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val firstPlace = place().withTitle(title = "AAA")
            val secondPlace = place().withTitle(title = "BBB")
            val thirdPlace = place().withTitle(title = "CCC")
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(thirdPlace, firstPlace, secondPlace))

            getPlaceList(accountId = accountId, memoId = memo.id) shouldBe listOf(firstPlace, secondPlace, thirdPlace)
        }

        test("TC-MEMO-PLACE-DOMAIN-011 삭제된 장소는 메모의 연결된 장소 조회에서 빠진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptPlace = place()
            val deletedPlace = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(keptPlace, deletedPlace))

            placeTransaction.upsert(accountId = accountId, placeList = listOf(deletedPlace.copy(isDeleted = true)), placeTagList = emptyList())

            getPlaceList(accountId = accountId, memoId = memo.id) shouldBe listOf(keptPlace)
        }

        test("TC-MEMO-PLACE-DOMAIN-012 장소 연결은 장소 목록의 노출과 순서를 바꾸지 않는다") {
            listOf("AAA" to "BBB", "BBB" to "AAA").forEach { (linkedTitle, unlinkedTitle) ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo()
                val linkedPlace = place().withTitle(title = linkedTitle)
                val unlinkedPlace = place().withTitle(title = unlinkedTitle)
                insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(linkedPlace))
                placeTransaction.upsert(accountId = accountId, placeList = listOf(unlinkedPlace), placeTagList = emptyList())

                val placeList =
                    database
                        .accountPlaceDao()
                        .page(accountId = accountId, query = "", sort = "title")
                        .loadAll()

                placeList.map { place -> place.id } shouldBe listOf(linkedPlace, unlinkedPlace).sortedBy { place -> place.detail.title }.map { place -> place.id }
            }
        }

        test("TC-MEMO-PLACE-DOMAIN-013 연결된 장소의 제목은 메모 검색에 쓰이지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val query = "query-${fixtureMonkey.giveMeOne<Uuid>()}"
            val memo = memo().copy(isDeleted = false).withTitle(title = "memo-title")
            val memoWithDescription = memo.copy(detail = memo.detail.copy(description = "memo-description"))
            val place = place().withTitle(title = "place-$query")
            insertMemoWithPlaceList(accountId = accountId, memo = memoWithDescription, placeList = listOf(place))

            val memoList =
                database
                    .searchMemoDao()
                    .page(accountId = accountId, query = query, sort = "title")
                    .loadAll()

            memoList.shouldBeEmpty()
        }

        test("TC-MEMO-PLACE-DOMAIN-014 장소 연결은 태그로 메모를 조회한 결과를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(isFinished = false, isDeleted = false, primaryTagId = null)
            val place = place()
            val tag = fixtureMonkey.localTag(isFinished = false, isDeleted = false)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            placeTransaction.upsert(
                accountId = accountId,
                placeList = listOf(place),
                placeTagList =
                    listOf(
                        PlaceTagLocalEntity(
                            placeId = place.id,
                            tagId = tag.id,
                            isDeleted = false,
                            updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                            createdAt = fixtureMonkey.giveMeOne<Instant>(),
                        ),
                    ),
            )

            val memoList =
                database
                    .accountTagMemoDao()
                    .page(accountId = accountId, tagId = tag.id, scope = TagScopeLocalEntity.SELF.queryValue, sort = "title")
                    .loadAll()

            memoList.shouldBeEmpty()
        }

        test("TC-MEMO-PLACE-DOMAIN-015 필터를 고르지 않은 메모 목록에서 장소 연결은 노출과 순서를 바꾸지 않는다") {
            listOf("AAA" to "BBB", "BBB" to "AAA").forEach { (linkedTitle, unlinkedTitle) ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val linkedMemo = memo().copy(isFinished = false, isDeleted = false).withTitle(title = linkedTitle)
                val unlinkedMemo = memo().copy(isFinished = false, isDeleted = false).withTitle(title = unlinkedTitle)
                insertMemoWithPlaceList(accountId = accountId, memo = linkedMemo, placeList = listOf(place()))
                memoTransaction.upsert(accountId = accountId, memoList = listOf(unlinkedMemo), memoTagList = emptyList())

                val memoList =
                    database
                        .accountMemoDao()
                        .page(accountId = accountId, sort = "title")
                        .loadAll()

                memoList.map { memo -> memo.id } shouldBe listOf(linkedMemo, unlinkedMemo).sortedBy { memo -> memo.detail.title }.map { memo -> memo.id }
            }
        }

        test("TC-MEMO-PLACE-DOMAIN-016 장소 연결은 캘린더의 메모 노출을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val start = LocalDateTime(year = 2026, month = 9, day = 25, hour = 9, minute = 0)
            val endInclusive = LocalDateTime(year = 2026, month = 9, day = 26, hour = 9, minute = 0)
            val linkedMemo = memo().copy(isDeleted = false).withPeriod(start = start, endInclusive = endInclusive)
            val unlinkedMemo = memo().copy(isDeleted = false).withPeriod(start = start, endInclusive = endInclusive)
            insertMemoWithPlaceList(accountId = accountId, memo = linkedMemo, placeList = listOf(place()))
            memoTransaction.upsert(accountId = accountId, memoList = listOf(unlinkedMemo), memoTagList = emptyList())

            val calendarMemoList =
                database
                    .accountCalendarMemoDao()
                    .get(accountId = accountId, start = start.date, endInclusive = endInclusive.date)
                    .first()

            calendarMemoList.map { memo -> memo.id } shouldContainExactlyInAnyOrder listOf(linkedMemo.id, unlinkedMemo.id)
        }

        test("TC-MEMO-PLACE-DOMAIN-017 연결된 메모의 제목은 장소 검색에 쓰이지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val query = "query-${fixtureMonkey.giveMeOne<Uuid>()}"
            val memo = memo().copy(isDeleted = false).withTitle(title = "memo-$query")
            val place = place().let { value -> value.copy(detail = value.detail.copy(title = "place-title", description = "place-description", address = "place-address")) }
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))

            val placeList =
                database
                    .searchPlaceDao()
                    .page(accountId = accountId, query = query, sort = "title")
                    .loadAll()

            placeList.shouldBeEmpty()
        }

        test("TC-MEMO-PLACE-DOMAIN-018 메모 연결은 태그로 장소를 조회한 결과를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(isFinished = false, isDeleted = false, primaryTagId = null)
            val place = place()
            val tag = fixtureMonkey.localTag(isFinished = false, isDeleted = false)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = listOf(memoTag(memoId = memo.id, tagId = tag.id, memo = memo)),
                memoPlaceList = listOf(memoPlace(memoId = memo.id, placeId = place.id, memo = memo)),
            )

            val placeList =
                database
                    .accountTagPlaceDao()
                    .page(accountId = accountId, tagId = tag.id, scope = TagScopeLocalEntity.SELF.queryValue, sort = "title")
                    .loadAll()

            placeList.shouldBeEmpty()
        }

        listOf(
            "완료된" to { memo: MemoLocalEntity -> memo.copy(isFinished = true, isDeleted = false) },
            "삭제된" to { memo: MemoLocalEntity -> memo.copy(isFinished = false, isDeleted = true) },
        ).forEach { (label, change) ->
            test("TC-MEMO-DETAIL-DOMAIN-006 $label 메모도 장소 연결을 만들 수 있다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = change(memo())
                val place = place()
                placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
                memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

                accountMemoPlaceTransaction.upsert(accountId = accountId, memoId = memo.id, placeId = place.id, isDeleted = false, updatedAt = fixtureMonkey.giveMeOne<Instant>())

                getPlaceList(accountId = accountId, memoId = memo.id) shouldBe listOf(place)
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .sample()

        private fun place(): PlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceLocalEntity>()
                .setExp(PlaceLocalEntity::detail, placeDetail())
                .setExp(PlaceLocalEntity::isDeleted, false)
                .sample()

        private fun placeDetail(): PlaceDetailLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceDetailLocalEntity>()
                .setExp(PlaceDetailLocalEntity::latitude, fixtureMonkey.giveMeOne<Long>() % 90 + 0.5)
                .setExp(PlaceDetailLocalEntity::longitude, fixtureMonkey.giveMeOne<Long>() % 180 + 0.5)
                .sample()

        private fun PlaceLocalEntity.withTitle(title: String): PlaceLocalEntity = copy(detail = detail.copy(title = title))

        private fun MemoLocalEntity.withTitle(title: String): MemoLocalEntity = copy(detail = detail.copy(title = title))

        private fun MemoLocalEntity.withPeriod(
            start: LocalDateTime,
            endInclusive: LocalDateTime,
        ): MemoLocalEntity = copy(detail = detail.copy(isAllDay = false, start = start, endInclusive = endInclusive))

        private suspend fun <T : Any> PagingSource<Int, T>.loadAll(): List<T> =
            load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, T>>()
                .data

        private fun memoPlace(
            memoId: Uuid,
            placeId: Uuid,
            memo: MemoLocalEntity,
        ): MemoPlaceLocalEntity =
            MemoPlaceLocalEntity(
                memoId = memoId,
                placeId = placeId,
                isDeleted = false,
                updatedAt = memo.updatedAt,
                createdAt = memo.createdAt,
            )

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

        private fun <T> SQLiteStatement.readAll(read: (SQLiteStatement) -> T): List<T> =
            buildList {
                while (step()) {
                    add(read(this@readAll))
                }
            }

        private fun SQLiteStatement.toMemoPlace(): MemoPlaceLocalEntity =
            MemoPlaceLocalEntity(
                memoId = Uuid.parse(getText(0)),
                placeId = Uuid.parse(getText(1)),
                isDeleted = getBoolean(2),
                updatedAt = Instant.fromEpochMilliseconds(getLong(3)),
                createdAt = Instant.fromEpochMilliseconds(getLong(4)),
            )
    }
}
