package io.github.taetae98coding.diary.domain.file.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.file.FileDownloadSetting
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.file.repository.FileDownloadSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
public class GetFileDownloadSettingUseCase internal constructor(
    private val fileDownloadSettingRepository: FileDownloadSettingRepository,
) : FlowUseCase<Unit, FileDownloadSetting>() {
    override fun execute(parameter: Unit): Flow<Result<FileDownloadSetting>> = fileDownloadSettingRepository.get().map { setting -> Result.success(setting) }
}
