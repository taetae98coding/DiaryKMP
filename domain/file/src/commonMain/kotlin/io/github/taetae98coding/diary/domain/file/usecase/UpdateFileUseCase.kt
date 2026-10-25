package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.DiaryFileDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.MAX_FILE_NAME_LENGTH
import io.github.taetae98coding.diary.domain.file.exception.FileNameBlankException
import io.github.taetae98coding.diary.domain.file.exception.FileNameTooLongException
import io.github.taetae98coding.diary.domain.file.exception.FileTitleBlankException
import io.github.taetae98coding.diary.domain.file.fileNameLength
import io.github.taetae98coding.diary.domain.file.repository.FileDetailRepository
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class UpdateFileUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val fileDetailRepository: FileDetailRepository,
) : UseCase<UpdateFileUseCase.Parameter, DiaryFile>() {
    override suspend fun execute(parameter: Parameter): DiaryFile {
        val detail = parameter.detail

        detail.violation()?.let { exception -> throw exception }
        check(getAccountUseCase.requireAccount() is Account.User) { "Only a signed-in account can update a file." }

        return fileDetailRepository.updateDetail(id = parameter.id, detail = detail)
    }

    private fun DiaryFileDetail.violation(): Exception? =
        when {
            title.isBlank() -> FileTitleBlankException()
            name.isBlank() -> FileNameBlankException()
            name.fileNameLength() > MAX_FILE_NAME_LENGTH -> FileNameTooLongException()
            else -> null
        }

    public data class Parameter(
        val id: Uuid,
        val detail: DiaryFileDetail,
    )
}
