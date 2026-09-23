package io.github.taetae98coding.diary.core.database.impl.place.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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

        test("TC-PLACE-ADD-DATA-001 장소와 현재 계정의 연결을 함께 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-PLACE-ADD-DATA-002 TC-PLACE-ADD-DATA-003 제목, 설명, 컬러, 좌표와 미삭제 상태, 추가 시각을 그대로 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val now = instant()
            val place =
                place().copy(
                    detail =
                        PlaceDetailLocalEntity(
                            title = "title-${fixtureMonkey.giveMeOne<String>()}",
                            description = "description-${fixtureMonkey.giveMeOne<String>()}",
                            color = fixtureMonkey.giveMeOne<Long>(),
                            latitude = 37.5,
                            longitude = 127.0,
                            address = "address-${fixtureMonkey.giveMeOne<String>()}",
                        ),
                    isDeleted = false,
                    updatedAt = now,
                    createdAt = now,
                )

            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            findPlaceList() shouldBe listOf(place)
        }

        test("같은 식별자의 장소를 다시 저장하면 덮어쓴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val placeList = List(3) { place() }.sortedBy { place -> place.id.toString() }

            placeList.forEach { place ->
                transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            }

            findPlaceList() shouldBe placeList
        }

        test("TC-PLACE-DETAIL-DOMAIN-004 수정은 제목, 설명, 컬러, 좌표와 수정 시각만 바꾼다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place().copy(isDeleted = false)
            val newDetail = placeDetail()
            val updatedAt = instant()
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

        test("TC-PLACE-DETAIL-DOMAIN-017 삭제 상태가 된 장소를 수정해도 삭제 상태로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place().copy(isDeleted = true)
            val newDetail = placeDetail()
            val updatedAt = instant()
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

        test("TC-PLACE-DETAIL-DOMAIN-010 삭제는 삭제 여부와 수정 시각만 바꾼다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place().copy(isDeleted = false)
            val updatedAt = instant()
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

        test("TC-PLACE-DETAIL-DOMAIN-019 삭제는 저장된 주소를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val address = "address-${fixtureMonkey.giveMeOne<String>()}"
            val place = place().copy(isDeleted = false)
            val storedPlace = place.copy(detail = place.detail.copy(address = address))
            transaction.upsert(accountId = accountId, placeList = listOf(storedPlace), placeTagList = emptyList())

            transaction.updateDeleted(
                accountId = accountId,
                placeId = storedPlace.id,
                isDeleted = true,
                updatedAt = instant(),
            )

            findPlaceList().single().detail.address shouldBe address
        }

        test("TC-PLACE-DETAIL-DATA-004 삭제해도 장소와 계정 연결을 저장소에서 제거하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place().copy(isDeleted = false)
            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            transaction.updateDeleted(
                accountId = accountId,
                placeId = place.id,
                isDeleted = true,
                updatedAt = instant(),
            )

            findPlaceList().size shouldBe 1
            findAccountPlaceList() shouldBe
                listOf(
                    AccountPlaceLocalEntity(
                        accountId = accountId,
                        placeId = place.id,
                        isDirty = true,
                    ),
                )
        }

        test("다른 계정의 장소는 수정하거나 삭제하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place().copy(isDeleted = false)
            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            val updateCount =
                transaction.updateDetail(
                    accountId = otherAccountId,
                    placeId = place.id,
                    detail = placeDetail(),
                    updatedAt = instant(),
                )
            val deleteCount =
                transaction.updateDeleted(
                    accountId = otherAccountId,
                    placeId = place.id,
                    isDeleted = true,
                    updatedAt = instant(),
                )

            updateCount shouldBe 0
            deleteCount shouldBe 0
            findPlaceList() shouldBe listOf(place)
        }

        test("저장되지 않은 식별자를 수정하거나 삭제하면 아무것도 바뀌지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place().copy(isDeleted = false)
            transaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            val updateCount =
                transaction.updateDetail(
                    accountId = accountId,
                    placeId = fixtureMonkey.giveMeOne<Uuid>(),
                    detail = placeDetail(),
                    updatedAt = instant(),
                )
            val deleteCount =
                transaction.updateDeleted(
                    accountId = accountId,
                    placeId = fixtureMonkey.giveMeOne<Uuid>(),
                    isDeleted = true,
                    updatedAt = instant(),
                )

            updateCount shouldBe 0
            deleteCount shouldBe 0
            findPlaceList() shouldBe listOf(place)
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun place(): PlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceLocalEntity>()
                .setExp(PlaceLocalEntity::detail, placeDetail())
                .setExp(PlaceLocalEntity::updatedAt, instant())
                .setExp(PlaceLocalEntity::createdAt, instant())
                .sample()

        private fun placeDetail(): PlaceDetailLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceDetailLocalEntity>()
                .setExp(PlaceDetailLocalEntity::latitude, fixtureMonkey.giveMeOne<Long>() % 90 + 0.5)
                .setExp(PlaceDetailLocalEntity::longitude, fixtureMonkey.giveMeOne<Long>() % 180 + 0.5)
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

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

        private fun SQLiteStatement.toAccountPlace(): AccountPlaceLocalEntity =
            AccountPlaceLocalEntity(
                accountId = Uuid.parse(getText(0)),
                placeId = Uuid.parse(getText(1)),
                isDirty = getBoolean(2),
            )
    }
}
