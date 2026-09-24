package io.github.taetae98coding.diary.domain.setting.usecase

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.setting.repository.MusicDownloadProxySettingRepository
import org.koin.core.annotation.Factory

@Factory
public class SetMusicDownloadProxySettingUseCase internal constructor(
    private val musicDownloadProxySettingRepository: MusicDownloadProxySettingRepository,
) : UseCase<MusicDownloadProxySetting, Unit>() {
    override suspend fun execute(parameter: MusicDownloadProxySetting) {
        musicDownloadProxySettingRepository.upsert(setting = parameter)
    }
}
