package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.playlist.link.toMusicDownloadTargetOrNull
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import org.koin.core.annotation.Factory

@Factory
public class ReadMusicDownloadTargetUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountMusicRepository: AccountMusicRepository,
) : UseCase<ListSort, List<MusicDownloadTarget>>() {
    override suspend fun execute(parameter: ListSort): List<MusicDownloadTarget> {
        val account = getAccountUseCase.requireAccount()

        return accountMusicRepository
            .readList(account = account, sort = parameter)
            .mapNotNull { music -> music.toMusicDownloadTargetOrNull() }
    }
}
