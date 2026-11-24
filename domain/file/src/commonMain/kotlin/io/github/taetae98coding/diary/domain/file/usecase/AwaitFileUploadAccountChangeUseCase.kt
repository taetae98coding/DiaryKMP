package io.github.taetae98coding.diary.domain.file.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.core.invoke
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

@Inject
public class AwaitFileUploadAccountChangeUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
) : UseCase<Uuid, Unit>() {
    override suspend fun execute(parameter: Uuid) {
        getAccountUseCase().first { result -> result.isSuccess && result.getOrNull()?.id != parameter }
    }
}
