package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetFileUploadSucceededEventUseCase internal constructor(
    private val fileUploadManager: FileUploadManager,
) : FlowUseCase<Unit, FileUploadEvent.Succeeded>() {
    override fun execute(parameter: Unit): Flow<Result<FileUploadEvent.Succeeded>> = fileUploadManager.succeededEvent.map { event -> Result.success(event) }
}
