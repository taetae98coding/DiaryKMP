package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.repository.FileDetailRepository
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class FetchFileUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val fileDetailRepository: FileDetailRepository,
) : UseCase<Uuid, DiaryFile>() {
    override suspend fun execute(parameter: Uuid): DiaryFile {
        check(getAccountUseCase.requireAccount() is Account.User) { "Only a signed-in account can read a file." }

        return fileDetailRepository.fetch(id = parameter)
    }
}
