package io.github.taetae98coding.diary.domain.file.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
public class GetFileUploadEventUseCase internal constructor(
    private val fileUploadManager: FileUploadManager,
) : FlowUseCase<Unit, FileUploadEvent>() {
    override fun execute(parameter: Unit): Flow<Result<FileUploadEvent>> = fileUploadManager.event.map { event -> Result.success(event) }
}
