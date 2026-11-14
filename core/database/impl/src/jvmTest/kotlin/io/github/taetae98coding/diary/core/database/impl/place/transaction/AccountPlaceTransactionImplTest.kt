package io.github.taetae98coding.diary.core.database.impl.place.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memoplace.transaction.AccountMemoPlaceTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.testing.place.placeDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.place.placeLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.every
import io.mockk.spyk
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountPlaceTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountPlaceTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountPlaceTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun findPlaceList(): List<PlaceLocalEntity> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT id, title, description, color, latitude, longitude, address, is_deleted, updated_at, created_at
                    FROM place
                    ORDER BY id ASC
                    """,
                ) { statement -> statement.readAll { it.toPlace() } }
            }

        suspend fun findAccountPlaceList(): List<AccountPlaceLocalEntity> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT account_id, place_id, is_dirty
                    FROM account_place
                    ORDER BY place_id ASC
                    """,
                ) { statement -> statement.readAll { it.toAccountPlace() } }
            }

        suspend fun findPlaceTagList(): List<Triple<Uuid, Uuid, Boolean>> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT place_tag.place_id, place_tag.tag_id, place_tag.is_deleted
                    FROM place_tag
                    INNER JOIN account_place_tag
                        ON account_place_tag.place_id = place_tag.place_id
                        AND account_place_tag.tag_id = place_tag.tag_id
                    """,
                ) { statement -> statement.readAll { it.toLink() } }
            }

        suspend fun findMemoPlaceList(): List<Triple<Uuid, Uuid, Boolean>> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT memo_place.memo_id, memo_place.place_id, memo_place.is_deleted
                    FROM memo_place
                    INNER JOIN account_memo_place
                        ON account_memo_place.memo_id = memo_place.memo_id
                        AND account_memo_place.place_id = memo_place.place_id
                    """,
                ) { statement -> statement.readAll { it.toLink() } }
            }

        suspend fun linkMemo(
            accountId: Uuid,
            placeId: Uuid,
        ): Uuid {
            val memoId = Arb.kotlinUuid().next()

            AccountMemoPlaceTransactionImpl(database = database).upsert(
                accountId = accountId,
                memoId = memoId,
                placeId = placeId,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )

            return memoId
        }

        test("TC-71726817-3873-40c5-8081-8856b2290f1c 장소와 현재 계정의 연결을 함께 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val place = place()

            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            findPlaceList() shouldBe listOf(place)
            findAccountPlaceList() shouldBe
                listOf(
                    AccountPlaceLocalEntity(
                        accountId = accountId,
                        placeId = place.id,
                        isDirty = true,
                    ),
                )
        }

        test("TC-a1f67716-6619-4c28-bb0b-db49aee35c12 태그를 고른 장소 추가는 장소와 계정 연결, 태그 연결을 함께 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val place = place()
            val tagIdList = List(2) { Arb.kotlinUuid().next() }

            transaction.upsert(
                accountId = accountId,
                placeList = listOf(place),
                placeTagList =
                    tagIdList.map { tagId ->
                        EntityTagLocalEntity(
                            entityId = place.id,
                            tagId = tagId,
                            isDeleted = false,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                            createdAt = Arb.secondPrecisionInstant().next(),
                        )
                    },
            )

            findPlaceList() shouldBe listOf(place)
            findAccountPlaceList() shouldBe listOf(AccountPlaceLocalEntity(accountId = accountId, placeId = place.id, isDirty = true))
            findPlaceTagList() shouldContainExactlyInAnyOrder tagIdList.map { tagId -> Triple(place.id, tagId, false) }
        }

        test("TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 장소 추가·수정·삭제·실행 취소는 업로드 대기 상태가 된다") {
            val accountId = Arb.kotlinUuid().next()
            val place = place().copy(isDeleted = false)
            val pending = listOf(AccountPlaceLocalEntity(accountId = accountId, placeId = place.id, isDirty = true))

            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            findAccountPlaceList() shouldBe pending

            database.accountPlaceDao().upsert(AccountPlaceLocalEntity(accountId = accountId, placeId = place.id, isDirty = false))
            transaction.updateDetail(accountId = accountId, placeId = place.id, detail = placeDetail(), updatedAt = Arb.secondPrecisionInstant().next())
            findAccountPlaceList() shouldBe pending

            database.accountPlaceDao().upsert(AccountPlaceLocalEntity(accountId = accountId, placeId = place.id, isDirty = false))
            transaction.updateDeleted(accountId = accountId, placeId = place.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
            findAccountPlaceList() shouldBe pending

            database.accountPlaceDao().upsert(AccountPlaceLocalEntity(accountId = accountId, placeId = place.id, isDirty = false))
            transaction.updateDeleted(accountId = accountId, placeId = place.id, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next())
            findAccountPlaceList() shouldBe pending
        }

        test("TC-064634f6-5b7f-472c-86b4-753a09051180 TC-5a548f34-e80b-480e-a5d6-4d14e63aea09 제목, 설명, 컬러, 좌표와 미삭제 상태, 추가 시각을 그대로 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val now = Arb.secondPrecisionInstant().next()
            val place =
                place().copy(
                    detail =
                        PlaceDetailLocalEntity(
                            title = "title-${Arb.string().next()}",
                            description = "description-${Arb.string().next()}",
                            color = Arb.long().next(),
                            latitude = 37.5,
                            longitude = 127.0,
                            address = "address-${Arb.string().next()}",
                        ),
                    isDeleted = false,
                    updatedAt = now,
                    createdAt = now,
                )

            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            findPlaceList() shouldBe listOf(place)
        }

        test("같은 식별자의 장소를 다시 저장하면 덮어쓴다") {
            val accountId = Arb.kotlinUuid().next()
            val place = place()
            val changedPlace = place.copy(detail = placeDetail())

            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            transaction.upsert(accountId = accountId, placeList = listOf(changedPlace), placeTagList = emptyList())

            findPlaceList() shouldBe listOf(changedPlace)
            findAccountPlaceList() shouldBe
                listOf(
                    AccountPlaceLocalEntity(
                        accountId = accountId,
                        placeId = place.id,
                        isDirty = true,
                    ),
                )
        }

        test("서로 다른 식별자의 장소는 각각 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val placeList = List(3) { place() }.sortedBy { place -> place.id.toString() }

            placeList.forEach { place ->
                transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            }

            findPlaceList() shouldBe placeList
        }

        test("TC-cef7c2d0-5c96-4770-98e7-0b394dce8b7c 수정은 제목, 설명, 컬러, 좌표와 수정 시각만 바꾼다") {
            val accountId = Arb.kotlinUuid().next()
            val place = place().copy(isDeleted = false)
            val newDetail = placeDetail()
            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            val count =
                transaction.updateDetail(
                    accountId = accountId,
                    placeId = place.id,
                    detail = newDetail,
                    updatedAt = updatedAt,
                )

            count shouldBe 1
            findPlaceList() shouldBe
                listOf(
                    place.copy(
                        detail = newDetail,
                        updatedAt = updatedAt,
                    ),
                )
            findAccountPlaceList() shouldBe
                listOf(
                    AccountPlaceLocalEntity(
                        accountId = accountId,
                        placeId = place.id,
                        isDirty = true,
                    ),
                )
        }

        test("TC-a51d0a4a-f218-4b3d-99ce-f8da1f06eec6 수정은 같은 장소를 덮어쓰고 계정 연결과 메모 연결을 남긴다") {
            val accountId = Arb.kotlinUuid().next()
            val place = place().copy(isDeleted = false)
            val newDetail = placeDetail()
            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            val memoId = linkMemo(accountId = accountId, placeId = place.id)

            transaction.updateDetail(accountId = accountId, placeId = place.id, detail = newDetail, updatedAt = updatedAt)

            findPlaceList() shouldBe listOf(place.copy(detail = newDetail, updatedAt = updatedAt))
            findAccountPlaceList() shouldBe listOf(AccountPlaceLocalEntity(accountId = accountId, placeId = place.id, isDirty = true))
            findMemoPlaceList() shouldBe listOf(Triple(memoId, place.id, false))
        }

        test("TC-dbdf8a25-1a6d-46a1-82d9-12a47a841bb1 삭제 상태가 된 장소를 수정해도 삭제 상태로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val place = place().copy(isDeleted = true)
            val newDetail = placeDetail()
            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            val count =
                transaction.updateDetail(
                    accountId = accountId,
                    placeId = place.id,
                    detail = newDetail,
                    updatedAt = updatedAt,
                )

            count shouldBe 1
            findPlaceList() shouldBe
                listOf(
                    place.copy(
                        detail = newDetail,
                        isDeleted = true,
                        updatedAt = updatedAt,
                    ),
                )
        }

        test("TC-76611214-e640-4cba-a148-fcff540c273f 삭제는 삭제 여부와 수정 시각만 바꾼다") {
            val accountId = Arb.kotlinUuid().next()
            val place = place().copy(isDeleted = false)
            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            val count =
                transaction.updateDeleted(
                    accountId = accountId,
                    placeId = place.id,
                    isDeleted = true,
                    updatedAt = updatedAt,
                )

            count shouldBe 1
            findPlaceList() shouldBe
                listOf(
                    place.copy(
                        isDeleted = true,
                        updatedAt = updatedAt,
                    ),
                )
        }

        test("TC-31a0031d-a93b-4967-83b6-3c4c11abcdad 삭제는 저장된 주소를 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val address = "address-${Arb.string().next()}"
            val place = place().copy(isDeleted = false)
            val storedPlace = place.copy(detail = place.detail.copy(address = address))
            transaction.upsert(accountId = accountId, placeList = listOf(storedPlace), placeTagList = emptyList())

            transaction.updateDeleted(
                accountId = accountId,
                placeId = storedPlace.id,
                isDeleted = true,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )

            findPlaceList().single().detail.address shouldBe address
        }

        test("TC-454e7312-d0dd-4603-a7b6-00a92504c393 삭제해도 장소와 계정 연결, 메모 연결을 저장소에서 제거하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val place = place().copy(isDeleted = false)
            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            val memoId = linkMemo(accountId = accountId, placeId = place.id)

            transaction.updateDeleted(
                accountId = accountId,
                placeId = place.id,
                isDeleted = true,
                updatedAt = updatedAt,
            )

            findPlaceList() shouldBe listOf(place.copy(isDeleted = true, updatedAt = updatedAt))
            findAccountPlaceList() shouldBe
                listOf(
                    AccountPlaceLocalEntity(
                        accountId = accountId,
                        placeId = place.id,
                        isDirty = true,
                    ),
                )
            findMemoPlaceList() shouldBe listOf(Triple(memoId, place.id, false))
        }

        test("다른 계정의 장소는 수정하거나 삭제하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val place = place().copy(isDeleted = false)
            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            val updateCount =
                transaction.updateDetail(
                    accountId = otherAccountId,
                    placeId = place.id,
                    detail = placeDetail(),
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            val deleteCount =
                transaction.updateDeleted(
                    accountId = otherAccountId,
                    placeId = place.id,
                    isDeleted = true,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )

            updateCount shouldBe 0
            deleteCount shouldBe 0
            findPlaceList() shouldBe listOf(place)
        }

        test("저장되지 않은 식별자를 수정하거나 삭제하면 아무것도 바뀌지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val place = place().copy(isDeleted = false)
            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            val updateCount =
                transaction.updateDetail(
                    accountId = accountId,
                    placeId = Arb.kotlinUuid().next(),
                    detail = placeDetail(),
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            val deleteCount =
                transaction.updateDeleted(
                    accountId = accountId,
                    placeId = Arb.kotlinUuid().next(),
                    isDeleted = true,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )

            updateCount shouldBe 0
            deleteCount shouldBe 0
            findPlaceList() shouldBe listOf(place)
        }

        test("TC-6d7ec354-4dd3-4336-9f46-42ddff1469ee 수정이나 삭제의 저장에 실패하면 장소의 내용과 계정 연결이 그대로 남는다") {
            val actionList: List<suspend (AccountPlaceTransactionImpl, Uuid, Uuid) -> Unit> =
                listOf(
                    { failingTransaction, accountId, placeId ->
                        failingTransaction.updateDetail(
                            accountId = accountId,
                            placeId = placeId,
                            detail = placeDetail(),
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        )
                    },
                    { failingTransaction, accountId, placeId ->
                        failingTransaction.updateDeleted(
                            accountId = accountId,
                            placeId = placeId,
                            isDeleted = true,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        )
                    },
                )

            actionList.forEach { action ->
                val accountId = Arb.kotlinUuid().next()
                val place = place().copy(isDeleted = false)
                transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
                val beforePlaceList = findPlaceList()
                val beforeAccountPlaceList = findAccountPlaceList()
                val throwable = IllegalStateException(Arb.string().next())
                val failingDao = spyk(database.accountPlaceDao())
                coEvery { failingDao.markPending(accountId = any(), placeId = any()) } throws throwable
                val failingDatabase = spyk(database)
                every { failingDatabase.accountPlaceDao() } returns failingDao
                val failingTransaction = AccountPlaceTransactionImpl(database = failingDatabase)

                shouldThrow<IllegalStateException> {
                    action(failingTransaction, accountId, place.id)
                }.message shouldBe throwable.message

                findPlaceList() shouldBe beforePlaceList
                findAccountPlaceList() shouldBe beforeAccountPlaceList
            }
        }
    }) {
    public companion object {
        private fun place(): PlaceLocalEntity =
            Arb
                .placeLocalEntity()
                .next()
                .copy(
                    detail = placeDetail(),
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun placeDetail(): PlaceDetailLocalEntity =
            Arb
                .placeDetailLocalEntity()
                .next()
                .copy(
                    latitude = Arb.long().next() % 90 + 0.5,
                    longitude = Arb.long().next() % 180 + 0.5,
                )

        private fun <T> SQLiteStatement.readAll(read: (SQLiteStatement) -> T): List<T> =
            buildList {
                while (step()) {
                    add(read(this@readAll))
                }
            }

        private fun SQLiteStatement.toPlace(): PlaceLocalEntity =
            PlaceLocalEntity(
                id = Uuid.parse(getText(0)),
                detail =
                    PlaceDetailLocalEntity(
                        title = getText(1),
                        description = getText(2),
                        color = getLong(3),
                        latitude = getDouble(4),
                        longitude = getDouble(5),
                        address = getText(6),
                    ),
                isDeleted = getBoolean(7),
                updatedAt = Instant.fromEpochMilliseconds(getLong(8)),
                createdAt = Instant.fromEpochMilliseconds(getLong(9)),
            )

        private fun SQLiteStatement.toLink(): Triple<Uuid, Uuid, Boolean> =
            Triple(
                Uuid.parse(getText(0)),
                Uuid.parse(getText(1)),
                getBoolean(2),
            )

        private fun SQLiteStatement.toAccountPlace(): AccountPlaceLocalEntity =
            AccountPlaceLocalEntity(
                accountId = Uuid.parse(getText(0)),
                placeId = Uuid.parse(getText(1)),
                isDirty = getBoolean(2),
            )
    }
}
