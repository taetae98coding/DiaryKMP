package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class RequestFileUploadUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val fileUploadManager: FileUploadManager,
) : UseCase<FileUri, Unit>() {
    override suspend fun execute(parameter: FileUri) {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        check(account is Account.User) { "Only a signed-in account can upload a file." }

        fileUploadManager.requestUpload(uri = parameter, accountId = account.id)
    }
}
