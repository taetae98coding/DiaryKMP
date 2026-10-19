package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.file.repository.FileDetailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class GetUpdatedFileUseCase internal constructor(
    private val fileDetailRepository: FileDetailRepository,
) : FlowUseCase<Unit, Map<Uuid, DiaryFile>>() {
    override fun execute(parameter: Unit): Flow<Result<Map<Uuid, DiaryFile>>> = fileDetailRepository.getUpdated().map { fileMap -> Result.success(fileMap) }
}
