package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetFileUploadEventUseCase internal constructor(
    private val fileUploadManager: FileUploadManager,
) : FlowUseCase<Unit, FileUploadEvent>() {
    override fun execute(parameter: Unit): Flow<Result<FileUploadEvent>> = fileUploadManager.event.map { event -> Result.success(event) }
}
