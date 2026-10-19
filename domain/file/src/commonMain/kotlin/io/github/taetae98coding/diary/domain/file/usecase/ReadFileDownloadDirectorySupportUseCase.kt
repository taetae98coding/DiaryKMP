package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.repository.FileDownloadSettingRepository
import org.koin.core.annotation.Factory

@Factory
public class ReadFileDownloadDirectorySupportUseCase internal constructor(
    private val fileDownloadSettingRepository: FileDownloadSettingRepository,
) : UseCase<Unit, Boolean>() {
    override suspend fun execute(parameter: Unit): Boolean = fileDownloadSettingRepository.isDirectorySelectable()
}
