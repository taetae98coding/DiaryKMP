package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import org.koin.core.annotation.Factory

@Factory
public class StopViewingFileScreenUseCase internal constructor(
    private val fileUploadManager: FileUploadManager,
) : UseCase<FileScreen, Unit>() {
    override suspend fun execute(parameter: FileScreen) {
        fileUploadManager.stopViewing(screen = parameter)
    }
}
