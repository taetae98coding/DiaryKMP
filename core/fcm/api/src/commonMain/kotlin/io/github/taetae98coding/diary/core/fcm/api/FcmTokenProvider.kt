package io.github.taetae98coding.diary.core.fcm.api

public interface FcmTokenProvider {
    public suspend fun getToken(): String?
}
