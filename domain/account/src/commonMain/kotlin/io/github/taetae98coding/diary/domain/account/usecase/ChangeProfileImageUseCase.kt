package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import org.koin.core.annotation.Factory

@Factory
public class ChangeProfileImageUseCase internal constructor(
    private val userDataRepository: UserDataRepository,
) : UseCase<FileUri, Unit>() {
    override suspend fun execute(parameter: FileUri) {
        userDataRepository.updateProfileImage(uri = parameter)
    }
}
