package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import org.koin.core.annotation.Factory

@Factory
public class StartViewingFileHomeUseCase internal constructor(
    private val fileUploadManager: FileUploadManager,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        fileUploadManager.setFileHomeViewing(isViewing = true)
    }
}
