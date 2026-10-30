package io.github.taetae98coding.diary.core.datastore.impl

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.GeminiSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDirectory
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import org.koin.core.Koin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createTempDirectory
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class SettingRestartTest :
    FunSpec({
        test("TC-248fde95-f36c-453f-8893-f07f4bc4b0bd 앱을 다시 시작해도 저장한 Gemini 설정을 제공한다") {
            val suffix = fixtureMonkey.giveMeOne<Uuid>()
            val setting = GeminiSettingLocalEntity(apiKey = "apiKey-$suffix", model = "models/$suffix", systemPrompt = "지시문 $suffix")
            val directory = createTempDirectory("diary-setting")
            createSettingKoin(directory).get<GeminiSettingLocalDataSource>().upsert(setting = setting)

            val restartedDirectory = restartedDirectory(directory = directory, name = DataStoreModule.GEMINI_SETTING_NAME)

            createSettingKoin(restartedDirectory).get<GeminiSettingLocalDataSource>().get().first() shouldBe setting
        }
    })

// 앞선 실행의 저장 영역은 닫을 수 없으므로, 저장된 파일을 새 폴더로 옮겨 처음 여는 것처럼 다시 연다.
private fun restartedDirectory(
    directory: Path,
    name: String,
): Path {
    val restartedDirectory = createTempDirectory("diary-setting")
    directory.resolve(name).copyTo(restartedDirectory.resolve(name))

    return restartedDirectory
}

private fun createSettingKoin(directory: Path): Koin =
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
