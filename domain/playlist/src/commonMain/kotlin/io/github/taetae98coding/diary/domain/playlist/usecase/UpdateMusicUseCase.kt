package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.playlist.link.toOptionalYoutubeVideoLinkOrThrow
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class UpdateMusicUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val findMusicUseCase: FindMusicUseCase,
    private val accountMusicRepository: AccountMusicRepository,
    private val clock: Clock,
) : UseCase<UpdateMusicUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        val detail = parameter.detail.validated(id = parameter.id)
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        val updatedCount =
            accountMusicRepository.updateDetail(
                account = account,
                musicId = parameter.id,
                detail = detail,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return updatedCount
    }

    private suspend fun MusicDetail.validated(id: Uuid): MusicDetail {
        val validatedLink = link.toOptionalYoutubeVideoLinkOrThrow()

        if (title.isNotBlank()) return copy(link = validatedLink)

        val storedTitle =
            findMusicUseCase(parameter = id)
                .first()
                .getOrThrow()
                ?.detail
                ?.title
                .orEmpty()

        return copy(
            title = storedTitle,
            link = validatedLink,
        )
    }

    public data class Parameter(
        val id: Uuid,
        val detail: MusicDetail,
    )
}
