package io.github.taetae98coding.diary.domain.setting.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.setting.repository.MapSettingRepository

@Inject
public class SetDefaultMapProviderUseCase internal constructor(
    private val mapSettingRepository: MapSettingRepository,
) : UseCase<MapProvider, Unit>() {
    override suspend fun execute(parameter: MapProvider) {
        mapSettingRepository.setDefaultProvider(provider = parameter)
    }
}
