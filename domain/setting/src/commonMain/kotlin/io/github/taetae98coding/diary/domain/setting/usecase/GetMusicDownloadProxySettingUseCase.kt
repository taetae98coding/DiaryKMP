package io.github.taetae98coding.diary.domain.setting.usecase

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.setting.repository.MusicDownloadProxySettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetMusicDownloadProxySettingUseCase internal constructor(
    private val musicDownloadProxySettingRepository: MusicDownloadProxySettingRepository,
) : FlowUseCase<Unit, MusicDownloadProxySetting>() {
    override fun execute(parameter: Unit): Flow<Result<MusicDownloadProxySetting>> = musicDownloadProxySettingRepository.get().map { setting -> Result.success(setting) }
}
