package io.github.taetae98coding.diary.core.integrity.impl

import io.github.taetae98coding.diary.core.integrity.api.PlayIntegrityToken
import io.github.taetae98coding.diary.core.integrity.api.PlayIntegrityTokenProvider
import org.koin.core.annotation.Factory

@Factory
internal class UnsupportedPlayIntegrityTokenProvider : PlayIntegrityTokenProvider {
    override suspend fun getToken(requestHash: String): PlayIntegrityToken? = null
}
