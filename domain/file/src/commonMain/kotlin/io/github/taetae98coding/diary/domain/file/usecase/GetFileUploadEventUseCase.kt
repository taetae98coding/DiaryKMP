package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetFileUploadEventUseCase internal constructor(
    private val fileUploadManager: FileUploadManager,
) : FlowUseCase<FileScreen, FileUploadEvent>() {
    override fun execute(parameter: FileScreen): Flow<Result<FileUploadEvent>> = fileUploadManager.getEvent(screen = parameter).map { event -> Result.success(event) }
}
