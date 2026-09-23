package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.playlist.link.toYoutubeVideoLinkOrThrow
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

    // 링크, 제목, 가수는 곡이 반드시 가져야 하는 정보이므로 비우는 수정을 저장된 값으로 되돌린다.
    // 링크를 비운 것은 저장된 링크를 그대로 쓰겠다는 뜻이므로 YouTube 링크 판정 대상이 아니다.
    private suspend fun MusicDetail.validated(id: Uuid): MusicDetail {
        val enteredLink =
            link
                .trim()
                .takeIf { value -> value.isNotEmpty() }
                ?.toYoutubeVideoLinkOrThrow()

        if (enteredLink != null && title.isNotBlank() && artist.isNotBlank()) return copy(link = enteredLink)

        val stored =
            findMusicUseCase(parameter = id)
                .first()
                .getOrThrow()
                ?.detail

        return copy(
            link = enteredLink ?: stored?.link.orEmpty(),
            title = title.ifBlank { stored?.title.orEmpty() },
            artist = artist.ifBlank { stored?.artist.orEmpty() },
        )
    }

    public data class Parameter(
        val id: Uuid,
        val detail: MusicDetail,
    )
}
