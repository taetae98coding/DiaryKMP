package io.github.taetae98coding.diary.domain.file.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.repository.FileDownloadSettingRepository

@Inject
public class ReadFileDownloadDirectorySupportUseCase internal constructor(
    private val fileDownloadSettingRepository: FileDownloadSettingRepository,
) : UseCase<Unit, Boolean>() {
    override suspend fun execute(parameter: Unit): Boolean = fileDownloadSettingRepository.isDirectorySelectable()
}
