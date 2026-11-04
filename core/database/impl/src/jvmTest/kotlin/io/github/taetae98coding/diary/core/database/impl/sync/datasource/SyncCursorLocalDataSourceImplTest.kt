package io.github.taetae98coding.diary.core.database.impl.sync.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import kotlin.uuid.Uuid

class SyncCursorLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        SyncKindLocalEntity.entries.forEach { kind ->
            test("TC-cce8783a-21bb-4316-bdb7-74d4d2fe8853 ${kind.name}의 기록된 순번이 없으면 가장 작은 내려받기 위치를 돌려준다") {
                val accountId = Arb.kotlinUuid().next()

                dataSource.read(accountId = accountId, kind = kind) shouldBe SyncCursorLocalDataSource.DEFAULT_USN
            }

            test("${kind.name}는 저장 값으로 기록된 순번을 읽는다") {
                val accountId = Arb.kotlinUuid().next()
                val usn = Arb.long().next()
                database.syncCursorDao().upsert(SyncCursorLocalEntity(accountId = accountId, kind = kind.persistentValue, usn = usn))

                dataSource.read(accountId = accountId, kind = kind) shouldBe usn
            }
        }
    })
