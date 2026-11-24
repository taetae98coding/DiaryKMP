package io.github.taetae98coding.diary.domain.holiday.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository

@Inject
public class SelectAllHolidayUseCase internal constructor(
    private val holidaySettingRepository: HolidaySettingRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        holidaySettingRepository.submitHiddenKeySet(
            hiddenKeySet = emptySet(),
        )
    }
}
