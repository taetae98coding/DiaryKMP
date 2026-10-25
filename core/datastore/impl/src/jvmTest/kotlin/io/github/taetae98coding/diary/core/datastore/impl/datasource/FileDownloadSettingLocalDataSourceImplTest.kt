package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.FileDownloadDirectoryLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.FileDownloadSettingData
import io.github.taetae98coding.diary.core.datastore.impl.FileDownloadSettingSerializer
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import java.nio.file.Files

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileDownloadSettingLocalDataSourceImplTest :
    FunSpec({
        test("TC-76624134-bce5-4b1e-83e1-e02281c9ffed 한 번도 고르지 않았으면 고른 폴더가 없다") {
            runTest {
                withDataStore(path = tempSettingPath()) { dataStore ->
                    FileDownloadSettingLocalDataSourceImpl(dataStore = dataStore).getDirectory().first() shouldBe null
                }
            }
        }

        test("TC-786bc68f-5f47-40d0-975b-a7463878d6d4 저장한 폴더는 저장소를 다시 열어도 유지된다") {
            runTest {
                val path = tempSettingPath()
                val directory = FileDownloadDirectoryLocalEntity(uri = fixtureMonkey.nonBlankString(), name = fixtureMonkey.giveMeOne<String>())

                withDataStore(path = path) { dataStore ->
                    FileDownloadSettingLocalDataSourceImpl(dataStore = dataStore).upsertDirectory(directory = directory)
                }

                withDataStore(path = path) { dataStore ->
                    FileDownloadSettingLocalDataSourceImpl(dataStore = dataStore).getDirectory().first() shouldBe directory
                }
            }
        }

        test("TC-89660e14-4b3d-4a19-bb56-7b5f27a5b744 되돌린 뒤에는 고른 폴더가 없다") {
            runTest {
                withDataStore(path = tempSettingPath()) { dataStore ->
                    val dataSource = FileDownloadSettingLocalDataSourceImpl(dataStore = dataStore)

                    dataSource.upsertDirectory(directory = FileDownloadDirectoryLocalEntity(uri = fixtureMonkey.nonBlankString(), name = fixtureMonkey.giveMeOne<String>()))
                    dataSource.upsertDirectory(directory = null)

                    dataSource.getDirectory().first() shouldBe null
                }
            }
        }

        test("TC-2f29a22e-d3a9-4e80-8e50-2b477d7e10e6 저장된 값을 읽지 못하면 고른 폴더가 없는 것이 아니라 실패로 알린다") {
            runTest {
                val dataStore = mockk<DataStore<FileDownloadSettingData>>()
                every { dataStore.data } returns flow { throw IllegalStateException("read") }

                shouldThrow<IllegalStateException> { FileDownloadSettingLocalDataSourceImpl(dataStore = dataStore).getDirectory().first() }
            }
        }
    })

private fun tempSettingPath(): String = Files.createTempDirectory("file-download-setting").resolve("file-download-setting.json").toString()

private suspend fun kotlinx.coroutines.test.TestScope.withDataStore(
    path: String,
    block: suspend (DataStore<FileDownloadSettingData>) -> Unit,
) {
    val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))

    try {
        block(
            DataStoreFactory.create(
                storage = OkioStorage(fileSystem = FileSystem.SYSTEM, serializer = FileDownloadSettingSerializer, producePath = { path.toPath() }),
                scope = scope,
            ),
        )
    } finally {
        scope.cancel()
        testScheduler.advanceUntilIdle()
    }
}
