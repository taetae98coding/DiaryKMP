package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.playlist.link.toYoutubeVideoIdOrNull
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class FindMusicDownloadTargetUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountMusicRepository: AccountMusicRepository,
) : UseCase<ListSort, List<MusicDownloadTarget>>() {
    override suspend fun execute(parameter: ListSort): List<MusicDownloadTarget> {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        return accountMusicRepository
            .findList(account = account, sort = parameter)
            .mapNotNull { music -> music.toDownloadTargetOrNull() }
    }

    private fun Music.toDownloadTargetOrNull(): MusicDownloadTarget? {
        val videoId = detail.link.toYoutubeVideoIdOrNull() ?: return null

        return MusicDownloadTarget(id = id, videoId = videoId)
    }
}
