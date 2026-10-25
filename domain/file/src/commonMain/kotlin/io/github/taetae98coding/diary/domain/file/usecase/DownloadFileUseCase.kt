package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.FileDownloadName
import io.github.taetae98coding.diary.domain.file.repository.FileDetailRepository
import io.github.taetae98coding.diary.domain.file.repository.FileDownloadSettingRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class DownloadFileUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val fileDetailRepository: FileDetailRepository,
    private val fileDownloadSettingRepository: FileDownloadSettingRepository,
) : UseCase<DiaryFile, FileDownloadResult>() {
    override suspend fun execute(parameter: DiaryFile): FileDownloadResult {
        check(getAccountUseCase.requireAccount() is Account.User) { "Only a signed-in account can download a file." }

        val directory = fileDownloadSettingRepository.get().first().directory
        val name = FileDownloadName(name = parameter.name)

        return fileDetailRepository.download(
            id = parameter.id,
            directory = directory,
            nameCandidate = name::candidate,
        )
    }
}
