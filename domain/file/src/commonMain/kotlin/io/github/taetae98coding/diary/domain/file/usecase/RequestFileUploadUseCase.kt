package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.FileUploadContent
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import io.github.taetae98coding.diary.domain.file.exception.FileNotSelectedException
import io.github.taetae98coding.diary.domain.file.exception.FileTitleBlankException
import org.koin.core.annotation.Factory

@Factory
public class RequestFileUploadUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val fileUploadManager: FileUploadManager,
) : UseCase<RequestFileUploadUseCase.Parameter, Unit>() {
    override suspend fun execute(parameter: Parameter) {
        if (parameter.title.isBlank()) throw FileTitleBlankException()

        val uri = parameter.uri ?: throw FileNotSelectedException()
        val account = getAccountUseCase.requireAccount()

        check(account is Account.User) { "Only a signed-in account can upload a file." }

        fileUploadManager.requestUpload(
            content = FileUploadContent(uri = uri, title = parameter.title, description = parameter.description),
            accountId = account.id,
        )
    }

    public data class Parameter(
        val uri: FileUri?,
        val title: String,
        val description: String,
    )
}
