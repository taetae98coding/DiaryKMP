package io.github.taetae98coding.diary.core.datastore.impl

import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.GeminiSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.MapSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.MapProviderLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlin.io.path.copyTo
import kotlin.io.path.createTempDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class DataStoreModuleTest :
    FunSpec({
        test("역할별 설정을 서로 다른 파일에 나눠 보관한다") {
            val directory = createTempDirectory("diary-setting")
            val graph = createDataStoreTestGraph(directory)

            val mapDataSource = graph.mapSettingLocalDataSource
            val holidayDataSource = graph.holidaySettingLocalDataSource
            val geminiDataSource = graph.geminiSettingLocalDataSource
            val geminiSetting = GeminiSettingLocalEntity(apiKey = "storedApiKey", model = "models/gemini-flash", systemPrompt = "지시문")

            mapDataSource.upsertDefaultProvider(provider = MapProviderLocalEntity.GOOGLE)
            holidayDataSource.addHiddenKey(key = "초복")
            geminiDataSource.upsert(setting = geminiSetting)

            mapDataSource.getDefaultProvider().first() shouldBe MapProviderLocalEntity.GOOGLE
            holidayDataSource.getHiddenKeySet().first() shouldBe setOf("초복")
            geminiDataSource.get().first() shouldBe geminiSetting

            directory.listDirectoryEntries().map { path -> path.name }.toSet() shouldBe
                setOf(
                    DataStoreModule.MAP_SETTING_NAME,
                    DataStoreModule.HOLIDAY_SETTING_NAME,
                    DataStoreModule.GEMINI_SETTING_NAME,
                )
        }

        test("한 역할을 저장해도 다른 역할의 저장 파일은 만들지 않는다") {
            val directory = createTempDirectory("diary-setting")
            val graph = createDataStoreTestGraph(directory)

            graph.mapSettingLocalDataSource.upsertDefaultProvider(provider = MapProviderLocalEntity.NAVER)

            directory.listDirectoryEntries().map { path -> path.name } shouldBe listOf(DataStoreModule.MAP_SETTING_NAME)
        }

        test("저장한 적 없는 역할은 비어 있는 설정을 제공한다") {
            val directory = createTempDirectory("diary-setting")
            val graph = createDataStoreTestGraph(directory)

            graph.mapSettingLocalDataSource.getDefaultProvider().first() shouldBe null
            graph.holidaySettingLocalDataSource.getHiddenKeySet().first() shouldBe emptySet()
            graph.geminiSettingLocalDataSource.get().first() shouldBe
                GeminiSettingLocalEntity(apiKey = "", model = "", systemPrompt = "")
        }

        test("TC-93be946b-b249-4c2a-92b9-09246245e8d1 앱을 다시 시작해도 저장한 기본 지도를 제공한다") {
            val directory = createTempDirectory("diary-setting")
            createDataStoreTestGraph(directory).mapSettingLocalDataSource.upsertDefaultProvider(provider = MapProviderLocalEntity.GOOGLE)
            // 앞선 실행의 저장 영역은 닫을 수 없으므로, 저장된 파일을 새 폴더로 옮겨 처음 여는 것처럼 다시 연다.
            val restartedDirectory = createTempDirectory("diary-setting")
            directory.resolve(DataStoreModule.MAP_SETTING_NAME).copyTo(restartedDirectory.resolve(DataStoreModule.MAP_SETTING_NAME))

            createDataStoreTestGraph(restartedDirectory).mapSettingLocalDataSource.getDefaultProvider().first() shouldBe MapProviderLocalEntity.GOOGLE
        }
    })
