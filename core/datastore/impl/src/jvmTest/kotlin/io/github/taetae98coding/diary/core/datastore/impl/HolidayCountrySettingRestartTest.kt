package io.github.taetae98coding.diary.core.datastore.impl

import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.HolidayCountryOptionLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlin.io.path.copyTo
import kotlin.io.path.createTempDirectory

class HolidayCountrySettingRestartTest :
    FunSpec({
        test("TC-a0f2ad95-90dd-430e-89d3-fee58de68b8f 앱을 종료하고 다시 시작해도 국가 설정이 유지된다") {
            val directory = createTempDirectory("diary-setting")
            val dataSource = createDataStoreTestGraph(directory).holidaySettingLocalDataSource
            dataSource.addCountryOption(option = HolidayCountryOptionLocalEntity.KOREA)
            dataSource.addCountryOption(option = HolidayCountryOptionLocalEntity.UNITED_STATES)
            dataSource.removeCountryOption(option = HolidayCountryOptionLocalEntity.DEVICE)
            // 앞선 실행의 저장 영역은 닫을 수 없으므로, 저장된 파일을 새 폴더로 옮겨 처음 여는 것처럼 다시 연다.
            val restartedDirectory = createTempDirectory("diary-setting")
            directory.resolve(DataStoreModule.HOLIDAY_SETTING_NAME).copyTo(restartedDirectory.resolve(DataStoreModule.HOLIDAY_SETTING_NAME))

            createDataStoreTestGraph(restartedDirectory).holidaySettingLocalDataSource.getCountryOptionSet().first() shouldBe
                setOf(HolidayCountryOptionLocalEntity.KOREA, HolidayCountryOptionLocalEntity.UNITED_STATES)
        }
    })
