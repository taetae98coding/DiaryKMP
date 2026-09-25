package io.github.taetae98coding.diary.core.integrity.api

public interface PlayIntegrityTokenProvider {
    public suspend fun getToken(requestHash: String): PlayIntegrityToken?
}
