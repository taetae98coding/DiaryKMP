package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class AwaitFileUploadAccountChangeUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
) : UseCase<Uuid, Unit>() {
    override suspend fun execute(parameter: Uuid) {
        getAccountUseCase(parameter = Unit).first { result -> result.isSuccess && result.getOrNull()?.id != parameter }
    }
}
