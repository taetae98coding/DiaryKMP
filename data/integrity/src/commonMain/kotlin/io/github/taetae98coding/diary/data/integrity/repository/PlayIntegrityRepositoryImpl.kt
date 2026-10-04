package io.github.taetae98coding.diary.data.integrity.repository

import io.github.taetae98coding.diary.core.integrity.api.PlayIntegrityTokenProvider
import io.github.taetae98coding.diary.core.model.integrity.PlayIntegrityVerdict
import io.github.taetae98coding.diary.core.network.api.integrity.datasource.PlayIntegrityRemoteDataSource
import io.github.taetae98coding.diary.data.integrity.mapper.toDomain
import io.github.taetae98coding.diary.domain.integrity.repository.PlayIntegrityRepository
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class PlayIntegrityRepositoryImpl(
    private val playIntegrityTokenProvider: PlayIntegrityTokenProvider,
    private val playIntegrityRemoteDataSource: PlayIntegrityRemoteDataSource,
) : PlayIntegrityRepository {
    override suspend fun fetch(): PlayIntegrityVerdict? {
        val token = playIntegrityTokenProvider.getToken(requestHash = Uuid.random().toHexString()) ?: return null

        return playIntegrityRemoteDataSource.decode(token = token.token, packageName = token.packageName).toDomain()
    }
}
