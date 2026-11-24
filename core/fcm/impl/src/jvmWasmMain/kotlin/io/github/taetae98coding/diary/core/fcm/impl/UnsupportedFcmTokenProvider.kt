package io.github.taetae98coding.diary.core.fcm.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.fcm.api.FcmTokenProvider

@Inject
@ContributesBinding(AppScope::class)
internal class UnsupportedFcmTokenProvider : FcmTokenProvider {
    override suspend fun getInstallationId(): String? = null
}
