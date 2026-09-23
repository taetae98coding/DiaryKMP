@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class FindMusicUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountMusicRepository: AccountMusicRepository,
) : FlowUseCase<Uuid, Music?>() {
    override fun execute(parameter: Uuid): Flow<Result<Music?>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountMusicRepository
                        .find(
                            account = account,
                            musicId = parameter,
                        ).map { music -> Result.success(music) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
}
