package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class ToggleHolidayVisibilityUseCase internal constructor(
    private val getSettingHolidayUseCase: GetSettingHolidayUseCase,
    private val holidaySettingRepository: HolidaySettingRepository,
) : UseCase<String, Unit>() {
    override suspend fun execute(parameter: String) {
        val holidaySettingList = getSettingHolidayUseCase(parameter = Unit).first().getOrThrow()
        val holidaySetting = holidaySettingList.find { holidaySetting -> holidaySetting.name == parameter } ?: return

        if (holidaySetting.isVisible) {
            holidaySettingRepository.addHiddenKey(key = parameter)
        } else {
            holidaySettingRepository.removeHiddenKey(key = parameter)
        }
    }
}
