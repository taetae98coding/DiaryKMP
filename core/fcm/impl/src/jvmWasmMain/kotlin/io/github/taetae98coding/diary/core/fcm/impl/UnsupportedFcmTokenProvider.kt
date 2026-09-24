package io.github.taetae98coding.diary.core.fcm.impl

import io.github.taetae98coding.diary.core.fcm.api.FcmTokenProvider
import org.koin.core.annotation.Factory

@Factory
internal class UnsupportedFcmTokenProvider : FcmTokenProvider {
    override suspend fun getToken(): String? = null
}
