package io.github.taetae98coding.diary.core.database.impl

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.impl.qr.transaction.AccountQrTransactionImpl
import io.github.taetae98coding.diary.core.testing.qr.localQr
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes
import kotlin.io.path.walk
import kotlin.uuid.Uuid

class JvmDatabaseStorageTest :
    FunSpec({
        lateinit var userHome: Path
        var originalUserHome: String? = null

        beforeTest {
            originalUserHome = System.getProperty(USER_HOME_PROPERTY)
            userHome = tempdir().toPath()
            System.setProperty(USER_HOME_PROPERTY, userHome.toString())
        }

        afterTest {
            originalUserHome?.let { System.setProperty(USER_HOME_PROPERTY, it) }
        }

        fun openDatabase(databaseDirectory: String): DiaryDatabase =
            DatabaseModule().providesDiaryDatabase(
                builder = JvmDatabaseModule().providesDiaryDatabaseBuilder(databaseDirectory = databaseDirectory),
                driver = BundledSQLiteDriver(),
            )

        fun storageDirectory(databaseDirectory: String): Path = userHome.resolve("Library/Application Support").resolve(databaseDirectory)

        test("TC-6663a50b-174d-465a-a59f-45549fe4c37e 저장 위치가 없으면 처음 사용할 때 만든다") {
            listOf(DEV_DIRECTORY, REAL_DIRECTORY).forEach { databaseDirectory ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val qr = fixtureMonkey.localQr(isDeleted = false)
                storageDirectory(databaseDirectory).shouldNotExist()

                val database = openDatabase(databaseDirectory)
                try {
                    AccountQrTransactionImpl(database = database).upsert(accountId = accountId, qrList = listOf(qr))
                } finally {
                    database.close()
                }

                storageDirectory(databaseDirectory).resolve(DiaryDatabase.NAME).shouldExist()
                val reopened = openDatabase(databaseDirectory)
                try {
                    reopened.accountQrDao().find(accountId = accountId, qrId = qr.id).first() shouldBe qr
                } finally {
                    reopened.close()
                }
            }
        }

        test("TC-babfb3f7-1e5d-41a9-a36d-608250aa5c16 이미 있는 저장 위치의 데이터를 다른 환경으로 옮기거나 복사하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val qr = fixtureMonkey.localQr(isDeleted = false)
            val realDatabase = openDatabase(REAL_DIRECTORY)
            try {
                AccountQrTransactionImpl(database = realDatabase).upsert(accountId = accountId, qrList = listOf(qr))
            } finally {
                realDatabase.close()
            }
            val realFilesBefore = storageDirectory(REAL_DIRECTORY).fileContents()
            storageDirectory(DEV_DIRECTORY).shouldNotExist()

            val devDatabase = openDatabase(DEV_DIRECTORY)
            try {
                devDatabase
                    .accountQrDao()
                    .find(accountId = accountId, qrId = qr.id)
                    .first()
                    .shouldBeNull()
            } finally {
                devDatabase.close()
            }

            storageDirectory(REAL_DIRECTORY).fileContents() shouldBe realFilesBefore
            val reopenedReal = openDatabase(REAL_DIRECTORY)
            try {
                reopenedReal.accountQrDao().find(accountId = accountId, qrId = qr.id).first() shouldBe qr
            } finally {
                reopenedReal.close()
            }
        }
    }) {
    private companion object {
        const val USER_HOME_PROPERTY = "user.home"
        const val DEV_DIRECTORY = "DiaryDev"
        const val REAL_DIRECTORY = "Diary"

        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun Path.fileContents(): Map<String, List<Byte>> =
            walk()
                .filter { it.isRegularFile() }
                .associate { relativize(it).toString() to it.readBytes().toList() }
    }
}
