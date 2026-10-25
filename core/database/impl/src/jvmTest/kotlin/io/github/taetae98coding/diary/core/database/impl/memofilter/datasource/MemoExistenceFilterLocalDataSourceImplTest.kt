package io.github.taetae98coding.diary.core.database.impl.memofilter.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import java.io.File
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class MemoExistenceFilterLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: MemoExistenceFilterLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = MemoExistenceFilterLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-c5680cae-322b-4917-86c4-31904d3eb10b 저장된 유무 필터가 없으면 조회 결과가 없다") {
            dataSource.find().first().shouldBeNull()
        }

        test("TC-76fa8f4d-5f72-4699-8501-75a9d2891d5d 한 축을 바꿔도 다른 축의 상태는 그대로 유지된다") {
            dataSource.upsertHasDate(hasDate = true)
            dataSource.upsertHasTag(hasTag = false)

            dataSource.upsertHasPlace(hasPlace = false)

            dataSource.find().first() shouldBe
                MemoExistenceFilterLocalEntity(
                    hasDate = true,
                    hasTag = false,
                    hasPlace = false,
                )
        }

        test("TC-e1809211-a095-4491-8368-160d00b7b157 축을 전체로 되돌리면 그 축만 비운다") {
            dataSource.upsertHasDate(hasDate = true)
            dataSource.upsertHasTag(hasTag = true)

            dataSource.upsertHasDate(hasDate = null)

            dataSource.find().first() shouldBe
                MemoExistenceFilterLocalEntity(
                    hasDate = null,
                    hasTag = true,
                    hasPlace = null,
                )
        }

        test("TC-482234a7-ebb5-48b8-8351-7ceed4987334 저장된 유무 필터 상태는 새로 조회를 시작해도 유지된다") {
            dataSource.upsertHasPlace(hasPlace = true)

            dataSource.find().first()?.hasPlace shouldBe true
            dataSource.find().first()?.hasPlace shouldBe true
        }

        test("TC-1bc331a6-2db6-46f3-a08f-2688a64ecf56 데이터베이스를 닫고 다시 열어도 유무 필터 상태를 유지한다") {
            val file = File.createTempFile("memo-existence-filter", ".db")

            try {
                val firstDatabase =
                    Room
                        .databaseBuilder<DiaryDatabase>(name = file.absolutePath)
                        .setDriver(BundledSQLiteDriver())
                        .build()
                MemoExistenceFilterLocalDataSourceImpl(database = firstDatabase).upsertHasTag(hasTag = false)
                firstDatabase.close()

                val secondDatabase =
                    Room
                        .databaseBuilder<DiaryDatabase>(name = file.absolutePath)
                        .setDriver(BundledSQLiteDriver())
                        .build()

                try {
                    MemoExistenceFilterLocalDataSourceImpl(database = secondDatabase)
                        .find()
                        .first()
                        ?.hasTag shouldBe false
                } finally {
                    secondDatabase.close()
                }
            } finally {
                file.delete()
            }
        }

        test("TC-20d8a16c-4b36-4fbe-9b46-be1607c6e8df 유무 필터 상태 변경은 동기화 업로드 대기 목록에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            dataSource.upsertHasDate(hasDate = true)
            dataSource.upsertHasTag(hasTag = false)

            database.accountMemoSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountTagSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountMemoTagSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountPlaceSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountMemoPlaceSyncDao().findPending(accountId = accountId).shouldBeEmpty()
        }

        test("여러 번 저장해도 유무 필터 행은 하나만 유지된다") {
            dataSource.upsertHasDate(hasDate = true)
            dataSource.upsertHasTag(hasTag = true)
            dataSource.upsertHasPlace(hasPlace = true)

            dataSource.find().first() shouldBe
                MemoExistenceFilterLocalEntity(
                    hasDate = true,
                    hasTag = true,
                    hasPlace = true,
                )
        }
    })
