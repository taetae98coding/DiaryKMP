package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import org.koin.core.annotation.Factory

@Factory
public class RefreshFileUseCase internal constructor(
    private val fileRepository: FileRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        fileRepository.refresh()
    }
}
