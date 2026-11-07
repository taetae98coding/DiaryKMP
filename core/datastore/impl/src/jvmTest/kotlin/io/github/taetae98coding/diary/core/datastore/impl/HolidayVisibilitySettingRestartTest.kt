package io.github.taetae98coding.diary.core.datastore.impl

import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDirectory
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import org.koin.core.Koin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createTempDirectory

class HolidayVisibilitySettingRestartTest :
    FunSpec({
        test("TC-af66fad9-88ce-4615-968a-5008dbd5f523 앱을 종료하고 다시 시작해도 숨김으로 고른 공휴일이 유지된다") {
            val directory = createTempDirectory("diary-setting")
            val hiddenKeySet = setOf(Arb.nonBlankString().next(), Arb.nonBlankString().next())
            val dataSource = createKoin(directory).get<HolidaySettingLocalDataSource>()
            hiddenKeySet.forEach { key -> dataSource.addHiddenKey(key = key) }
            // 앞선 실행의 저장 영역은 닫을 수 없으므로, 저장된 파일을 새 폴더로 옮겨 처음 여는 것처럼 다시 연다.
            val restartedDirectory = createTempDirectory("diary-setting")
            directory.resolve(DataStoreModule.HOLIDAY_SETTING_NAME).copyTo(restartedDirectory.resolve(DataStoreModule.HOLIDAY_SETTING_NAME))

            createKoin(restartedDirectory).get<HolidaySettingLocalDataSource>().getHiddenKeySet().first() shouldBe hiddenKeySet
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
