package io.github.taetae98coding.diary.core.database.impl.tagfilter.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.tagfilter.entity.TagFilterLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.AccountDataTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import java.io.File
import kotlin.uuid.Uuid

class AccountTagFilterLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountTagFilterLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountTagFilterLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-c3277845-4c50-4e2e-ac84-24ad2a01a4fa 저장된 필터 선택이 없으면 조회 결과가 없다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            dataSource.find(accountId = accountId).first().shouldBeNull()
        }

        test("TC-07ef4c1b-2b17-4fa8-a31b-5271c930eca6 저장한 필터 선택을 다시 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            dataSource.upsert(accountId = accountId, isTopLevelOnly = true)

            dataSource.find(accountId = accountId).first() shouldBe
                TagFilterLocalEntity(
                    accountId = accountId,
                    isTopLevelOnly = true,
                )
        }

        test("TC-07ef4c1b-2b17-4fa8-a31b-5271c930eca6 필터 선택을 다시 바꾸면 마지막 선택만 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            dataSource.upsert(accountId = accountId, isTopLevelOnly = true)
            dataSource.upsert(accountId = accountId, isTopLevelOnly = false)

            dataSource.find(accountId = accountId).first() shouldBe
                TagFilterLocalEntity(
                    accountId = accountId,
                    isTopLevelOnly = false,
                )
        }

        test("TC-c470f606-63db-4b25-93a9-1706d28e3945 한 계정의 필터 선택은 다른 계정에 적용되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()

            dataSource.upsert(accountId = accountId, isTopLevelOnly = true)

            dataSource.find(accountId = accountId).first()?.isTopLevelOnly shouldBe true
            dataSource.find(accountId = otherAccountId).first().shouldBeNull()
        }

        test("TC-bbf536b5-7c52-4b50-88e8-c5c428f8506c 강제 전체 재동기화로 계정 데이터를 지우면 그 계정의 필터 선택은 꺼진 상태로 돌아간다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            dataSource.upsert(accountId = accountId, isTopLevelOnly = true)
            dataSource.upsert(accountId = otherAccountId, isTopLevelOnly = true)

            AccountDataTransactionImpl(database = database).delete(accountId = accountId)

            dataSource.find(accountId = accountId).first().shouldBeNull()
            dataSource.find(accountId = otherAccountId).first()?.isTopLevelOnly shouldBe true
        }

        test("TC-07ef4c1b-2b17-4fa8-a31b-5271c930eca6 데이터베이스를 닫고 다시 열어도 필터 선택을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val file = File.createTempFile("tag-filter", ".db")

            try {
                val firstDatabase =
                    Room
                        .databaseBuilder<DiaryDatabase>(name = file.absolutePath)
                        .setDriver(BundledSQLiteDriver())
                        .build()
                AccountTagFilterLocalDataSourceImpl(database = firstDatabase).upsert(accountId = accountId, isTopLevelOnly = true)
                firstDatabase.close()

                val secondDatabase =
                    Room
                        .databaseBuilder<DiaryDatabase>(name = file.absolutePath)
                        .setDriver(BundledSQLiteDriver())
                        .build()

                try {
                    AccountTagFilterLocalDataSourceImpl(database = secondDatabase)
                        .find(accountId = accountId)
                        .first()
                        ?.isTopLevelOnly shouldBe true
                } finally {
                    secondDatabase.close()
                }
            } finally {
                file.delete()
            }
        }

        test("TC-0735acf8-97de-43a5-b146-d9a3134454a8 필터 선택은 동기화 업로드 대기 목록에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            dataSource.upsert(accountId = accountId, isTopLevelOnly = true)

            database.accountTagSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountMemoSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountMemoTagSyncDao().findPending(accountId = accountId).shouldBeEmpty()
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
