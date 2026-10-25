package io.github.taetae98coding.diary.core.datastore.impl

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.BrowserSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDirectory
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
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

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class BrowserSettingRestartTest :
    FunSpec({
        test("TC-24ce5650-4241-4a4b-a51c-f6ba571c5ab1 앱을 종료하고 다시 시작해도 저장한 선택을 제공한다") {
            val profileDirectory = fixtureMonkey.nonBlankString()
            val directory = createTempDirectory("diary-setting")
            createKoin(directory).get<BrowserSettingLocalDataSource>().upsertChromeSessionProfileDirectory(directory = profileDirectory)
            // 앞선 실행의 저장 영역은 닫을 수 없으므로, 저장된 파일을 새 폴더로 옮겨 처음 여는 것처럼 다시 연다.
            val restartedDirectory = createTempDirectory("diary-setting")
            directory.resolve(DataStoreModule.BROWSER_SETTING_NAME).copyTo(restartedDirectory.resolve(DataStoreModule.BROWSER_SETTING_NAME))

            createKoin(restartedDirectory).get<BrowserSettingLocalDataSource>().getChromeSessionProfileDirectory().first() shouldBe profileDirectory
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
