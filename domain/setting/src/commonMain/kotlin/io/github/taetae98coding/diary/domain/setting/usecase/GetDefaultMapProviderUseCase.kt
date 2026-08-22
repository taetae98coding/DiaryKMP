package io.github.taetae98coding.diary.domain.setting.usecase

import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.setting.repository.MapSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetDefaultMapProviderUseCase internal constructor(
    private val mapSettingRepository: MapSettingRepository,
) : FlowUseCase<Unit, MapProvider>() {
    override fun execute(parameter: Unit): Flow<Result<MapProvider>> =
        mapSettingRepository
            .getDefaultProvider()
            .map { provider -> Result.success(provider) }
}
