package io.github.taetae98coding.diary.domain.file.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.repository.FileDownloadSettingRepository

@Inject
public class UnsetFileDownloadDirectoryUseCase internal constructor(
    private val fileDownloadSettingRepository: FileDownloadSettingRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        fileDownloadSettingRepository.unsetDirectory()
    }
}
