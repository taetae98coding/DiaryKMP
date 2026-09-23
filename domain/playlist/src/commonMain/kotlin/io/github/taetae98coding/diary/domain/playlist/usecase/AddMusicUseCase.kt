package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.playlist.exception.MusicTitleBlankException
import io.github.taetae98coding.diary.domain.playlist.link.toOptionalYoutubeVideoLinkOrThrow
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddMusicUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountMusicRepository: AccountMusicRepository,
    private val clock: Clock,
) : UseCase<MusicDetail, Uuid>() {
    override suspend fun execute(parameter: MusicDetail): Uuid {
        val detail = parameter.validated()

        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val now = clock.now()
        val music =
            Music(
                id = Uuid.random(),
                detail = detail,
                isDeleted = false,
                updatedAt = now,
                createdAt = now,
            )

        accountMusicRepository.upsert(
            account = account,
            music = music,
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return music.id
    }

    private fun MusicDetail.validated(): MusicDetail {
        if (title.isBlank()) throw MusicTitleBlankException()

        return copy(link = link.toOptionalYoutubeVideoLinkOrThrow())
    }
}
