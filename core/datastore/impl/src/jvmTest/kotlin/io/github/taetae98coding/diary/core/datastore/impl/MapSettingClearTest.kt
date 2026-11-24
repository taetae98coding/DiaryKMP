package io.github.taetae98coding.diary.core.datastore.impl

import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.MapSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.MapProviderLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists

class MapSettingClearTest :
    FunSpec({
        test("TC-488978b9-ecaf-40fc-9f4f-79863d28924e 데스크톱 앱의 저장 영역이 지워지면 저장된 기본 지도가 없는 상태로 돌아간다") {
            val directory = createTempDirectory("diary-setting")
            val dataSource = createDataStoreTestGraph(directory).mapSettingLocalDataSource
            dataSource.upsertDefaultProvider(provider = MapProviderLocalEntity.GOOGLE)
            dataSource.getDefaultProvider().first() shouldBe MapProviderLocalEntity.GOOGLE
            directory.resolve(DataStoreModule.MAP_SETTING_NAME).exists() shouldBe true
            // 앞선 실행의 저장 영역은 닫을 수 없으므로, 저장된 파일을 옮기지 않은 새 폴더를 지워진 저장 영역으로 보고 처음 여는 것처럼 연다.
            val clearedDirectory = createTempDirectory("diary-setting")

            createDataStoreTestGraph(clearedDirectory).mapSettingLocalDataSource.getDefaultProvider().first() shouldBe null
        }
    })
