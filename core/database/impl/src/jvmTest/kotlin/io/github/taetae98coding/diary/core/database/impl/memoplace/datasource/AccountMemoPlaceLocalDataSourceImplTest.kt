package io.github.taetae98coding.diary.core.database.impl.memoplace.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memoplace.transaction.AccountMemoPlaceSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountMemoPlaceLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountMemoPlaceLocalDataSourceImpl
        lateinit var memoTransaction: AccountMemoTransactionImpl
        lateinit var placeTransaction: AccountPlaceTransactionImpl
        lateinit var syncTransaction: AccountMemoPlaceSyncTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountMemoPlaceLocalDataSourceImpl(database = database)
            memoTransaction = AccountMemoTransactionImpl(database = database)
            placeTransaction = AccountPlaceTransactionImpl(database = database)
            syncTransaction = AccountMemoPlaceSyncTransactionImpl(database = database)
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
                memoPlaceList =
                    placeList.map { place ->
                        MemoPlaceLocalEntity(
                            memoId = memo.id,
                            placeId = place.id,
                            isDeleted = false,
                            updatedAt = memo.updatedAt,
                            createdAt = memo.createdAt,
                        )
                    },
            )
        }

        test("TC-MEMO-DETAIL-DOMAIN-007 삭제된 장소는 연결이 남아 있어도 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))

            placeTransaction.upsert(accountId = accountId, placeList = listOf(place.copy(isDeleted = true)), placeTagList = emptyList())

            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-MEMO-DETAIL-DATA-049 TC-MEMO-PLACE-CARD-DOMAIN-029 조회한 장소는 연결한 순서와 관계없이 제목 오름차순으로 정렬된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val lastPlace = place(title = LAST_PLACE_TITLE)
            val firstPlace = place(title = FIRST_PLACE_TITLE)
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(lastPlace, firstPlace))

            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(firstPlace, lastPlace)
        }

        test("TC-MEMO-DETAIL-DATA-044 연결된 장소의 제목이 바뀌면 장소 카드의 조회 결과가 갱신된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place(title = FIRST_PLACE_TITLE)
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            val renamedPlace = place.copy(detail = place.detail.copy(title = LAST_PLACE_TITLE))

            placeTransaction.upsert(accountId = accountId, placeList = listOf(renamedPlace), placeTagList = emptyList())

            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(renamedPlace)
        }

        test("TC-MEMO-PLACE-DATA-009 가리키는 장소가 기기에 없는 연결은 저장되지만 조회에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val missingPlaceId = fixtureMonkey.giveMeOne<Uuid>()
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoPlaceList = emptyList(),
            )

            syncTransaction.save(
                accountId = accountId,
                memoPlaceList = listOf(memoPlace(memoId = memo.id, placeId = missingPlaceId)),
                cursor = 1L,
            )

            database.memoPlaceDao().findByMemoIdList(listOf(memo.id)).size shouldBe 1
            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-MEMO-PLACE-DATA-009 가리키는 메모가 기기에 없는 연결은 저장되지만 조회에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val missingMemoId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            syncTransaction.save(
                accountId = accountId,
                memoPlaceList = listOf(memoPlace(memoId = missingMemoId, placeId = place.id)),
                cursor = 1L,
            )

            database.memoPlaceDao().findByMemoIdList(listOf(missingMemoId)).size shouldBe 1
            dataSource.getPlaceList(accountId = accountId, memoId = missingMemoId).first().shouldBeEmpty()
        }

        test("TC-MEMO-PLACE-DATA-012 메모가 내려받아지면 먼저 저장되어 있던 연결의 장소가 함께 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            syncTransaction.save(
                accountId = accountId,
                memoPlaceList = listOf(memoPlace(memoId = memo.id, placeId = place.id)),
                cursor = 1L,
            )
            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()

            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoPlaceList = emptyList(),
            )

            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(place)
        }

        test("다른 계정의 장소는 연결이 있어도 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place()))

            dataSource.getPlaceList(accountId = otherAccountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-MEMO-PLACE-DOMAIN-019 계정과 연결되지 않은 장소는 메모의 연결된 장소로 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val otherAccountPlace = place()
            placeTransaction.upsert(accountId = otherAccountId, placeList = listOf(otherAccountPlace), placeTagList = emptyList())

            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoPlaceList = listOf(memoPlace(memoId = memo.id, placeId = otherAccountPlace.id)),
            )

            database.memoPlaceDao().findByMemoIdList(listOf(memo.id)).size shouldBe 1
            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-MEMO-PLACE-DOMAIN-020 같은 태그와 연결되어 있어도 메모와 장소는 연결되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            placeTransaction.upsert(
                accountId = accountId,
                placeList = listOf(place),
                placeTagList =
                    listOf(
                        PlaceTagLocalEntity(
                            placeId = place.id,
                            tagId = tagId,
                            isDeleted = false,
                            updatedAt = place.updatedAt,
                            createdAt = place.createdAt,
                        ),
                    ),
            )

            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList =
                    listOf(
                        MemoTagLocalEntity(
                            memoId = memo.id,
                            tagId = tagId,
                            isDeleted = false,
                            updatedAt = memo.updatedAt,
                            createdAt = memo.createdAt,
                        ),
                    ),
            )

            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-MEMO-PLACE-DOMAIN-021 삭제된 장소의 삭제가 다른 기기에서 받은 내용으로 풀리면 다시 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place.copy(isDeleted = true)), placeTagList = emptyList())
            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()

            AccountPlaceSyncTransactionImpl(database = database).save(
                accountId = accountId,
                placeList = listOf(place.copy(isDeleted = false)),
                cursor = 1L,
            )

            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(place)
        }
    }) {
    public companion object {
        private const val FIRST_PLACE_TITLE = "ApplePlace"
        private const val LAST_PLACE_TITLE = "ZebraPlace"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::updatedAt, instant())
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()

        private fun place(title: String = "title-${fixtureMonkey.giveMeOne<String>()}"): PlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceLocalEntity>()
                .setExp(PlaceLocalEntity::detail, placeDetail(title = title))
                .setExp(PlaceLocalEntity::isDeleted, false)
                .setExp(PlaceLocalEntity::updatedAt, instant())
                .setExp(PlaceLocalEntity::createdAt, instant())
                .sample()

        private fun placeDetail(title: String): PlaceDetailLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceDetailLocalEntity>()
                .setExp(PlaceDetailLocalEntity::title, title)
                .setExp(PlaceDetailLocalEntity::latitude, fixtureMonkey.giveMeOne<Long>() % 90 + 0.5)
                .setExp(PlaceDetailLocalEntity::longitude, fixtureMonkey.giveMeOne<Long>() % 180 + 0.5)
                .sample()

        private fun memoPlace(
            memoId: Uuid,
            placeId: Uuid,
        ): MemoPlaceLocalEntity =
            MemoPlaceLocalEntity(
                memoId = memoId,
                placeId = placeId,
                isDeleted = false,
                updatedAt = instant(),
                createdAt = instant(),
            )

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
