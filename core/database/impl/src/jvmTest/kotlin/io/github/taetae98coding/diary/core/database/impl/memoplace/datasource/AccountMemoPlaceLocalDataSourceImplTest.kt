package io.github.taetae98coding.diary.core.database.impl.memoplace.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memoplace.transaction.AccountMemoPlaceSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceTransactionImpl
import io.github.taetae98coding.diary.core.testing.memo.memoLocalEntity
import io.github.taetae98coding.diary.core.testing.place.placeDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.place.placeLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.flow.first
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

        test("TC-9a99bb31-632c-406d-8388-9ea5832f4709 삭제된 장소는 연결이 남아 있어도 조회되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val place = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))

            placeTransaction.upsert(accountId = accountId, placeList = listOf(place.copy(isDeleted = true)), placeTagList = emptyList())

            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-0b17dba7-61ac-4cd2-8b6a-03bd8e7ae30b TC-8508844b-1be2-4047-981e-c6d7c0bffb8a 조회한 장소는 연결한 순서와 관계없이 제목 오름차순으로 정렬된다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val lastPlace = place(title = LAST_PLACE_TITLE)
            val firstPlace = place(title = FIRST_PLACE_TITLE)
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(lastPlace, firstPlace))

            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(firstPlace, lastPlace)
        }

        test("TC-b18406f5-53d9-4a5e-938d-44b9dcfba5cb 연결된 장소의 제목이 바뀌면 장소 카드의 조회 결과가 갱신된다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val place = place(title = FIRST_PLACE_TITLE)
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            val renamedPlace = place.copy(detail = place.detail.copy(title = LAST_PLACE_TITLE))

            placeTransaction.upsert(accountId = accountId, placeList = listOf(renamedPlace), placeTagList = emptyList())

            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(renamedPlace)
        }

        test("TC-aedb521d-bcdb-4b58-8d14-bb5d48ff2cda 가리키는 장소가 기기에 없는 연결은 저장되지만 조회에 나타나지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val missingPlaceId = Arb.kotlinUuid().next()
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoPlaceList = emptyList(),
            )

            syncTransaction.upsert(
                accountId = accountId,
                memoPlaceList = listOf(memoPlace(memoId = memo.id, placeId = missingPlaceId)),
                cursor = 1L,
            )

            database.memoPlaceDao().findByMemoIdList(listOf(memo.id)).size shouldBe 1
            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-aedb521d-bcdb-4b58-8d14-bb5d48ff2cda 가리키는 메모가 기기에 없는 연결은 저장되지만 조회에 나타나지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val missingMemoId = Arb.kotlinUuid().next()
            val place = place()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            syncTransaction.upsert(
                accountId = accountId,
                memoPlaceList = listOf(memoPlace(memoId = missingMemoId, placeId = place.id)),
                cursor = 1L,
            )

            database.memoPlaceDao().findByMemoIdList(listOf(missingMemoId)).size shouldBe 1
            dataSource.getPlaceList(accountId = accountId, memoId = missingMemoId).first().shouldBeEmpty()
        }

        test("TC-802c3ffe-e237-4fca-a2db-62c0f53b780e 메모가 내려받아지면 먼저 저장되어 있던 연결의 장소가 함께 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val place = place()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            syncTransaction.upsert(
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
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val memo = memo()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place()))

            dataSource.getPlaceList(accountId = otherAccountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-fb3a87f9-8ebe-4f14-93cd-d4b2b84d3e8c 계정과 연결되지 않은 장소는 메모의 연결된 장소로 조회되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
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

        test("TC-dadfb4a4-e539-451c-b0f7-e0dae73e9bbc 같은 태그와 연결되어 있어도 메모와 장소는 연결되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagId = Arb.kotlinUuid().next()
            val memo = memo()
            val place = place()
            placeTransaction.upsert(
                accountId = accountId,
                placeList = listOf(place),
                placeTagList =
                    listOf(
                        EntityTagLocalEntity(
                            entityId = place.id,
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

        test("TC-2cd98d83-4a47-406c-a124-dfe5265b5750 삭제된 장소의 삭제가 다른 기기에서 받은 내용으로 풀리면 다시 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val place = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place.copy(isDeleted = true)), placeTagList = emptyList())
            dataSource.getPlaceList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()

            AccountPlaceSyncTransactionImpl(database = database).upsert(
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

        private fun memo(): MemoLocalEntity =
            Arb
                .memoLocalEntity()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun place(title: String = "title-${Arb.string().next()}"): PlaceLocalEntity =
            Arb
                .placeLocalEntity()
                .next()
                .copy(
                    detail = placeDetail(title = title),
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun placeDetail(title: String): PlaceDetailLocalEntity =
            Arb
                .placeDetailLocalEntity()
                .next()
                .copy(
                    title = title,
                    latitude = Arb.long().next() % 90 + 0.5,
                    longitude = Arb.long().next() % 180 + 0.5,
                )

        private fun memoPlace(
            memoId: Uuid,
            placeId: Uuid,
        ): MemoPlaceLocalEntity =
            MemoPlaceLocalEntity(
                memoId = memoId,
                placeId = placeId,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
                createdAt = Arb.secondPrecisionInstant().next(),
            )
    }
}
