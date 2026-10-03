package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import org.koin.core.annotation.Factory

@Factory
public class ReconcileFileUploadUseCase internal constructor(
    private val fileUploadManager: FileUploadManager,
) : UseCase<Account, Unit>() {
    override suspend fun execute(parameter: Account) {
        when (parameter) {
            Account.Guest -> fileUploadManager.cancelUpload(exceptAccountId = null)
            is Account.User -> fileUploadManager.cancelUpload(exceptAccountId = parameter.id)
        }
    }
}
