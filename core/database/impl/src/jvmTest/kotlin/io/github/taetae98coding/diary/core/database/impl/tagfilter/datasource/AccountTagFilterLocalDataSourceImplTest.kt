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

        test("TC-TAG-HOME-DATA-009 저장된 필터 선택이 없으면 조회 결과가 없다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            dataSource.find(accountId = accountId).first().shouldBeNull()
        }

        test("TC-TAG-HOME-DATA-008 저장한 필터 선택을 다시 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            dataSource.upsert(accountId = accountId, isTopLevelOnly = true)

            dataSource.find(accountId = accountId).first() shouldBe
                TagFilterLocalEntity(
                    accountId = accountId,
                    isTopLevelOnly = true,
                )
        }

        test("TC-TAG-HOME-DATA-008 필터 선택을 다시 바꾸면 마지막 선택만 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            dataSource.upsert(accountId = accountId, isTopLevelOnly = true)
            dataSource.upsert(accountId = accountId, isTopLevelOnly = false)

            dataSource.find(accountId = accountId).first() shouldBe
                TagFilterLocalEntity(
                    accountId = accountId,
                    isTopLevelOnly = false,
                )
        }

        test("TC-TAG-HOME-DATA-010 한 계정의 필터 선택은 다른 계정에 적용되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()

            dataSource.upsert(accountId = accountId, isTopLevelOnly = true)

            dataSource.find(accountId = accountId).first()?.isTopLevelOnly shouldBe true
            dataSource.find(accountId = otherAccountId).first().shouldBeNull()
        }

        test("TC-TAG-HOME-DATA-013 강제 전체 재동기화로 계정 데이터를 지우면 그 계정의 필터 선택은 꺼진 상태로 돌아간다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            dataSource.upsert(accountId = accountId, isTopLevelOnly = true)
            dataSource.upsert(accountId = otherAccountId, isTopLevelOnly = true)

            AccountDataTransactionImpl(database = database).delete(accountId = accountId)

            dataSource.find(accountId = accountId).first().shouldBeNull()
            dataSource.find(accountId = otherAccountId).first()?.isTopLevelOnly shouldBe true
        }

        test("TC-TAG-HOME-DATA-008 데이터베이스를 닫고 다시 열어도 필터 선택을 유지한다") {
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

        test("TC-TAG-HOME-DATA-011 필터 선택은 동기화 업로드 대기 목록에 나타나지 않는다") {
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
