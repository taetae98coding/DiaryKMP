package io.github.taetae98coding.diary.domain.playlist.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.playlist.link.toOptionalYoutubeVideoLinkOrThrow
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Inject
public class UpdateMusicUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountMusicRepository: AccountMusicRepository,
    private val clock: Clock,
) : UseCase<UpdateMusicUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        val linkValidatedDetail = parameter.detail.copy(link = parameter.detail.link.toOptionalYoutubeVideoLinkOrThrow())
        val account = getAccountUseCase.requireAccount()
        val detail = linkValidatedDetail.withStoredTitleForBlank(account = account, id = parameter.id)

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

    private suspend fun MusicDetail.withStoredTitleForBlank(
        account: Account,
        id: Uuid,
    ): MusicDetail {
        if (title.isNotBlank()) return this

        val stored =
            accountMusicRepository
                .find(account = account, musicId = id)
                .first()
                ?.detail

        return copy(title = stored?.title.orEmpty())
    }

    public data class Parameter(
        val id: Uuid,
        val detail: MusicDetail,
    )
}
