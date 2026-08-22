package io.github.taetae98coding.diary.domain.setting.usecase

import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.setting.repository.MapSettingRepository
import org.koin.core.annotation.Factory

@Factory
public class SetDefaultMapProviderUseCase internal constructor(
    private val mapSettingRepository: MapSettingRepository,
) : UseCase<MapProvider, Unit>() {
    override suspend fun execute(parameter: MapProvider) {
        mapSettingRepository.setDefaultProvider(provider = parameter)
    }
}
