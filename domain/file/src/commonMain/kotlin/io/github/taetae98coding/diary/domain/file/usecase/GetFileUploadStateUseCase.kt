package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetFileUploadStateUseCase internal constructor(
    private val fileUploadManager: FileUploadManager,
) : FlowUseCase<Unit, FileUploadState>() {
    override fun execute(parameter: Unit): Flow<Result<FileUploadState>> = fileUploadManager.state.map { state -> Result.success(state) }
}
