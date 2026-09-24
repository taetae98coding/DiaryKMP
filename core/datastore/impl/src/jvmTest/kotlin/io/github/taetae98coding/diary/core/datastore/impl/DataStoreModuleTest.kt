package io.github.taetae98coding.diary.core.datastore.impl

import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.BrowserSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.GeminiSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.MapSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.MapProviderLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDirectory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import org.koin.core.Koin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class DataStoreModuleTest :
    FunSpec({
        test("역할별 설정을 서로 다른 파일에 나눠 보관한다") {
            val directory = createTempDirectory("diary-setting")
            val koin = createKoin(directory)

            val mapDataSource = koin.get<MapSettingLocalDataSource>()
            val holidayDataSource = koin.get<HolidaySettingLocalDataSource>()
            val geminiDataSource = koin.get<GeminiSettingLocalDataSource>()
            val browserDataSource = koin.get<BrowserSettingLocalDataSource>()
            val geminiSetting = GeminiSettingLocalEntity(apiKey = "storedApiKey", model = "models/gemini-flash", systemPrompt = "지시문")

            mapDataSource.setDefaultProvider(provider = MapProviderLocalEntity.GOOGLE)
            holidayDataSource.addHiddenKey(key = "초복")
            geminiDataSource.upsert(setting = geminiSetting)
            browserDataSource.setChromeSessionProfileDirectory(directory = "Profile 1")

            mapDataSource.getDefaultProvider().first() shouldBe MapProviderLocalEntity.GOOGLE
            holidayDataSource.getHiddenKeySet().first() shouldBe setOf("초복")
            geminiDataSource.get().first() shouldBe geminiSetting
            browserDataSource.getChromeSessionProfileDirectory().first() shouldBe "Profile 1"

            directory.listDirectoryEntries().map { path -> path.name }.toSet() shouldBe
                setOf(
                    DataStoreModule.MAP_SETTING_NAME,
                    DataStoreModule.HOLIDAY_SETTING_NAME,
                    DataStoreModule.GEMINI_SETTING_NAME,
                    DataStoreModule.BROWSER_SETTING_NAME,
                )
        }

        test("한 역할을 저장해도 다른 역할의 저장 파일은 만들지 않는다") {
            val directory = createTempDirectory("diary-setting")
            val koin = createKoin(directory)

            koin.get<MapSettingLocalDataSource>().setDefaultProvider(provider = MapProviderLocalEntity.NAVER)

            directory.listDirectoryEntries().map { path -> path.name } shouldBe listOf(DataStoreModule.MAP_SETTING_NAME)
        }

        test("TC-SETTING-BROWSER-DATA-001 저장한 적 없는 역할은 비어 있는 설정을 제공한다") {
            val directory = createTempDirectory("diary-setting")
            val koin = createKoin(directory)

            koin.get<MapSettingLocalDataSource>().getDefaultProvider().first() shouldBe null
            koin.get<HolidaySettingLocalDataSource>().getHiddenKeySet().first() shouldBe emptySet()
            koin.get<GeminiSettingLocalDataSource>().get().first() shouldBe
                GeminiSettingLocalEntity(apiKey = "", model = "", systemPrompt = "")
            koin.get<BrowserSettingLocalDataSource>().getChromeSessionProfileDirectory().first() shouldBe ""
        }
    })

private fun createKoin(directory: Path): Koin =
    koinApplication<DataStoreTestKoinApplication> {
        modules(
            module {
                single<String>(qualifier = named<DiarySettingDirectory>()) { directory.toString() }
                single<SettingPathResolver> {
                    SettingPathResolver { name -> directory.resolve(name).toString() }
                }
            },
        )
    }.koin
