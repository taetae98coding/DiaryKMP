package io.github.taetae98coding.diary.core.integrity.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.integrity.api.PlayIntegrityToken
import io.github.taetae98coding.diary.core.integrity.api.PlayIntegrityTokenProvider

@Inject
@ContributesBinding(AppScope::class)
internal class UnsupportedPlayIntegrityTokenProvider : PlayIntegrityTokenProvider {
    override suspend fun getToken(requestHash: String): PlayIntegrityToken? = null
}
